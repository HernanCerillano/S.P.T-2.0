package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.OTDetalle;
import java.util.List;

@Repository
public interface OTDetalleRepository extends JpaRepository<OTDetalle, Long> {
    boolean existsByPieza_IdPieza(Long idPieza);
    boolean existsByTipoServicio_IdServicio(Long idServicio);
    List<OTDetalle> findByOrdenTrabajo_IdOt(Long idOt);
    List<OTDetalle> findByPieza_IdPieza(Long idPieza);
    List<OTDetalle> findByTipoServicio_IdServicio(Long idServicio);
}
