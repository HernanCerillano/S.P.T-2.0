package com.SPT.Controller;

import com.SPT.Services.impl.WhatsappInboundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/twilio")
public class TwilioWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);

    private final WhatsappInboundService inboundService;

    public TwilioWebhookController(WhatsappInboundService inboundService) {
        this.inboundService = inboundService;
    }

    @PostMapping(value = "/inbound", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> inbound(
            @RequestParam("From") String from,
            @RequestParam(value = "Body", required = false) String body,
            @RequestParam(value = "ButtonPayload", required = false) String buttonPayload) {
        // TODO producción: validar X-Twilio-Signature antes de procesar.
        log.info("Inbound WhatsApp from={} body='{}' buttonPayload='{}'", from, body, buttonPayload);
        try {
            inboundService.procesar(from, body, buttonPayload);
        } catch (Exception e) {
            log.error("Error procesando inbound WhatsApp", e);
        }
        // Responder 200 OK siempre y rápido: Twilio reintenta si tarda > 15s o no es 2xx.
        return ResponseEntity.ok().build();
    }
}
