package com.SPT.Services.impl;

import com.SPT.Dtos.Request.PagoEmpleadoRequest;
import com.SPT.Dtos.Response.PagoEmpleadoResponse;
import com.SPT.Mapper.PagoEmpleadoMapper;
import com.SPT.Model.Empleado;
import com.SPT.Model.PagoEmpleado;
import com.SPT.Repository.EmpleadoRepository;
import com.SPT.Repository.PagoEmpleadoRepository;
import com.SPT.Services.PagoEmpleadoService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoEmpleadoServiceImpl implements PagoEmpleadoService {

    private final PagoEmpleadoRepository pagoEmpleadoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PagoEmpleadoMapper pagoEmpleadoMapper;

    public PagoEmpleadoServiceImpl(PagoEmpleadoRepository pagoEmpleadoRepository,
                                    EmpleadoRepository empleadoRepository,
                                    PagoEmpleadoMapper pagoEmpleadoMapper) {
        this.pagoEmpleadoRepository = pagoEmpleadoRepository;
        this.empleadoRepository = empleadoRepository;
        this.pagoEmpleadoMapper = pagoEmpleadoMapper;
    }

    @Override
    @Transactional
    public PagoEmpleadoResponse registrar(PagoEmpleadoRequest request) {
        Empleado empleado = empleadoRepository.findById(request.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + request.getIdEmpleado()));

        PagoEmpleado entity = new PagoEmpleado();
        entity.setEmpleado(empleado);
        entity.setMonto(request.getMonto());
        entity.setPeriodoDesde(request.getPeriodoDesde());
        entity.setPeriodoHasta(request.getPeriodoHasta());
        entity.setFechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDateTime.now());
        entity.setMetodoPago(request.getMetodoPago());
        entity.setObservaciones(request.getObservaciones());
        entity.setFechaCreacion(LocalDateTime.now());

        return pagoEmpleadoMapper.toResponse(pagoEmpleadoRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PagoEmpleadoResponse obtenerPorId(Long id) {
        return pagoEmpleadoMapper.toResponse(pagoEmpleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago de empleado no encontrado: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoEmpleadoResponse> listarTodos() {
        return pagoEmpleadoRepository.findAll().stream()
                .map(pagoEmpleadoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoEmpleadoResponse> listarPorEmpleado(Long idEmpleado) {
        return pagoEmpleadoRepository.findByEmpleado_IdEmpleado(idEmpleado).stream()
                .map(pagoEmpleadoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PagoEmpleado entity = pagoEmpleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago de empleado no encontrado: " + id));
        pagoEmpleadoRepository.delete(entity);
    }
}
