package com.SPT.Dtos.Response;

import com.SPT.Model.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoResponse {

    private Long idPago;
    private Long idOt;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private LocalDateTime fechaPago;
    private String observaciones;

    public PagoResponse() {
    }

    public PagoResponse(Long idPago, Long idOt, BigDecimal monto, MetodoPago metodoPago, LocalDateTime fechaPago, String observaciones) {
        this.idPago = idPago;
        this.idOt = idOt;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fechaPago = fechaPago;
        this.observaciones = observaciones;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Long getIdOt() {
        return idOt;
    }

    public void setIdOt(Long idOt) {
        this.idOt = idOt;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
