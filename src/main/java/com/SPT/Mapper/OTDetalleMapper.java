package com.SPT.Mapper;

import com.SPT.Dtos.Request.OTDetalleRequest;
import com.SPT.Dtos.Response.OTDetalleResponse;
import com.SPT.Model.OTDetalle;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Pieza;
import com.SPT.Model.TipoServicio;
import org.springframework.stereotype.Component;

@Component
public class OTDetalleMapper {

    public OTDetalle toEntity(OTDetalleRequest request) {
        if (request == null) {
            return null;
        }

        OTDetalle entity = new OTDetalle();
        entity.setOrdenTrabajo(mapOrdenTrabajo(request.getIdOt()));
        entity.setTipoItem(request.getTipoItem());
        entity.setPieza(mapPieza(request.getIdPieza()));
        entity.setTipoServicio(mapTipoServicio(request.getIdServicio()));
        entity.setDescripcionItem(request.getDescripcionItem());
        entity.setCantidad(request.getCantidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setSubtotal(request.getSubtotal());
        return entity;
    }

    public OTDetalleResponse toResponse(OTDetalle entity) {
        if (entity == null) {
            return null;
        }

        OTDetalleResponse response = new OTDetalleResponse();
        response.setIdDetalleOt(entity.getIdDetalleOt());
        response.setIdOt(entity.getOrdenTrabajo() != null ? entity.getOrdenTrabajo().getIdOt() : null);
        response.setTipoItem(entity.getTipoItem());
        response.setIdPieza(entity.getPieza() != null ? entity.getPieza().getIdPieza() : null);
        response.setIdServicio(entity.getTipoServicio() != null ? entity.getTipoServicio().getIdServicio() : null);
        response.setDescripcionItem(entity.getDescripcionItem());
        response.setCantidad(entity.getCantidad());
        response.setPrecioUnitario(entity.getPrecioUnitario());
        response.setSubtotal(entity.getSubtotal());
        return response;
    }

    public void updateEntityFromRequest(OTDetalleRequest request, OTDetalle entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setOrdenTrabajo(mapOrdenTrabajo(request.getIdOt()));
        entity.setTipoItem(request.getTipoItem());
        entity.setPieza(mapPieza(request.getIdPieza()));
        entity.setTipoServicio(mapTipoServicio(request.getIdServicio()));
        entity.setDescripcionItem(request.getDescripcionItem());
        entity.setCantidad(request.getCantidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setSubtotal(request.getSubtotal());
    }

    private OrdenTrabajo mapOrdenTrabajo(Long idOt) {
        if (idOt == null) {
            return null;
        }
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setIdOt(idOt);
        return ordenTrabajo;
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
