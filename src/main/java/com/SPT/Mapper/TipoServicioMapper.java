package com.SPT.Mapper;

import com.SPT.Dtos.Request.TipoServicioRequest;
import com.SPT.Dtos.Response.TipoServicioResponse;
import com.SPT.Model.TipoServicio;
import org.springframework.stereotype.Component;

@Component
public class TipoServicioMapper {

    public TipoServicio toEntity(TipoServicioRequest request) {
        if (request == null) {
            return null;
        }

        TipoServicio entity = new TipoServicio();
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setPrecioBase(request.getPrecioBase());
        entity.setActivo(request.isActivo());
        return entity;
    }

    public TipoServicioResponse toResponse(TipoServicio entity) {
        if (entity == null) {
            return null;
        }

        TipoServicioResponse response = new TipoServicioResponse();
        response.setIdServicio(entity.getIdServicio());
        response.setNombre(entity.getNombre());
        response.setDescripcion(entity.getDescripcion());
        response.setPrecioBase(entity.getPrecioBase());
        response.setActivo(entity.isActivo());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(TipoServicioRequest request, TipoServicio entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setPrecioBase(request.getPrecioBase());
        entity.setActivo(request.isActivo());
    }
}
