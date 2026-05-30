package com.SPT.Dtos.Request;

/**
 * PUT /api/configuracion/whatsapp — soporta updates parciales: cualquier campo {@code null}
 * se interpreta como "no modificar". {@code templateRecordatorioBody}, si viene, se persiste
 * en {@code configuracion_taller.plantilla_wp_recordatorio} (proxy, no se duplica).
 */
public class ConfiguracionWhatsappRequest {

    // Proxy: si viene no-null, escribe en configuracion_taller.plantilla_wp_recordatorio.
    private String templateRecordatorioBody;

    private String respuestaConfirmar;
    private String respuestaCancelar;
    private String respuestaBienvenida;
    private String respuestaNoEntendido;
    private Boolean optOutHabilitado;
    private String optOutPalabrasClave;
    private String respuestaOptOut;

    public String getTemplateRecordatorioBody() { return templateRecordatorioBody; }
    public void setTemplateRecordatorioBody(String templateRecordatorioBody) { this.templateRecordatorioBody = templateRecordatorioBody; }

    public String getRespuestaConfirmar() { return respuestaConfirmar; }
    public void setRespuestaConfirmar(String respuestaConfirmar) { this.respuestaConfirmar = respuestaConfirmar; }

    public String getRespuestaCancelar() { return respuestaCancelar; }
    public void setRespuestaCancelar(String respuestaCancelar) { this.respuestaCancelar = respuestaCancelar; }

    public String getRespuestaBienvenida() { return respuestaBienvenida; }
    public void setRespuestaBienvenida(String respuestaBienvenida) { this.respuestaBienvenida = respuestaBienvenida; }

    public String getRespuestaNoEntendido() { return respuestaNoEntendido; }
    public void setRespuestaNoEntendido(String respuestaNoEntendido) { this.respuestaNoEntendido = respuestaNoEntendido; }

    public Boolean getOptOutHabilitado() { return optOutHabilitado; }
    public void setOptOutHabilitado(Boolean optOutHabilitado) { this.optOutHabilitado = optOutHabilitado; }

    public String getOptOutPalabrasClave() { return optOutPalabrasClave; }
    public void setOptOutPalabrasClave(String optOutPalabrasClave) { this.optOutPalabrasClave = optOutPalabrasClave; }

    public String getRespuestaOptOut() { return respuestaOptOut; }
    public void setRespuestaOptOut(String respuestaOptOut) { this.respuestaOptOut = respuestaOptOut; }
}
