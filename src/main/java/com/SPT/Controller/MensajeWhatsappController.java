package com.SPT.Controller;

import com.SPT.Dtos.Response.MensajeWhatsappResponse;
import com.SPT.Model.MensajeWhatsapp;
import com.SPT.Repository.MensajeWhatsappRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MensajeWhatsappController {

    private final MensajeWhatsappRepository repository;

    public MensajeWhatsappController(MensajeWhatsappRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/mensajes-whatsapp")
    public ResponseEntity<List<MensajeWhatsappResponse>> listar() {
        List<MensajeWhatsappResponse> result = repository
                .findAll(Sort.by(Sort.Direction.DESC, "idMensaje"))
                .stream()
                .limit(50)
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private MensajeWhatsappResponse toResponse(MensajeWhatsapp m) {
        MensajeWhatsappResponse r = new MensajeWhatsappResponse();
        r.setIdMensaje(m.getIdMensaje());
        r.setIdCita(m.getCita() != null ? m.getCita().getIdCita() : null);
        r.setTelefonoDestino(m.getTelefonoDestino());
        r.setContenido(m.getContenido());
        r.setEstado(m.getEstado());
        r.setIntentos(m.getIntentos());
        r.setFechaProgramada(m.getFechaProgramada());
        r.setFechaEnvio(m.getFechaEnvio());
        r.setErrorMensaje(m.getErrorMensaje());
        r.setFechaCreacion(m.getFechaCreacion());
        return r;
    }
}
