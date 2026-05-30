package com.SPT.Services.impl;

import com.SPT.Dtos.Response.*;
import com.SPT.Model.*;
import com.SPT.Repository.*;
import com.SPT.Services.ReporteService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final VistaGananciasDiariasRepository diariasRepository;
    private final VistaGananciasSemanalesRepository semanalesRepository;
    private final VistaGananciasMensualesRepository mensualesRepository;
    private final VistaManoObraOtRepository manoObraOtRepository;
    private final VistaMovimientoCajaRepository movimientoCajaRepository;

    public ReporteServiceImpl(VistaGananciasDiariasRepository diariasRepository,
                               VistaGananciasSemanalesRepository semanalesRepository,
                               VistaGananciasMensualesRepository mensualesRepository,
                               VistaManoObraOtRepository manoObraOtRepository,
                               VistaMovimientoCajaRepository movimientoCajaRepository) {
        this.diariasRepository = diariasRepository;
        this.semanalesRepository = semanalesRepository;
        this.mensualesRepository = mensualesRepository;
        this.manoObraOtRepository = manoObraOtRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteGananciaDiariaResponse> gananciasDiarias(LocalDate desde, LocalDate hasta) {
        List<VistaGananciasDiarias> rows = (desde != null && hasta != null)
                ? diariasRepository.findByFechaBetween(desde, hasta)
                : diariasRepository.findAll();
        return rows.stream()
                .map(r -> new ReporteGananciaDiariaResponse(r.getFecha(), r.getIngresosBrutos(), r.getTotalSueldos(), r.getGananciaNeta()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteGananciasSemanalResponse> gananciasSemanales() {
        return semanalesRepository.findAll().stream()
                .map(r -> new ReporteGananciasSemanalResponse(r.getAnioSemana(), r.getDesde(), r.getHasta(),
                        r.getIngresosBrutos(), r.getTotalSueldos(), r.getGananciaNeta()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteGananciasMensualResponse> gananciasMensuales() {
        return mensualesRepository.findAll().stream()
                .map(r -> new ReporteGananciasMensualResponse(r.getAnioMes(), r.getIngresosBrutos(),
                        r.getTotalSueldos(), r.getGananciaNeta()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteManoObraOtResponse manoObraOt(Long idOt) {
        VistaManoObraOt r = manoObraOtRepository.findById(idOt)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada en vista: " + idOt));
        return new ReporteManoObraOtResponse(r.getIdOt(), r.getNumeroOt(), r.getTotalOt(), r.getCostoManoObra(), r.getSubtotalNetoOt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteManoObraOtResponse> manoObraEmpleados() {
        return manoObraOtRepository.findAll().stream()
                .map(r -> new ReporteManoObraOtResponse(r.getIdOt(), r.getNumeroOt(), r.getTotalOt(), r.getCostoManoObra(), r.getSubtotalNetoOt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteMovimientoCajaResponse> movimientosCaja() {
        // Ordenado por fecha DESC (lo más reciente primero) — la vista SQL no tiene ORDER BY
        // y findAll() devuelve sin orden garantizado.
        return movimientoCajaRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        (VistaMovimientoCaja r) -> r.getId().getFecha(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> new ReporteMovimientoCajaResponse(
                        r.getId().getFecha(),
                        r.getMonto(),
                        r.getTipo(),
                        r.getId().getConcepto()))
                .collect(Collectors.toList());
    }
}
