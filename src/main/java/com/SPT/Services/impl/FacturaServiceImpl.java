package com.SPT.Services.impl;

import com.SPT.Dtos.Request.FacturaDesdeOrigenRequest;
import com.SPT.Dtos.Response.FacturaResponse;
import com.SPT.Mapper.FacturaMapper;
import com.SPT.Model.EstadoFactura;
import com.SPT.Model.Factura;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.Presupuesto;
import com.SPT.Repository.FacturaRepository;
import com.SPT.Repository.OrdenTrabajoRepository;
import com.SPT.Repository.PresupuestoRepository;
import com.SPT.Services.FacturaService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final FacturaMapper facturaMapper;

    public FacturaServiceImpl(FacturaRepository facturaRepository,
                               PresupuestoRepository presupuestoRepository,
                               OrdenTrabajoRepository ordenTrabajoRepository,
                               FacturaMapper facturaMapper) {
        this.facturaRepository = facturaRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.facturaMapper = facturaMapper;
    }

    @Override
    @Transactional
    public FacturaResponse crearDesdePresupuesto(Long idPresupuesto, FacturaDesdeOrigenRequest request) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado: " + idPresupuesto));

        BigDecimal subtotal = presupuesto.getPrecioPersonalizado() != null
                ? presupuesto.getPrecioPersonalizado()
                : (presupuesto.getTotal() != null ? presupuesto.getTotal() : BigDecimal.ZERO);
        BigDecimal impuestos = request.getImpuestos() != null ? request.getImpuestos() : BigDecimal.ZERO;

        Factura factura = new Factura();
        factura.setNumeroFactura(resolverNumero(request.getNumeroFactura()));
        factura.setTipoFactura(request.getTipoFactura());
        factura.setCliente(presupuesto.getCliente());
        factura.setPresupuesto(presupuesto);
        factura.setSubtotal(subtotal);
        factura.setImpuestos(impuestos);
        factura.setTotal(subtotal.add(impuestos));
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setObservaciones(request.getObservaciones());
        factura.setFechaCreacion(LocalDateTime.now());
        factura.setFechaModificacion(LocalDateTime.now());

        return facturaMapper.toResponse(facturaRepository.save(factura));
    }

    @Override
    @Transactional
    public FacturaResponse crearDesdeOt(Long idOt, FacturaDesdeOrigenRequest request) {
        OrdenTrabajo ot = ordenTrabajoRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada: " + idOt));

        BigDecimal subtotal = ot.getPrecioPersonalizado() != null
                ? ot.getPrecioPersonalizado()
                : (ot.getTotal() != null ? ot.getTotal() : BigDecimal.ZERO);
        BigDecimal impuestos = request.getImpuestos() != null ? request.getImpuestos() : BigDecimal.ZERO;

        Factura factura = new Factura();
        factura.setNumeroFactura(resolverNumero(request.getNumeroFactura()));
        factura.setTipoFactura(request.getTipoFactura());
        factura.setCliente(ot.getCliente());
        factura.setOrdenTrabajo(ot);
        factura.setSubtotal(subtotal);
        factura.setImpuestos(impuestos);
        factura.setTotal(subtotal.add(impuestos));
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setObservaciones(request.getObservaciones());
        factura.setFechaCreacion(LocalDateTime.now());
        factura.setFechaModificacion(LocalDateTime.now());

        return facturaMapper.toResponse(facturaRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse obtenerPorId(Long id) {
        return facturaMapper.toResponse(facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponse> listar(Long idCliente, EstadoFactura estado) {
        List<Factura> facturas;
        if (idCliente != null && estado != null) {
            facturas = facturaRepository.findByCliente_IdClienteAndEstado(idCliente, estado);
        } else if (idCliente != null) {
            facturas = facturaRepository.findByCliente_IdCliente(idCliente);
        } else if (estado != null) {
            facturas = facturaRepository.findByEstado(estado);
        } else {
            facturas = facturaRepository.findAll();
        }
        return facturas.stream().map(facturaMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacturaResponse anular(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + id));
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalStateException("La factura ya está anulada");
        }
        factura.setEstado(EstadoFactura.ANULADA);
        factura.setFechaModificacion(LocalDateTime.now());
        return facturaMapper.toResponse(facturaRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public String proximoNumero() {
        int max = facturaRepository.findAll().stream()
                .map(Factura::getNumeroFactura)
                .filter(n -> n != null && !n.isBlank())
                .mapToInt(this::parseNumero)
                .max()
                .orElse(0);
        return String.format("%05d", max + 1);
    }

    /** Si el request manda un número, se respeta. Si viene null/vacío, se calcula el próximo. */
    private String resolverNumero(String numeroRequest) {
        return numeroRequest != null && !numeroRequest.isBlank() ? numeroRequest : proximoNumero();
    }

    /** Extrae el entero más largo del string (tolera prefijos/sufijos no numéricos). */
    private int parseNumero(String numero) {
        String soloDigitos = numero.replaceAll("[^0-9]", "");
        if (soloDigitos.isEmpty()) return 0;
        try {
            return Integer.parseInt(soloDigitos);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
