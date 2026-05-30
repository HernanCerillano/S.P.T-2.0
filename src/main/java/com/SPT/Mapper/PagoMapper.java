package com.SPT.Mapper;

import com.SPT.Dtos.Request.PagoRequest;
import com.SPT.Dtos.Response.PagoResponse;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public Pago toEntity(PagoRequest request) {
        if (request == null) {
            return null;
        }

        Pago entity = new Pago();
        entity.setOrdenTrabajo(mapOrdenTrabajo(request.getIdOt()));
        entity.setMonto(request.getMonto());
        entity.setMetodoPago(request.getMetodoPago());
        entity.setFechaPago(request.getFechaPago());
        entity.setObservaciones(request.getObservaciones());
        return entity;
    }

    public PagoResponse toResponse(Pago entity) {
        if (entity == null) {
            return null;
        }

        PagoResponse response = new PagoResponse();
        response.setIdPago(entity.getIdPago());
        response.setIdOt(entity.getOrdenTrabajo() != null ? entity.getOrdenTrabajo().getIdOt() : null);
        response.setMonto(entity.getMonto());
        response.setMetodoPago(entity.getMetodoPago());
        response.setFechaPago(entity.getFechaPago());
        response.setObservaciones(entity.getObservaciones());
        return response;
    }

    public void updateEntityFromRequest(PagoRequest request, Pago entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setOrdenTrabajo(mapOrdenTrabajo(request.getIdOt()));
        entity.setMonto(request.getMonto());
        entity.setMetodoPago(request.getMetodoPago());
        entity.setFechaPago(request.getFechaPago());
        entity.setObservaciones(request.getObservaciones());
    }

    private OrdenTrabajo mapOrdenTrabajo(Long idOt) {
        if (idOt == null) {
            return null;
        }
        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setIdOt(idOt);
        return ordenTrabajo;
    }
}
