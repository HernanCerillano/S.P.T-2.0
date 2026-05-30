package com.SPT.Services;

import com.SPT.Dtos.Request.VehiculoRequest;
import com.SPT.Dtos.Response.VehiculoResponse;
import java.util.List;

public interface VehiculoService {
    VehiculoResponse crear(VehiculoRequest request);
    VehiculoResponse actualizar(Long idVehiculo, VehiculoRequest request);
    VehiculoResponse obtenerPorId(Long idVehiculo);
    List<VehiculoResponse> listarTodos();
    List<VehiculoResponse> listarPorCliente(Long idCliente);
    void eliminar(Long idVehiculo);
    void cambiarEstado(Long idVehiculo, boolean activo);
}
