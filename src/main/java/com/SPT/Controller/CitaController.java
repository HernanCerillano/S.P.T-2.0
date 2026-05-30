package com.SPT.Controller;

import com.SPT.Dtos.Request.CitaRequest;
import com.SPT.Dtos.Response.CitaResponse;
import com.SPT.Services.CitaService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(@RequestBody CitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(request));
    }

    @PutMapping("/{idCita}")
    public ResponseEntity<CitaResponse> actualizar(@PathVariable Long idCita, @RequestBody CitaRequest request) {
        return ResponseEntity.ok(citaService.actualizar(idCita, request));
    }

    @GetMapping("/{idCita}")
    public ResponseEntity<CitaResponse> obtenerPorId(@PathVariable Long idCita) {
        return ResponseEntity.ok(citaService.obtenerPorId(idCita));
    }

    @GetMapping
    public ResponseEntity<List<CitaResponse>> listar(@RequestParam(required = false) Long idCliente,
                                                     @RequestParam(required = false) Long idVehiculo,
                                                     @RequestParam(required = false) LocalDateTime desde,
                                                     @RequestParam(required = false) LocalDateTime hasta,
                                                     @RequestParam(required = false) String patente) {
        if (idCliente != null || idVehiculo != null || desde != null || hasta != null || patente != null) {
            return ResponseEntity.ok(citaService.filtrar(idCliente, idVehiculo, desde, hasta, patente));
        }
        return ResponseEntity.ok(citaService.listarTodasOrdenadas());
    }

    @DeleteMapping("/{idCita}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idCita) {
        citaService.eliminar(idCita);
        return ResponseEntity.noContent().build();
    }
}
