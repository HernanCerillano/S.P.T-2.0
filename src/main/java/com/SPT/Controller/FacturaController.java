package com.SPT.Controller;

import com.SPT.Dtos.Request.FacturaDesdeOrigenRequest;
import com.SPT.Dtos.Response.FacturaResponse;
import com.SPT.Model.EstadoFactura;
import com.SPT.Services.FacturaService;
import com.SPT.Services.PdfFacturaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturaService facturaService;
    private final PdfFacturaService pdfFacturaService;

    public FacturaController(FacturaService facturaService, PdfFacturaService pdfFacturaService) {
        this.facturaService = facturaService;
        this.pdfFacturaService = pdfFacturaService;
    }

    @PostMapping("/desde-presupuesto/{id}")
    public ResponseEntity<FacturaResponse> crearDesdePresupuesto(
            @PathVariable Long id,
            @Valid @RequestBody FacturaDesdeOrigenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facturaService.crearDesdePresupuesto(id, request));
    }

    @PostMapping("/desde-ot/{id}")
    public ResponseEntity<FacturaResponse> crearDesdeOt(
            @PathVariable Long id,
            @Valid @RequestBody FacturaDesdeOrigenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facturaService.crearDesdeOt(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listar(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) EstadoFactura estado) {
        return ResponseEntity.ok(facturaService.listar(idCliente, estado));
    }

    @GetMapping("/proximo-numero")
    public ResponseEntity<java.util.Map<String, String>> proximoNumero() {
        return ResponseEntity.ok(java.util.Map.of("numero", facturaService.proximoNumero()));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<FacturaResponse> anular(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.anular(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] pdfBytes = pdfFacturaService.generarPdfFactura(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "factura-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
