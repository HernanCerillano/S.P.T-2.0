package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "vista_ganancias_semanales")
public class VistaGananciasSemanales {

    @Id
    @Column(name = "anio_semana")
    private Integer anioSemana;

    @Column(name = "desde", nullable = false)
    private LocalDate desde;

    @Column(name = "hasta", nullable = false)
    private LocalDate hasta;

    @Column(name = "ingresos_brutos", precision = 14, scale = 2, nullable = false)
    private BigDecimal ingresosBrutos;

    @Column(name = "total_sueldos", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalSueldos;

    @Column(name = "ganancia_neta", precision = 14, scale = 2, nullable = false)
    private BigDecimal gananciaNeta;

    public Integer getAnioSemana() { return anioSemana; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public BigDecimal getIngresosBrutos() { return ingresosBrutos; }
    public BigDecimal getTotalSueldos() { return totalSueldos; }
    public BigDecimal getGananciaNeta() { return gananciaNeta; }
}
