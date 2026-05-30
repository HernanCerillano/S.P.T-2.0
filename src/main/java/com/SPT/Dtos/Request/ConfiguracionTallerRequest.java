package com.SPT.Dtos.Request;

import jakarta.validation.constraints.Min;

public class ConfiguracionTallerRequest {

    private String nombreTaller;
    private String telefono;
    private String direccion;
    private String email;
    private String cuit;
    private String logoPath;
    private String plantillaWpRecordatorio;

    @Min(0)
    private int horasAnticipacionWp;

    private boolean wpHabilitado;

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
}
