package com.SPT.Repository;

import com.SPT.Model.VistaGananciasDiarias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VistaGananciasDiariasRepository extends JpaRepository<VistaGananciasDiarias, LocalDate> {
    List<VistaGananciasDiarias> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
