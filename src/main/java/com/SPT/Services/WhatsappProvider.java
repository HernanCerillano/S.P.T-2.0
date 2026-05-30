package com.SPT.Services;

import com.SPT.Model.Cita;
import com.SPT.Model.MensajeWhatsapp;

public interface WhatsappProvider {
    EnvioResultado enviar(MensajeWhatsapp mensaje, Cita cita);

    /**
     * Envía un mensaje de texto libre (no template) a un número.
     * Usado para auto-respuestas inbound dentro de la ventana de servicio de 24h.
     *
     * @param to   número destino (con o sin prefijo {@code whatsapp:})
     * @param body texto del mensaje
     */
    EnvioResultado enviarLibre(String to, String body);
}
