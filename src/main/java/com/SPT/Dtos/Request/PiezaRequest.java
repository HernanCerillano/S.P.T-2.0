package com.SPT.Dtos.Request;

import java.math.BigDecimal;

public class PiezaRequest {

    private String nombre;
    private String marca;
    private String medidas;
    private String calidad;
    private BigDecimal precioUnitario;
    private boolean activo;

    public PiezaRequest() {
    }

    public PiezaRequest(String nombre, String marca, String medidas, String calidad,
                        BigDecimal precioUnitario, boolean activo) {
        this.nombre = nombre;
        this.marca = marca;
        this.medidas = medidas;
        this.calidad = calidad;
        this.precioUnitario = precioUnitario;
        this.activo = activo;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getMedidas() { return medidas; }
    public void setMedidas(String medidas) { this.medidas = medidas; }

    public String getCalidad() { return calidad; }
    public void setCalidad(String calidad) { this.calidad = calidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
