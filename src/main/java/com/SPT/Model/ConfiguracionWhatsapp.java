package com.SPT.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_whatsapp")
public class ConfiguracionWhatsapp {

    @Id
    @Column(name = "id", columnDefinition = "TINYINT UNSIGNED")
    private Byte id;  // siempre 1 (singleton)

    @Lob
    @Column(name = "respuesta_confirmar", nullable = false, columnDefinition = "TEXT")
    private String respuestaConfirmar;

    @Lob
    @Column(name = "respuesta_cancelar", nullable = false, columnDefinition = "TEXT")
    private String respuestaCancelar;

    @Lob
    @Column(name = "respuesta_bienvenida", nullable = false, columnDefinition = "TEXT")
    private String respuestaBienvenida;

    @Lob
    @Column(name = "respuesta_no_entendido", nullable = false, columnDefinition = "TEXT")
    private String respuestaNoEntendido;

    @Column(name = "opt_out_habilitado", nullable = false)
    private boolean optOutHabilitado;

    @Column(name = "opt_out_palabras_clave", nullable = false, length = 255)
    private String optOutPalabrasClave;

    @Lob
    @Column(name = "respuesta_opt_out", nullable = false, columnDefinition = "TEXT")
    private String respuestaOptOut;

    @Column(name = "modificado_por", length = 50)
    private String modificadoPor;

    @Column(name = "fecha_modificacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    public ConfiguracionWhatsapp() {}

    public Byte getId() { return id; }
    public void setId(Byte id) { this.id = id; }

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

    public String getModificadoPor() { return modificadoPor; }
    public void setModificadoPor(String modificadoPor) { this.modificadoPor = modificadoPor; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
