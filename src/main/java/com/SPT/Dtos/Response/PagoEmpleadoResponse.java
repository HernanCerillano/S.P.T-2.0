package com.SPT.Dtos.Response;

import com.SPT.Model.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PagoEmpleadoResponse {

    private Long idPagoEmpleado;
    private Long idEmpleado;
    private String nombreEmpleado;
    private BigDecimal monto;
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;
    private LocalDateTime fechaPago;
    private MetodoPago metodoPago;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    public Long getIdPagoEmpleado() { return idPagoEmpleado; }
    public void setIdPagoEmpleado(Long idPagoEmpleado) { this.idPagoEmpleado = idPagoEmpleado; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getPeriodoDesde() { return periodoDesde; }
    public void setPeriodoDesde(LocalDate periodoDesde) { this.periodoDesde = periodoDesde; }

    public LocalDate getPeriodoHasta() { return periodoHasta; }
    public void setPeriodoHasta(LocalDate periodoHasta) { this.periodoHasta = periodoHasta; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
