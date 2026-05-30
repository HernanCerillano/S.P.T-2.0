package com.SPT.Repository;

import com.SPT.Model.PagoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagoEmpleadoRepository extends JpaRepository<PagoEmpleado, Long> {
    List<PagoEmpleado> findByEmpleado_IdEmpleado(Long idEmpleado);
}
