package com.SPT.Dtos.Request;

import com.SPT.Model.TipoItemDetalle;
import java.math.BigDecimal;

public class PresupuestoDetalleRequest {

    private Long idPresupuesto;
    private TipoItemDetalle tipoItem;
    private Long idPieza;
    private Long idServicio;
    private String descripcionItem;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public PresupuestoDetalleRequest() {
    }

    public PresupuestoDetalleRequest(Long idPresupuesto, TipoItemDetalle tipoItem, Long idPieza, Long idServicio, String descripcionItem, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idPresupuesto = idPresupuesto;
        this.tipoItem = tipoItem;
        this.idPieza = idPieza;
        this.idServicio = idServicio;
        this.descripcionItem = descripcionItem;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getIdPresupuesto() {
        return idPresupuesto;
    }

    public void setIdPresupuesto(Long idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public TipoItemDetalle getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(TipoItemDetalle tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Long getIdPieza() {
        return idPieza;
    }

    public void setIdPieza(Long idPieza) {
        this.idPieza = idPieza;
    }

    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public String getDescripcionItem() {
        return descripcionItem;
    }

    public void setDescripcionItem(String descripcionItem) {
        this.descripcionItem = descripcionItem;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
