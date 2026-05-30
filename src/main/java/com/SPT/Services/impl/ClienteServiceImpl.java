package com.SPT.Services.impl;

import com.SPT.Dtos.Request.ClienteRequest;
import com.SPT.Dtos.Response.ClienteResponse;
import com.SPT.Mapper.ClienteMapper;
import com.SPT.Model.Cliente;
import com.SPT.Model.VistaSaldoCliente;
import com.SPT.Repository.CitaRepository;
import com.SPT.Repository.ClienteRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Repository.VehiculoRepository;
import com.SPT.Repository.VistaSaldoClienteRepository;
import com.SPT.Services.ClienteService;
import com.SPT.Services.OrdenTrabajoService;
import com.SPT.Services.PresupuestoService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final CitaRepository citaRepository;
    private final PresupuestoService presupuestoService;
    private final OrdenTrabajoService ordenTrabajoService;
    private final ClienteMapper clienteMapper;
    private final VistaSaldoClienteRepository vistaSaldoClienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                              VehiculoRepository vehiculoRepository,
                              PresupuestoRepository presupuestoRepository,
                              OrdenTrabajoRepository ordenTrabajoRepository,
                              CitaRepository citaRepository,
                              PresupuestoService presupuestoService,
                              OrdenTrabajoService ordenTrabajoService,
                              ClienteMapper clienteMapper,
                              VistaSaldoClienteRepository vistaSaldoClienteRepository) {
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.citaRepository = citaRepository;
        this.presupuestoService = presupuestoService;
        this.ordenTrabajoService = ordenTrabajoService;
        this.clienteMapper = clienteMapper;
        this.vistaSaldoClienteRepository = vistaSaldoClienteRepository;
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        Cliente entity = clienteMapper.toEntity(request);
        LocalDateTime ahora = LocalDateTime.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        Cliente saved = clienteRepository.save(entity);
        return clienteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long idCliente, ClienteRequest request) {
        Cliente entity = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + idCliente));
        clienteMapper.updateEntityFromRequest(request, entity);
        entity.setFechaModificacion(LocalDateTime.now());
        return clienteMapper.toResponse(clienteRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long idCliente) {
        Cliente entity = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + idCliente));
        ClienteResponse response = clienteMapper.toResponse(entity);
        poblarSaldo(response, idCliente);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(c -> {
                    ClienteResponse r = clienteMapper.toResponse(c);
                    poblarSaldo(r, c.getIdCliente());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos(boolean activo) {
        return clienteRepository.findByActivo(activo).stream()
                .map(c -> {
                    ClienteResponse r = clienteMapper.toResponse(c);
                    poblarSaldo(r, c.getIdCliente());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscar(String q) {
        if (q == null || q.isBlank()) return java.util.List.of();
        return clienteRepository
                .buscarPorNombreOApellido(q.trim(), org.springframework.data.domain.PageRequest.of(0, 10))
                .stream()
                .map(c -> {
                    ClienteResponse r = clienteMapper.toResponse(c);
                    poblarSaldo(r, c.getIdCliente());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idCliente) {
        Cliente entity = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + idCliente));

        presupuestoRepository.findByCliente_IdCliente(idCliente)
                .forEach(p -> presupuestoService.eliminar(p.getIdPresupuesto()));

        ordenTrabajoRepository.findByCliente_IdCliente(idCliente)
                .forEach(ot -> ordenTrabajoService.eliminar(ot.getIdOt()));

        citaRepository.findByCliente_IdCliente(idCliente)
                .forEach(citaRepository::delete);

        vehiculoRepository.findByCliente_IdCliente(idCliente)
                .forEach(vehiculoRepository::delete);

        clienteRepository.delete(entity);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idCliente, boolean activo) {
        Cliente entity = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + idCliente));
        entity.setActivo(activo);
        entity.setFechaModificacion(LocalDateTime.now());
        clienteRepository.save(entity);
    }

    private void poblarSaldo(ClienteResponse response, Long idCliente) {
        vistaSaldoClienteRepository.findByIdCliente(idCliente)
                .ifPresentOrElse(
                        v -> response.setSaldoPendiente(v.getSaldoPendiente()),
                        () -> response.setSaldoPendiente(BigDecimal.ZERO));
    }
}
