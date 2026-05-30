package com.SPT.Controller;

import com.SPT.Dtos.Request.ConfiguracionTallerRequest;
import com.SPT.Dtos.Response.ConfiguracionTallerResponse;
import com.SPT.Services.ConfiguracionTallerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionTallerController {

    private final ConfiguracionTallerService configuracionService;

    public ConfiguracionTallerController(ConfiguracionTallerService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public ResponseEntity<ConfiguracionTallerResponse> obtener() {
        return ResponseEntity.ok(configuracionService.obtener());
    }

    @PutMapping
    public ResponseEntity<ConfiguracionTallerResponse> actualizar(
            @Valid @RequestBody ConfiguracionTallerRequest request) {
        return ResponseEntity.ok(configuracionService.actualizar(request));
    }
}
