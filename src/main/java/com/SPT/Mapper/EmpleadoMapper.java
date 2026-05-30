package com.SPT.Mapper;

import com.SPT.Dtos.Request.EmpleadoRequest;
import com.SPT.Dtos.Response.EmpleadoResponse;
import com.SPT.Model.Empleado;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoMapper {

    public Empleado toEntity(EmpleadoRequest request) {
        if (request == null) return null;
        Empleado e = new Empleado();
        e.setNombre(request.getNombre());
        e.setApellido(request.getApellido());
        e.setDni(request.getDni());
        e.setTelefono(request.getTelefono());
        e.setEmail(request.getEmail());
        e.setRolTaller(request.getRolTaller());
        e.setSueldoBase(request.getSueldoBase());
        e.setActivo(request.isActivo());
        return e;
    }

    public EmpleadoResponse toResponse(Empleado e) {
        if (e == null) return null;
        EmpleadoResponse r = new EmpleadoResponse();
        r.setIdEmpleado(e.getIdEmpleado());
        r.setNombre(e.getNombre());
        r.setApellido(e.getApellido());
        r.setDni(e.getDni());
        r.setTelefono(e.getTelefono());
        r.setEmail(e.getEmail());
        r.setRolTaller(e.getRolTaller());
        r.setSueldoBase(e.getSueldoBase());
        r.setActivo(e.isActivo());
        r.setFechaCreacion(e.getFechaCreacion());
        r.setFechaModificacion(e.getFechaModificacion());
        return r;
    }

    public void updateEntityFromRequest(EmpleadoRequest request, Empleado entity) {
        if (request == null || entity == null) return;
        entity.setNombre(request.getNombre());
        entity.setApellido(request.getApellido());
        entity.setDni(request.getDni());
        entity.setTelefono(request.getTelefono());
        entity.setEmail(request.getEmail());
        entity.setRolTaller(request.getRolTaller());
        entity.setSueldoBase(request.getSueldoBase());
        entity.setActivo(request.isActivo());
    }
}
