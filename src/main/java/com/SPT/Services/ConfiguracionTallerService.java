package com.SPT.Services;

import com.SPT.Dtos.Request.ConfiguracionTallerRequest;
import com.SPT.Dtos.Response.ConfiguracionTallerResponse;

public interface ConfiguracionTallerService {
    ConfiguracionTallerResponse obtener();
    ConfiguracionTallerResponse actualizar(ConfiguracionTallerRequest request);
}
