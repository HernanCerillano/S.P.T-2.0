package com.SPT.Repository;

import com.SPT.Model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByFechaInicioBetween(LocalDateTime desde, LocalDateTime hasta);
    List<Evento> findByFechaInicioGreaterThanEqualAndFechaInicioLessThanEqual(LocalDateTime desde, LocalDateTime hasta);
}
