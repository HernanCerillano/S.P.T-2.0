package com.SPT.Dtos.Request;

import java.math.BigDecimal;

public class TipoServicioRequest {

    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private boolean activo;

    public TipoServicioRequest() {
    }

    public TipoServicioRequest(String nombre, String descripcion, BigDecimal precioBase, boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
