package com.SPT.Mapper;

import com.SPT.Dtos.Response.FacturaResponse;
import com.SPT.Model.Cliente;
import com.SPT.Model.Factura;
import org.springframework.stereotype.Component;

@Component
public class FacturaMapper {

    public FacturaResponse toResponse(Factura entity) {
        if (entity == null) return null;
        FacturaResponse r = new FacturaResponse();
        r.setIdFactura(entity.getIdFactura());
        r.setNumeroFactura(entity.getNumeroFactura());
        r.setTipoFactura(entity.getTipoFactura());
        Cliente c = entity.getCliente();
        if (c != null) {
            r.setIdCliente(c.getIdCliente());
            r.setNombreCliente(c.getNombre() + " " + c.getApellido());
        }
        if (entity.getPresupuesto() != null) r.setIdPresupuesto(entity.getPresupuesto().getIdPresupuesto());
        if (entity.getOrdenTrabajo() != null) r.setIdOt(entity.getOrdenTrabajo().getIdOt());
        r.setSubtotal(entity.getSubtotal());
        r.setImpuestos(entity.getImpuestos());
        r.setTotal(entity.getTotal());
        r.setEstado(entity.getEstado());
        r.setFechaEmision(entity.getFechaEmision());
        r.setObservaciones(entity.getObservaciones());
        r.setFechaCreacion(entity.getFechaCreacion());
        return r;
    }
}
