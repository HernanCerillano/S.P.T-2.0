package com.SPT.Controller;

import com.SPT.Dtos.Request.PiezaRequest;
import com.SPT.Dtos.Response.PiezaResponse;
import com.SPT.Services.PiezaService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/piezas")
public class PiezaController {

    private final PiezaService piezaService;

    public PiezaController(PiezaService piezaService) {
        this.piezaService = piezaService;
    }

    @PostMapping
    public ResponseEntity<PiezaResponse> crear(@RequestBody PiezaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(piezaService.crear(request));
    }

    @PutMapping("/{idPieza}")
    public ResponseEntity<PiezaResponse> actualizar(@PathVariable Long idPieza, @RequestBody PiezaRequest request) {
        return ResponseEntity.ok(piezaService.actualizar(idPieza, request));
    }

    @GetMapping("/{idPieza}")
    public ResponseEntity<PiezaResponse> obtenerPorId(@PathVariable Long idPieza) {
        return ResponseEntity.ok(piezaService.obtenerPorId(idPieza));
    }

    @GetMapping
    public ResponseEntity<List<PiezaResponse>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String medidas,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String calidad
    ) {
        if (marca != null || medidas != null || precioMin != null || precioMax != null || calidad != null) {
            return ResponseEntity.ok(piezaService.filtrar(marca, nombre, medidas, precioMin, precioMax, calidad));
        }
        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(piezaService.buscarPorNombre(nombre));
        }
        if (activo != null) {
            return ResponseEntity.ok(piezaService.listarActivos(activo));
        }
        return ResponseEntity.ok(piezaService.listarTodos());
    }

    @DeleteMapping("/{idPieza}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idPieza) {
        piezaService.eliminar(idPieza);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idPieza}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long idPieza, @RequestParam boolean activo) {
        piezaService.cambiarEstado(idPieza, activo);
        return ResponseEntity.noContent().build();
    }
}
