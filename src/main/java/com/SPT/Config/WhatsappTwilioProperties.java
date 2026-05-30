package com.SPT.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp.twilio")
public record WhatsappTwilioProperties(
        String accountSid,
        String authToken,
        String from,
        String contentSid
) {}
