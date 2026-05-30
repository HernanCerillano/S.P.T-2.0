package com.SPT.Services.impl;

import com.SPT.Dtos.Request.EventoRequest;
import com.SPT.Dtos.Response.CalendarioItemDTO;
import com.SPT.Dtos.Response.EventoResponse;
import com.SPT.Mapper.EventoMapper;
import com.SPT.Model.Cita;
import com.SPT.Model.Empleado;
import com.SPT.Model.Evento;
import com.SPT.Repository.CitaRepository;
import com.SPT.Repository.EmpleadoRepository;
import com.SPT.Repository.EventoRepository;
import com.SPT.Services.EventoService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CitaRepository citaRepository;
    private final EventoMapper eventoMapper;

    public EventoServiceImpl(EventoRepository eventoRepository,
                              EmpleadoRepository empleadoRepository,
                              CitaRepository citaRepository,
                              EventoMapper eventoMapper) {
        this.eventoRepository = eventoRepository;
        this.empleadoRepository = empleadoRepository;
        this.citaRepository = citaRepository;
        this.eventoMapper = eventoMapper;
    }

    @Override
    @Transactional
    public EventoResponse crear(EventoRequest request) {
        Evento entity = eventoMapper.toEntity(request);
        if (request.getIdEmpleado() != null) {
            Empleado emp = empleadoRepository.findById(request.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getIdEmpleado()));
            entity.setEmpleado(emp);
        }
        LocalDateTime ahora = LocalDateTime.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        return eventoMapper.toResponse(eventoRepository.save(entity));
    }

    @Override
    @Transactional
    public EventoResponse actualizar(Long id, EventoRequest request) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
        eventoMapper.updateEntityFromRequest(request, entity);
        if (request.getIdEmpleado() != null) {
            Empleado emp = empleadoRepository.findById(request.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getIdEmpleado()));
            entity.setEmpleado(emp);
        } else {
            entity.setEmpleado(null);
        }
        entity.setFechaModificacion(LocalDateTime.now());
        return eventoMapper.toResponse(eventoRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public EventoResponse obtenerPorId(Long id) {
        return eventoMapper.toResponse(eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoResponse> listarTodos() {
        return eventoRepository.findAll().stream()
                .map(eventoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Evento entity = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
        eventoRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioItemDTO> obtenerCalendario(LocalDateTime desde, LocalDateTime hasta) {
        List<CalendarioItemDTO> items = new ArrayList<>();

        List<Cita> citas = (desde != null && hasta != null)
                ? citaRepository.findByFechaHoraBetween(desde, hasta)
                : citaRepository.findAll();

        for (Cita c : citas) {
            CalendarioItemDTO item = new CalendarioItemDTO();
            item.setId(c.getIdCita());
            item.setOrigen(CalendarioItemDTO.Origen.CITA);
            String clienteNombre = c.getCliente() != null
                    ? c.getCliente().getNombre() + " " + c.getCliente().getApellido() : "";
            item.setTitulo("Cita: " + clienteNombre.trim());
            item.setDescripcion(c.getObservaciones());
            item.setFechaInicio(c.getFechaHora());
            item.setFechaFin(c.getFechaHora() != null ? c.getFechaHora().plusHours(1) : null);
            item.setTodoElDia(false);
            item.setTipo(c.getTipoCita() != null ? c.getTipoCita().name() : null);
            items.add(item);
        }

        List<Evento> eventos = (desde != null && hasta != null)
                ? eventoRepository.findByFechaInicioBetween(desde, hasta)
                : eventoRepository.findAll();

        for (Evento e : eventos) {
            CalendarioItemDTO item = new CalendarioItemDTO();
            item.setId(e.getIdEvento());
            item.setOrigen(CalendarioItemDTO.Origen.EVENTO);
            item.setTitulo(e.getTitulo());
            item.setDescripcion(e.getDescripcion());
            item.setFechaInicio(e.getFechaInicio());
            item.setFechaFin(e.getFechaFin());
            item.setTodoElDia(e.isTodoElDia());
            item.setColor(e.getColor());
            item.setTipo(e.getTipo() != null ? e.getTipo().name() : null);
            items.add(item);
        }

        items.sort(Comparator.comparing(CalendarioItemDTO::getFechaInicio,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }
}
