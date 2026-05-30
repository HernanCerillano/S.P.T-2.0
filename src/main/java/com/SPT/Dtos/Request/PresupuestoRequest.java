package com.SPT.Dtos.Request;

import com.SPT.Model.EstadoPresupuesto;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PresupuestoRequest {

    private String numeroPresupuesto;

    @NotNull
    private Long idCliente;

    @NotNull
    private Long idVehiculo;

    private String resumen;
    private BigDecimal total;
    private BigDecimal precioPersonalizado;
    private EstadoPresupuesto estado;
    private boolean ocultarPreciosItems;

    public PresupuestoRequest() {
    }

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
}
