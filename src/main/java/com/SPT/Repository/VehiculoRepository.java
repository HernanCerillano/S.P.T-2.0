package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.Vehiculo;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByCliente_IdCliente(Long idCliente);
    Optional<Vehiculo> findByPatente(String patente);
}
