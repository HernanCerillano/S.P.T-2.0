package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vista_movimiento_caja")
public class VistaMovimientoCaja {

    @EmbeddedId
    private VistaMovimientoCajaId id;

    @Column(name = "monto", precision = 14, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    public VistaMovimientoCajaId getId() { return id; }
    public BigDecimal getMonto() { return monto; }
    public String getTipo() { return tipo; }
}
