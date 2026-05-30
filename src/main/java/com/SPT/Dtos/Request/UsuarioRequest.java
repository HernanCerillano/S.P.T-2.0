package com.SPT.Dtos.Request;

import com.SPT.Model.RolUsuario;

public class UsuarioRequest {

    private String username;
    private String passwordHash;
    private RolUsuario rol;
    private boolean activo;

    public UsuarioRequest() {
    }

    public UsuarioRequest(String username, String passwordHash, RolUsuario rol, boolean activo) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
