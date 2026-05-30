package com.SPT.Mapper;

import com.SPT.Dtos.Request.ClienteRequest;
import com.SPT.Dtos.Response.ClienteResponse;
import com.SPT.Model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        if (request == null) return null;
        Cliente entity = new Cliente();
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setTelefono(request.getTelefono());
        entity.setWhatsapp(request.getWhatsapp());
        entity.setEmail(request.getEmail());
        entity.setDireccion(request.getDireccion());
        entity.setActivo(request.isActivo());
        return entity;
    }

    public ClienteResponse toResponse(Cliente entity) {
        if (entity == null) return null;
        ClienteResponse response = new ClienteResponse();
        response.setIdCliente(entity.getIdCliente());
        response.setNombre(entity.getNombre());
        response.setApellido(entity.getApellido());
        response.setTelefono(entity.getTelefono());
        response.setWhatsapp(entity.getWhatsapp());
        response.setEmail(entity.getEmail());
        response.setDireccion(entity.getDireccion());
        response.setActivo(entity.isActivo());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setFechaModificacion(entity.getFechaModificacion());
        return response;
    }

    public void updateEntityFromRequest(ClienteRequest request, Cliente entity) {
        if (request == null || entity == null) return;
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setTelefono(request.getTelefono());
        entity.setWhatsapp(request.getWhatsapp());
        entity.setEmail(request.getEmail());
        entity.setDireccion(request.getDireccion());
        entity.setActivo(request.isActivo());
    }
}
