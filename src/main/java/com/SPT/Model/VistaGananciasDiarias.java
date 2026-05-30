package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "vista_ganancias_diarias")
public class VistaGananciasDiarias {

    @Id
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "ingresos_brutos", precision = 14, scale = 2, nullable = false)
    private BigDecimal ingresosBrutos;

    @Column(name = "total_sueldos", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalSueldos;

    @Column(name = "ganancia_neta", precision = 14, scale = 2, nullable = false)
    private BigDecimal gananciaNeta;

    public LocalDate getFecha() { return fecha; }
    public BigDecimal getIngresosBrutos() { return ingresosBrutos; }
    public BigDecimal getTotalSueldos() { return totalSueldos; }
    public BigDecimal getGananciaNeta() { return gananciaNeta; }
}
