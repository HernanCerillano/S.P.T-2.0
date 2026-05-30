package com.SPT.Services.impl;

import com.SPT.Dtos.Request.PresupuestoDetalleRequest;
import com.SPT.Dtos.Request.PresupuestoRequest;
import com.SPT.Dtos.Response.PresupuestoDetalleResponse;
import com.SPT.Dtos.Response.PresupuestoResponse;
import com.SPT.Mapper.PresupuestoDetalleMapper;
import com.SPT.Mapper.PresupuestoMapper;
import com.SPT.Model.Cliente;
import com.SPT.Model.EstadoPresupuesto;
import com.SPT.Model.Pieza;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.PresupuestoDetalle;
import com.SPT.Model.TipoItemDetalle;
import com.SPT.Model.TipoServicio;
import com.SPT.Model.Vehiculo;
import com.SPT.Repository.ClienteRepository;
import com.SPT.Repository.PiezaRepository;
import com.SPT.Repository.PresupuestoDetalleRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Repository.TipoServicioRepository;
import com.SPT.Repository.VehiculoRepository;
import com.SPT.Services.OrdenTrabajoService;
import com.SPT.Services.PresupuestoService;
import com.SPT.Services.exception.BusinessException;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PresupuestoServiceImpl implements PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final PresupuestoDetalleRepository presupuestoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PiezaRepository piezaRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final OrdenTrabajoService ordenTrabajoService;
    private final PresupuestoMapper presupuestoMapper;
    private final PresupuestoDetalleMapper detalleMapper;

    public PresupuestoServiceImpl(PresupuestoRepository presupuestoRepository,
                                  PresupuestoDetalleRepository presupuestoDetalleRepository,
                                  ClienteRepository clienteRepository,
                                  VehiculoRepository vehiculoRepository,
                                  PiezaRepository piezaRepository,
                                  TipoServicioRepository tipoServicioRepository,
                                  OrdenTrabajoService ordenTrabajoService,
                                  PresupuestoMapper presupuestoMapper,
                                  PresupuestoDetalleMapper detalleMapper) {
        this.presupuestoRepository = presupuestoRepository;
        this.presupuestoDetalleRepository = presupuestoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.piezaRepository = piezaRepository;
        this.tipoServicioRepository = tipoServicioRepository;
        this.ordenTrabajoService = ordenTrabajoService;
        this.presupuestoMapper = presupuestoMapper;
        this.detalleMapper = detalleMapper;
    }

    @Override
    @Transactional
    public PresupuestoResponse crear(PresupuestoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + request.getIdVehiculo()));

        Presupuesto entity = presupuestoMapper.toEntity(request);
        entity.setCliente(cliente);
        entity.setVehiculo(vehiculo);
        entity.setNumeroPresupuesto(generarNumeroSiFalta(null));
        entity.setEstado(EstadoPresupuesto.ACTIVO);
        entity.setTotal(request.getTotal() != null ? request.getTotal() : BigDecimal.ZERO);
        entity.setFechaCreacion(LocalDateTime.now());
        entity.setFechaModificacion(LocalDateTime.now());

        Presupuesto saved = presupuestoRepository.save(entity);
        return presupuestoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PresupuestoResponse actualizar(Long idPresupuesto, PresupuestoRequest request) {
        Presupuesto entity = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + request.getIdVehiculo()));

        String numeroActual = entity.getNumeroPresupuesto();
        presupuestoMapper.updateEntityFromRequest(request, entity);
        entity.setCliente(cliente);
        entity.setVehiculo(vehiculo);
        entity.setNumeroPresupuesto(numeroActual);
        entity.setFechaModificacion(LocalDateTime.now());

        Presupuesto saved = presupuestoRepository.save(entity);
        return presupuestoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoResponse obtenerPorId(Long idPresupuesto) {
        Presupuesto entity = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));
        return presupuestoMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoResponse> listarTodos() {
        return presupuestoRepository.findAll().stream()
                .map(presupuestoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoResponse> filtrar(String numero,
                                             Long idCliente,
                                             Long idVehiculo,
                                             LocalDateTime desde,
                                             LocalDateTime hasta,
                                             EstadoPresupuesto estado,
                                             String patente) {
        return presupuestoRepository.findAll().stream()
                .filter(p -> numero == null || numero.isBlank() || containsIgnoreCase(p.getNumeroPresupuesto(), numero))
                .filter(p -> idCliente == null || (p.getCliente() != null && Objects.equals(p.getCliente().getIdCliente(), idCliente)))
                .filter(p -> idVehiculo == null || (p.getVehiculo() != null && Objects.equals(p.getVehiculo().getIdVehiculo(), idVehiculo)))
                .filter(p -> patente == null || patente.isBlank() || (p.getVehiculo() != null && containsIgnoreCase(p.getVehiculo().getPatente(), patente)))
                .filter(p -> estado == null || p.getEstado() == estado)
                .filter(p -> desde == null || (p.getFechaCreacion() != null && !p.getFechaCreacion().isBefore(desde)))
                .filter(p -> hasta == null || (p.getFechaCreacion() != null && !p.getFechaCreacion().isAfter(hasta)))
                .map(presupuestoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PresupuestoResponse archivar(Long idPresupuesto) {
        Presupuesto entity = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));
        entity.setEstado(EstadoPresupuesto.ARCHIVADO);
        entity.setFechaModificacion(LocalDateTime.now());
        return presupuestoMapper.toResponse(presupuestoRepository.save(entity));
    }

    @Override
    @Transactional
    public PresupuestoResponse desarchivar(Long idPresupuesto) {
        Presupuesto entity = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));
        entity.setEstado(EstadoPresupuesto.ACTIVO);
        entity.setFechaModificacion(LocalDateTime.now());
        return presupuestoMapper.toResponse(presupuestoRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long idPresupuesto) {
        Presupuesto entity = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));

        if (entity.getOrdenTrabajo() != null && entity.getOrdenTrabajo().getIdOt() != null) {
            ordenTrabajoService.eliminar(entity.getOrdenTrabajo().getIdOt());
            entity.setOrdenTrabajo(null);
        }

        presupuestoDetalleRepository.deleteAll(
                presupuestoDetalleRepository.findByPresupuesto_IdPresupuesto(idPresupuesto));

        presupuestoRepository.delete(entity);
    }

    @Override
    @Transactional
    public PresupuestoDetalleResponse agregarDetalle(Long idPresupuesto, PresupuestoDetalleRequest request) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));

        PresupuestoDetalle detalle = detalleMapper.toEntity(request);
        detalle.setPresupuesto(presupuesto);
        aplicarDatosItem(detalle, request);
        detalle.setSubtotal(calcularSubtotal(detalle.getCantidad(), detalle.getPrecioUnitario()));

        PresupuestoDetalle saved = presupuestoDetalleRepository.save(detalle);
        recalcularTotal(idPresupuesto);
        return detalleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PresupuestoDetalleResponse actualizarDetalle(Long idDetallePresupuesto, PresupuestoDetalleRequest request) {
        PresupuestoDetalle detalle = presupuestoDetalleRepository.findById(idDetallePresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de presupuesto no encontrado: " + idDetallePresupuesto));

        detalleMapper.updateEntityFromRequest(request, detalle);
        aplicarDatosItem(detalle, request);
        detalle.setSubtotal(calcularSubtotal(detalle.getCantidad(), detalle.getPrecioUnitario()));

        PresupuestoDetalle saved = presupuestoDetalleRepository.save(detalle);
        if (saved.getPresupuesto() != null) {
            recalcularTotal(saved.getPresupuesto().getIdPresupuesto());
        }
        return detalleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void eliminarDetalle(Long idDetallePresupuesto) {
        PresupuestoDetalle detalle = presupuestoDetalleRepository.findById(idDetallePresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de presupuesto no encontrado: " + idDetallePresupuesto));

        Long idPresupuesto = detalle.getPresupuesto() != null ? detalle.getPresupuesto().getIdPresupuesto() : null;
        presupuestoDetalleRepository.delete(detalle);
        if (idPresupuesto != null) {
            recalcularTotal(idPresupuesto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoDetalleResponse> listarDetalles(Long idPresupuesto) {
        return presupuestoDetalleRepository.findByPresupuesto_IdPresupuesto(idPresupuesto)
                .stream()
                .map(detalleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recalcularTotal(Long idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));

        BigDecimal total = presupuestoDetalleRepository.findByPresupuesto_IdPresupuesto(idPresupuesto)
                .stream()
                .map(PresupuestoDetalle::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        presupuesto.setTotal(total);
        presupuesto.setFechaModificacion(LocalDateTime.now());
        presupuestoRepository.save(presupuesto);
    }

    private void aplicarDatosItem(PresupuestoDetalle detalle, PresupuestoDetalleRequest request) {
        if (detalle.getTipoItem() == null) {
            throw new BusinessException("El tipo de item es obligatorio");
        }
        if (detalle.getTipoItem() == TipoItemDetalle.SERVICIO) {
            detalle.setCantidad(1);
        }
        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero");
        }

        if (detalle.getTipoItem() == TipoItemDetalle.PIEZA) {
            Long idPieza = request.getIdPieza();
            if (idPieza == null) {
                throw new BusinessException("Para item PIEZA se requiere idPieza");
            }
            Pieza pieza = piezaRepository.findById(idPieza)
                    .orElseThrow(() -> new ResourceNotFoundException("Pieza no encontrada: " + idPieza));
            detalle.setPieza(pieza);
            detalle.setTipoServicio(null);
            if (detalle.getDescripcionItem() == null || detalle.getDescripcionItem().isBlank()) {
                detalle.setDescripcionItem(pieza.getNombre());
            }
            if (detalle.getPrecioUnitario() == null) {
                detalle.setPrecioUnitario(pieza.getPrecioUnitario());
            }
            return;
        }

        Long idServicio = request.getIdServicio();
        if (idServicio == null) {
            throw new BusinessException("Para item SERVICIO se requiere idServicio");
        }
        TipoServicio servicio = tipoServicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));
        detalle.setTipoServicio(servicio);
        detalle.setPieza(null);
        if (detalle.getDescripcionItem() == null || detalle.getDescripcionItem().isBlank()) {
            detalle.setDescripcionItem(servicio.getNombre());
        }
        if (detalle.getPrecioUnitario() == null) {
            detalle.setPrecioUnitario(servicio.getPrecioBase());
        }
    }

    private BigDecimal calcularSubtotal(Integer cantidad, BigDecimal precioUnitario) {
        if (precioUnitario == null) {
            throw new BusinessException("El precio unitario es obligatorio");
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    private String generarNumeroSiFalta(String numero) {
        if (numero != null && !numero.isBlank()) {
            return numero;
        }
        long siguiente = presupuestoRepository.findTopByOrderByIdPresupuestoDesc()
                .map(p -> p.getIdPresupuesto() + 1)
                .orElse(1L);
        return String.format("PRESS-%07d", siguiente);
    }

    private boolean containsIgnoreCase(String source, String value) {
        return source != null && source.toLowerCase().contains(value.toLowerCase());
    }
}
