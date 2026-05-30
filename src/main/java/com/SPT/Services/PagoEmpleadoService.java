package com.SPT.Services;

import com.SPT.Dtos.Request.PagoEmpleadoRequest;
import com.SPT.Dtos.Response.PagoEmpleadoResponse;
import java.util.List;

public interface PagoEmpleadoService {
    PagoEmpleadoResponse registrar(PagoEmpleadoRequest request);
    PagoEmpleadoResponse obtenerPorId(Long id);
    List<PagoEmpleadoResponse> listarTodos();
    List<PagoEmpleadoResponse> listarPorEmpleado(Long idEmpleado);
    void eliminar(Long id);
}
