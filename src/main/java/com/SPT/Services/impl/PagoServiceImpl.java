package com.SPT.Services.impl;

import com.SPT.Dtos.Request.PagoRequest;
import com.SPT.Dtos.Response.PagoResponse;
import com.SPT.Mapper.PagoMapper;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Pago;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PagoRepository;
import com.SPT.Services.PagoService;
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
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final PagoMapper pagoMapper;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           OrdenTrabajoRepository ordenTrabajoRepository,
                           PagoMapper pagoMapper) {
        this.pagoRepository = pagoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.pagoMapper = pagoMapper;
    }

    @Override
    @Transactional
    public PagoResponse registrarPago(PagoRequest request) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto debe ser mayor a cero");
        }

        OrdenTrabajo ot = ordenTrabajoRepository.findById(request.getIdOt())
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + request.getIdOt()));

        BigDecimal saldo = saldoPendientePorOt(request.getIdOt());
        if (request.getMonto().compareTo(saldo) > 0) {
            throw new BusinessException("El pago supera el saldo pendiente. Saldo: " + saldo);
        }

        Pago pago = pagoMapper.toEntity(request);
        pago.setOrdenTrabajo(ot);
        pago.setFechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDateTime.now());

        Pago saved = pagoRepository.save(pago);
        return pagoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodos() {
        return pagoRepository.findAll().stream().map(pagoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> filtrar(Long idCliente, Long idVehiculo, LocalDateTime desde, LocalDateTime hasta, String patente) {
        return pagoRepository.findAll().stream()
                .filter(p -> idCliente == null || (p.getOrdenTrabajo() != null
                        && p.getOrdenTrabajo().getCliente() != null
                        && Objects.equals(p.getOrdenTrabajo().getCliente().getIdCliente(), idCliente)))
                .filter(p -> idVehiculo == null || (p.getOrdenTrabajo() != null
                        && p.getOrdenTrabajo().getVehiculo() != null
                        && Objects.equals(p.getOrdenTrabajo().getVehiculo().getIdVehiculo(), idVehiculo)))
                .filter(p -> patente == null || patente.isBlank() || (p.getOrdenTrabajo() != null
                        && p.getOrdenTrabajo().getVehiculo() != null
                        && containsIgnoreCase(p.getOrdenTrabajo().getVehiculo().getPatente(), patente)))
                .filter(p -> desde == null || (p.getFechaPago() != null && !p.getFechaPago().isBefore(desde)))
                .filter(p -> hasta == null || (p.getFechaPago() != null && !p.getFechaPago().isAfter(hasta)))
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + idPago));
        pagoRepository.delete(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalPagadoPorOt(Long idOt) {
        ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));
        return pagoRepository.sumMontoByOrdenTrabajo(idOt);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal saldoPendientePorOt(Long idOt) {
        OrdenTrabajo ot = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOt));

        BigDecimal totalOt = ot.getTotal() == null ? BigDecimal.ZERO : ot.getTotal();
        BigDecimal pagado = pagoRepository.sumMontoByOrdenTrabajo(idOt);
        BigDecimal saldo = totalOt.subtract(pagado);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    private boolean containsIgnoreCase(String source, String value) {
        return source != null && value != null && source.toLowerCase().contains(value.toLowerCase());
    }
}
