package com.SPT.Services.impl;

import com.SPT.Dtos.Request.OTDetalleRequest;
import com.SPT.Dtos.Request.OrdenTrabajoRequest;
import com.SPT.Dtos.Response.OTDetalleResponse;
import com.SPT.Dtos.Response.OrdenTrabajoResponse;
import com.SPT.Mapper.OTDetalleMapper;
import com.SPT.Mapper.OrdenTrabajoMapper;
import com.SPT.Model.Cliente;
import com.SPT.Model.EstadoPresupuesto;
import com.SPT.Model.EstadoOT;
import com.SPT.Model.OTDetalle;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Pieza;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.PresupuestoDetalle;
import com.SPT.Model.TipoItemDetalle;
import com.SPT.Model.TipoServicio;
import com.SPT.Model.Vehiculo;
import com.SPT.Repository.ClienteRepository;
import com.SPT.Repository.OTDetalleRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PagoRepository;
import com.SPT.Repository.PiezaRepository;
import com.SPT.Repository.PresupuestoDetalleRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Repository.TipoServicioRepository;
import com.SPT.Repository.VehiculoRepository;
import com.SPT.Services.OrdenTrabajoService;
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
public class OrdenTrabajoServiceImpl implements OrdenTrabajoService {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OTDetalleRepository otDetalleRepository;
    private final PagoRepository pagoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final PresupuestoDetalleRepository presupuestoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PiezaRepository piezaRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final OrdenTrabajoMapper ordenTrabajoMapper;
    private final OTDetalleMapper otDetalleMapper;

    public OrdenTrabajoServiceImpl(OrdenTrabajoRepository ordenTrabajoRepository,
                                   OTDetalleRepository otDetalleRepository,
                                   PagoRepository pagoRepository,
                                   PresupuestoRepository presupuestoRepository,
                                   PresupuestoDetalleRepository presupuestoDetalleRepository,
                                   ClienteRepository clienteRepository,
                                   VehiculoRepository vehiculoRepository,
                                   PiezaRepository piezaRepository,
                                   TipoServicioRepository tipoServicioRepository,
                                   OrdenTrabajoMapper ordenTrabajoMapper,
                                   OTDetalleMapper otDetalleMapper) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.otDetalleRepository = otDetalleRepository;
        this.pagoRepository = pagoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.presupuestoDetalleRepository = presupuestoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.piezaRepository = piezaRepository;
        this.tipoServicioRepository = tipoServicioRepository;
        this.ordenTrabajoMapper = ordenTrabajoMapper;
        this.otDetalleMapper = otDetalleMapper;
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse crear(OrdenTrabajoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + request.getIdVehiculo()));

        OrdenTrabajo entity = ordenTrabajoMapper.toEntity(request);
        entity.setCliente(cliente);
        entity.setVehiculo(vehiculo);
        entity.setNumeroOt(generarNumeroSiFalta(null));
        entity.setEstado(request.getEstado() != null ? request.getEstado() : EstadoOT.EN_ESPERA);
        entity.setTotal(request.getTotal() != null ? request.getTotal() : BigDecimal.ZERO);
        entity.setFechaCreacion(LocalDateTime.now());
        entity.setFechaModificacion(LocalDateTime.now());

        if (request.getIdPresupuesto() != null) {
            Presupuesto presupuesto = presupuestoRepository.findById(request.getIdPresupuesto())
                    .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + request.getIdPresupuesto()));
            if (ordenTrabajoRepository.findByPresupuesto_IdPresupuesto(request.getIdPresupuesto()).isPresent()) {
                throw new BusinessException("El presupuesto ya tiene una OT asociada");
            }
            entity.setPresupuesto(presupuesto);
        }

        OrdenTrabajo saved = ordenTrabajoRepository.save(entity);
        return ordenTrabajoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse crearDesdePresupuesto(Long idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));

        if (ordenTrabajoRepository.findByPresupuesto_IdPresupuesto(idPresupuesto).isPresent()) {
            throw new BusinessException("El presupuesto ya fue convertido en OT");
        }

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setNumeroOt(generarNumeroSiFalta(null));
        ot.setPresupuesto(presupuesto);
        ot.setCliente(presupuesto.getCliente());
        ot.setVehiculo(presupuesto.getVehiculo());
        ot.setResumenTrabajo(presupuesto.getResumen());
        ot.setEstado(EstadoOT.EN_ESPERA);
        ot.setTotal(BigDecimal.ZERO);
        ot.setFechaCreacion(LocalDateTime.now());
        ot.setFechaModificacion(LocalDateTime.now());

        OrdenTrabajo savedOt = ordenTrabajoRepository.save(ot);

        List<PresupuestoDetalle> detallesPresupuesto = presupuestoDetalleRepository.findByPresupuesto_IdPresupuesto(idPresupuesto);
        for (PresupuestoDetalle det : detallesPresupuesto) {
            OTDetalle nuevo = new OTDetalle();
            nuevo.setOrdenTrabajo(savedOt);
            nuevo.setTipoItem(det.getTipoItem());
            nuevo.setPieza(det.getPieza());
            nuevo.setTipoServicio(det.getTipoServicio());
            nuevo.setDescripcionItem(det.getDescripcionItem());
            nuevo.setCantidad(det.getCantidad());
            nuevo.setPrecioUnitario(det.getPrecioUnitario());
            nuevo.setSubtotal(det.getSubtotal());
            otDetalleRepository.save(nuevo);
        }

        presupuesto.setEstado(EstadoPresupuesto.ACEPTADO);
        presupuesto.setFechaModificacion(LocalDateTime.now());
        presupuestoRepository.save(presupuesto);

        recalcularTotal(savedOt.getIdOt());
        return obtenerPorId(savedOt.getIdOt());
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse actualizar(Long idOt, OrdenTrabajoRequest request) {
        OrdenTrabajo entity = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));

        validarEditable(entity);

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + request.getIdVehiculo()));

        String numeroActual = entity.getNumeroOt();
        ordenTrabajoMapper.updateEntityFromRequest(request, entity);
        entity.setCliente(cliente);
        entity.setVehiculo(vehiculo);
        entity.setNumeroOt(numeroActual);
        entity.setFechaModificacion(LocalDateTime.now());

        OrdenTrabajo saved = ordenTrabajoRepository.save(entity);
        return ordenTrabajoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponse cambiarEstado(Long idOt, EstadoOT estado) {
        OrdenTrabajo entity = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));

        EstadoOT estadoAnterior = entity.getEstado();
        entity.setEstado(estado);

        if (!esFinalizada(estadoAnterior) && esFinalizada(estado)) {
            entity.setFechaFinalizacion(LocalDateTime.now());
        }

        entity.setFechaModificacion(LocalDateTime.now());
        OrdenTrabajo saved = ordenTrabajoRepository.save(entity);
        return ordenTrabajoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenTrabajoResponse obtenerPorId(Long idOt) {
        OrdenTrabajo entity = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));
        return ordenTrabajoMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponse> listarTodos() {
        return ordenTrabajoRepository.findAll().stream()
                .map(ordenTrabajoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponse> filtrar(String numeroOt,
                                              Long idCliente,
                                              Long idVehiculo,
                                              LocalDateTime desde,
                                              LocalDateTime hasta,
                                              EstadoOT estado,
                                              String patente) {
        return ordenTrabajoRepository.findAll().stream()
                .filter(ot -> numeroOt == null || numeroOt.isBlank() || containsIgnoreCase(ot.getNumeroOt(), numeroOt))
                .filter(ot -> idCliente == null || (ot.getCliente() != null && Objects.equals(ot.getCliente().getIdCliente(), idCliente)))
                .filter(ot -> idVehiculo == null || (ot.getVehiculo() != null && Objects.equals(ot.getVehiculo().getIdVehiculo(), idVehiculo)))
                .filter(ot -> patente == null || patente.isBlank() || (ot.getVehiculo() != null && containsIgnoreCase(ot.getVehiculo().getPatente(), patente)))
                .filter(ot -> estado == null || ot.getEstado() == estado)
                .filter(ot -> desde == null || (ot.getFechaCreacion() != null && !ot.getFechaCreacion().isBefore(desde)))
                .filter(ot -> hasta == null || (ot.getFechaCreacion() != null && !ot.getFechaCreacion().isAfter(hasta)))
                .map(ordenTrabajoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idOt) {
        OrdenTrabajo entity = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));

        Presupuesto presupuesto = entity.getPresupuesto();
        if (presupuesto != null && presupuesto.getIdPresupuesto() != null) {
            entity.setPresupuesto(null);
            presupuesto.setOrdenTrabajo(null);
            presupuesto.setEstado(EstadoPresupuesto.ACTIVO);
            presupuesto.setFechaModificacion(LocalDateTime.now());
            presupuestoRepository.save(presupuesto);
        }

        otDetalleRepository.deleteAll(otDetalleRepository.findByOrdenTrabajo_IdOt(idOt));
        pagoRepository.deleteAll(pagoRepository.findByOrdenTrabajo_IdOt(idOt));
        ordenTrabajoRepository.delete(entity);
    }

    @Override
    @Transactional
    public OTDetalleResponse agregarDetalle(Long idOt, OTDetalleRequest request) {
        OrdenTrabajo ot = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));

        validarEditable(ot);

        OTDetalle detalle = otDetalleMapper.toEntity(request);
        detalle.setOrdenTrabajo(ot);
        aplicarDatosItem(detalle, request);
        detalle.setSubtotal(calcularSubtotal(detalle.getCantidad(), detalle.getPrecioUnitario()));

        OTDetalle saved = otDetalleRepository.save(detalle);
        recalcularTotal(idOt);
        return otDetalleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OTDetalleResponse actualizarDetalle(Long idDetalleOt, OTDetalleRequest request) {
        OTDetalle detalle = otDetalleRepository.findById(idDetalleOt)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle OT no encontrado: " + idDetalleOt));

        validarEditable(detalle.getOrdenTrabajo());

        otDetalleMapper.updateEntityFromRequest(request, detalle);
        aplicarDatosItem(detalle, request);
        detalle.setSubtotal(calcularSubtotal(detalle.getCantidad(), detalle.getPrecioUnitario()));

        OTDetalle saved = otDetalleRepository.save(detalle);
        if (saved.getOrdenTrabajo() != null) {
            recalcularTotal(saved.getOrdenTrabajo().getIdOt());
        }
        return otDetalleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void eliminarDetalle(Long idDetalleOt) {
        OTDetalle detalle = otDetalleRepository.findById(idDetalleOt)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle OT no encontrado: " + idDetalleOt));

        OrdenTrabajo ot = detalle.getOrdenTrabajo();
        validarEditable(ot);

        Long idOt = ot != null ? ot.getIdOt() : null;
        otDetalleRepository.delete(detalle);
        if (idOt != null) {
            recalcularTotal(idOt);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OTDetalleResponse> listarDetalles(Long idOt) {
        return otDetalleRepository.findByOrdenTrabajo_IdOt(idOt)
                .stream()
                .map(otDetalleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recalcularTotal(Long idOt) {
        OrdenTrabajo ot = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));

        BigDecimal total = otDetalleRepository.findByOrdenTrabajo_IdOt(idOt)
                .stream()
                .map(OTDetalle::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ot.setTotal(total);
        ot.setFechaModificacion(LocalDateTime.now());
        ordenTrabajoRepository.save(ot);
    }

    @Override
    @Transactional(readOnly = true)
    public String proximoNumero() {
        // Delegamos al mismo helper que usa crear() — garantiza que lo que muestra el form
        // coincide exactamente con el que se asignará al guardar.
        return generarNumeroSiFalta(null);
    }

    private void aplicarDatosItem(OTDetalle detalle, OTDetalleRequest request) {
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

    private String generarNumeroSiFalta(String numeroOt) {
        if (numeroOt != null && !numeroOt.isBlank()) {
            return numeroOt;
        }
        long siguiente = ordenTrabajoRepository.findTopByOrderByIdOtDesc()
                .map(ot -> ot.getIdOt() + 1)
                .orElse(1L);
        return String.format("OT-%04d", siguiente);
    }

    private void validarEditable(OrdenTrabajo ordenTrabajo) {
        if (ordenTrabajo != null && esFinalizada(ordenTrabajo.getEstado())) {
            throw new BusinessException("La OT finalizada no puede editarse");
        }
    }

    private boolean esFinalizada(EstadoOT estado) {
        return estado == EstadoOT.FINALIZADA_COMPLETA || estado == EstadoOT.FINALIZADA_INCOMPLETA;
    }

    private boolean containsIgnoreCase(String source, String value) {
        return source != null && source.toLowerCase().contains(value.toLowerCase());
    }
}
