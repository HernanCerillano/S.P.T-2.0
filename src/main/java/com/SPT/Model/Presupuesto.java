package com.SPT.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "presupuesto")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presupuesto")
    private Long idPresupuesto;

    @Column(name = "numero_presupuesto")
    private String numeroPresupuesto;

    @Lob
    @Column(name = "resumen", columnDefinition = "LONGTEXT")
    private String resumen;

    @Column(name = "total", precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "precio_personalizado", precision = 14, scale = 2)
    private BigDecimal precioPersonalizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPresupuesto estado;

    @Column(name = "ocultar_precios_items", nullable = false)
    private boolean ocultarPreciosItems;

    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PresupuestoDetalle> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrdenTrabajo ordenTrabajo;

    public Presupuesto() {
    }

    public Long getIdPresupuesto() { return idPresupuesto; }
    public void setIdPresupuesto(Long idPresupuesto) { this.idPresupuesto = idPresupuesto; }

    public String getNumeroPresupuesto() { return numeroPresupuesto; }
    public void setNumeroPresupuesto(String numeroPresupuesto) { this.numeroPresupuesto = numeroPresupuesto; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getPrecioPersonalizado() { return precioPersonalizado; }
    public void setPrecioPersonalizado(BigDecimal precioPersonalizado) { this.precioPersonalizado = precioPersonalizado; }

    public EstadoPresupuesto getEstado() { return estado; }
    public void setEstado(EstadoPresupuesto estado) { this.estado = estado; }

    public boolean isOcultarPreciosItems() { return ocultarPreciosItems; }
    public void setOcultarPreciosItems(boolean ocultarPreciosItems) { this.ocultarPreciosItems = ocultarPreciosItems; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public List<PresupuestoDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<PresupuestoDetalle> detalles) { this.detalles = detalles; }

    public OrdenTrabajo getOrdenTrabajo() { return ordenTrabajo; }
    public void setOrdenTrabajo(OrdenTrabajo ordenTrabajo) { this.ordenTrabajo = ordenTrabajo; }
}
