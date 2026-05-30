package com.SPT.Repository;

import com.SPT.Model.VistaMovimientoCaja;
import com.SPT.Model.VistaMovimientoCajaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VistaMovimientoCajaRepository extends JpaRepository<VistaMovimientoCaja, VistaMovimientoCajaId> {
}
