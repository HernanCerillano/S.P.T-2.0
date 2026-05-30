package com.SPT.Services.impl;

import com.SPT.Dtos.Request.ConfiguracionTallerRequest;
import com.SPT.Dtos.Response.ConfiguracionTallerResponse;
import com.SPT.Model.ConfiguracionTaller;
import com.SPT.Repository.ConfiguracionTallerRepository;
import com.SPT.Services.ConfiguracionTallerService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionTallerServiceImpl implements ConfiguracionTallerService {

    private static final Byte ID_SINGLETON = 1;

    private final ConfiguracionTallerRepository repository;

    public ConfiguracionTallerServiceImpl(ConfiguracionTallerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionTallerResponse obtener() {
        ConfiguracionTaller entity = repository.findById(ID_SINGLETON)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración del taller no encontrada"));
        return toResponse(entity);
    }

    @Override
    @Transactional
    public ConfiguracionTallerResponse actualizar(ConfiguracionTallerRequest request) {
        ConfiguracionTaller entity = repository.findById(ID_SINGLETON)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración del taller no encontrada"));
        entity.setNombreTaller(request.getNombreTaller());
        entity.setTelefono(request.getTelefono());
        entity.setDireccion(request.getDireccion());
        entity.setEmail(request.getEmail());
        entity.setCuit(request.getCuit());
        entity.setLogoPath(request.getLogoPath());
        entity.setPlantillaWpRecordatorio(request.getPlantillaWpRecordatorio());
        entity.setHorasAnticipacionWp(request.getHorasAnticipacionWp());
        entity.setWpHabilitado(request.isWpHabilitado());
        entity.setFechaModificacion(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    private ConfiguracionTallerResponse toResponse(ConfiguracionTaller entity) {
        ConfiguracionTallerResponse r = new ConfiguracionTallerResponse();
        r.setNombreTaller(entity.getNombreTaller());
        r.setTelefono(entity.getTelefono());
        r.setDireccion(entity.getDireccion());
        r.setEmail(entity.getEmail());
        r.setCuit(entity.getCuit());
        r.setLogoPath(entity.getLogoPath());
        r.setPlantillaWpRecordatorio(entity.getPlantillaWpRecordatorio());
        r.setHorasAnticipacionWp(entity.getHorasAnticipacionWp());
        r.setWpHabilitado(entity.isWpHabilitado());
        r.setFechaModificacion(entity.getFechaModificacion());
        return r;
    }
}
