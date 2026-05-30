package com.SPT.Dtos.Response;

import com.SPT.Model.EstadoPresupuesto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PresupuestoResponse {

    private Long idPresupuesto;
    private String numeroPresupuesto;
    private Long idCliente;
    private Long idVehiculo;
    private String resumen;
    private BigDecimal total;
    private BigDecimal precioPersonalizado;
    private EstadoPresupuesto estado;
    private boolean ocultarPreciosItems;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public PresupuestoResponse() {
    }

    public Long getIdPresupuesto() { return idPresupuesto; }
    public void setIdPresupuesto(Long idPresupuesto) { this.idPresupuesto = idPresupuesto; }

    public String getNumeroPresupuesto() { return numeroPresupuesto; }
    public void setNumeroPresupuesto(String numeroPresupuesto) { this.numeroPresupuesto = numeroPresupuesto; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public Long getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Long idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getPrecioPersonalizado() { return precioPersonalizado; }
    public void setPrecioPersonalizado(BigDecimal precioPersonalizado) { this.precioPersonalizado = precioPersonalizado; }

    public EstadoPresupuesto getEstado() { return estado; }
    public void setEstado(EstadoPresupuesto estado) { this.estado = estado; }

    public boolean isOcultarPreciosItems() { return ocultarPreciosItems; }
    public void setOcultarPreciosItems(boolean ocultarPreciosItems) { this.ocultarPreciosItems = ocultarPreciosItems; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
