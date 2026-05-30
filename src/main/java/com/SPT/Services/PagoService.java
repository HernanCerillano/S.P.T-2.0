package com.SPT.Services;

import com.SPT.Dtos.Request.PagoRequest;
import com.SPT.Dtos.Response.PagoResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {
    PagoResponse registrarPago(PagoRequest request);
    List<PagoResponse> listarTodos();
    List<PagoResponse> filtrar(Long idCliente, Long idVehiculo, LocalDateTime desde, LocalDateTime hasta, String patente);
    void eliminar(Long idPago);
    BigDecimal totalPagadoPorOt(Long idOt);
    BigDecimal saldoPendientePorOt(Long idOt);
}
