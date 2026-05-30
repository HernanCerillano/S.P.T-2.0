package com.SPT.Services.impl;

import com.SPT.Model.Cita;
import com.SPT.Model.MensajeWhatsapp;
import com.SPT.Services.EnvioResultado;
import com.SPT.Services.WhatsappProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "noop", matchIfMissing = true)
public class NoopWhatsappProvider implements WhatsappProvider {

    private static final Logger log = LoggerFactory.getLogger(NoopWhatsappProvider.class);

    @Override
    public EnvioResultado enviar(MensajeWhatsapp mensaje, Cita cita) {
        String cliente  = cita != null && cita.getCliente() != null
                ? (nvl(cita.getCliente().getNombre()) + " " + nvl(cita.getCliente().getApellido())).trim()
                : "?";
        String vehiculo = cita != null && cita.getVehiculo() != null
                ? nvl(cita.getVehiculo().getMarca()) + " " + nvl(cita.getVehiculo().getModelo())
                  + " (" + nvl(cita.getVehiculo().getPatente()) + ")"
                : "?";
        String fecha = cita != null && cita.getFechaHora() != null
                ? cita.getFechaHora().toLocalDate().toString() : "?";
        String hora  = cita != null && cita.getFechaHora() != null
                ? cita.getFechaHora().toLocalTime().toString().substring(0, 5) : "?";

        log.info("[NOOP] WhatsApp simulado → to={} | cliente={} | vehiculo={} | fecha={} | hora={} | contenido={}",
                mensaje.getTelefonoDestino(), cliente, vehiculo, fecha, hora, mensaje.getContenido());
        return EnvioResultado.ok("NOOP-" + System.currentTimeMillis());
    }

    @Override
    public EnvioResultado enviarLibre(String to, String body) {
        log.info("[NOOP] WhatsApp libre simulado → to={} | body={}", to, body);
        return EnvioResultado.ok("NOOP-LIBRE-" + System.currentTimeMillis());
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
