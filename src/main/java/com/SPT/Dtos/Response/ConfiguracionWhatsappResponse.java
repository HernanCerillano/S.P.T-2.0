package com.SPT.Dtos.Response;

import java.time.LocalDateTime;

public class ConfiguracionWhatsappResponse {

    // Proxies a configuracion_taller — fuente de verdad real del body del recordatorio.
    // El envío real lo controla Twilio (ContentSid aprobado por Meta).
    private String templateRecordatorioBody;
    private String templateRecordatorioEstado;  // siempre "APROBADO" (cosmético)

    private String respuestaConfirmar;
    private String respuestaCancelar;
    private String respuestaBienvenida;
    private String respuestaNoEntendido;
    private boolean optOutHabilitado;
    private String optOutPalabrasClave;
    private String respuestaOptOut;
    private LocalDateTime fechaModificacion;

    public String getTemplateRecordatorioBody() { return templateRecordatorioBody; }
    public void setTemplateRecordatorioBody(String templateRecordatorioBody) { this.templateRecordatorioBody = templateRecordatorioBody; }

    public String getTemplateRecordatorioEstado() { return templateRecordatorioEstado; }
    public void setTemplateRecordatorioEstado(String templateRecordatorioEstado) { this.templateRecordatorioEstado = templateRecordatorioEstado; }

    public String getRespuestaConfirmar() { return respuestaConfirmar; }
    public void setRespuestaConfirmar(String respuestaConfirmar) { this.respuestaConfirmar = respuestaConfirmar; }

    public String getRespuestaCancelar() { return respuestaCancelar; }
    public void setRespuestaCancelar(String respuestaCancelar) { this.respuestaCancelar = respuestaCancelar; }

    public String getRespuestaBienvenida() { return respuestaBienvenida; }
    public void setRespuestaBienvenida(String respuestaBienvenida) { this.respuestaBienvenida = respuestaBienvenida; }

    public String getRespuestaNoEntendido() { return respuestaNoEntendido; }
    public void setRespuestaNoEntendido(String respuestaNoEntendido) { this.respuestaNoEntendido = respuestaNoEntendido; }

    public boolean isOptOutHabilitado() { return optOutHabilitado; }
    public void setOptOutHabilitado(boolean optOutHabilitado) { this.optOutHabilitado = optOutHabilitado; }

    public String getOptOutPalabrasClave() { return optOutPalabrasClave; }
    public void setOptOutPalabrasClave(String optOutPalabrasClave) { this.optOutPalabrasClave = optOutPalabrasClave; }

    public String getRespuestaOptOut() { return respuestaOptOut; }
    public void setRespuestaOptOut(String respuestaOptOut) { this.respuestaOptOut = respuestaOptOut; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
