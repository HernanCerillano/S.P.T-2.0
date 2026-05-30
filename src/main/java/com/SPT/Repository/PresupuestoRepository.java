package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.EstadoPresupuesto;
import com.SPT.Model.Presupuesto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {
    Optional<Presupuesto> findTopByOrderByIdPresupuestoDesc();
    Optional<Presupuesto> findByNumeroPresupuesto(String numeroPresupuesto);
    List<Presupuesto> findByEstado(EstadoPresupuesto estado);
    List<Presupuesto> findByCliente_IdCliente(Long idCliente);
    List<Presupuesto> findByVehiculo_IdVehiculo(Long idVehiculo);
    List<Presupuesto> findByFechaCreacionBetween(LocalDateTime desde, LocalDateTime hasta);
}
