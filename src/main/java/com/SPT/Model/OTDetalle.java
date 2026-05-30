package com.SPT.Model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "ot_detalle")
public class OTDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_ot")
    private Long idDetalleOt;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false)
    private TipoItemDetalle tipoItem;

    @Lob
    @Column(name = "descripcion_item", columnDefinition = "LONGTEXT")
    private String descripcionItem;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "id_ot", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne
    @JoinColumn(name = "id_pieza")
    private Pieza pieza;

    @ManyToOne
    @JoinColumn(name = "id_servicio")
    private TipoServicio tipoServicio;

    public OTDetalle() {
    }

    public OTDetalle(Long idDetalleOt, TipoItemDetalle tipoItem, String descripcionItem, Integer cantidad,
                     BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idDetalleOt = idDetalleOt;
        this.tipoItem = tipoItem;
        this.descripcionItem = descripcionItem;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getIdDetalleOt() {
        return idDetalleOt;
    }

    public void setIdDetalleOt(Long idDetalleOt) {
        this.idDetalleOt = idDetalleOt;
    }

    public TipoItemDetalle getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(TipoItemDetalle tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getDescripcionItem() {
        return descripcionItem;
    }

    public void setDescripcionItem(String descripcionItem) {
        this.descripcionItem = descripcionItem;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public OrdenTrabajo getOrdenTrabajo() {
        return ordenTrabajo;
    }

    public void setOrdenTrabajo(OrdenTrabajo ordenTrabajo) {
        this.ordenTrabajo = ordenTrabajo;
    }

    public Pieza getPieza() {
        return pieza;
    }

    public void setPieza(Pieza pieza) {
        this.pieza = pieza;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }
}
