package com.SPT.Services.impl;

import com.SPT.Dtos.Request.OtEmpleadoRequest;
import com.SPT.Dtos.Response.OtEmpleadoResponse;
import com.SPT.Mapper.OtEmpleadoMapper;
import com.SPT.Model.Empleado;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.OtEmpleado;
import com.SPT.Repository.EmpleadoRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.OtEmpleadoRepository;
import com.SPT.Services.OtEmpleadoService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtEmpleadoServiceImpl implements OtEmpleadoService {

    private final OtEmpleadoRepository otEmpleadoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final OtEmpleadoMapper otEmpleadoMapper;

    public OtEmpleadoServiceImpl(OtEmpleadoRepository otEmpleadoRepository,
                                  OrdenTrabajoRepository ordenTrabajoRepository,
                                  EmpleadoRepository empleadoRepository,
                                  OtEmpleadoMapper otEmpleadoMapper) {
        this.otEmpleadoRepository = otEmpleadoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.empleadoRepository = empleadoRepository;
        this.otEmpleadoMapper = otEmpleadoMapper;
    }

    @Override
    @Transactional
    public OtEmpleadoResponse asignar(Long idOt, OtEmpleadoRequest request) {
        OrdenTrabajo ot = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada: " + idOt));
        Empleado empleado = empleadoRepository.findById(request.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getIdEmpleado()));

        otEmpleadoRepository.findByOrdenTrabajo_IdOtAndEmpleado_IdEmpleado(idOt, request.getIdEmpleado())
                .ifPresent(e -> { throw new IllegalStateException("El empleado ya está asignado a esta OT"); });

        OtEmpleado entity = new OtEmpleado();
        entity.setOrdenTrabajo(ot);
        entity.setEmpleado(empleado);
        entity.setCostoManoObra(request.getCostoManoObra());
        entity.setHorasTrabajadas(request.getHorasTrabajadas());
        entity.setObservaciones(request.getObservaciones());
        entity.setFechaCreacion(LocalDateTime.now());

        return otEmpleadoMapper.toResponse(otEmpleadoRepository.save(entity));
    }

    @Override
    @Transactional
    public OtEmpleadoResponse actualizar(Long idOt, Long idOtEmpleado, OtEmpleadoRequest request) {
        OtEmpleado entity = otEmpleadoRepository.findById(idOtEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + idOtEmpleado));
        if (!entity.getOrdenTrabajo().getIdOt().equals(idOt)) {
            throw new ResourceNotFoundException("Asignación no pertenece a la OT: " + idOt);
        }
        entity.setCostoManoObra(request.getCostoManoObra());
        entity.setHorasTrabajadas(request.getHorasTrabajadas());
        entity.setObservaciones(request.getObservaciones());
        return otEmpleadoMapper.toResponse(otEmpleadoRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtEmpleadoResponse> listarPorOt(Long idOt) {
        return otEmpleadoRepository.findByOrdenTrabajo_IdOt(idOt).stream()
                .map(otEmpleadoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idOt, Long idOtEmpleado) {
        OtEmpleado entity = otEmpleadoRepository.findById(idOtEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada: " + idOtEmpleado));
        if (!entity.getOrdenTrabajo().getIdOt().equals(idOt)) {
            throw new ResourceNotFoundException("Asignación no pertenece a la OT: " + idOt);
        }
        otEmpleadoRepository.delete(entity);
    }
}
