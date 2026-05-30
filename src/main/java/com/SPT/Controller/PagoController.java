package com.SPT.Controller;

import com.SPT.Dtos.Request.PagoRequest;
import com.SPT.Dtos.Response.PagoResponse;
import com.SPT.Services.PagoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> registrar(@RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(request));
    }

    @DeleteMapping("/{idPago}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idPago) {
        pagoService.eliminar(idPago);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listar(@RequestParam(required = false) Long idCliente,
                                                     @RequestParam(required = false) Long idVehiculo,
                                                     @RequestParam(required = false) LocalDateTime desde,
                                                     @RequestParam(required = false) LocalDateTime hasta,
                                                     @RequestParam(required = false) String patente) {
        if (idCliente != null || idVehiculo != null || desde != null || hasta != null || patente != null) {
            return ResponseEntity.ok(pagoService.filtrar(idCliente, idVehiculo, desde, hasta, patente));
        }
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/ot/{idOt}/total-pagado")
    public ResponseEntity<BigDecimal> totalPagado(@PathVariable Long idOt) {
        return ResponseEntity.ok(pagoService.totalPagadoPorOt(idOt));
    }

    @GetMapping("/ot/{idOt}/saldo-pendiente")
    public ResponseEntity<BigDecimal> saldoPendiente(@PathVariable Long idOt) {
        return ResponseEntity.ok(pagoService.saldoPendientePorOt(idOt));
    }
}
