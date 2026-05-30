package com.SPT.Services;

import com.SPT.Dtos.Request.PresupuestoDetalleRequest;
import com.SPT.Dtos.Request.PresupuestoRequest;
import com.SPT.Dtos.Response.PresupuestoDetalleResponse;
import com.SPT.Dtos.Response.PresupuestoResponse;
import com.SPT.Model.EstadoPresupuesto;
import java.time.LocalDateTime;
import java.util.List;

public interface PresupuestoService {
    PresupuestoResponse crear(PresupuestoRequest request);
    PresupuestoResponse actualizar(Long idPresupuesto, PresupuestoRequest request);
    PresupuestoResponse obtenerPorId(Long idPresupuesto);
    List<PresupuestoResponse> listarTodos();
    List<PresupuestoResponse> filtrar(String numero, Long idCliente, Long idVehiculo, LocalDateTime desde,
                                      LocalDateTime hasta, EstadoPresupuesto estado, String patente);
    PresupuestoResponse archivar(Long idPresupuesto);
    PresupuestoResponse desarchivar(Long idPresupuesto);
    void eliminar(Long idPresupuesto);

    PresupuestoDetalleResponse agregarDetalle(Long idPresupuesto, PresupuestoDetalleRequest request);
    PresupuestoDetalleResponse actualizarDetalle(Long idDetallePresupuesto, PresupuestoDetalleRequest request);
    void eliminarDetalle(Long idDetallePresupuesto);
    List<PresupuestoDetalleResponse> listarDetalles(Long idPresupuesto);

    void recalcularTotal(Long idPresupuesto);
}
