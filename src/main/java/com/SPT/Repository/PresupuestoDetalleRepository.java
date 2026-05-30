package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.PresupuestoDetalle;
import java.util.List;

@Repository
public interface PresupuestoDetalleRepository extends JpaRepository<PresupuestoDetalle, Long> {
    boolean existsByPieza_IdPieza(Long idPieza);
    boolean existsByTipoServicio_IdServicio(Long idServicio);
    List<PresupuestoDetalle> findByPresupuesto_IdPresupuesto(Long idPresupuesto);
    List<PresupuestoDetalle> findByPieza_IdPieza(Long idPieza);
    List<PresupuestoDetalle> findByTipoServicio_IdServicio(Long idServicio);
}
