package com.SPT.Services;

import com.SPT.Dtos.Request.CitaRequest;
import com.SPT.Dtos.Response.CitaResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface CitaService {
    CitaResponse crear(CitaRequest request);
    CitaResponse actualizar(Long idCita, CitaRequest request);
    CitaResponse obtenerPorId(Long idCita);
    List<CitaResponse> listarTodasOrdenadas();
    List<CitaResponse> filtrar(Long idCliente, Long idVehiculo, LocalDateTime desde, LocalDateTime hasta, String patente);
    void eliminar(Long idCita);
}
