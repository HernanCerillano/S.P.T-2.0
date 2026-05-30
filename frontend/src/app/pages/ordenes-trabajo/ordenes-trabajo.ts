import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { concatMap, forkJoin, from, toArray } from 'rxjs';
import {
  ClienteResponse,
  EstadoOT,
  FacturaDesdeOrigenRequest,
  FacturaResponse,
  OTDetalleRequest,
  OTDetalleResponse,
  OrdenTrabajoFilters,
  OrdenTrabajoRequest,
  OrdenTrabajoResponse,
  PagoResponse,
  PiezaRequest,
  PiezaResponse,
  SptApiService,
  TipoFactura,
  TipoItemDetalle,
  TipoServicioResponse,
  VehiculoResponse,
} from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';
import { ClienteBuscador } from '../../components/cliente-buscador/cliente-buscador';

@Component({
  selector: 'app-ordenes-trabajo',
  imports: [CommonModule, FormsModule, ClienteBuscador],
  templateUrl: './ordenes-trabajo.html',
  styleUrl: './ordenes-trabajo.scss',
})
export class OrdenesTrabajo implements OnInit, OnDestroy {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly estados: EstadoOT[] = ['EN_ESPERA', 'EN_PROCESO', 'FINALIZADA_COMPLETA', 'FINALIZADA_INCOMPLETA'];
  protected readonly tiposItem: TipoItemDetalle[] = ['PIEZA', 'SERVICIO'];
  protected readonly tiposFactura: TipoFactura[] = ['A', 'B', 'C', 'X', 'OTRO'];

  protected rows: OrdenTrabajoResponse[] = [];
  protected filtros: OrdenTrabajoFilters = this.newFiltros();
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected proximoNumeroOT = '';
  protected clienteSeleccionadoAlta: ClienteResponse | null = null;
  protected error = '';
  protected success = '';

  protected form: OrdenTrabajoRequest = this.newForm();

  protected seleccionado: OrdenTrabajoResponse | null = null;
  protected seleccionadoForm: OrdenTrabajoRequest = this.newForm();
  protected detalles: OTDetalleResponse[] = [];
  protected detalleForm: OTDetalleRequest = this.newDetalleForm();
  protected detalleLoading = false;
  protected detalleSaving = false;
  protected editandoSeleccionado = false;
  protected guardandoSeleccionado = false;
  protected downloadingPdf = false;
  protected usarPrecioPersonalizado = false;

  protected piezasCatalogo: PiezaResponse[] = [];
  protected serviciosCatalogo: TipoServicioResponse[] = [];
  protected clientesCatalogo: ClienteResponse[] = [];
  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected pagosRows: PagoResponse[] = [];
  protected catalogosCargados = false;
  protected detalleEditandoId: number | null = null;
  protected altaDetalles: OTDetalleRequest[] = [];
  protected altaDetalleForm: OTDetalleRequest = this.newDetalleForm();
  protected creandoPiezaRapida = false;
  protected showCrearPiezaAlta = false;
  protected showCrearPiezaDetalle = false;
  protected quickPiezaAltaForm: PiezaRequest = this.newQuickPiezaForm();
  protected quickPiezaDetalleForm: PiezaRequest = this.newQuickPiezaForm();
  protected altaPiezaBusqueda = '';
  protected altaServicioBusqueda = '';
  protected detallePiezaBusqueda = '';
  protected detalleServicioBusqueda = '';
  protected comboAbierto: 'altaPieza' | 'altaServicio' | 'detallePieza' | 'detalleServicio' | null = null;

  // ── Factura inline ───────────────────────────────────────────────────────────
  protected facturaDeOt: FacturaResponse | null = null;
  protected showFacturaPanel = false;
  protected facturaForm: FacturaDesdeOrigenRequest = this.newFacturaForm();
  protected savingFactura = false;
  protected downloadingFacturaPdf = false;
  protected proximoNumeroFacturaInline = '';

  private pagosRefreshTimer: number | null = null;

  ngOnInit(): void {
    this.cargar();
    this.cargarCatalogosSiHaceFalta();
    this.cargarPagos();
    this.iniciarAutoRefreshPagos();
  }

  ngOnDestroy(): void {
    if (this.pagosRefreshTimer != null) {
      window.clearInterval(this.pagosRefreshTimer);
      this.pagosRefreshTimer = null;
    }
  }

  protected onTogglePrecioPersonalizado(): void {
    if (!this.usarPrecioPersonalizado) {
      this.seleccionadoForm.precioPersonalizado = null;
    } else if (this.seleccionadoForm.precioPersonalizado == null) {
      this.seleccionadoForm.precioPersonalizado = this.seleccionado?.total ?? 0;
    }
  }

  protected totalMostrado(ot: OrdenTrabajoResponse | null): number {
    if (!ot) return 0;
    return ot.precioPersonalizado != null ? ot.precioPersonalizado : (ot.total ?? 0);
  }

  protected toggleForm(): void {
    this.showForm = !this.showForm;
    this.error = '';
    this.success = '';
    if (this.showForm) {
      this.cargarCatalogosSiHaceFalta();
      this.api.proximoNumeroOt().subscribe({
        next: (resp) => (this.proximoNumeroOT = resp.numero),
        error: () => { /* sin red; el backend igual auto-asigna al guardar */ },
      });
    }
    if (!this.showForm) {
      this.form = this.newForm();
      this.clienteSeleccionadoAlta = null;
      this.altaDetalleForm = this.newDetalleForm();
      this.altaDetalles = [];
      this.showCrearPiezaAlta = false;
      this.quickPiezaAltaForm = this.newQuickPiezaForm();
      this.altaPiezaBusqueda = '';
      this.altaServicioBusqueda = '';
      this.comboAbierto = null;
    }
  }

  protected guardar(): void {
    this.error = '';
    this.success = '';
    this.saving = true;
    const itemsAlta = this.altaDetalles.map((item) => ({ ...item }));

    this.api.createOrdenTrabajo(this.form).subscribe({
      next: (otCreada) => {
        const finalizarCreacion = (otFinal: OrdenTrabajoResponse, mensaje: string): void => {
          this.rows = [otFinal, ...this.rows.filter((r) => r.idOt !== otFinal.idOt)];
          this.success = mensaje;
          this.form = this.newForm();
          this.altaDetalleForm = this.newDetalleForm();
          this.altaDetalles = [];
          this.showForm = false;
          this.saving = false;
          this.abrirDetalle(otFinal);
        };

        if (itemsAlta.length === 0) {
          finalizarCreacion(otCreada, 'OT creada. Ya podes agregar piezas/servicios en el detalle.');
          return;
        }

        this.guardarItemsIniciales(otCreada.idOt, itemsAlta).subscribe({
          next: () => {
            this.api.getOrdenTrabajo(otCreada.idOt).subscribe({
              next: (otFinal) => {
                finalizarCreacion(otFinal, `OT creada con ${itemsAlta.length} item(s).`);
              },
              error: () => {
                finalizarCreacion(otCreada, `OT creada con ${itemsAlta.length} item(s).`);
              }
            });
          },
          error: (err) => {
            this.rows = [otCreada, ...this.rows];
            this.success = 'OT creada, pero hubo un error al cargar algunos items iniciales.';
            this.error = err?.error?.message ?? 'No se pudieron crear todos los items iniciales.';
            this.form = this.newForm();
            this.altaDetalleForm = this.newDetalleForm();
            this.altaDetalles = [];
            this.showForm = false;
            this.saving = false;
            this.abrirDetalle(otCreada);
          }
        });
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo crear la OT.';
        this.saving = false;
      },
    });
  }

  protected abrirDetalle(row: OrdenTrabajoResponse): void {
    if (this.seleccionado?.idOt === row.idOt && !this.editandoSeleccionado) {
      this.cerrarDetalle();
      return;
    }
    this.error = '';
    this.success = '';
    this.editandoSeleccionado = false;
    this.seleccionado = row;
    this.usarPrecioPersonalizado = row.precioPersonalizado != null;
    this.detalleForm = this.newDetalleForm();
    this.detalleEditandoId = null;
    this.cargarCatalogosSiHaceFalta();
    this.cargarDetalleOt(row.idOt);
  }

  protected cerrarDetalle(): void {
    this.seleccionado = null;
    this.seleccionadoForm = this.newForm();
    this.detalles = [];
    this.detalleForm = this.newDetalleForm();
    this.editandoSeleccionado = false;
    this.usarPrecioPersonalizado = false;
    this.detalleEditandoId = null;
    this.showCrearPiezaDetalle = false;
    this.quickPiezaDetalleForm = this.newQuickPiezaForm();
    this.detallePiezaBusqueda = '';
    this.detalleServicioBusqueda = '';
    this.comboAbierto = null;
    this.facturaDeOt = null;
    this.showFacturaPanel = false;
    this.facturaForm = this.newFacturaForm();
    this.proximoNumeroFacturaInline = '';
  }

  // ── Factura inline ───────────────────────────────────────────────────────────
  protected toggleFacturaPanel(): void {
    this.showFacturaPanel = !this.showFacturaPanel;
    if (!this.showFacturaPanel) {
      this.facturaForm = this.newFacturaForm();
      this.proximoNumeroFacturaInline = '';
    } else {
      this.api.proximoNumeroFactura().subscribe({
        next: (resp) => (this.proximoNumeroFacturaInline = resp.numero),
        error: () => { /* backend lo auto-asigna si viene blank */ },
      });
    }
  }

  protected crearFacturaParaOt(): void {
    if (!this.seleccionado || this.savingFactura) return;
    this.error = '';
    this.success = '';
    this.savingFactura = true;
    this.facturaForm.numeroFactura = this.proximoNumeroFacturaInline;
    this.api.crearFacturaDesdeOt(this.seleccionado.idOt, this.facturaForm).subscribe({
      next: (factura) => {
        this.facturaDeOt = factura;
        this.showFacturaPanel = false;
        this.facturaForm = this.newFacturaForm();
        this.savingFactura = false;
        this.success = `Factura ${factura.numeroFactura} creada.`;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo crear la factura.';
        this.savingFactura = false;
      },
    });
  }

  protected descargarFacturaPdfOt(): void {
    if (!this.facturaDeOt || this.downloadingFacturaPdf) return;
    this.error = '';
    this.downloadingFacturaPdf = true;
    this.api.downloadFacturaPdf(this.facturaDeOt.idFactura).subscribe({
      next: async (blob) => {
        try {
          await this.descargarBlob(blob, `Factura-${this.facturaDeOt!.numeroFactura}.pdf`);
        } finally {
          this.downloadingFacturaPdf = false;
        }
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo descargar el PDF de la factura.';
        this.downloadingFacturaPdf = false;
      },
    });
  }

  protected activarEdicion(): void {
    if (!this.seleccionado) {
      return;
    }
    this.seleccionadoForm = this.mapOtToRequest(this.seleccionado);
    this.editandoSeleccionado = true;
  }

  protected cancelarEdicionSeleccionado(): void {
    if (this.seleccionado) {
      this.seleccionadoForm = this.mapOtToRequest(this.seleccionado);
    }
    this.editandoSeleccionado = false;
  }

  protected guardarCambiosSeleccionado(): void {
    if (!this.seleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.guardandoSeleccionado = true;

    this.api.updateOrdenTrabajo(this.seleccionado.idOt, this.seleccionadoForm).subscribe({
      next: (updated) => {
        this.seleccionado = updated;
        this.seleccionadoForm = this.mapOtToRequest(updated);
        this.reemplazarFila(updated);
        this.editandoSeleccionado = false;
        this.guardandoSeleccionado = false;
        this.success = 'OT actualizada correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo actualizar la OT.';
        this.guardandoSeleccionado = false;
      },
    });
  }

  protected cambiarEstadoSeleccionado(estado: EstadoOT): void {
    if (!this.seleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.guardandoSeleccionado = true;

    this.api.cambiarEstadoOrdenTrabajo(this.seleccionado.idOt, estado).subscribe({
      next: (updated) => {
        this.seleccionado = updated;
        this.seleccionadoForm = this.mapOtToRequest(updated);
        this.reemplazarFila(updated);
        this.editandoSeleccionado = false;
        this.guardandoSeleccionado = false;
        this.success = `Estado actualizado a ${updated.estado}.`;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo cambiar el estado de la OT.';
        this.guardandoSeleccionado = false;
      }
    });
  }

  protected descargarPdfSeleccionado(): void {
    if (!this.seleccionado || this.downloadingPdf) {
      return;
    }

    this.error = '';
    this.downloadingPdf = true;
    this.api.downloadOrdenTrabajoPdf(this.seleccionado.idOt).subscribe({
      next: async (blob) => {
        try {
          await this.descargarBlob(blob, `${this.numeroOtLabel(this.seleccionado)}.pdf`);
        } finally {
          this.downloadingPdf = false;
        }
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo generar el PDF de la OT.';
        this.downloadingPdf = false;
      }
    });
  }

  protected async eliminarSeleccionado(): Promise<void> {
    if (!this.seleccionado) {
      return;
    }

    const ot = this.seleccionado;
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar orden de trabajo',
      message: `Vas a borrar la OT ${this.numeroOtLabel(ot)}.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.guardandoSeleccionado = true;
    this.api.deleteOrdenTrabajo(ot.idOt).subscribe({
      next: () => {
        this.rows = this.rows.filter((row) => row.idOt !== ot.idOt);
        this.cerrarDetalle();
        this.guardandoSeleccionado = false;
        this.cargar();
        this.success = 'Orden de trabajo eliminada correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar la orden de trabajo.';
        this.guardandoSeleccionado = false;
      }
    });
  }

  protected guardarDetalle(): void {
    if (!this.seleccionado || !this.editandoSeleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.detalleSaving = true;

    const payload = this.normalizarDetalleRequest(this.detalleForm);
    const request$ = this.detalleEditandoId == null
      ? this.api.addOtDetalle(this.seleccionado.idOt, payload)
      : this.api.updateOtDetalle(this.detalleEditandoId, payload);

    request$.subscribe({
      next: () => {
        this.success = this.detalleEditandoId == null
          ? 'Item agregado a la OT.'
          : 'Item actualizado de la OT.';
        this.detalleForm = this.newDetalleForm();
        this.detalleEditandoId = null;
        this.detallePiezaBusqueda = '';
        this.detalleServicioBusqueda = '';
        this.comboAbierto = null;
        this.detalleSaving = false;
        this.cargarDetalleOt(this.seleccionado!.idOt);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo agregar el item a la OT.';
        this.detalleSaving = false;
      },
    });
  }

  protected eliminarDetalle(idDetalleOt: number): void {
    if (!this.seleccionado || !this.editandoSeleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.detalleSaving = true;

    this.api.deleteOtDetalle(idDetalleOt).subscribe({
      next: () => {
        this.success = 'Item eliminado de la OT.';
        this.detalleSaving = false;
        this.cargarDetalleOt(this.seleccionado!.idOt);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el item de la OT.';
        this.detalleSaving = false;
      },
    });
  }

  protected onTipoItemDetalleChange(): void {
    if (this.detalleForm.tipoItem === 'PIEZA') {
      this.detalleForm.idServicio = null;
      this.detalleServicioBusqueda = '';
    } else {
      this.detalleForm.idPieza = null;
      this.showCrearPiezaDetalle = false;
      this.detalleForm.cantidad = 1;
      this.detallePiezaBusqueda = '';
    }
  }

  protected onTipoItemAltaChange(): void {
    if (this.altaDetalleForm.tipoItem === 'PIEZA') {
      this.altaDetalleForm.idServicio = null;
      this.altaServicioBusqueda = '';
    } else {
      this.altaDetalleForm.idPieza = null;
      this.showCrearPiezaAlta = false;
      this.altaDetalleForm.cantidad = 1;
      this.altaPiezaBusqueda = '';
    }
  }

  protected piezasAltaFiltradas(): PiezaResponse[] {
    return this.filtrarPiezas(this.altaPiezaBusqueda);
  }

  protected serviciosAltaFiltrados(): TipoServicioResponse[] {
    return this.filtrarServicios(this.altaServicioBusqueda);
  }

  protected piezasDetalleFiltradas(): PiezaResponse[] {
    return this.filtrarPiezas(this.detallePiezaBusqueda);
  }

  protected serviciosDetalleFiltrados(): TipoServicioResponse[] {
    return this.filtrarServicios(this.detalleServicioBusqueda);
  }

  protected abrirCombo(
    combo: 'altaPieza' | 'altaServicio' | 'detallePieza' | 'detalleServicio',
    disabled = false
  ): void {
    if (disabled) {
      return;
    }
    this.comboAbierto = combo;
  }

  protected cerrarComboConDelay(): void {
    window.setTimeout(() => {
      this.comboAbierto = null;
    }, 120);
  }

  protected onAltaPiezaInput(): void {
    this.altaDetalleForm.idPieza = null;
    this.abrirCombo('altaPieza');
  }

  protected onAltaServicioInput(): void {
    this.altaDetalleForm.idServicio = null;
    this.abrirCombo('altaServicio');
  }

  protected onDetallePiezaInput(): void {
    this.detalleForm.idPieza = null;
    this.abrirCombo('detallePieza', !this.editandoSeleccionado);
  }

  protected onDetalleServicioInput(): void {
    this.detalleForm.idServicio = null;
    this.abrirCombo('detalleServicio', !this.editandoSeleccionado);
  }

  protected seleccionarPiezaAlta(pieza: PiezaResponse): void {
    this.altaDetalleForm.idPieza = pieza.idPieza;
    this.altaPiezaBusqueda = this.piezaOpcionLabel(pieza);
    this.comboAbierto = null;
  }

  protected seleccionarServicioAlta(servicio: TipoServicioResponse): void {
    this.altaDetalleForm.idServicio = servicio.idServicio;
    this.altaServicioBusqueda = this.servicioOpcionLabel(servicio);
    this.comboAbierto = null;
  }

  protected seleccionarPiezaDetalle(pieza: PiezaResponse): void {
    if (!this.editandoSeleccionado) {
      return;
    }
    this.detalleForm.idPieza = pieza.idPieza;
    this.detallePiezaBusqueda = this.piezaOpcionLabel(pieza);
    this.comboAbierto = null;
  }

  protected seleccionarServicioDetalle(servicio: TipoServicioResponse): void {
    if (!this.editandoSeleccionado) {
      return;
    }
    this.detalleForm.idServicio = servicio.idServicio;
    this.detalleServicioBusqueda = this.servicioOpcionLabel(servicio);
    this.comboAbierto = null;
  }

  protected toggleCrearPiezaAlta(): void {
    this.showCrearPiezaAlta = !this.showCrearPiezaAlta;
    if (!this.showCrearPiezaAlta) {
      this.quickPiezaAltaForm = this.newQuickPiezaForm();
    }
  }

  protected toggleCrearPiezaDetalle(): void {
    if (!this.editandoSeleccionado) {
      return;
    }
    this.showCrearPiezaDetalle = !this.showCrearPiezaDetalle;
    if (!this.showCrearPiezaDetalle) {
      this.quickPiezaDetalleForm = this.newQuickPiezaForm();
    }
  }

  protected crearPiezaRapida(contexto: 'alta' | 'detalle'): void {
    if (this.creandoPiezaRapida) {
      return;
    }

    const form = contexto === 'alta' ? this.quickPiezaAltaForm : this.quickPiezaDetalleForm;
    this.error = '';
    this.success = '';
    this.creandoPiezaRapida = true;

    this.api.createPieza(form).subscribe({
      next: (pieza) => {
        this.actualizarCatalogoPiezas(pieza);
        if (contexto === 'alta') {
          this.altaDetalleForm.idPieza = pieza.idPieza;
          this.altaPiezaBusqueda = this.piezaOpcionLabel(pieza);
          this.showCrearPiezaAlta = false;
          this.quickPiezaAltaForm = this.newQuickPiezaForm();
        } else {
          this.detalleForm.idPieza = pieza.idPieza;
          this.detallePiezaBusqueda = this.piezaOpcionLabel(pieza);
          this.showCrearPiezaDetalle = false;
          this.quickPiezaDetalleForm = this.newQuickPiezaForm();
        }
        this.success = 'Pieza creada y seleccionada.';
        this.creandoPiezaRapida = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo crear la pieza.';
        this.creandoPiezaRapida = false;
      }
    });
  }

  protected agregarItemAlta(): void {
    try {
      this.error = '';
      const item = this.normalizarDetalleRequestBase(this.altaDetalleForm, null);
      this.altaDetalles = [...this.altaDetalles, item];
      this.altaDetalleForm = this.newDetalleForm();
      this.altaPiezaBusqueda = '';
      this.altaServicioBusqueda = '';
      this.comboAbierto = null;
    } catch (e) {
      this.error = e instanceof Error ? e.message : 'No se pudo preparar el item inicial.';
    }
  }

  protected quitarItemAlta(index: number): void {
    this.altaDetalles = this.altaDetalles.filter((_, i) => i !== index);
  }

  protected editarDetalle(item: OTDetalleResponse): void {
    if (!this.editandoSeleccionado) {
      return;
    }

    this.detalleEditandoId = item.idDetalleOt;
    this.detalleForm = {
      idOt: item.idOt ?? null,
      tipoItem: item.tipoItem,
      idPieza: item.idPieza ?? null,
      idServicio: item.idServicio ?? null,
      descripcionItem: item.descripcionItem ?? '',
      cantidad: Number(item.cantidad ?? 1),
      precioUnitario: item.precioUnitario != null ? Number(item.precioUnitario) : null,
      subtotal: item.subtotal != null ? Number(item.subtotal) : null,
    };
    this.detallePiezaBusqueda = item.idPieza ? this.piezaLabelById(item.idPieza) : '';
    this.detalleServicioBusqueda = item.idServicio ? this.servicioLabelById(item.idServicio) : '';
    this.comboAbierto = null;
  }

  protected cancelarEdicionDetalle(): void {
    this.detalleEditandoId = null;
    this.detalleForm = this.newDetalleForm();
    this.detallePiezaBusqueda = '';
    this.detalleServicioBusqueda = '';
    this.comboAbierto = null;
  }

  protected subtotalPreview(): number {
    return this.subtotalPreviewFor(this.detalleForm);
  }

  protected subtotalAltaPreview(): number {
    return this.subtotalPreviewFor(this.altaDetalleForm);
  }

  protected badgeClass(estado: EstadoOT): string {
    if (estado === 'FINALIZADA_COMPLETA' || estado === 'FINALIZADA_INCOMPLETA') {
      return 'badge badge-neutral';
    }
    if (estado === 'EN_PROCESO') {
      return 'badge badge-warn';
    }
    return 'badge badge-success';
  }

  protected totalPagadoOt(idOt: number): number {
    return this.pagosRows
      .filter((p) => p.idOt === idOt)
      .reduce((acc, p) => acc + Number(p.monto ?? 0), 0);
  }

  protected saldoPendienteOt(row: OrdenTrabajoResponse): number {
    return Math.max(0, this.totalMostrado(row) - this.totalPagadoOt(row.idOt));
  }

  protected estadoPagoLabel(row: OrdenTrabajoResponse): string {
    return this.saldoPendienteOt(row) <= 0 ? 'PAGADO' : 'PENDIENTE';
  }

  protected puedeFinalizar(row: OrdenTrabajoResponse | null): boolean {
    if (!row) {
      return false;
    }
    return row.estado !== 'FINALIZADA_COMPLETA' && row.estado !== 'FINALIZADA_INCOMPLETA';
  }

  protected aplicarFiltros(): void {
    this.cargar();
  }

  protected limpiarFiltros(): void {
    this.filtros = this.newFiltros();
    this.cargar();
  }

  protected vehiculosParaCliente(idCliente: number | null | undefined): VehiculoResponse[] {
    if (!idCliente || idCliente <= 0) {
      return this.vehiculosCatalogo;
    }
    return this.vehiculosCatalogo.filter((v) => v.idCliente === idCliente);
  }

  protected clienteLabel(idCliente: number): string {
    const cliente = this.clientesCatalogo.find((c) => c.idCliente === idCliente);
    return cliente ? `${cliente.idCliente} - ${cliente.nombre} ${cliente.apellido}` : `Cliente ${idCliente}`;
  }

  protected vehiculoLabel(idVehiculo: number): string {
    const vehiculo = this.vehiculosCatalogo.find((v) => v.idVehiculo === idVehiculo);
    return vehiculo
      ? `ID ${vehiculo.idVehiculo} - ${vehiculo.patente} - ${vehiculo.marca} ${vehiculo.modelo}`
      : `Vehiculo ${idVehiculo}`;
  }

  protected piezaOpcionLabel(pieza: PiezaResponse): string {
    return `${pieza.idPieza} - ${pieza.nombre} (${pieza.marca})`;
  }

  protected servicioOpcionLabel(servicio: TipoServicioResponse): string {
    return `${servicio.idServicio} - ${servicio.nombre}`;
  }

  protected totalItemsAlta(): number {
    return this.altaDetalles.reduce((acc, item) => acc + Number(item.subtotal ?? 0), 0);
  }

  protected numeroOtLabel(row: OrdenTrabajoResponse | null): string {
    if (!row) {
      return '';
    }
    return row.numeroOt?.trim() || `OT-${String(row.idOt).padStart(4, '0')}`;
  }

  private piezaLabelById(idPieza: number): string {
    const pieza = this.piezasCatalogo.find((p) => p.idPieza === idPieza);
    return pieza ? this.piezaOpcionLabel(pieza) : `Pieza ${idPieza}`;
  }

  private servicioLabelById(idServicio: number): string {
    const servicio = this.serviciosCatalogo.find((s) => s.idServicio === idServicio);
    return servicio ? this.servicioOpcionLabel(servicio) : `Servicio ${idServicio}`;
  }

  private cargar(): void {
    this.loading = true;
    this.api.listOrdenesTrabajo(this.filtrosRequest()).subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
        this.cargarPagos();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar las OT.';
        this.loading = false;
      },
    });
  }

  private cargarDetalleOt(idOt: number): void {
    this.detalleLoading = true;

    forkJoin({
      ot: this.api.getOrdenTrabajo(idOt),
      detalles: this.api.listOtDetalles(idOt),
    }).subscribe({
      next: ({ ot, detalles }) => {
        this.seleccionado = ot;
        this.seleccionadoForm = this.mapOtToRequest(ot);
        this.detalles = detalles;
        this.reemplazarFila(ot);
        this.detalleLoading = false;
        this.cargarPagos();
        this.cargarFacturaDeOt(idOt);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo cargar el detalle de la OT.';
        this.detalleLoading = false;
      },
    });
  }

  private cargarFacturaDeOt(idOt: number): void {
    this.facturaDeOt = null;
    this.api.listFacturas().subscribe({
      next: (facturas) => {
        this.facturaDeOt = facturas.find((f) => f.idOt === idOt) ?? null;
      },
    });
  }

  private cargarCatalogosSiHaceFalta(): void {
    if (this.catalogosCargados) {
      return;
    }

    forkJoin({
      clientes: this.api.listClientes(),
      vehiculos: this.api.listVehiculos(),
      piezas: this.api.listPiezas(),
      servicios: this.api.listServicios(),
    }).subscribe({
      next: ({ clientes, vehiculos, piezas, servicios }) => {
        this.clientesCatalogo = clientes;
        this.vehiculosCatalogo = vehiculos;
        this.piezasCatalogo = piezas.filter((p) => p.activo);
        this.serviciosCatalogo = servicios.filter((s) => s.activo);
        this.catalogosCargados = true;
      },
      error: () => {
        // no-op: el usuario puede cargar IDs manuales si no se pudo traer catalogos
      },
    });
  }

  private cargarPagos(): void {
    this.api.listPagos().subscribe({
      next: (data) => {
        this.pagosRows = data;
      }
    });
  }

  private iniciarAutoRefreshPagos(): void {
    if (this.pagosRefreshTimer != null) {
      window.clearInterval(this.pagosRefreshTimer);
    }
    this.pagosRefreshTimer = window.setInterval(() => {
      this.cargarPagos();
    }, 8000);
  }

  private guardarItemsIniciales(idOt: number, itemsAlta: OTDetalleRequest[]) {
    return from(itemsAlta).pipe(
      concatMap((item) =>
        this.api.addOtDetalle(idOt, {
          ...item,
          idOt,
        })
      ),
      toArray()
    );
  }

  private actualizarCatalogoPiezas(pieza: PiezaResponse): void {
    const sinDuplicado = this.piezasCatalogo.filter((item) => item.idPieza !== pieza.idPieza);
    this.piezasCatalogo = [...sinDuplicado, pieza]
      .filter((item) => item.activo)
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  private normalizarDetalleRequest(form: OTDetalleRequest): OTDetalleRequest {
    return this.normalizarDetalleRequestBase(form, this.seleccionado?.idOt ?? null);
  }

  private normalizarDetalleRequestBase(form: OTDetalleRequest, idOt: number | null): OTDetalleRequest {
    const tipoItem = form.tipoItem;
    const cantidad = tipoItem === 'SERVICIO' ? 1 : Math.max(1, Number(form.cantidad ?? 1));
    let idPieza = tipoItem === 'PIEZA' ? this.toNullableNumber(form.idPieza) : null;
    let idServicio = tipoItem === 'SERVICIO' ? this.toNullableNumber(form.idServicio) : null;
    let descripcionItem = form.descripcionItem?.trim() ?? '';
    let precioUnitario = this.toNullableNumber(form.precioUnitario);

    if (tipoItem === 'PIEZA') {
      if (idPieza == null) {
        throw new Error('Selecciona una pieza.');
      }
      const pieza = this.piezasCatalogo.find((p) => p.idPieza === idPieza);
      if (!descripcionItem && pieza) {
        descripcionItem = pieza.nombre;
      }
      if (precioUnitario == null && pieza) {
        precioUnitario = Number(pieza.precioUnitario);
      }
    }

    if (tipoItem === 'SERVICIO') {
      if (idServicio == null) {
        throw new Error('Selecciona un servicio.');
      }
      const servicio = this.serviciosCatalogo.find((s) => s.idServicio === idServicio);
      if (!descripcionItem && servicio) {
        descripcionItem = servicio.nombre;
      }
      if (precioUnitario == null && servicio) {
        precioUnitario = Number(servicio.precioBase);
      }
    }

    const subtotal = precioUnitario == null ? null : cantidad * Number(precioUnitario);

    return {
      idOt,
      tipoItem,
      idPieza,
      idServicio,
      descripcionItem,
      cantidad,
      precioUnitario,
      subtotal,
    };
  }

  private subtotalPreviewFor(form: OTDetalleRequest): number {
    const cantidad = form.tipoItem === 'SERVICIO' ? 1 : Number(form.cantidad ?? 0);
    let precio = this.toNullableNumber(form.precioUnitario);

    if (precio == null) {
      if (form.tipoItem === 'PIEZA' && form.idPieza) {
        const pieza = this.piezasCatalogo.find((p) => p.idPieza === form.idPieza);
        precio = pieza ? Number(pieza.precioUnitario) : null;
      } else if (form.tipoItem === 'SERVICIO' && form.idServicio) {
        const servicio = this.serviciosCatalogo.find((s) => s.idServicio === form.idServicio);
        precio = servicio ? Number(servicio.precioBase) : null;
      }
    }

    if (!cantidad || precio == null) {
      return 0;
    }
    return cantidad * Number(precio);
  }

  private toNullableNumber(value: number | null | undefined): number | null {
    return value === null || value === undefined || Number.isNaN(Number(value)) ? null : Number(value);
  }

  private reemplazarFila(updated: OrdenTrabajoResponse): void {
    this.rows = this.rows.map((row) => (row.idOt === updated.idOt ? updated : row));
  }

  private async descargarBlob(blob: Blob, filename: string): Promise<void> {
    try {
      const [{ save }, { writeFile }] = await Promise.all([
        import('@tauri-apps/plugin-dialog'),
        import('@tauri-apps/plugin-fs'),
      ]);
      const selectedPath = await save({
        defaultPath: filename,
        filters: [
          {
            name: 'PDF',
            extensions: ['pdf'],
          },
        ],
      });

      if (typeof selectedPath === 'string' && selectedPath) {
        await writeFile(selectedPath, new Uint8Array(await blob.arrayBuffer()));
        this.success = 'PDF guardado en la ubicacion seleccionada.';
        return;
      }
      if (selectedPath === null) {
        return;
      }
    } catch {
      if (this.esEntornoTauri()) {
        this.error = 'No se pudo abrir el selector de guardado nativo.';
        return;
      }
    }

    const showSaveFilePicker = (window as Window & {
      showSaveFilePicker?: (options?: unknown) => Promise<any>;
    }).showSaveFilePicker;

    if (typeof showSaveFilePicker === 'function') {
      try {
        const handle = await showSaveFilePicker({
          suggestedName: filename,
          types: [
            {
              description: 'PDF',
              accept: {
                'application/pdf': ['.pdf'],
              },
            },
          ],
        });
        const writable = await handle.createWritable();
        await writable.write(await blob.arrayBuffer());
        await writable.close();
        this.success = 'PDF guardado en la ubicacion seleccionada.';
        return;
      } catch (error) {
        const mensaje = error instanceof Error ? error.message : '';
        if (mensaje.toLowerCase().includes('abort') || mensaje.toLowerCase().includes('cancel')) {
          return;
        }
      }
    }

    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  private esEntornoTauri(): boolean {
    const scope = window as Window & { __TAURI__?: unknown; __TAURI_INTERNALS__?: unknown };
    return typeof scope.__TAURI__ !== 'undefined' || typeof scope.__TAURI_INTERNALS__ !== 'undefined';
  }

  private mapOtToRequest(data: OrdenTrabajoResponse): OrdenTrabajoRequest {
    return {
      numeroOt: data.numeroOt ?? '',
      idPresupuesto: data.idPresupuesto ?? null,
      idCliente: data.idCliente,
      idVehiculo: data.idVehiculo,
      resumenTrabajo: data.resumenTrabajo ?? '',
      estado: data.estado ?? 'EN_ESPERA',
      total: data.total ?? 0,
      precioPersonalizado: data.precioPersonalizado ?? null,
      ocultarPreciosItems: data.ocultarPreciosItems ?? false,
      fechaFinalizacion: data.fechaFinalizacion ?? null,
    };
  }

  protected onClienteSeleccionadoAlta(c: ClienteResponse | null): void {
    this.clienteSeleccionadoAlta = c;
    this.form.idCliente = c?.idCliente ?? 0;
    this.form.idVehiculo = 0;  // reset vehículo al cambiar cliente
  }

  private newForm(): OrdenTrabajoRequest {
    return {
      numeroOt: '',
      idPresupuesto: null,
      idCliente: 0,
      idVehiculo: 0,
      resumenTrabajo: '',
      estado: 'EN_ESPERA',
      total: 0,
      precioPersonalizado: null,
      ocultarPreciosItems: false,
      fechaFinalizacion: null,
    };
  }

  private newFiltros(): OrdenTrabajoFilters {
    return {
      numeroOt: '',
      idCliente: 0,
      idVehiculo: 0,
      patente: '',
      desde: '',
      hasta: '',
      estado: undefined,
    };
  }

  private filtrosRequest(): OrdenTrabajoFilters | undefined {
    const req: OrdenTrabajoFilters = {};
    if (this.filtros.numeroOt?.trim()) {
      req.numeroOt = this.filtros.numeroOt.trim();
    }
    if ((this.filtros.idCliente ?? 0) > 0) {
      req.idCliente = this.filtros.idCliente;
    }
    if ((this.filtros.idVehiculo ?? 0) > 0) {
      req.idVehiculo = this.filtros.idVehiculo;
    }
    if (this.filtros.patente?.trim()) {
      req.patente = this.filtros.patente.trim();
    }
    if (this.filtros.desde) {
      req.desde = this.filtros.desde;
    }
    if (this.filtros.hasta) {
      req.hasta = this.filtros.hasta;
    }
    if (this.filtros.estado) {
      req.estado = this.filtros.estado;
    }
    return Object.keys(req).length ? req : undefined;
  }

  private filtrarPiezas(termino: string): PiezaResponse[] {
    const query = termino.trim().toLowerCase();
    if (!query) {
      return this.piezasCatalogo;
    }
    return this.piezasCatalogo.filter((pieza) => {
      const label = `${pieza.idPieza} ${pieza.nombre} ${pieza.marca} ${pieza.medidas}`.toLowerCase();
      return label.includes(query);
    });
  }

  private filtrarServicios(termino: string): TipoServicioResponse[] {
    const query = termino.trim().toLowerCase();
    if (!query) {
      return this.serviciosCatalogo;
    }
    return this.serviciosCatalogo.filter((servicio) => {
      const label = `${servicio.idServicio} ${servicio.nombre} ${servicio.descripcion}`.toLowerCase();
      return label.includes(query);
    });
  }

  private newDetalleForm(): OTDetalleRequest {
    return {
      idOt: null,
      tipoItem: 'PIEZA',
      idPieza: null,
      idServicio: null,
      descripcionItem: '',
      cantidad: 1,
      precioUnitario: null,
      subtotal: null,
    };
  }

  private newQuickPiezaForm(): PiezaRequest {
    return {
      nombre: '',
      marca: '',
      medidas: '',
      calidad: '',
      precioUnitario: 0,
      activo: true,
    };
  }

  private newFacturaForm(): FacturaDesdeOrigenRequest {
    return {
      numeroFactura: '',
      tipoFactura: 'B',
      impuestos: 0,
      observaciones: '',
    };
  }
}
