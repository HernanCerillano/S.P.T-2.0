package com.SPT.Dtos.Response;

import java.math.BigDecimal;

public class ReporteManoObraOtResponse {
    private Long idOt;
    private String numeroOt;
    private BigDecimal totalOt;
    private BigDecimal costoManoObra;
    private BigDecimal subtotalNetoOt;

    public ReporteManoObraOtResponse(Long idOt, String numeroOt, BigDecimal totalOt, BigDecimal costoManoObra, BigDecimal subtotalNetoOt) {
        this.idOt = idOt;
        this.numeroOt = numeroOt;
        this.totalOt = totalOt;
        this.costoManoObra = costoManoObra;
        this.subtotalNetoOt = subtotalNetoOt;
    }

    public Long getIdOt() { return idOt; }
    public String getNumeroOt() { return numeroOt; }
    public BigDecimal getTotalOt() { return totalOt; }
    public BigDecimal getCostoManoObra() { return costoManoObra; }
    public BigDecimal getSubtotalNetoOt() { return subtotalNetoOt; }
}
