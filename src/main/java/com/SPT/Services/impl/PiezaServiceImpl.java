package com.SPT.Services.impl;

import com.SPT.Dtos.Request.PiezaRequest;
import com.SPT.Dtos.Response.PiezaResponse;
import com.SPT.Mapper.PiezaMapper;
import com.SPT.Model.OTDetalle;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Pieza;
import com.SPT.Model.Presupuesto;
import com.SPT.Model.PresupuestoDetalle;
import com.SPT.Repository.OTDetalleRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PiezaRepository;
import com.SPT.Repository.PresupuestoDetalleRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Services.PiezaService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PiezaServiceImpl implements PiezaService {

    private final PiezaRepository piezaRepository;
    private final PresupuestoDetalleRepository presupuestoDetalleRepository;
    private final OTDetalleRepository otDetalleRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final PiezaMapper piezaMapper;

    public PiezaServiceImpl(PiezaRepository piezaRepository,
                            PresupuestoDetalleRepository presupuestoDetalleRepository,
                            OTDetalleRepository otDetalleRepository,
                            PresupuestoRepository presupuestoRepository,
                            OrdenTrabajoRepository ordenTrabajoRepository,
                            PiezaMapper piezaMapper) {
        this.piezaRepository = piezaRepository;
        this.presupuestoDetalleRepository = presupuestoDetalleRepository;
        this.otDetalleRepository = otDetalleRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.piezaMapper = piezaMapper;
    }

    @Override
    @Transactional
    public PiezaResponse crear(PiezaRequest request) {
        Pieza entity = piezaMapper.toEntity(request);
        LocalDateTime ahora = LocalDateTime.now();
        entity.setFechaCreacion(ahora);
        entity.setFechaModificacion(ahora);
        return piezaMapper.toResponse(piezaRepository.save(entity));
    }

    @Override
    @Transactional
    public PiezaResponse actualizar(Long idPieza, PiezaRequest request) {
        Pieza entity = piezaRepository.findById(idPieza)
                .orElseThrow(() -> new ResourceNotFoundException("Pieza no encontrada: " + idPieza));
        piezaMapper.updateEntityFromRequest(request, entity);
        entity.setFechaModificacion(LocalDateTime.now());
        return piezaMapper.toResponse(piezaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PiezaResponse obtenerPorId(Long idPieza) {
        Pieza entity = piezaRepository.findById(idPieza)
                .orElseThrow(() -> new ResourceNotFoundException("Pieza no encontrada: " + idPieza));
        return piezaMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiezaResponse> listarTodos() {
        return piezaRepository.findAll().stream().map(piezaMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiezaResponse> listarActivos(boolean activo) {
        return piezaRepository.findByActivo(activo).stream().map(piezaMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiezaResponse> buscarPorNombre(String nombre) {
        return piezaRepository.findByNombreContainingIgnoreCase(nombre == null ? "" : nombre)
                .stream()
                .map(piezaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PiezaResponse> filtrar(String marca, String nombre, String medidas,
                                       BigDecimal precioMin, BigDecimal precioMax, String calidad) {
        BigDecimal min = precioMin == null ? BigDecimal.ZERO : precioMin;
        BigDecimal max = precioMax == null ? new BigDecimal("9999999999") : precioMax;
        return piezaRepository
                .findByMarcaContainingIgnoreCaseAndNombreContainingIgnoreCaseAndMedidasContainingIgnoreCaseAndPrecioUnitarioBetweenAndCalidadContainingIgnoreCase(
                        marca == null ? "" : marca,
                        nombre == null ? "" : nombre,
                        medidas == null ? "" : medidas,
                        min, max,
                        calidad == null ? "" : calidad)
                .stream()
                .map(piezaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idPieza) {
        Pieza entity = piezaRepository.findById(idPieza)
                .orElseThrow(() -> new ResourceNotFoundException("Pieza no encontrada: " + idPieza));

        List<Long> presupuestosAfectados = presupuestoDetalleRepository.findByPieza_IdPieza(idPieza)
                .stream()
                .map(PresupuestoDetalle::getPresupuesto)
                .filter(java.util.Objects::nonNull)
                .map(Presupuesto::getIdPresupuesto)
                .distinct()
                .toList();
        presupuestoDetalleRepository.deleteAll(presupuestoDetalleRepository.findByPieza_IdPieza(idPieza));

        List<Long> otsAfectadas = otDetalleRepository.findByPieza_IdPieza(idPieza)
                .stream()
                .map(OTDetalle::getOrdenTrabajo)
                .filter(java.util.Objects::nonNull)
                .map(OrdenTrabajo::getIdOt)
                .distinct()
                .toList();
        otDetalleRepository.deleteAll(otDetalleRepository.findByPieza_IdPieza(idPieza));

        piezaRepository.delete(entity);

        presupuestosAfectados.forEach(this::recalcularPresupuestoSilencioso);
        otsAfectadas.forEach(this::recalcularOtSilencioso);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idPieza, boolean activo) {
        Pieza entity = piezaRepository.findById(idPieza)
                .orElseThrow(() -> new ResourceNotFoundException("Pieza no encontrada: " + idPieza));
        entity.setActivo(activo);
        entity.setFechaModificacion(LocalDateTime.now());
        piezaRepository.save(entity);
    }

    private void recalcularPresupuestoSilencioso(Long idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto).orElse(null);
        if (presupuesto == null) return;
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
        if (ot == null) return;
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
