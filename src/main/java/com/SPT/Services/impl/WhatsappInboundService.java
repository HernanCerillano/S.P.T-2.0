package com.SPT.Services.impl;

import com.SPT.Model.Cita;
import com.SPT.Model.Cliente;
import com.SPT.Model.ConfiguracionTaller;
import com.SPT.Model.ConfiguracionWhatsapp;
import com.SPT.Repository.CitaRepository;
import com.SPT.Repository.ClienteRepository;
import com.SPT.Repository.ConfiguracionTallerRepository;
import com.SPT.Repository.ConfiguracionWhatsappRepository;
import com.SPT.Services.WhatsappProvider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Procesa mensajes inbound de WhatsApp (webhook de Twilio) y dispara auto-respuestas
 * configurables: confirmar, cancelar, bienvenida, fallback y opt-out.
 *
 * NOTA DE DISEÑO: por restricción del proyecto, este servicio NO modifica la cita
 * (la entidad Cita no tiene campo de estado). Confirmar/Cancelar solo envían el texto
 * configurado; la próxima cita del cliente se lee únicamente para rellenar {fecha}/{hora}.
 */
@Service
public class WhatsappInboundService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappInboundService.class);
    private static final Byte ID_CONFIG = 1;
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ConfiguracionWhatsappRepository configRepo;
    private final ConfiguracionTallerRepository tallerRepo;
    private final ClienteRepository clienteRepo;
    private final CitaRepository citaRepo;
    private final WhatsappProvider provider;

    public WhatsappInboundService(ConfiguracionWhatsappRepository configRepo,
                                  ConfiguracionTallerRepository tallerRepo,
                                  ClienteRepository clienteRepo,
                                  CitaRepository citaRepo,
                                  WhatsappProvider provider) {
        this.configRepo = configRepo;
        this.tallerRepo = tallerRepo;
        this.clienteRepo = clienteRepo;
        this.citaRepo = citaRepo;
        this.provider = provider;
    }

    @Transactional
    public void procesar(String from, String body, String buttonPayload) {
        if (from == null || from.isBlank()) {
            log.warn("Inbound WhatsApp sin remitente — se ignora");
            return;
        }

        ConfiguracionWhatsapp config = configRepo.findById(ID_CONFIG).orElse(null);
        if (config == null) {
            log.warn("ConfiguracionWhatsapp (id=1) no inicializada — no se puede auto-responder");
            return;
        }
        ConfiguracionTaller taller = tallerRepo.findById(ID_CONFIG).orElse(null);

        String numeroLimpio = from.replace("whatsapp:", "").trim();
        String texto = body == null ? "" : body.trim();
        String textoUpper = texto.toUpperCase();

        Optional<Cliente> clienteOpt = buscarCliente(numeroLimpio);

        // 1. Opt-out (palabras clave)
        if (config.isOptOutHabilitado() && esOptOut(textoUpper, config.getOptOutPalabrasClave())) {
            clienteOpt.ifPresent(c -> {
                c.setWhatsappHabilitado(false);
                clienteRepo.save(c);
                log.info("Opt-out: cliente {} ({}) deshabilitó WhatsApp", c.getIdCliente(), numeroLimpio);
            });
            provider.enviarLibre(from, sustituirVariables(config.getRespuestaOptOut(),
                    clienteOpt.orElse(null), taller, null));
            return;
        }

        // 2. Confirmar (botón o texto)
        if ("CONFIRMAR".equalsIgnoreCase(buttonPayload) || textoMatch(textoUpper, "CONFIRMAR", "CONFIRMO", "SI")) {
            Cita proxima = clienteOpt.map(c -> proximaCita(c.getIdCliente())).orElse(null);
            provider.enviarLibre(from, sustituirVariables(config.getRespuestaConfirmar(),
                    clienteOpt.orElse(null), taller, proxima));
            return;
        }

        // 3. Cancelar (botón o texto)
        if ("CANCELAR".equalsIgnoreCase(buttonPayload) || textoMatch(textoUpper, "CANCELAR", "CANCELO", "NO")) {
            Cita proxima = clienteOpt.map(c -> proximaCita(c.getIdCliente())).orElse(null);
            provider.enviarLibre(from, sustituirVariables(config.getRespuestaCancelar(),
                    clienteOpt.orElse(null), taller, proxima));
            return;
        }

        // 4. Cliente desconocido → bienvenida
        if (clienteOpt.isEmpty()) {
            provider.enviarLibre(from, sustituirVariables(config.getRespuestaBienvenida(), null, taller, null));
            return;
        }

        // 5. Fallback → no entendido
        provider.enviarLibre(from, sustituirVariables(config.getRespuestaNoEntendido(),
                clienteOpt.orElse(null), taller, null));
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    /** Búsqueda best-effort por los últimos dígitos del número (formatos heterogéneos). */
    private Optional<Cliente> buscarCliente(String numeroLimpio) {
        String soloDigitos = numeroLimpio.replaceAll("[^0-9]", "");
        if (soloDigitos.isEmpty()) return Optional.empty();
        String fragmento = soloDigitos.length() >= 8
                ? soloDigitos.substring(soloDigitos.length() - 8)
                : soloDigitos;
        return clienteRepo.findFirstByWhatsappContaining(fragmento);
    }

    /** Próxima cita futura del cliente (solo lectura, para rellenar {fecha}/{hora}). */
    private Cita proximaCita(Long idCliente) {
        LocalDateTime ahora = LocalDateTime.now();
        return citaRepo.findByCliente_IdCliente(idCliente).stream()
                .filter(c -> c.getFechaHora() != null && !c.getFechaHora().isBefore(ahora))
                .min(Comparator.comparing(Cita::getFechaHora))
                .orElse(null);
    }

    private boolean esOptOut(String textoUpper, String palabrasClaveCsv) {
        if (palabrasClaveCsv == null || textoUpper.isEmpty()) return false;
        return Arrays.stream(palabrasClaveCsv.split(","))
                .map(String::trim).map(String::toUpperCase)
                .filter(p -> !p.isEmpty())
                .anyMatch(palabra -> textoUpper.equals(palabra) || textoUpper.contains(palabra));
    }

    private boolean textoMatch(String textoUpper, String... opciones) {
        return Arrays.stream(opciones).anyMatch(o -> textoUpper.equals(o) || textoUpper.startsWith(o + " "));
    }

    private String sustituirVariables(String plantilla, Cliente c, ConfiguracionTaller t, Cita cita) {
        if (plantilla == null) return "";
        String r = plantilla;
        if (c != null && c.getNombre() != null) r = r.replace("{nombre}", c.getNombre());
        if (t != null && t.getNombreTaller() != null) r = r.replace("{taller}", t.getNombreTaller());
        if (cita != null && cita.getFechaHora() != null) {
            r = r.replace("{fecha}", cita.getFechaHora().format(FMT_FECHA));
            r = r.replace("{hora}", cita.getFechaHora().format(FMT_HORA));
        }
        // Limpiar placeholders no resueltos para que no salgan literales en el mensaje.
        r = r.replaceAll("\\{[^}]+\\}", "");
        return r.trim();
    }
}
