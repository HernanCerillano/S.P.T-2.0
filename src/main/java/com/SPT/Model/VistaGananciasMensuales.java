package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vista_ganancias_mensuales")
public class VistaGananciasMensuales {

    @Id
    @Column(name = "anio_mes")
    private String anioMes;

    @Column(name = "ingresos_brutos", precision = 14, scale = 2, nullable = false)
    private BigDecimal ingresosBrutos;

    @Column(name = "total_sueldos", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalSueldos;

    @Column(name = "ganancia_neta", precision = 14, scale = 2, nullable = false)
    private BigDecimal gananciaNeta;

    public String getAnioMes() { return anioMes; }
    public BigDecimal getIngresosBrutos() { return ingresosBrutos; }
    public BigDecimal getTotalSueldos() { return totalSueldos; }
    public BigDecimal getGananciaNeta() { return gananciaNeta; }
}
