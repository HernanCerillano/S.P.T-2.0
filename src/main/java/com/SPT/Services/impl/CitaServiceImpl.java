package com.SPT.Services.impl;

import com.SPT.Dtos.Request.CitaRequest;
import com.SPT.Dtos.Response.CitaResponse;
import com.SPT.Mapper.CitaMapper;
import com.SPT.Model.Cita;
import com.SPT.Model.Cliente;
import com.SPT.Model.Vehiculo;
import com.SPT.Repository.CitaRepository;
import com.SPT.Repository.ClienteRepository;
import com.SPT.Repository.VehiculoRepository;
import com.SPT.Services.CitaService;
import com.SPT.Services.WhatsappService;
import com.SPT.Services.exception.BusinessException;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final CitaMapper citaMapper;
    private final WhatsappService whatsappService;

    public CitaServiceImpl(CitaRepository citaRepository,
                           ClienteRepository clienteRepository,
                           VehiculoRepository vehiculoRepository,
                           CitaMapper citaMapper,
                           WhatsappService whatsappService) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.citaMapper = citaMapper;
        this.whatsappService = whatsappService;
    }

    @Override
    @Transactional
    public CitaResponse crear(CitaRequest request) {
        validarSuperposicion(request.getFechaHora(), null);

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + request.getIdVehiculo()));

        Cita entity = citaMapper.toEntity(request);
        entity.setCliente(cliente);
        entity.setVehiculo(vehiculo);
        entity.setFechaCreacion(LocalDateTime.now());

        Cita saved = citaRepository.save(entity);
        whatsappService.encolarRecordatorioCita(saved);
        return citaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CitaResponse actualizar(Long idCita, CitaRequest request) {
        Cita entity = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + idCita));

        validarSuperposicion(request.getFechaHora(), idCita);

        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.getIdCliente()));
        Vehiculo vehiculo = vehiculoRepository.findById(request.getIdVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado: " + request.getIdVehiculo()));

        citaMapper.updateEntityFromRequest(request, entity);
        entity.setCliente(cliente);
        entity.setVehiculo(vehiculo);

        Cita saved = citaRepository.save(entity);
        whatsappService.sincronizarPorCita(saved);
        return citaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long idCita) {
        Cita entity = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + idCita));
        return citaMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listarTodasOrdenadas() {
        return citaRepository.findAll().stream()
                .sorted(Comparator.comparing(Cita::getIdCita))
                .map(citaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> filtrar(Long idCliente, Long idVehiculo, LocalDateTime desde, LocalDateTime hasta, String patente) {
        return citaRepository.findAll().stream()
                .filter(c -> idCliente == null || (c.getCliente() != null && Objects.equals(c.getCliente().getIdCliente(), idCliente)))
                .filter(c -> idVehiculo == null || (c.getVehiculo() != null && Objects.equals(c.getVehiculo().getIdVehiculo(), idVehiculo)))
                .filter(c -> patente == null || patente.isBlank() || (c.getVehiculo() != null && containsIgnoreCase(c.getVehiculo().getPatente(), patente)))
                .filter(c -> desde == null || (c.getFechaHora() != null && !c.getFechaHora().isBefore(desde)))
                .filter(c -> hasta == null || (c.getFechaHora() != null && !c.getFechaHora().isAfter(hasta)))
                .sorted(Comparator.comparing(Cita::getIdCita))
                .map(citaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long idCita) {
        Cita entity = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + idCita));
        whatsappService.cancelarPorCita(idCita);
        citaRepository.delete(entity);
    }

    private void validarSuperposicion(LocalDateTime fechaHora, Long idCita) {
        if (fechaHora == null) {
            throw new BusinessException("La fecha y hora de la cita es obligatoria");
        }

        boolean existe = idCita == null
                ? citaRepository.existsByFechaHora(fechaHora)
                : citaRepository.existsByFechaHoraAndIdCitaNot(fechaHora, idCita);

        if (existe) {
            throw new BusinessException("Ya existe una cita en ese horario");
        }
    }

    private boolean containsIgnoreCase(String source, String value) {
        return source != null && value != null && source.toLowerCase().contains(value.toLowerCase());
    }
}
