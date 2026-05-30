package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vista_saldo_ot")
public class VistaSaldoOt {

    @Id
    @Column(name = "id_ot")
    private Long idOt;

    @Column(name = "numero_ot")
    private String numeroOt;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_vehiculo", nullable = false)
    private Long idVehiculo;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "total_ot", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalOt;

    @Column(name = "total_pagado", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalPagado;

    @Column(name = "saldo_pendiente", precision = 14, scale = 2, nullable = false)
    private BigDecimal saldoPendiente;

    public Long getIdOt() { return idOt; }
    public String getNumeroOt() { return numeroOt; }
    public Long getIdCliente() { return idCliente; }
    public Long getIdVehiculo() { return idVehiculo; }
    public String getEstado() { return estado; }
    public BigDecimal getTotalOt() { return totalOt; }
    public BigDecimal getTotalPagado() { return totalPagado; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
}
