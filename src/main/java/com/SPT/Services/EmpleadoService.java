package com.SPT.Services;

import com.SPT.Dtos.Request.EmpleadoRequest;
import com.SPT.Dtos.Response.EmpleadoResponse;
import java.util.List;

public interface EmpleadoService {
    EmpleadoResponse crear(EmpleadoRequest request);
    EmpleadoResponse actualizar(Long idEmpleado, EmpleadoRequest request);
    EmpleadoResponse obtenerPorId(Long idEmpleado);
    List<EmpleadoResponse> listarTodos();
    List<EmpleadoResponse> listarActivos(boolean activo);
    void cambiarEstado(Long idEmpleado, boolean activo);
    void eliminar(Long idEmpleado);
}
