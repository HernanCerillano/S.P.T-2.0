package com.SPT.Services.impl;

import com.SPT.Dtos.Request.TipoServicioRequest;
import com.SPT.Dtos.Response.TipoServicioResponse;
import com.SPT.Mapper.TipoServicioMapper;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.OTDetalle;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.PresupuestoDetalle;
import com.SPT.Model.TipoServicio;
import com.SPT.Repository.OTDetalleRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PresupuestoDetalleRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Repository.TipoServicioRepository;
import com.SPT.Services.TipoServicioService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TipoServicioServiceImpl implements TipoServicioService {

    private final TipoServicioRepository tipoServicioRepository;
    private final PresupuestoDetalleRepository presupuestoDetalleRepository;
    private final OTDetalleRepository otDetalleRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final TipoServicioMapper tipoServicioMapper;

    public TipoServicioServiceImpl(TipoServicioRepository tipoServicioRepository,
                                   PresupuestoDetalleRepository presupuestoDetalleRepository,
                                   OTDetalleRepository otDetalleRepository,
                                   PresupuestoRepository presupuestoRepository,
                                   OrdenTrabajoRepository ordenTrabajoRepository,
                                   TipoServicioMapper tipoServicioMapper) {
        this.tipoServicioRepository = tipoServicioRepository;
        this.presupuestoDetalleRepository = presupuestoDetalleRepository;
        this.otDetalleRepository = otDetalleRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.tipoServicioMapper = tipoServicioMapper;
    }

    @Override
    @Transactional
    public TipoServicioResponse crear(TipoServicioRequest request) {
        TipoServicio entity = tipoServicioMapper.toEntity(request);
        LocalDateTime ahora = LocalDateTime.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        TipoServicio saved = tipoServicioRepository.save(entity);
        return tipoServicioMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TipoServicioResponse actualizar(Long idServicio, TipoServicioRequest request) {
        TipoServicio entity = tipoServicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));
        tipoServicioMapper.updateEntityFromRequest(request, entity);
        entity.setFechaModificacion(LocalDateTime.now());
        TipoServicio saved = tipoServicioRepository.save(entity);
        return tipoServicioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoServicioResponse obtenerPorId(Long idServicio) {
        TipoServicio entity = tipoServicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));
        return tipoServicioMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicioResponse> listarTodos() {
        return tipoServicioRepository.findAll().stream().map(tipoServicioMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicioResponse> listarActivos(boolean activo) {
        return tipoServicioRepository.findByActivo(activo).stream().map(tipoServicioMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicioResponse> buscarPorNombre(String nombre) {
        return tipoServicioRepository.findByNombreContainingIgnoreCase(nombre == null ? "" : nombre)
                .stream()
                .map(tipoServicioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idServicio) {
        TipoServicio entity = tipoServicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));

        List<Long> presupuestosAfectados = presupuestoDetalleRepository.findByTipoServicio_IdServicio(idServicio)
                .stream()
                .map(PresupuestoDetalle::getPresupuesto)
                .filter(java.util.Objects::nonNull)
                .map(Presupuesto::getIdPresupuesto)
                .distinct()
                .toList();
        presupuestoDetalleRepository.deleteAll(presupuestoDetalleRepository.findByTipoServicio_IdServicio(idServicio));

        List<Long> otsAfectadas = otDetalleRepository.findByTipoServicio_IdServicio(idServicio)
                .stream()
                .map(OTDetalle::getOrdenTrabajo)
                .filter(java.util.Objects::nonNull)
                .map(OrdenTrabajo::getIdOt)
                .distinct()
                .toList();
        otDetalleRepository.deleteAll(otDetalleRepository.findByTipoServicio_IdServicio(idServicio));

        tipoServicioRepository.delete(entity);

        presupuestosAfectados.forEach(this::recalcularPresupuestoSilencioso);
        otsAfectadas.forEach(this::recalcularOtSilencioso);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idServicio, boolean activo) {
        TipoServicio entity = tipoServicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + idServicio));

        if (!activo) {
            boolean usadoEnPresupuesto = presupuestoDetalleRepository.existsByTipoServicio_IdServicio(idServicio);
            boolean usadoEnOt = otDetalleRepository.existsByTipoServicio_IdServicio(idServicio);
            if (usadoEnPresupuesto || usadoEnOt) {
                entity.setActivo(false);
                entity.setFechaModificacion(LocalDateTime.now());
                tipoServicioRepository.save(entity);
                return;
            }
        }

        entity.setActivo(activo);
        entity.setFechaModificacion(LocalDateTime.now());
        tipoServicioRepository.save(entity);
    }

    private void recalcularPresupuestoSilencioso(Long idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto).orElse(null);
        if (presupuesto == null) {
            return;
        }

        BigDecimal total = presupuestoDetalleRepository.findByPresupuesto_IdPresupuesto(idPresupuesto)
                .stream()
                .map(PresupuestoDetalle::getSubtotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        presupuesto.setTotal(total);
        presupuesto.setFechaModificacion(LocalDateTime.now());
        presupuestoRepository.save(presupuesto);
    }

    private void recalcularOtSilencioso(Long idOt) {
        OrdenTrabajo ot = ordenTrabajoRepository.findById(idOt).orElse(null);
        if (ot == null) {
            return;
        }

        BigDecimal total = otDetalleRepository.findByOrdenTrabajo_IdOt(idOt)
                .stream()
                .map(OTDetalle::getSubtotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ot.setTotal(total);
        ot.setFechaModificacion(LocalDateTime.now());
        ordenTrabajoRepository.save(ot);
    }
}
