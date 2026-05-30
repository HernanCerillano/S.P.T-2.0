package com.SPT.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.SPT.Model.Cliente;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByActivo(boolean activo);

    /**
     * Búsqueda best-effort por número de WhatsApp. El campo {@code whatsapp} es texto
     * libre (puede tener prefijos/espacios/guiones distintos al inbound de Twilio),
     * por eso se matchea por un fragmento (típicamente los últimos dígitos).
     */
    Optional<Cliente> findFirstByWhatsappContaining(String fragmento);

    /** Autocomplete: matchea LIKE %q% en nombre o apellido, solo activos. */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND ("
            + "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "ORDER BY c.apellido, c.nombre")
    List<Cliente> buscarPorNombreOApellido(@Param("q") String q, Pageable pageable);
}
