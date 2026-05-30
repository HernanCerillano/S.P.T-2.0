package com.SPT.Controller;

import com.SPT.Dtos.Request.ConfiguracionWhatsappRequest;
import com.SPT.Dtos.Response.ConfiguracionWhatsappResponse;
import com.SPT.Services.ConfiguracionWhatsappService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion/whatsapp")
public class ConfiguracionWhatsappController {

    private final ConfiguracionWhatsappService service;

    public ConfiguracionWhatsappController(ConfiguracionWhatsappService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ConfiguracionWhatsappResponse> obtener() {
        return ResponseEntity.ok(service.obtener());
    }

    @PutMapping
    public ResponseEntity<ConfiguracionWhatsappResponse> actualizar(
            @Valid @RequestBody ConfiguracionWhatsappRequest request) {
        return ResponseEntity.ok(service.actualizar(request));
    }
}
