package com.SPT.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class VistaMovimientoCajaId implements Serializable {

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "concepto", nullable = false)
    private String concepto;

    public VistaMovimientoCajaId() {}

    public VistaMovimientoCajaId(LocalDateTime fecha, String concepto) {
        this.fecha = fecha;
        this.concepto = concepto;
    }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VistaMovimientoCajaId)) return false;
        VistaMovimientoCajaId that = (VistaMovimientoCajaId) o;
        return Objects.equals(fecha, that.fecha) && Objects.equals(concepto, that.concepto);
    }

    @Override
    public int hashCode() { return Objects.hash(fecha, concepto); }
}
