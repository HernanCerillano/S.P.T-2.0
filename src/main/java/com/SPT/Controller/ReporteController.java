package com.SPT.Controller;

import com.SPT.Dtos.Response.*;
import com.SPT.Services.ReporteService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ganancias/diarias")
    public ResponseEntity<List<ReporteGananciaDiariaResponse>> gananciasDiarias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.gananciasDiarias(desde, hasta));
    }

    @GetMapping("/ganancias/semanales")
    public ResponseEntity<List<ReporteGananciasSemanalResponse>> gananciasSemanales() {
        return ResponseEntity.ok(reporteService.gananciasSemanales());
    }

    @GetMapping("/ganancias/mensuales")
    public ResponseEntity<List<ReporteGananciasMensualResponse>> gananciasMensuales() {
        return ResponseEntity.ok(reporteService.gananciasMensuales());
    }

    @GetMapping("/mano-obra/ot/{id}")
    public ResponseEntity<ReporteManoObraOtResponse> manoObraOt(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.manoObraOt(id));
    }

    @GetMapping("/mano-obra/empleados")
    public ResponseEntity<List<ReporteManoObraOtResponse>> manoObraEmpleados() {
        return ResponseEntity.ok(reporteService.manoObraEmpleados());
    }

    @GetMapping("/caja")
    public ResponseEntity<List<ReporteMovimientoCajaResponse>> caja() {
        return ResponseEntity.ok(reporteService.movimientosCaja());
    }
}
