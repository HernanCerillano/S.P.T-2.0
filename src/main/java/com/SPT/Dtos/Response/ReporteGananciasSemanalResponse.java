package com.SPT.Dtos.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReporteGananciasSemanalResponse {
    private Integer anioSemana;
    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal ingresosBrutos;
    private BigDecimal totalSueldos;
    private BigDecimal gananciaNeta;

    public ReporteGananciasSemanalResponse(Integer anioSemana, LocalDate desde, LocalDate hasta,
                                            BigDecimal ingresosBrutos, BigDecimal totalSueldos, BigDecimal gananciaNeta) {
        this.anioSemana = anioSemana;
        this.desde = desde;
        this.hasta = hasta;
        this.ingresosBrutos = ingresosBrutos;
        this.totalSueldos = totalSueldos;
        this.gananciaNeta = gananciaNeta;
    }

    public Integer getAnioSemana() { return anioSemana; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public BigDecimal getIngresosBrutos() { return ingresosBrutos; }
    public BigDecimal getTotalSueldos() { return totalSueldos; }
    public BigDecimal getGananciaNeta() { return gananciaNeta; }
}
