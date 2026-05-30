package com.SPT.Services;

import com.SPT.Dtos.Request.UsuarioRequest;
import com.SPT.Dtos.Response.UsuarioResponse;
import java.util.List;

public interface UsuarioService {
    UsuarioResponse crear(UsuarioRequest request);
    UsuarioResponse actualizar(Long idUsuario, UsuarioRequest request);
    UsuarioResponse obtenerPorId(Long idUsuario);
    List<UsuarioResponse> listarTodos();
    void cambiarEstado(Long idUsuario, boolean activo);
}
