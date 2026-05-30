package com.SPT.Controller;

import com.SPT.Dtos.Request.OtEmpleadoRequest;
import com.SPT.Dtos.Response.OtEmpleadoResponse;
import com.SPT.Services.OtEmpleadoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes/{idOt}/empleados")
public class OtEmpleadoController {

    private final OtEmpleadoService otEmpleadoService;

    public OtEmpleadoController(OtEmpleadoService otEmpleadoService) {
        this.otEmpleadoService = otEmpleadoService;
    }

    @PostMapping
    public ResponseEntity<OtEmpleadoResponse> asignar(
            @PathVariable Long idOt,
            @Valid @RequestBody OtEmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(otEmpleadoService.asignar(idOt, request));
    }

    @PutMapping("/{idOtEmpleado}")
    public ResponseEntity<OtEmpleadoResponse> actualizar(
            @PathVariable Long idOt,
            @PathVariable Long idOtEmpleado,
            @Valid @RequestBody OtEmpleadoRequest request) {
        return ResponseEntity.ok(otEmpleadoService.actualizar(idOt, idOtEmpleado, request));
    }

    @GetMapping
    public ResponseEntity<List<OtEmpleadoResponse>> listar(@PathVariable Long idOt) {
        return ResponseEntity.ok(otEmpleadoService.listarPorOt(idOt));
    }

    @DeleteMapping("/{idOtEmpleado}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idOt,
            @PathVariable Long idOtEmpleado) {
        otEmpleadoService.eliminar(idOt, idOtEmpleado);
        return ResponseEntity.noContent().build();
    }
}
