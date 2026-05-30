package com.SPT.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_taller")
public class ConfiguracionTaller {

    @Id
    @Column(name = "id_configuracion")
    private Byte idConfiguracion;

    @Column(name = "nombre_taller")
    private String nombreTaller;

    @Column(name = "telefono", length = 64)
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "email")
    private String email;

    @Column(name = "cuit", length = 32)
    private String cuit;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Lob
    @Column(name = "plantilla_wp_recordatorio", columnDefinition = "TEXT")
    private String plantillaWpRecordatorio;

    @Column(name = "horas_anticipacion_wp", nullable = false)
    private int horasAnticipacionWp;

    @Column(name = "wp_habilitado", nullable = false)
    private boolean wpHabilitado;

    @Column(name = "fecha_modificacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    public ConfiguracionTaller() {}

    public Byte getIdConfiguracion() { return idConfiguracion; }
    public void setIdConfiguracion(Byte idConfiguracion) { this.idConfiguracion = idConfiguracion; }

    public String getNombreTaller() { return nombreTaller; }
    public void setNombreTaller(String nombreTaller) { this.nombreTaller = nombreTaller; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getPlantillaWpRecordatorio() { return plantillaWpRecordatorio; }
    public void setPlantillaWpRecordatorio(String plantillaWpRecordatorio) { this.plantillaWpRecordatorio = plantillaWpRecordatorio; }

    public int getHorasAnticipacionWp() { return horasAnticipacionWp; }
    public void setHorasAnticipacionWp(int horasAnticipacionWp) { this.horasAnticipacionWp = horasAnticipacionWp; }

    public boolean isWpHabilitado() { return wpHabilitado; }
    public void setWpHabilitado(boolean wpHabilitado) { this.wpHabilitado = wpHabilitado; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
