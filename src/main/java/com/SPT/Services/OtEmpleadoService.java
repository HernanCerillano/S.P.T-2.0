package com.SPT.Services;

import com.SPT.Dtos.Request.OtEmpleadoRequest;
import com.SPT.Dtos.Response.OtEmpleadoResponse;
import java.util.List;

public interface OtEmpleadoService {
    OtEmpleadoResponse asignar(Long idOt, OtEmpleadoRequest request);
    OtEmpleadoResponse actualizar(Long idOt, Long idOtEmpleado, OtEmpleadoRequest request);
    List<OtEmpleadoResponse> listarPorOt(Long idOt);
    void eliminar(Long idOt, Long idOtEmpleado);
}
