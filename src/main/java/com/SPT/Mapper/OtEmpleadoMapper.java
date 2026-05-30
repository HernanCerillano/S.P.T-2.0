package com.SPT.Mapper;

import com.SPT.Dtos.Response.OtEmpleadoResponse;
import com.SPT.Model.Empleado;
import com.SPT.Model.OrdenTrabajo;
import com.SPT.Model.OtEmpleado;
import org.springframework.stereotype.Component;

@Component
public class OtEmpleadoMapper {

    public OtEmpleadoResponse toResponse(OtEmpleado entity) {
        if (entity == null) return null;
        OtEmpleadoResponse r = new OtEmpleadoResponse();
        r.setIdOtEmpleado(entity.getIdOtEmpleado());
        OrdenTrabajo ot = entity.getOrdenTrabajo();
        if (ot != null) r.setIdOt(ot.getIdOt());
        Empleado emp = entity.getEmpleado();
        if (emp != null) {
            r.setIdEmpleado(emp.getIdEmpleado());
            r.setNombreEmpleado(emp.getNombre() + " " + emp.getApellido());
        }
        r.setCostoManoObra(entity.getCostoManoObra());
        r.setHorasTrabajadas(entity.getHorasTrabajadas());
        r.setObservaciones(entity.getObservaciones());
        r.setFechaCreacion(entity.getFechaCreacion());
        return r;
    }
}
