package com.SPT.Services;

import com.SPT.Dtos.Request.FacturaDesdeOrigenRequest;
import com.SPT.Dtos.Response.FacturaResponse;
import com.SPT.Model.EstadoFactura;
import java.util.List;

public interface FacturaService {
    FacturaResponse crearDesdePresupuesto(Long idPresupuesto, FacturaDesdeOrigenRequest request);
    FacturaResponse crearDesdeOt(Long idOt, FacturaDesdeOrigenRequest request);
    FacturaResponse obtenerPorId(Long id);
    List<FacturaResponse> listar(Long idCliente, EstadoFactura estado);
    FacturaResponse anular(Long id);

    /** Próximo número de factura formateado como "%05d" (ej: "00001", "00002"). */
    String proximoNumero();
}
