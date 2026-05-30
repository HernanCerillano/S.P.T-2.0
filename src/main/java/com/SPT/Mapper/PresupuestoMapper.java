package com.SPT.Mapper;

import com.SPT.Dtos.Request.PresupuestoRequest;
import com.SPT.Dtos.Response.PresupuestoResponse;
import com.SPT.Model.Cliente;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class PresupuestoMapper {

    public Presupuesto toEntity(PresupuestoRequest request) {
        if (request == null) return null;
        Presupuesto entity = new Presupuesto();
        entity.setNumeroPresupuesto(request.getNumeroPresupuesto());
        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setVehiculo(mapVehiculo(request.getIdVehiculo()));
        entity.setResumen(request.getResumen());
        entity.setTotal(request.getTotal());
        entity.setPrecioPersonalizado(request.getPrecioPersonalizado());
        entity.setEstado(request.getEstado());
        entity.setOcultarPreciosItems(request.isOcultarPreciosItems());
        return entity;
    }

    public PresupuestoResponse toResponse(Presupuesto entity) {
        if (entity == null) return null;
        PresupuestoResponse response = new PresupuestoResponse();
        response.setIdPresupuesto(entity.getIdPresupuesto());
        response.setNumeroPresupuesto(entity.getNumeroPresupuesto());
        response.setIdCliente(entity.getCliente() != null ? entity.getCliente().getIdCliente() : null);
        response.setIdVehiculo(entity.getVehiculo() != null ? entity.getVehiculo().getIdVehiculo() : null);
        response.setResumen(entity.getResumen());
        response.setTotal(entity.getTotal());
        response.setPrecioPersonalizado(entity.getPrecioPersonalizado());
        response.setEstado(entity.getEstado());
        response.setOcultarPreciosItems(entity.isOcultarPreciosItems());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(PresupuestoRequest request, Presupuesto entity) {
        if (request == null || entity == null) return;
        entity.setNumeroPresupuesto(request.getNumeroPresupuesto());
        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setVehiculo(mapVehiculo(request.getIdVehiculo()));
        entity.setResumen(request.getResumen());
        entity.setTotal(request.getTotal());
        entity.setPrecioPersonalizado(request.getPrecioPersonalizado());
        entity.setEstado(request.getEstado());
        entity.setOcultarPreciosItems(request.isOcultarPreciosItems());
    }

    private Cliente mapCliente(Long idCliente) {
        if (idCliente == null) return null;
        Cliente c = new Cliente();
        c.setIdCliente(idCliente);
        return c;
    }

    private Vehiculo mapVehiculo(Long idVehiculo) {
        if (idVehiculo == null) return null;
        Vehiculo v = new Vehiculo();
        v.setIdVehiculo(idVehiculo);
        return v;
    }
}
