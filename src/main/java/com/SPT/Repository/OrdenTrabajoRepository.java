package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.EstadoOT;
import com.SPT.Model.OrdenTrabajo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {
    Optional<OrdenTrabajo> findTopByOrderByIdOtDesc();
    Optional<OrdenTrabajo> findByNumeroOt(String numeroOt);
    Optional<OrdenTrabajo> findByPresupuesto_IdPresupuesto(Long idPresupuesto);
    List<OrdenTrabajo> findByEstado(EstadoOT estado);
    List<OrdenTrabajo> findByCliente_IdCliente(Long idCliente);
    List<OrdenTrabajo> findByVehiculo_IdVehiculo(Long idVehiculo);
    List<OrdenTrabajo> findByFechaCreacionBetween(LocalDateTime desde, LocalDateTime hasta);
}
