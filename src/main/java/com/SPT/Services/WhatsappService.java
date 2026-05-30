package com.SPT.Services;

import com.SPT.Model.Cita;

public interface WhatsappService {
    void encolarRecordatorioCita(Cita cita);
    void sincronizarPorCita(Cita cita);
    void cancelarPorCita(Long idCita);
    void procesarPendientes();
}
