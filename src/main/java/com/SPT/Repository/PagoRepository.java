package com.SPT.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.SPT.Model.Pago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByOrdenTrabajo_IdOt(Long idOt);
    List<Pago> findByFechaPagoBetween(LocalDateTime desde, LocalDateTime hasta);
    List<Pago> findByOrdenTrabajo_Cliente_IdCliente(Long idCliente);
    List<Pago> findByOrdenTrabajo_Vehiculo_IdVehiculo(Long idVehiculo);

    @Query("select coalesce(sum(p.monto), 0) from Pago p where p.ordenTrabajo.idOt = :idOt")
    BigDecimal sumMontoByOrdenTrabajo(@Param("idOt") Long idOt);
}
