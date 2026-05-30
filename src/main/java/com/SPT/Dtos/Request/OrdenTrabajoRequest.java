package com.SPT.Dtos.Request;

import com.SPT.Model.EstadoOT;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdenTrabajoRequest {

    private String numeroOt;
    private Long idPresupuesto;

    @NotNull
    private Long idCliente;

    @NotNull
    private Long idVehiculo;

    private String resumenTrabajo;
    private EstadoOT estado;
    private BigDecimal total;
    private BigDecimal precioPersonalizado;
    private boolean ocultarPreciosItems;
    private LocalDateTime fechaFinalizacion;

    public OrdenTrabajoRequest() {
    }

    public String getNumeroOt() { return numeroOt; }
    public void setNumeroOt(String numeroOt) { this.numeroOt = numeroOt; }

    public Long getIdPresupuesto() { return idPresupuesto; }
    public void setIdPresupuesto(Long idPresupuesto) { this.idPresupuesto = idPresupuesto; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public Long getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Long idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getResumenTrabajo() { return resumenTrabajo; }
    public void setResumenTrabajo(String resumenTrabajo) { this.resumenTrabajo = resumenTrabajo; }

    public EstadoOT getEstado() { return estado; }
    public void setEstado(EstadoOT estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getPrecioPersonalizado() { return precioPersonalizado; }
    public void setPrecioPersonalizado(BigDecimal precioPersonalizado) { this.precioPersonalizado = precioPersonalizado; }

    public boolean isOcultarPreciosItems() { return ocultarPreciosItems; }
    public void setOcultarPreciosItems(boolean ocultarPreciosItems) { this.ocultarPreciosItems = ocultarPreciosItems; }

    public LocalDateTime getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }
}
