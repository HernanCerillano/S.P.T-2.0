package com.SPT.Dtos.Response;

import java.math.BigDecimal;

public class ReporteGananciasMensualResponse {
    private String anioMes;
    private BigDecimal ingresosBrutos;
    private BigDecimal totalSueldos;
    private BigDecimal gananciaNeta;

    public ReporteGananciasMensualResponse(String anioMes, BigDecimal ingresosBrutos, BigDecimal totalSueldos, BigDecimal gananciaNeta) {
        this.anioMes = anioMes;
        this.ingresosBrutos = ingresosBrutos;
        this.totalSueldos = totalSueldos;
        this.gananciaNeta = gananciaNeta;
    }

    public String getAnioMes() { return anioMes; }
    public BigDecimal getIngresosBrutos() { return ingresosBrutos; }
    public BigDecimal getTotalSueldos() { return totalSueldos; }
    public BigDecimal getGananciaNeta() { return gananciaNeta; }
}
