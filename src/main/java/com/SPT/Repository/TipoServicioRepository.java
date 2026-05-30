package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SPT.Model.TipoServicio;
import java.util.List;

@Repository
public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
    List<TipoServicio> findByActivo(boolean activo);
    List<TipoServicio> findByNombreContainingIgnoreCase(String nombre);
}
