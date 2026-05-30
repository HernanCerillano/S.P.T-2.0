package com.SPT.Repository;

import com.SPT.Model.VistaGananciasSemanales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VistaGananciasSemanalesRepository extends JpaRepository<VistaGananciasSemanales, Integer> {
}
