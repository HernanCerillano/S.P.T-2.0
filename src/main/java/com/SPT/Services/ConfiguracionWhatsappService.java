package com.SPT.Services;

import com.SPT.Dtos.Request.ConfiguracionWhatsappRequest;
import com.SPT.Dtos.Response.ConfiguracionWhatsappResponse;

public interface ConfiguracionWhatsappService {
    ConfiguracionWhatsappResponse obtener();
    ConfiguracionWhatsappResponse actualizar(ConfiguracionWhatsappRequest request);
}
