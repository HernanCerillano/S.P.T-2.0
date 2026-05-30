package com.SPT.Services;

import com.SPT.Dtos.Request.EventoRequest;
import com.SPT.Dtos.Response.CalendarioItemDTO;
import com.SPT.Dtos.Response.EventoResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoService {
    EventoResponse crear(EventoRequest request);
    EventoResponse actualizar(Long id, EventoRequest request);
    EventoResponse obtenerPorId(Long id);
    List<EventoResponse> listarTodos();
    void eliminar(Long id);
    List<CalendarioItemDTO> obtenerCalendario(LocalDateTime desde, LocalDateTime hasta);
}
