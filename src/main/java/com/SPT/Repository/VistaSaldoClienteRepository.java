package com.SPT.Repository;

import com.SPT.Model.VistaSaldoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VistaSaldoClienteRepository extends JpaRepository<VistaSaldoCliente, Long> {
    Optional<VistaSaldoCliente> findByIdCliente(Long idCliente);
}
