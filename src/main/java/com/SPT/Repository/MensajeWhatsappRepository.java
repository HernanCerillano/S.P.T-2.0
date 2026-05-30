package com.SPT.Repository;

import com.SPT.Model.EstadoMensaje;
import com.SPT.Model.MensajeWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensajeWhatsappRepository extends JpaRepository<MensajeWhatsapp, Long> {
    List<MensajeWhatsapp> findByEstadoAndFechaProgramadaBefore(EstadoMensaje estado, LocalDateTime momento);
    List<MensajeWhatsapp> findByCita_IdCita(Long idCita);
}
