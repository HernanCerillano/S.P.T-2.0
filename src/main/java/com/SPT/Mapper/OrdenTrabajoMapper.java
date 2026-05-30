package com.SPT.Mapper;

import com.SPT.Dtos.Request.OrdenTrabajoRequest;
import com.SPT.Dtos.Response.OrdenTrabajoResponse;
import com.SPT.Model.Cliente;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class OrdenTrabajoMapper {

    public OrdenTrabajo toEntity(OrdenTrabajoRequest request) {
        if (request == null) return null;
        OrdenTrabajo entity = new OrdenTrabajo();
        entity.setNumeroOt(request.getNumeroOt());
        entity.setPresupuesto(mapPresupuesto(request.getIdPresupuesto()));
        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setVehiculo(mapVehiculo(request.getIdVehiculo()));
        entity.setResumenTrabajo(request.getResumenTrabajo());
        entity.setEstado(request.getEstado());
        entity.setTotal(request.getTotal());
        entity.setPrecioPersonalizado(request.getPrecioPersonalizado());
        entity.setOcultarPreciosItems(request.isOcultarPreciosItems());
        entity.setFechaFinalizacion(request.getFechaFinalizacion());
        return entity;
    }

    public OrdenTrabajoResponse toResponse(OrdenTrabajo entity) {
        if (entity == null) return null;
        OrdenTrabajoResponse response = new OrdenTrabajoResponse();
        response.setIdOt(entity.getIdOt());
        response.setNumeroOt(entity.getNumeroOt());
        response.setIdPresupuesto(entity.getPresupuesto() != null ? entity.getPresupuesto().getIdPresupuesto() : null);
        response.setIdCliente(entity.getCliente() != null ? entity.getCliente().getIdCliente() : null);
        response.setIdVehiculo(entity.getVehiculo() != null ? entity.getVehiculo().getIdVehiculo() : null);
        response.setResumenTrabajo(entity.getResumenTrabajo());
        response.setEstado(entity.getEstado());
        response.setTotal(entity.getTotal());
        response.setPrecioPersonalizado(entity.getPrecioPersonalizado());
        response.setOcultarPreciosItems(entity.isOcultarPreciosItems());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaFinalizacion(entity.getFechaFinalizacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(OrdenTrabajoRequest request, OrdenTrabajo entity) {
        if (request == null || entity == null) return;
        entity.setNumeroOt(request.getNumeroOt());
        entity.setPresupuesto(mapPresupuesto(request.getIdPresupuesto()));
        entity.setCliente(mapCliente(request.getIdCliente()));
        entity.setVehiculo(mapVehiculo(request.getIdVehiculo()));
        entity.setResumenTrabajo(request.getResumenTrabajo());
        entity.setEstado(request.getEstado());
        entity.setTotal(request.getTotal());
        entity.setPrecioPersonalizado(request.getPrecioPersonalizado());
        entity.setOcultarPreciosItems(request.isOcultarPreciosItems());
        entity.setFechaFinalizacion(request.getFechaFinalizacion());
    }

    private Presupuesto mapPresupuesto(Long idPresupuesto) {
        if (idPresupuesto == null) return null;
        Presupuesto p = new Presupuesto();
        p.setIdPresupuesto(idPresupuesto);
        return p;
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
