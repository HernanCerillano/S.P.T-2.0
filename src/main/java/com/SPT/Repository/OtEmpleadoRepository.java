package com.SPT.Repository;

import com.SPT.Model.OtEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtEmpleadoRepository extends JpaRepository<OtEmpleado, Long> {
    List<OtEmpleado> findByOrdenTrabajo_IdOt(Long idOt);
    Optional<OtEmpleado> findByOrdenTrabajo_IdOtAndEmpleado_IdEmpleado(Long idOt, Long idEmpleado);
}
