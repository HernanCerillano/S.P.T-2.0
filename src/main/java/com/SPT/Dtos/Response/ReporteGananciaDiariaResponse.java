package com.SPT.Dtos.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReporteGananciaDiariaResponse {
    private LocalDate fecha;
    private BigDecimal ingresosBrutos;
    private BigDecimal totalSueldos;
    private BigDecimal gananciaNeta;

    public ReporteGananciaDiariaResponse(LocalDate fecha, BigDecimal ingresosBrutos, BigDecimal totalSueldos, BigDecimal gananciaNeta) {
        this.fecha = fecha;
        this.ingresosBrutos = ingresosBrutos;
        this.totalSueldos = totalSueldos;
        this.gananciaNeta = gananciaNeta;
    }

    public LocalDate getFecha() { return fecha; }
    public BigDecimal getIngresosBrutos() { return ingresosBrutos; }
    public BigDecimal getTotalSueldos() { return totalSueldos; }
    public BigDecimal getGananciaNeta() { return gananciaNeta; }
}
