package com.SPT.Services;

import com.SPT.Dtos.Response.*;
import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    List<ReporteGananciaDiariaResponse> gananciasDiarias(LocalDate desde, LocalDate hasta);
    List<ReporteGananciasSemanalResponse> gananciasSemanales();
    List<ReporteGananciasMensualResponse> gananciasMensuales();
    ReporteManoObraOtResponse manoObraOt(Long idOt);
    List<ReporteManoObraOtResponse> manoObraEmpleados();
    List<ReporteMovimientoCajaResponse> movimientosCaja();
}
