package com.SPT.Dtos.Response;

import com.SPT.Model.TipoCita;
import java.time.LocalDateTime;

public class CitaResponse {

    private Long idCita;
    private Long idCliente;
    private Long idVehiculo;
    private LocalDateTime fechaHora;
    private TipoCita tipoCita;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    public CitaResponse() {
    }

    public CitaResponse(Long idCita, Long idCliente, Long idVehiculo, LocalDateTime fechaHora, TipoCita tipoCita, String observaciones, LocalDateTime fechaCreacion) {
        this.idCita = idCita;
        this.idCliente = idCliente;
        this.idVehiculo = idVehiculo;
        this.fechaHora = fechaHora;
        this.tipoCita = tipoCita;
        this.observaciones = observaciones;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getIdCita() {
        return idCita;
    }

    public void setIdCita(Long idCita) {
        this.idCita = idCita;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoCita getTipoCita() {
        return tipoCita;
    }

    public void setTipoCita(TipoCita tipoCita) {
        this.tipoCita = tipoCita;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
