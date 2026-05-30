package com.SPT.Dtos.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReporteMovimientoCajaResponse {
    private LocalDateTime fecha;
    private BigDecimal monto;
    private String tipo;
    private String concepto;

    public ReporteMovimientoCajaResponse(LocalDateTime fecha, BigDecimal monto, String tipo, String concepto) {
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.concepto = concepto;
    }

    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getMonto() { return monto; }
    public String getTipo() { return tipo; }
    public String getConcepto() { return concepto; }
}
