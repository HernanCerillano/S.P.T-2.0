package com.SPT.Mapper;

import com.SPT.Dtos.Request.EventoRequest;
import com.SPT.Dtos.Response.EventoResponse;
import com.SPT.Model.Empleado;
import com.SPT.Model.Evento;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public Evento toEntity(EventoRequest request) {
        Evento e = new Evento();
        e.setTitulo(request.getTitulo());
        e.setDescripcion(request.getDescripcion());
        e.setTipo(request.getTipo());
        e.setFechaInicio(request.getFechaInicio());
        e.setFechaFin(request.getFechaFin());
        e.setTodoElDia(request.isTodoElDia());
        e.setColor(request.getColor());
        return e;
    }

    public EventoResponse toResponse(Evento entity) {
        if (entity == null) return null;
        EventoResponse r = new EventoResponse();
        r.setIdEvento(entity.getIdEvento());
        r.setTitulo(entity.getTitulo());
        r.setDescripcion(entity.getDescripcion());
        r.setTipo(entity.getTipo());
        r.setFechaInicio(entity.getFechaInicio());
        r.setFechaFin(entity.getFechaFin());
        r.setTodoElDia(entity.isTodoElDia());
        r.setColor(entity.getColor());
        Empleado emp = entity.getEmpleado();
        if (emp != null) {
            r.setIdEmpleado(emp.getIdEmpleado());
            r.setNombreEmpleado(emp.getNombre() + " " + emp.getApellido());
        }
        r.setFechaCreacion(entity.getFechaCreacion());
        return r;
    }

    public void updateEntityFromRequest(EventoRequest request, Evento entity) {
        entity.setTitulo(request.getTitulo());
        entity.setDescripcion(request.getDescripcion());
        entity.setTipo(request.getTipo());
        entity.setFechaInicio(request.getFechaInicio());
        entity.setFechaFin(request.getFechaFin());
        entity.setTodoElDia(request.isTodoElDia());
        entity.setColor(request.getColor());
    }
}
