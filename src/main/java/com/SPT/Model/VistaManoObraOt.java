package com.SPT.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "vista_mano_obra_ot")
public class VistaManoObraOt {

    @Id
    @Column(name = "id_ot")
    private Long idOt;

    @Column(name = "numero_ot")
    private String numeroOt;

    @Column(name = "total_ot", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalOt;

    @Column(name = "costo_mano_obra", precision = 14, scale = 2, nullable = false)
    private BigDecimal costoManoObra;

    @Column(name = "subtotal_neto_ot", precision = 14, scale = 2, nullable = false)
    private BigDecimal subtotalNetoOt;

    public Long getIdOt() { return idOt; }
    public String getNumeroOt() { return numeroOt; }
    public BigDecimal getTotalOt() { return totalOt; }
    public BigDecimal getCostoManoObra() { return costoManoObra; }
    public BigDecimal getSubtotalNetoOt() { return subtotalNetoOt; }
}
