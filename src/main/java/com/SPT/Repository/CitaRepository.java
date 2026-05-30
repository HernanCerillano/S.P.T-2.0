package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.Cita;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    boolean existsByFechaHora(LocalDateTime fechaHora);
    boolean existsByFechaHoraAndIdCitaNot(LocalDateTime fechaHora, Long idCita);
    List<Cita> findByOrderByFechaHoraAsc();
    List<Cita> findByCliente_IdCliente(Long idCliente);
    List<Cita> findByVehiculo_IdVehiculo(Long idVehiculo);
    List<Cita> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
}
