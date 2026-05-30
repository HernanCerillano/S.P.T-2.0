package com.SPT.Services;

import com.SPT.Dtos.Request.ClienteRequest;
import com.SPT.Dtos.Response.ClienteResponse;
import java.util.List;

public interface ClienteService {
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Long idCliente, ClienteRequest request);
    ClienteResponse obtenerPorId(Long idCliente);
    List<ClienteResponse> listarTodos();
    List<ClienteResponse> listarActivos(boolean activo);
    /** Autocomplete: matchea LIKE %q% en nombre o apellido, hasta 10 resultados. */
    List<ClienteResponse> buscar(String q);
    void eliminar(Long idCliente);
    void cambiarEstado(Long idCliente, boolean activo);
}
