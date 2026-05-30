package com.SPT.Controller;

import com.SPT.Dtos.Request.PresupuestoDetalleRequest;
import com.SPT.Dtos.Request.PresupuestoRequest;
import com.SPT.Dtos.Response.PresupuestoDetalleResponse;
import com.SPT.Dtos.Response.PresupuestoResponse;
import com.SPT.Model.EstadoPresupuesto;
import com.SPT.Services.DocumentoPdfService;
import com.SPT.Services.PresupuestoService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;
    private final DocumentoPdfService documentoPdfService;

    public PresupuestoController(PresupuestoService presupuestoService, DocumentoPdfService documentoPdfService) {
        this.presupuestoService = presupuestoService;
        this.documentoPdfService = documentoPdfService;
    }

    @PostMapping
    public ResponseEntity<PresupuestoResponse> crear(@RequestBody PresupuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(request));
    }

    @PutMapping("/{idPresupuesto}")
    public ResponseEntity<PresupuestoResponse> actualizar(@PathVariable Long idPresupuesto,
                                                          @RequestBody PresupuestoRequest request) {
        return ResponseEntity.ok(presupuestoService.actualizar(idPresupuesto, request));
    }

    @PatchMapping("/{idPresupuesto}/archivar")
    public ResponseEntity<PresupuestoResponse> archivar(@PathVariable Long idPresupuesto) {
        return ResponseEntity.ok(presupuestoService.archivar(idPresupuesto));
    }

    @PatchMapping("/{idPresupuesto}/desarchivar")
    public ResponseEntity<PresupuestoResponse> desarchivar(@PathVariable Long idPresupuesto) {
        return ResponseEntity.ok(presupuestoService.desarchivar(idPresupuesto));
    }

    @GetMapping("/{idPresupuesto}")
    public ResponseEntity<PresupuestoResponse> obtenerPorId(@PathVariable Long idPresupuesto) {
        return ResponseEntity.ok(presupuestoService.obtenerPorId(idPresupuesto));
    }

    @DeleteMapping("/{idPresupuesto}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idPresupuesto) {
        presupuestoService.eliminar(idPresupuesto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PresupuestoResponse>> listar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idVehiculo,
            @RequestParam(required = false) LocalDateTime desde,
            @RequestParam(required = false) LocalDateTime hasta,
            @RequestParam(required = false) EstadoPresupuesto estado,
            @RequestParam(required = false) String patente
    ) {
        if (numero != null || idCliente != null || idVehiculo != null || desde != null || hasta != null || estado != null || patente != null) {
            return ResponseEntity.ok(presupuestoService.filtrar(numero, idCliente, idVehiculo, desde, hasta, estado, patente));
        }
        return ResponseEntity.ok(presupuestoService.listarTodos());
    }

    @PostMapping("/{idPresupuesto}/detalles")
    public ResponseEntity<PresupuestoDetalleResponse> agregarDetalle(@PathVariable Long idPresupuesto,
                                                                     @RequestBody PresupuestoDetalleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.agregarDetalle(idPresupuesto, request));
    }

    @PutMapping("/detalles/{idDetalle}")
    public ResponseEntity<PresupuestoDetalleResponse> actualizarDetalle(@PathVariable Long idDetalle,
                                                                        @RequestBody PresupuestoDetalleRequest request) {
        return ResponseEntity.ok(presupuestoService.actualizarDetalle(idDetalle, request));
    }

    @DeleteMapping("/detalles/{idDetalle}")
    public ResponseEntity<Void> eliminarDetalle(@PathVariable Long idDetalle) {
        presupuestoService.eliminarDetalle(idDetalle);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idPresupuesto}/detalles")
    public ResponseEntity<List<PresupuestoDetalleResponse>> listarDetalles(@PathVariable Long idPresupuesto) {
        return ResponseEntity.ok(presupuestoService.listarDetalles(idPresupuesto));
    }

    @GetMapping("/{idPresupuesto}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long idPresupuesto) {
        byte[] pdf = documentoPdfService.generarPdfPresupuesto(idPresupuesto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=presupuesto-" + idPresupuesto + ".pdf")
                .body(pdf);
    }
}
