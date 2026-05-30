package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vista_saldo_cliente")
public class VistaSaldoCliente {

    @Id
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido")
    private String apellido;

    @Column(name = "total_facturado", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalFacturado;

    @Column(name = "total_pagado", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalPagado;

    @Column(name = "saldo_pendiente", precision = 14, scale = 2, nullable = false)
    private BigDecimal saldoPendiente;

    public Long getIdCliente() { return idCliente; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public BigDecimal getTotalFacturado() { return totalFacturado; }
    public BigDecimal getTotalPagado() { return totalPagado; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
}
