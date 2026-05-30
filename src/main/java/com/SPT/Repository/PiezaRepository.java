package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.Pieza;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PiezaRepository extends JpaRepository<Pieza, Long> {
    List<Pieza> findByActivo(boolean activo);
    List<Pieza> findByNombreContainingIgnoreCase(String nombre);
    List<Pieza> findByMarcaContainingIgnoreCaseAndNombreContainingIgnoreCaseAndMedidasContainingIgnoreCaseAndPrecioUnitarioBetweenAndCalidadContainingIgnoreCase(
            String marca,
            String nombre,
            String medidas,
            BigDecimal precioMin,
            BigDecimal precioMax,
            String calidad
    );
}
