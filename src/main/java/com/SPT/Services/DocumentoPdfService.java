package com.SPT.Services;

public interface DocumentoPdfService {

    byte[] generarPdfPresupuesto(Long idPresupuesto);

    byte[] generarPdfOrdenTrabajo(Long idOt);
}
