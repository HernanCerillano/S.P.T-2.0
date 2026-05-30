package com.SPT.Controller;

import com.SPT.Dtos.Request.OTDetalleRequest;
import com.SPT.Dtos.Request.OrdenTrabajoRequest;
import com.SPT.Dtos.Response.OTDetalleResponse;
import com.SPT.Dtos.Response.OrdenTrabajoResponse;
import com.SPT.Model.EstadoOT;
import com.SPT.Services.DocumentoPdfService;
import com.SPT.Services.OrdenTrabajoService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes-trabajo")
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenTrabajoService;
    private final DocumentoPdfService documentoPdfService;

    public OrdenTrabajoController(OrdenTrabajoService ordenTrabajoService, DocumentoPdfService documentoPdfService) {
        this.ordenTrabajoService = ordenTrabajoService;
        this.documentoPdfService = documentoPdfService;
    }

    @PostMapping
    public ResponseEntity<OrdenTrabajoResponse> crear(@RequestBody OrdenTrabajoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenTrabajoService.crear(request));
    }

    @PostMapping("/desde-presupuesto/{idPresupuesto}")
    public ResponseEntity<OrdenTrabajoResponse> crearDesdePresupuesto(@PathVariable Long idPresupuesto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenTrabajoService.crearDesdePresupuesto(idPresupuesto));
    }

    @GetMapping("/proximo-numero")
    public ResponseEntity<java.util.Map<String, String>> proximoNumero() {
        return ResponseEntity.ok(java.util.Map.of("numero", ordenTrabajoService.proximoNumero()));
    }

    @PutMapping("/{idOt}")
    public ResponseEntity<OrdenTrabajoResponse> actualizar(@PathVariable Long idOt,
                                                           @RequestBody OrdenTrabajoRequest request) {
        return ResponseEntity.ok(ordenTrabajoService.actualizar(idOt, request));
    }

    @PatchMapping("/{idOt}/estado")
    public ResponseEntity<OrdenTrabajoResponse> cambiarEstado(@PathVariable Long idOt,
                                                              @RequestParam EstadoOT estado) {
        return ResponseEntity.ok(ordenTrabajoService.cambiarEstado(idOt, estado));
    }

    @GetMapping("/{idOt}")
    public ResponseEntity<OrdenTrabajoResponse> obtenerPorId(@PathVariable Long idOt) {
        return ResponseEntity.ok(ordenTrabajoService.obtenerPorId(idOt));
    }

    @DeleteMapping("/{idOt}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idOt) {
        ordenTrabajoService.eliminar(idOt);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OrdenTrabajoResponse>> listar(
            @RequestParam(required = false) String numeroOt,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idVehiculo,
            @RequestParam(required = false) LocalDateTime desde,
            @RequestParam(required = false) LocalDateTime hasta,
            @RequestParam(required = false) EstadoOT estado,
            @RequestParam(required = false) String patente
    ) {
        if (numeroOt != null || idCliente != null || idVehiculo != null || desde != null || hasta != null || estado != null || patente != null) {
            return ResponseEntity.ok(ordenTrabajoService.filtrar(numeroOt, idCliente, idVehiculo, desde, hasta, estado, patente));
        }
        return ResponseEntity.ok(ordenTrabajoService.listarTodos());
    }

    @PostMapping("/{idOt}/detalles")
    public ResponseEntity<OTDetalleResponse> agregarDetalle(@PathVariable Long idOt,
                                                            @RequestBody OTDetalleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenTrabajoService.agregarDetalle(idOt, request));
    }

    @PutMapping("/detalles/{idDetalle}")
    public ResponseEntity<OTDetalleResponse> actualizarDetalle(@PathVariable Long idDetalle,
                                                               @RequestBody OTDetalleRequest request) {
        return ResponseEntity.ok(ordenTrabajoService.actualizarDetalle(idDetalle, request));
    }

    @DeleteMapping("/detalles/{idDetalle}")
    public ResponseEntity<Void> eliminarDetalle(@PathVariable Long idDetalle) {
        ordenTrabajoService.eliminarDetalle(idDetalle);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idOt}/detalles")
    public ResponseEntity<List<OTDetalleResponse>> listarDetalles(@PathVariable Long idOt) {
        return ResponseEntity.ok(ordenTrabajoService.listarDetalles(idOt));
    }

    @GetMapping("/{idOt}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long idOt) {
        byte[] pdf = documentoPdfService.generarPdfOrdenTrabajo(idOt);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ot-" + idOt + ".pdf")
                .body(pdf);
    }
}
