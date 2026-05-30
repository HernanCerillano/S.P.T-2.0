package com.SPT.Repository;

import com.SPT.Model.VistaGananciasMensuales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VistaGananciasMensualesRepository extends JpaRepository<VistaGananciasMensuales, String> {
}
