package com.SPT.Services.impl;

import com.SPT.Dtos.Request.VehiculoRequest;
import com.SPT.Dtos.Response.VehiculoResponse;
import com.SPT.Mapper.VehiculoMapper;
import com.SPT.Model.Cliente;
import com.SPT.Model.Vehiculo;
import com.SPT.Repository.CitaRepository;
import com.SPT.Repository.ClienteRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Repository.VehiculoRepository;
import com.SPT.Services.OrdenTrabajoService;
import com.SPT.Services.PresupuestoService;
import com.SPT.Services.VehiculoService;
import com.SPT.Services.exception.BusinessException;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final CitaRepository citaRepository;
    private final PresupuestoService presupuestoService;
    private final OrdenTrabajoService ordenTrabajoService;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository,
                               ClienteRepository clienteRepository,
                               PresupuestoRepository presupuestoRepository,
                               OrdenTrabajoRepository ordenTrabajoRepository,
                               CitaRepository citaRepository,
                               PresupuestoService presupuestoService,
                               OrdenTrabajoService ordenTrabajoService,
                               VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.citaRepository = citaRepository;
        this.presupuestoService = presupuestoService;
        this.ordenTrabajoService = ordenTrabajoService;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Override
    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));

        vehiculoRepository.findByPatente(request.getPatente())
                .ifPresent(v -> {
                    throw new BusinessException("La patente ya existe: " + request.getPatente());
                });

        Vehiculo entity = vehiculoMapper.toEntity(request);
        entity.setCliente(cliente);
        LocalDateTime ahora = LocalDateTime.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        Vehiculo saved = vehiculoRepository.save(entity);
        return vehiculoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehiculoResponse actualizar(Long idVehiculo, VehiculoRequest request) {
        Vehiculo entity = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + idVehiculo));

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));

        vehiculoRepository.findByPatente(request.getPatente())
                .filter(v -> !v.getIdVehiculo().equals(idVehiculo))
                .ifPresent(v -> {
                    throw new BusinessException("La patente ya existe: " + request.getPatente());
                });

        vehiculoMapper.updateEntityFromRequest(request, entity);
        entity.setCliente(cliente);
        entity.setFechaModificacion(LocalDateTime.now());
        Vehiculo saved = vehiculoRepository.save(entity);
        return vehiculoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse obtenerPorId(Long idVehiculo) {
        Vehiculo entity = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + idVehiculo));
        return vehiculoMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarTodos() {
        return vehiculoRepository.findAll().stream().map(vehiculoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarPorCliente(Long idCliente) {
        return vehiculoRepository.findByCliente_IdCliente(idCliente).stream()
                .map(vehiculoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idVehiculo) {
        Vehiculo entity = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + idVehiculo));

        presupuestoRepository.findByVehiculo_IdVehiculo(idVehiculo)
                .forEach(p -> presupuestoService.eliminar(p.getIdPresupuesto()));

        ordenTrabajoRepository.findByVehiculo_IdVehiculo(idVehiculo)
                .forEach(ot -> ordenTrabajoService.eliminar(ot.getIdOt()));

        citaRepository.findByVehiculo_IdVehiculo(idVehiculo)
                .forEach(citaRepository::delete);

        vehiculoRepository.delete(entity);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idVehiculo, boolean activo) {
        Vehiculo entity = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + idVehiculo));
        entity.setActivo(activo);
        entity.setFechaModificacion(LocalDateTime.now());
        vehiculoRepository.save(entity);
    }
}
