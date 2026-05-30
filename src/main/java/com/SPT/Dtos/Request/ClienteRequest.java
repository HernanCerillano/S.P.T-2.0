package com.SPT.Dtos.Request;

import jakarta.validation.constraints.Size;

public class ClienteRequest {

    private String nombre;
    private String apellido;

    @Size(max = 255)
    private String telefono;

    @Size(max = 255)
    private String whatsapp;

    @Size(max = 255)
    private String email;

    private String direccion;
    private boolean activo;

    public ClienteRequest() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
