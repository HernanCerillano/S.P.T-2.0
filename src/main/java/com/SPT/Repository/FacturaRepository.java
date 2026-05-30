package com.SPT.Repository;

import com.SPT.Model.EstadoFactura;
import com.SPT.Model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByCliente_IdCliente(Long idCliente);
    List<Factura> findByEstado(EstadoFactura estado);
    List<Factura> findByCliente_IdClienteAndEstado(Long idCliente, EstadoFactura estado);
}
