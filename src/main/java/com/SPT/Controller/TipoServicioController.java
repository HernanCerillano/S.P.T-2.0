package com.SPT.Controller;

import com.SPT.Dtos.Request.TipoServicioRequest;
import com.SPT.Dtos.Response.TipoServicioResponse;
import com.SPT.Services.TipoServicioService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos-servicio")
public class TipoServicioController {

    private final TipoServicioService tipoServicioService;

    public TipoServicioController(TipoServicioService tipoServicioService) {
        this.tipoServicioService = tipoServicioService;
    }

    @PostMapping
    public ResponseEntity<TipoServicioResponse> crear(@RequestBody TipoServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoServicioService.crear(request));
    }

    @PutMapping("/{idServicio}")
    public ResponseEntity<TipoServicioResponse> actualizar(@PathVariable Long idServicio, @RequestBody TipoServicioRequest request) {
        return ResponseEntity.ok(tipoServicioService.actualizar(idServicio, request));
    }

    @GetMapping("/{idServicio}")
    public ResponseEntity<TipoServicioResponse> obtenerPorId(@PathVariable Long idServicio) {
        return ResponseEntity.ok(tipoServicioService.obtenerPorId(idServicio));
    }

    @GetMapping
    public ResponseEntity<List<TipoServicioResponse>> listar(@RequestParam(required = false) Boolean activo,
                                                             @RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(tipoServicioService.buscarPorNombre(nombre));
        }
        if (activo != null) {
            return ResponseEntity.ok(tipoServicioService.listarActivos(activo));
        }
        return ResponseEntity.ok(tipoServicioService.listarTodos());
    }

    @DeleteMapping("/{idServicio}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idServicio) {
        tipoServicioService.eliminar(idServicio);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idServicio}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long idServicio, @RequestParam boolean activo) {
        tipoServicioService.cambiarEstado(idServicio, activo);
        return ResponseEntity.noContent().build();
    }
}
