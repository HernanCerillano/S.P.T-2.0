package com.SPT.Services;

import com.SPT.Dtos.Request.PiezaRequest;
import com.SPT.Dtos.Response.PiezaResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PiezaService {
    PiezaResponse crear(PiezaRequest request);
    PiezaResponse actualizar(Long idPieza, PiezaRequest request);
    PiezaResponse obtenerPorId(Long idPieza);
    List<PiezaResponse> listarTodos();
    List<PiezaResponse> listarActivos(boolean activo);
    List<PiezaResponse> buscarPorNombre(String nombre);
    List<PiezaResponse> filtrar(String marca, String nombre, String medidas, BigDecimal precioMin, BigDecimal precioMax, String calidad);
    void eliminar(Long idPieza);
    void cambiarEstado(Long idPieza, boolean activo);
}
