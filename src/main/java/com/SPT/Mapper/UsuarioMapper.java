package com.SPT.Mapper;

import com.SPT.Dtos.Request.UsuarioRequest;
import com.SPT.Dtos.Response.UsuarioResponse;
import com.SPT.Model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequest request) {
        if (request == null) {
            return null;
        }

        Usuario entity = new Usuario();
        entity.setUserName(request.getUsername());
        entity.setPasswordHash(request.getPasswordHash());
        entity.setRol(request.getRol());
        entity.setActivo(request.isActivo());
        return entity;
    }

    public UsuarioResponse toResponse(Usuario entity) {
        if (entity == null) {
            return null;
        }

        UsuarioResponse response = new UsuarioResponse();
        response.setIdUsuario(entity.getIdUsuario());
        response.setUsername(entity.getUserName());
        response.setRol(entity.getRol());
        response.setActivo(entity.isActivo());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(UsuarioRequest request, Usuario entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setUserName(request.getUsername());
        entity.setPasswordHash(request.getPasswordHash());
        entity.setRol(request.getRol());
        entity.setActivo(request.isActivo());
    }
}
