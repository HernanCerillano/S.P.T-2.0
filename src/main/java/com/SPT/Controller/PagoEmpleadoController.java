package com.SPT.Controller;

import com.SPT.Dtos.Request.PagoEmpleadoRequest;
import com.SPT.Dtos.Response.PagoEmpleadoResponse;
import com.SPT.Services.PagoEmpleadoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos-empleado")
public class PagoEmpleadoController {

    private final PagoEmpleadoService pagoEmpleadoService;

    public PagoEmpleadoController(PagoEmpleadoService pagoEmpleadoService) {
        this.pagoEmpleadoService = pagoEmpleadoService;
    }

    @PostMapping
    public ResponseEntity<PagoEmpleadoResponse> registrar(@Valid @RequestBody PagoEmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoEmpleadoService.registrar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoEmpleadoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pagoEmpleadoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PagoEmpleadoResponse>> listar(
            @RequestParam(required = false) Long idEmpleado) {
        if (idEmpleado != null) {
            return ResponseEntity.ok(pagoEmpleadoService.listarPorEmpleado(idEmpleado));
        }
        return ResponseEntity.ok(pagoEmpleadoService.listarTodos());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoEmpleadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
