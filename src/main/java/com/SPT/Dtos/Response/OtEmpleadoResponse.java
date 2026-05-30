package com.SPT.Dtos.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OtEmpleadoResponse {

    private Long idOtEmpleado;
    private Long idOt;
    private Long idEmpleado;
    private String nombreEmpleado;
    private BigDecimal costoManoObra;
    private BigDecimal horasTrabajadas;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    public Long getIdOtEmpleado() { return idOtEmpleado; }
    public void setIdOtEmpleado(Long idOtEmpleado) { this.idOtEmpleado = idOtEmpleado; }

    public Long getIdOt() { return idOt; }
    public void setIdOt(Long idOt) { this.idOt = idOt; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public BigDecimal getCostoManoObra() { return costoManoObra; }
    public void setCostoManoObra(BigDecimal costoManoObra) { this.costoManoObra = costoManoObra; }

    public BigDecimal getHorasTrabajadas() { return horasTrabajadas; }
    public void setHorasTrabajadas(BigDecimal horasTrabajadas) { this.horasTrabajadas = horasTrabajadas; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
