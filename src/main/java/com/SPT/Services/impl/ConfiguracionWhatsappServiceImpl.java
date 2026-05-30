package com.SPT.Services.impl;

import com.SPT.Dtos.Request.ConfiguracionWhatsappRequest;
import com.SPT.Dtos.Response.ConfiguracionWhatsappResponse;
import com.SPT.Model.ConfiguracionTaller;
import com.SPT.Model.ConfiguracionWhatsapp;
import com.SPT.Repository.ConfiguracionTallerRepository;
import com.SPT.Repository.ConfiguracionWhatsappRepository;
import com.SPT.Services.ConfiguracionWhatsappService;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionWhatsappServiceImpl implements ConfiguracionWhatsappService {

    private static final Byte ID_SINGLETON = 1;
    /** Constante cosmética — el estado de aprobación real vive en Twilio/Meta. */
    private static final String ESTADO_TEMPLATE_DEFAULT = "APROBADO";

    private final ConfiguracionWhatsappRepository whatsappRepo;
    private final ConfiguracionTallerRepository tallerRepo;

    public ConfiguracionWhatsappServiceImpl(ConfiguracionWhatsappRepository whatsappRepo,
                                            ConfiguracionTallerRepository tallerRepo) {
        this.whatsappRepo = whatsappRepo;
        this.tallerRepo = tallerRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionWhatsappResponse obtener() {
        ConfiguracionWhatsapp w = whatsappRepo.findById(ID_SINGLETON)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de WhatsApp no encontrada"));
        ConfiguracionTaller t = tallerRepo.findById(ID_SINGLETON).orElse(null);
        return toResponse(w, t);
    }

    @Override
    @Transactional
    public ConfiguracionWhatsappResponse actualizar(ConfiguracionWhatsappRequest request) {
        ConfiguracionWhatsapp w = whatsappRepo.findById(ID_SINGLETON)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de WhatsApp no encontrada"));

        // Proxy: si viene templateRecordatorioBody, persistir en configuracion_taller.
        if (request.getTemplateRecordatorioBody() != null) {
            ConfiguracionTaller t = tallerRepo.findById(ID_SINGLETON)
                    .orElseThrow(() -> new ResourceNotFoundException("Configuración del taller no encontrada"));
            if (!Objects.equals(t.getPlantillaWpRecordatorio(), request.getTemplateRecordatorioBody())) {
                t.setPlantillaWpRecordatorio(request.getTemplateRecordatorioBody());
                tallerRepo.save(t);
            }
        }

        // Campos propios de configuracion_whatsapp — partial update (null = no modificar).
        if (request.getRespuestaConfirmar() != null) w.setRespuestaConfirmar(request.getRespuestaConfirmar());
        if (request.getRespuestaCancelar() != null) w.setRespuestaCancelar(request.getRespuestaCancelar());
        if (request.getRespuestaBienvenida() != null) w.setRespuestaBienvenida(request.getRespuestaBienvenida());
        if (request.getRespuestaNoEntendido() != null) w.setRespuestaNoEntendido(request.getRespuestaNoEntendido());
        if (request.getOptOutHabilitado() != null) w.setOptOutHabilitado(request.getOptOutHabilitado());
        if (request.getOptOutPalabrasClave() != null) w.setOptOutPalabrasClave(request.getOptOutPalabrasClave());
        if (request.getRespuestaOptOut() != null) w.setRespuestaOptOut(request.getRespuestaOptOut());

        w.setModificadoPor("sistema");
        whatsappRepo.save(w);

        return obtener();
    }

    private ConfiguracionWhatsappResponse toResponse(ConfiguracionWhatsapp w, ConfiguracionTaller t) {
        ConfiguracionWhatsappResponse r = new ConfiguracionWhatsappResponse();
        r.setTemplateRecordatorioBody(t != null ? t.getPlantillaWpRecordatorio() : null);
        r.setTemplateRecordatorioEstado(ESTADO_TEMPLATE_DEFAULT);
        r.setRespuestaConfirmar(w.getRespuestaConfirmar());
        r.setRespuestaCancelar(w.getRespuestaCancelar());
        r.setRespuestaBienvenida(w.getRespuestaBienvenida());
        r.setRespuestaNoEntendido(w.getRespuestaNoEntendido());
        r.setOptOutHabilitado(w.isOptOutHabilitado());
        r.setOptOutPalabrasClave(w.getOptOutPalabrasClave());
        r.setRespuestaOptOut(w.getRespuestaOptOut());
        r.setFechaModificacion(w.getFechaModificacion());
        return r;
    }
}
