package com.SPT.Services;

import com.SPT.Dtos.Request.OTDetalleRequest;
import com.SPT.Dtos.Request.OrdenTrabajoRequest;
import com.SPT.Dtos.Response.OTDetalleResponse;
import com.SPT.Dtos.Response.OrdenTrabajoResponse;
import com.SPT.Model.EstadoOT;
import java.time.LocalDateTime;
import java.util.List;

public interface OrdenTrabajoService {
    OrdenTrabajoResponse crear(OrdenTrabajoRequest request);
    OrdenTrabajoResponse crearDesdePresupuesto(Long idPresupuesto);
    OrdenTrabajoResponse actualizar(Long idOt, OrdenTrabajoRequest request);
    OrdenTrabajoResponse cambiarEstado(Long idOt, EstadoOT estado);
    OrdenTrabajoResponse obtenerPorId(Long idOt);
    List<OrdenTrabajoResponse> listarTodos();
    List<OrdenTrabajoResponse> filtrar(String numeroOt, Long idCliente, Long idVehiculo, LocalDateTime desde,
                                       LocalDateTime hasta, EstadoOT estado, String patente);
    void eliminar(Long idOt);

    OTDetalleResponse agregarDetalle(Long idOt, OTDetalleRequest request);
    OTDetalleResponse actualizarDetalle(Long idDetalleOt, OTDetalleRequest request);
    void eliminarDetalle(Long idDetalleOt);
    List<OTDetalleResponse> listarDetalles(Long idOt);

    void recalcularTotal(Long idOt);

    /** Próximo número de OT formateado como "OT-0001". */
    String proximoNumero();
}
