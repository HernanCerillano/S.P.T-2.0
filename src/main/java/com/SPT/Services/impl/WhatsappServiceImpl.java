package com.SPT.Services.impl;

import com.SPT.Model.Cita;
import com.SPT.Model.Cliente;
import com.SPT.Model.ConfiguracionTaller;
import com.SPT.Model.EstadoMensaje;
import com.SPT.Model.MensajeWhatsapp;
import com.SPT.Repository.ConfiguracionTallerRepository;
import com.SPT.Repository.MensajeWhatsappRepository;
import com.SPT.Services.EnvioResultado;
import com.SPT.Services.WhatsappProvider;
import com.SPT.Services.WhatsappService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WhatsappServiceImpl implements WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappServiceImpl.class);
    private static final Byte ID_CONFIG = 1;

    private final MensajeWhatsappRepository mensajeRepository;
    private final WhatsappProvider whatsappProvider;
    private final ConfiguracionTallerRepository configuracionRepository;

    public WhatsappServiceImpl(MensajeWhatsappRepository mensajeRepository,
                                WhatsappProvider whatsappProvider,
                                ConfiguracionTallerRepository configuracionRepository) {
        this.mensajeRepository = mensajeRepository;
        this.whatsappProvider = whatsappProvider;
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    @Transactional
    public void encolarRecordatorioCita(Cita cita) {
        ConfiguracionTaller config = obtenerConfig();
        if (config == null || !config.isWpHabilitado()) return;

        Cliente cliente = cita.getCliente();
        if (cliente == null) return;

        // Opt-out: el cliente pidió la baja (respondió STOP/BAJA/etc. por WhatsApp).
        if (!cliente.isWhatsappHabilitado()) {
            log.info("Cliente {} tiene WhatsApp deshabilitado (opt-out). No se encola recordatorio.",
                    cliente.getIdCliente());
            return;
        }

        String telefono = resolverTelefono(cliente);
        if (telefono == null || telefono.isBlank()) return;
        if (cita.getFechaHora() == null) return;

        String contenido = generarContenido(cita, cliente, config);
        int horas = config.getHorasAnticipacionWp() > 0 ? config.getHorasAnticipacionWp() : 24;
        LocalDateTime ahora = LocalDateTime.now();

        MensajeWhatsapp mensaje = new MensajeWhatsapp();
        mensaje.setCita(cita);
        mensaje.setTelefonoDestino(telefono);
        mensaje.setPlantilla("recordatorio_cita");
        mensaje.setContenido(contenido);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setIntentos(0);
        mensaje.setFechaProgramada(cita.getFechaHora().minusHours(horas));
        mensaje.setFechaCreacion(ahora);
        mensaje.setFechaModificacion(ahora);

        mensajeRepository.save(mensaje);
    }

    /**
     * Called when a Cita is modified. Updates any PENDIENTE messages with the new
     * scheduled date and re-rendered content. If a message was already ENVIADO,
     * enqueues a new PENDIENTE message so the client gets an updated reminder.
     */
    @Override
    @Transactional
    public void sincronizarPorCita(Cita cita) {
        ConfiguracionTaller config = obtenerConfig();
        int horas = (config != null && config.getHorasAnticipacionWp() > 0)
                ? config.getHorasAnticipacionWp() : 24;

        LocalDateTime nuevaFechaProgramada = cita.getFechaHora().minusHours(horas);
        LocalDateTime ahora = LocalDateTime.now();

        List<MensajeWhatsapp> mensajes = mensajeRepository.findByCita_IdCita(cita.getIdCita());

        boolean yaHayEnviado = false;

        for (MensajeWhatsapp m : mensajes) {
            switch (m.getEstado()) {
                case PENDIENTE -> {
                    if (nuevaFechaProgramada.isBefore(ahora)) {
                        // New schedule is already in the past → cancel
                        m.setEstado(EstadoMensaje.CANCELADO);
                        log.info("Mensaje WA {} cancelado: nueva fecha programada ya pasó", m.getIdMensaje());
                    } else {
                        // Reschedule and re-render
                        m.setFechaProgramada(nuevaFechaProgramada);
                        if (config != null && cita.getCliente() != null) {
                            String telefono = resolverTelefono(cita.getCliente());
                            if (telefono != null && !telefono.isBlank()) {
                                m.setTelefonoDestino(telefono);
                            }
                            m.setContenido(generarContenido(cita, cita.getCliente(), config));
                        }
                        log.info("Mensaje WA {} reprogramado a {}", m.getIdMensaje(), nuevaFechaProgramada);
                    }
                    mensajeRepository.save(m);
                }
                case ENVIADO -> yaHayEnviado = true;
                default -> { /* FALLIDO, CANCELADO → no action */ }
            }
        }

        // If already sent: create a new PENDIENTE so the client gets the updated info
        if (yaHayEnviado) {
            encolarRecordatorioCita(cita); // guards isWpHabilitado internally
        }
    }

    @Override
    @Transactional
    public void cancelarPorCita(Long idCita) {
        List<MensajeWhatsapp> mensajes = mensajeRepository.findByCita_IdCita(idCita);
        for (MensajeWhatsapp m : mensajes) {
            if (m.getEstado() == EstadoMensaje.PENDIENTE) {
                m.setEstado(EstadoMensaje.CANCELADO);
                m.setFechaModificacion(LocalDateTime.now());
                mensajeRepository.save(m);
            }
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${whatsapp.scheduler-delay-ms:60000}")
    @Transactional
    public void procesarPendientes() {
        ConfiguracionTaller config = obtenerConfig();
        if (config == null || !config.isWpHabilitado()) return;

        List<MensajeWhatsapp> listos = mensajeRepository
                .findByEstadoAndFechaProgramadaBefore(EstadoMensaje.PENDIENTE, LocalDateTime.now());

        for (MensajeWhatsapp m : listos) {
            m.setIntentos(m.getIntentos() + 1);
            try {
                EnvioResultado resultado = whatsappProvider.enviar(m, m.getCita());
                if (resultado.isOk()) {
                    m.setEstado(EstadoMensaje.ENVIADO);
                    m.setFechaEnvio(LocalDateTime.now());
                    log.info("Mensaje WhatsApp enviado: id={} sid={}", m.getIdMensaje(), resultado.getSid());
                } else {
                    String err = resultado.getError() != null ? resultado.getError() : "Error del provider";
                    m.setEstado(EstadoMensaje.FALLIDO);
                    m.setErrorMensaje(err.substring(0, Math.min(err.length(), 500)));
                    log.error("Fallo enviando WhatsApp id={}: {}", m.getIdMensaje(), resultado.getError());
                }
            } catch (Exception e) {
                m.setEstado(EstadoMensaje.FALLIDO);
                m.setErrorMensaje(e.getMessage() != null
                        ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500))
                        : "Error desconocido");
                log.error("Error enviando mensaje WhatsApp id={}: {}", m.getIdMensaje(), e.getMessage());
            }
            m.setFechaModificacion(LocalDateTime.now());
            mensajeRepository.save(m);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private ConfiguracionTaller obtenerConfig() {
        Optional<ConfiguracionTaller> opt = configuracionRepository.findById(ID_CONFIG);
        return opt.orElse(null);
    }

    private String resolverTelefono(Cliente cliente) {
        String tel = cliente.getWhatsapp();
        if (tel == null || tel.isBlank()) {
            tel = cliente.getTelefono();
        }
        return tel;
    }

    private String generarContenido(Cita cita, Cliente cliente, ConfiguracionTaller config) {
        String plantilla = config.getPlantillaWpRecordatorio();

        String nombreCliente = ((cliente.getNombre() != null ? cliente.getNombre() : "") +
                " " + (cliente.getApellido() != null ? cliente.getApellido() : "")).trim();
        String fechaCita = cita.getFechaHora() != null
                ? cita.getFechaHora().toLocalDate() + " " + cita.getFechaHora().toLocalTime().toString().substring(0, 5)
                : "";
        String nombreTaller = config.getNombreTaller() != null ? config.getNombreTaller() : "el taller";

        if (plantilla != null && !plantilla.isBlank()) {
            return plantilla
                    .replace("{{nombre_cliente}}", nombreCliente)
                    .replace("{{fecha_cita}}", fechaCita)
                    .replace("{{nombre_taller}}", nombreTaller);
        }

        // Fallback genérico si la plantilla no está configurada
        String vehiculo = cita.getVehiculo() != null && cita.getVehiculo().getPatente() != null
                ? cita.getVehiculo().getPatente() : "su vehículo";
        return String.format(
                "Hola %s, te recordamos tu turno para el vehículo %s el %s. ¡Te esperamos en %s!",
                nombreCliente, vehiculo, fechaCita, nombreTaller);
    }
}
