package com.SPT.Services;

import com.SPT.Dtos.Request.TipoServicioRequest;
import com.SPT.Dtos.Response.TipoServicioResponse;
import java.util.List;

public interface TipoServicioService {
    TipoServicioResponse crear(TipoServicioRequest request);
    TipoServicioResponse actualizar(Long idServicio, TipoServicioRequest request);
    TipoServicioResponse obtenerPorId(Long idServicio);
    List<TipoServicioResponse> listarTodos();
    List<TipoServicioResponse> listarActivos(boolean activo);
    List<TipoServicioResponse> buscarPorNombre(String nombre);
    void eliminar(Long idServicio);
    void cambiarEstado(Long idServicio, boolean activo);
}
