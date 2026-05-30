package com.SPT.Mapper;

import com.SPT.Dtos.Request.PiezaRequest;
import com.SPT.Dtos.Response.PiezaResponse;
import com.SPT.Model.Pieza;
import org.springframework.stereotype.Component;

@Component
public class PiezaMapper {

    public Pieza toEntity(PiezaRequest request) {
        if (request == null) {
            return null;
        }

        Pieza entity = new Pieza();
        entity.setNombre(request.getNombre());
        entity.setMarca(request.getMarca());
        entity.setMedidas(request.getMedidas());
        entity.setCalidad(request.getCalidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setActivo(request.isActivo());
        return entity;
    }

    public PiezaResponse toResponse(Pieza entity) {
        if (entity == null) {
            return null;
        }

        PiezaResponse response = new PiezaResponse();
        response.setIdPieza(entity.getIdPieza());
        response.setNombre(entity.getNombre());
        response.setMarca(entity.getMarca());
        response.setMedidas(entity.getMedidas());
        response.setCalidad(entity.getCalidad());
        response.setPrecioUnitario(entity.getPrecioUnitario());
        response.setActivo(entity.isActivo());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(PiezaRequest request, Pieza entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setNombre(request.getNombre());
        entity.setMarca(request.getMarca());
        entity.setMedidas(request.getMedidas());
        entity.setCalidad(request.getCalidad());
        entity.setPrecioUnitario(request.getPrecioUnitario());
        entity.setActivo(request.isActivo());
    }
}
