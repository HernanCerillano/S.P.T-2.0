package com.SPT.Services.impl;

import com.SPT.Config.WhatsappTwilioProperties;
import com.SPT.Model.Cita;
import com.SPT.Model.MensajeWhatsapp;
import com.SPT.Repository.ConfiguracionTallerRepository;
import com.SPT.Services.EnvioResultado;
import com.SPT.Services.WhatsappProvider;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "twilio")
public class TwilioWhatsappProvider implements WhatsappProvider {

    private static final Logger log = LoggerFactory.getLogger(TwilioWhatsappProvider.class);

    private final WhatsappTwilioProperties props;
    private final ConfiguracionTallerRepository configRepo;

    public TwilioWhatsappProvider(WhatsappTwilioProperties props,
                                   ConfiguracionTallerRepository configRepo) {
        this.props = props;
        this.configRepo = configRepo;
        Twilio.init(props.accountSid(), props.authToken());
        log.info("TwilioWhatsappProvider inicializado (from={})", props.from());
    }

    @Override
    public EnvioResultado enviar(MensajeWhatsapp mensaje, Cita cita) {
        // Null-guard: cita, cliente and vehiculo must be present to build template variables
        if (cita == null) {
            return EnvioResultado.fallo("Cita nula — no se puede armar el template");
        }
        if (cita.getCliente() == null) {
            return EnvioResultado.fallo("Cita sin cliente — no se puede armar el template");
        }
        if (cita.getVehiculo() == null) {
            return EnvioResultado.fallo("Cita sin vehículo — no se puede armar el template");
        }

        try {
            String to = normalizarNumero(mensaje.getTelefonoDestino());

            // ── Build the 5 template variables ───────────────────────────────────────────
            String nombreCliente = escape(
                    (nvl(cita.getCliente().getNombre()) + " " + nvl(cita.getCliente().getApellido())).trim()
            );
            String nombreTaller = escape(
                    configRepo.findById((byte) 1)
                            .map(c -> c.getNombreTaller() != null ? c.getNombreTaller() : "Taller")
                            .orElse("Taller")
            );
            String vehiculo = escape(
                    nvl(cita.getVehiculo().getMarca()) + " " + nvl(cita.getVehiculo().getModelo())
                    + " (" + nvl(cita.getVehiculo().getPatente()) + ")"
            );
            String fecha = cita.getFechaHora() != null
                    ? cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            String hora = cita.getFechaHora() != null
                    ? cita.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm")) : "";

            String contentVariables = String.format(
                    "{\"1\":\"%s\",\"2\":\"%s\",\"3\":\"%s\",\"4\":\"%s\",\"5\":\"%s\"}",
                    nombreCliente, nombreTaller, vehiculo, fecha, hora
            );

            log.debug("Twilio contentVariables: {}", contentVariables);

            Message message = Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber(props.from()),
                            mensaje.getContenido()  // fallback body — ignored when contentSid is valid
                    )
                    .setContentSid(props.contentSid())
                    .setContentVariables(contentVariables)
                    .create();

            log.info("WhatsApp enviado vía Twilio. SID={} to={} cliente={} taller={} vehiculo={} fecha={} hora={}",
                    message.getSid(), to, nombreCliente, nombreTaller, vehiculo, fecha, hora);
            return EnvioResultado.ok(message.getSid());

        } catch (ApiException e) {
            log.error("Error Twilio enviando WhatsApp: code={} msg={}", e.getCode(), e.getMessage());
            return EnvioResultado.fallo("TWILIO_" + e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado enviando WhatsApp", e);
            return EnvioResultado.fallo(e.getMessage());
        }
    }

    @Override
    public EnvioResultado enviarLibre(String to, String body) {
        try {
            String destino = to != null && to.startsWith("whatsapp:") ? to : normalizarNumero(to);
            Message message = Message.creator(
                            new PhoneNumber(destino),
                            new PhoneNumber(props.from()),
                            body
                    )
                    .create();
            log.info("WhatsApp libre enviado vía Twilio. SID={} to={}", message.getSid(), destino);
            return EnvioResultado.ok(message.getSid());
        } catch (ApiException e) {
            log.error("Error Twilio enviando WhatsApp libre: code={} msg={}", e.getCode(), e.getMessage());
            return EnvioResultado.fallo("TWILIO_" + e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado enviando WhatsApp libre", e);
            return EnvioResultado.fallo(e.getMessage());
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    /** Escapes backslashes and double-quotes so the value is safe inside a JSON string. */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    /** Normalises a phone number to the {@code whatsapp:+<digits>} format required by Twilio. */
    private String normalizarNumero(String telefono) {
        String limpio = telefono.replaceAll("[^+0-9]", "");
        if (!limpio.startsWith("+")) {
            limpio = "+" + limpio;
        }
        return "whatsapp:" + limpio;
    }
}
