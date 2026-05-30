package com.SPT.Mapper;

import com.SPT.Dtos.Request.VehiculoRequest;
import com.SPT.Dtos.Response.VehiculoResponse;
import com.SPT.Model.Cliente;
import com.SPT.Model.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class VehiculoMapper {

    public Vehiculo toEntity(VehiculoRequest request) {
        if (request == null) {
            return null;
        }

        Vehiculo entity = new Vehiculo();
        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setPatente(request.getPatente());
        entity.setMarca(request.getMarca());
        entity.setModelo(request.getModelo());
        entity.setAnio(request.getAnio());
        entity.setKilometraje(request.getKilometraje());
        entity.setActivo(request.isActivo());
        return entity;
    }

    public VehiculoResponse toResponse(Vehiculo entity) {
        if (entity == null) {
            return null;
        }

        VehiculoResponse response = new VehiculoResponse();
        response.setIdVehiculo(entity.getIdVehiculo());
        response.setIdCliente(entity.getCliente() != null ? entity.getCliente().getIdCliente() : null);
        response.setPatente(entity.getPatente());
        response.setMarca(entity.getMarca());
        response.setModelo(entity.getModelo());
        response.setAnio(entity.getAnio());
        response.setKilometraje(entity.getKilometraje());
        response.setActivo(entity.isActivo());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(VehiculoRequest request, Vehiculo entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setPatente(request.getPatente());
        entity.setMarca(request.getMarca());
        entity.setModelo(request.getModelo());
        entity.setAnio(request.getAnio());
        entity.setKilometraje(request.getKilometraje());
        entity.setActivo(request.isActivo());
    }

    private Cliente mapCliente(Long idCliente) {
        if (idCliente == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        return cliente;
    }
}
