package com.SPT.Mapper;

import com.SPT.Dtos.Response.PagoEmpleadoResponse;
import com.SPT.Model.Empleado;
import com.SPT.Model.PagoEmpleado;
import org.springframework.stereotype.Component;

@Component
public class PagoEmpleadoMapper {

    public PagoEmpleadoResponse toResponse(PagoEmpleado entity) {
        if (entity == null) return null;
        PagoEmpleadoResponse r = new PagoEmpleadoResponse();
        r.setIdPagoEmpleado(entity.getIdPagoEmpleado());
        Empleado emp = entity.getEmpleado();
        if (emp != null) {
            r.setIdEmpleado(emp.getIdEmpleado());
            r.setNombreEmpleado(emp.getNombre() + " " + emp.getApellido());
        }
        r.setMonto(entity.getMonto());
        r.setPeriodoDesde(entity.getPeriodoDesde());
        r.setPeriodoHasta(entity.getPeriodoHasta());
        r.setFechaPago(entity.getFechaPago());
        r.setMetodoPago(entity.getMetodoPago());
        r.setObservaciones(entity.getObservaciones());
        r.setFechaCreacion(entity.getFechaCreacion());
        return r;
    }
}
