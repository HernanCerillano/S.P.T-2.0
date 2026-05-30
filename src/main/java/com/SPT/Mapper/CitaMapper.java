package com.SPT.Mapper;

import com.SPT.Dtos.Request.CitaRequest;
import com.SPT.Dtos.Response.CitaResponse;
import com.SPT.Model.Cita;
import com.SPT.Model.Cliente;
import com.SPT.Model.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class CitaMapper {

    public Cita toEntity(CitaRequest request) {
        if (request == null) {
            return null;
        }

        Cita entity = new Cita();
        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setVehiculo(mapVehiculo(request.getIdVehiculo()));
        entity.setFechaHora(request.getFechaHora());
        entity.setTipoCita(request.getTipoCita());
        entity.setObservaciones(request.getObservaciones());
        return entity;
    }

    public CitaResponse toResponse(Cita entity) {
        if (entity == null) {
            return null;
        }

        CitaResponse response = new CitaResponse();
        response.setIdCita(entity.getIdCita());
        response.setIdCliente(entity.getCliente() != null ? entity.getCliente().getIdCliente() : null);
        response.setIdVehiculo(entity.getVehiculo() != null ? entity.getVehiculo().getIdVehiculo() : null);
        response.setFechaHora(entity.getFechaHora());
        response.setTipoCita(entity.getTipoCita());
        response.setObservaciones(entity.getObservaciones());
        response.setFechaCreacion(entity.getFechaCreacion());
        return response;
    }

    public void updateEntityFromRequest(CitaRequest request, Cita entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setVehiculo(mapVehiculo(request.getIdVehiculo()));
        entity.setFechaHora(request.getFechaHora());
        entity.setTipoCita(request.getTipoCita());
        entity.setObservaciones(request.getObservaciones());
    }

    private Cliente mapCliente(Long idCliente) {
        if (idCliente == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        return cliente;
    }

    private Vehiculo mapVehiculo(Long idVehiculo) {
        if (idVehiculo == null) {
            return null;
        }
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(idVehiculo);
        return vehiculo;
    }
}
