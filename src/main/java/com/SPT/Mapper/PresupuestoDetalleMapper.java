package com.SPT.Mapper;

import com.SPT.Dtos.Request.PresupuestoDetalleRequest;
import com.SPT.Dtos.Response.PresupuestoDetalleResponse;
import com.SPT.Model.Pieza;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.PresupuestoDetalle;
import com.SPT.Model.TipoServicio;
import org.springframework.stereotype.Component;

@Component
public class PresupuestoDetalleMapper {

    public PresupuestoDetalle toEntity(PresupuestoDetalleRequest request) {
        if (request == null) {
            return null;
        }

        PresupuestoDetalle entity = new PresupuestoDetalle();
        entity.setPresupuesto(mapPresupuesto(request.getIdPresupuesto()));
        entity.setTipoItem(request.getTipoItem());
        entity.setPieza(mapPieza(request.getIdPieza()));
        entity.setTipoServicio(mapTipoServicio(request.getIdServicio()));
        entity.setDescripcionItem(request.getDescripcionItem());
        entity.setCantidad(request.getCantidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setSubtotal(request.getSubtotal());
        return entity;
    }

    public PresupuestoDetalleResponse toResponse(PresupuestoDetalle entity) {
        if (entity == null) {
            return null;
        }

        PresupuestoDetalleResponse response = new PresupuestoDetalleResponse();
        response.setIdDetallePresupuesto(entity.getIdDetallePresupuesto());
        response.setIdPresupuesto(entity.getPresupuesto() != null ? entity.getPresupuesto().getIdPresupuesto() : null);
        response.setTipoItem(entity.getTipoItem());
        response.setIdPieza(entity.getPieza() != null ? entity.getPieza().getIdPieza() : null);
        response.setIdServicio(entity.getTipoServicio() != null ? entity.getTipoServicio().getIdServicio() : null);
        response.setDescripcionItem(entity.getDescripcionItem());
        response.setCantidad(entity.getCantidad());
        response.setPrecioUnitario(entity.getPrecioUnitario());
        response.setSubtotal(entity.getSubtotal());
        return response;
    }

    public void updateEntityFromRequest(PresupuestoDetalleRequest request, PresupuestoDetalle entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setPresupuesto(mapPresupuesto(request.getIdPresupuesto()));
        entity.setTipoItem(request.getTipoItem());
        entity.setPieza(mapPieza(request.getIdPieza()));
        entity.setTipoServicio(mapTipoServicio(request.getIdServicio()));
        entity.setDescripcionItem(request.getDescripcionItem());
        entity.setCantidad(request.getCantidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setSubtotal(request.getSubtotal());
    }

    private Presupuesto mapPresupuesto(Long idPresupuesto) {
        if (idPresupuesto == null) {
            return null;
        }
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(idPresupuesto);
        return presupuesto;
    }

    private Pieza mapPieza(Long idPieza) {
        if (idPieza == null) {
            return null;
        }
        Pieza pieza = new Pieza();
        pieza.setIdPieza(idPieza);
        return pieza;
    }

    private TipoServicio mapTipoServicio(Long idServicio) {
        if (idServicio == null) {
            return null;
        }
        TipoServicio servicio = new TipoServicio();
        servicio.setIdServicio(idServicio);
        return servicio;
    }
}
