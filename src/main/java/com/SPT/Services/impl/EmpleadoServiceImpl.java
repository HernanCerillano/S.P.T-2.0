package com.SPT.Services.impl;

import com.SPT.Dtos.Request.EmpleadoRequest;
import com.SPT.Dtos.Response.EmpleadoResponse;
import com.SPT.Mapper.EmpleadoMapper;
import com.SPT.Model.Empleado;
import com.SPT.Repository.EmpleadoRepository;
import com.SPT.Services.EmpleadoService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, EmpleadoMapper empleadoMapper) {
        this.empleadoRepository = empleadoRepository;
        this.empleadoMapper = empleadoMapper;
    }

    @Override
    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        Empleado entity = empleadoMapper.toEntity(request);
        LocalDateTime ahora = LocalDateTime.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        return empleadoMapper.toResponse(empleadoRepository.save(entity));
    }

    @Override
    @Transactional
    public EmpleadoResponse actualizar(Long idEmpleado, EmpleadoRequest request) {
        Empleado entity = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + idEmpleado));
        empleadoMapper.updateEntityFromRequest(request, entity);
        entity.setFechaModificacion(LocalDateTime.now());
        return empleadoMapper.toResponse(empleadoRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponse obtenerPorId(Long idEmpleado) {
        return empleadoMapper.toResponse(empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + idEmpleado)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodos() {
        return empleadoRepository.findAll().stream().map(empleadoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarActivos(boolean activo) {
        return empleadoRepository.findByActivo(activo).stream().map(empleadoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idEmpleado, boolean activo) {
        Empleado entity = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + idEmpleado));
        entity.setActivo(activo);
        entity.setFechaModificacion(LocalDateTime.now());
        empleadoRepository.save(entity);
    }

    @Override
    @Transactional
    public void eliminar(Long idEmpleado) {
        Empleado entity = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + idEmpleado));
        empleadoRepository.delete(entity);
    }
}
