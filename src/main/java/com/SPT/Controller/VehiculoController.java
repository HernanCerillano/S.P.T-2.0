package com.SPT.Controller;

import com.SPT.Dtos.Request.VehiculoRequest;
import com.SPT.Dtos.Response.VehiculoResponse;
import com.SPT.Services.VehiculoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping
    public ResponseEntity<VehiculoResponse> crear(@RequestBody VehiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(request));
    }

    @PutMapping("/{idVehiculo}")
    public ResponseEntity<VehiculoResponse> actualizar(@PathVariable Long idVehiculo, @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizar(idVehiculo, request));
    }

    @GetMapping("/{idVehiculo}")
    public ResponseEntity<VehiculoResponse> obtenerPorId(@PathVariable Long idVehiculo) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(idVehiculo));
    }

    @GetMapping
    public ResponseEntity<List<VehiculoResponse>> listar(@RequestParam(required = false) Long idCliente) {
        if (idCliente == null) {
            return ResponseEntity.ok(vehiculoService.listarTodos());
        }
        return ResponseEntity.ok(vehiculoService.listarPorCliente(idCliente));
    }

    @DeleteMapping("/{idVehiculo}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idVehiculo) {
        vehiculoService.eliminar(idVehiculo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idVehiculo}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long idVehiculo, @RequestParam boolean activo) {
        vehiculoService.cambiarEstado(idVehiculo, activo);
        return ResponseEntity.noContent().build();
    }
}
