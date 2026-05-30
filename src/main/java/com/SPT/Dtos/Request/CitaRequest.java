package com.SPT.Dtos.Request;

import com.SPT.Model.TipoCita;
import java.time.LocalDateTime;

public class CitaRequest {

    private Long idCliente;
    private Long idVehiculo;
    private LocalDateTime fechaHora;
    private TipoCita tipoCita;
    private String observaciones;

    public CitaRequest() {
    }

    public CitaRequest(Long idCliente, Long idVehiculo, LocalDateTime fechaHora, TipoCita tipoCita, String observaciones) {
        this.idCliente = idCliente;
        this.idVehiculo = idVehiculo;
        this.fechaHora = fechaHora;
        this.tipoCita = tipoCita;
        this.observaciones = observaciones;
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
}
