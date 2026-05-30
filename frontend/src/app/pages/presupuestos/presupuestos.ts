import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { concatMap, forkJoin, from, toArray } from 'rxjs';
import {
  ClienteResponse,
  EstadoPresupuesto,
  FacturaDesdeOrigenRequest,
  FacturaResponse,
  PiezaRequest,
  PiezaResponse,
  PresupuestoDetalleRequest,
  PresupuestoDetalleResponse,
  PresupuestoRequest,
  PresupuestoResponse,
  SptApiService,
  TipoFactura,
  TipoItemDetalle,
  TipoServicioResponse,
  VehiculoResponse,
} from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';
import { ClienteBuscador } from '../../components/cliente-buscador/cliente-buscador';

@Component({
  selector: 'app-presupuestos',
  imports: [CommonModule, FormsModule, ClienteBuscador],
  templateUrl: './presupuestos.html',
  styleUrl: './presupuestos.scss',
})
export class Presupuestos implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly estados: EstadoPresupuesto[] = ['ACTIVO', 'ACEPTADO', 'ARCHIVADO'];
  protected readonly tiposItem: TipoItemDetalle[] = ['PIEZA', 'SERVICIO'];
  protected readonly tiposFactura: TipoFactura[] = ['A', 'B', 'C', 'X', 'OTRO'];

  protected rows: PresupuestoResponse[] = [];
  protected filtroEstado: EstadoPresupuesto | '' = 'ACTIVO';
  protected filtroPatente = '';
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';

  protected form: PresupuestoRequest = this.newForm();
  protected clienteSeleccionadoAlta: ClienteResponse | null = null;

  protected seleccionado: PresupuestoResponse | null = null;
  protected seleccionadoForm: PresupuestoRequest = this.newForm();
  protected detalles: PresupuestoDetalleResponse[] = [];
  protected detalleForm: PresupuestoDetalleRequest = this.newDetalleForm();
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
  protected catalogosCargados = false;
  protected detalleEditandoId: number | null = null;
  protected altaDetalles: PresupuestoDetalleRequest[] = [];
  protected altaDetalleForm: PresupuestoDetalleRequest = this.newDetalleForm();
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
  protected facturaDePresupuesto: FacturaResponse | null = null;
  protected showFacturaPanel = false;
  protected facturaForm: FacturaDesdeOrigenRequest = this.newFacturaForm();
  protected savingFactura = false;
  protected downloadingFacturaPdf = false;

  ngOnInit(): void {
    this.cargar();
    this.cargarCatalogosSiHaceFalta();
  }

  protected onTogglePrecioPersonalizado(): void {
    if (!this.usarPrecioPersonalizado) {
      this.seleccionadoForm.precioPersonalizado = null;
    } else if (this.seleccionadoForm.precioPersonalizado == null) {
      this.seleccionadoForm.precioPersonalizado = this.seleccionado?.total ?? 0;
    }
  }

  protected totalMostrado(p: PresupuestoResponse | null): number {
    if (!p) return 0;
    return p.precioPersonalizado != null ? p.precioPersonalizado : (p.total ?? 0);
  }

  protected toggleForm(): void {
    this.showForm = !this.showForm;
    this.error = '';
    this.success = '';
    if (this.showForm) {
      this.cargarCatalogosSiHaceFalta();
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

  protected onClienteSeleccionadoAlta(c: ClienteResponse | null): void {
    this.clienteSeleccionadoAlta = c;
    this.form.idCliente = c?.idCliente ?? 0;
    this.form.idVehiculo = 0;
  }

  protected guardar(): void {
    this.error = '';
    this.success = '';
    this.saving = true;
    const itemsAlta = this.altaDetalles.map((item) => ({ ...item }));

    this.api.createPresupuesto(this.form).subscribe({
      next: (presupuestoCreado) => {
        const finalizarCreacion = (presupuestoFinal: PresupuestoResponse, mensaje: string): void => {
          this.rows = [presupuestoFinal, ...this.rows.filter((r) => r.idPresupuesto !== presupuestoFinal.idPresupuesto)];
          this.success = mensaje;
          this.form = this.newForm();
          this.altaDetalleForm = this.newDetalleForm();
          this.altaDetalles = [];
          this.showForm = false;
          this.saving = false;
          this.abrirDetalle(presupuestoFinal);
        };

        if (itemsAlta.length === 0) {
          finalizarCreacion(presupuestoCreado, 'Presupuesto creado. Ya podes agregar piezas/servicios en el detalle.');
          return;
        }

        this.guardarItemsIniciales(presupuestoCreado.idPresupuesto, itemsAlta).subscribe({
          next: () => {
            this.api.getPresupuesto(presupuestoCreado.idPresupuesto).subscribe({
              next: (presupuestoFinal) => {
                finalizarCreacion(presupuestoFinal, `Presupuesto creado con ${itemsAlta.length} item(s).`);
              },
              error: () => {
                finalizarCreacion(presupuestoCreado, `Presupuesto creado con ${itemsAlta.length} item(s).`);
              },
            });
          },
          error: (err) => {
            this.rows = [presupuestoCreado, ...this.rows];
            this.success = 'Presupuesto creado, pero hubo un error al cargar algunos items iniciales.';
            this.error = err?.error?.message ?? 'No se pudieron crear todos los items iniciales.';
            this.form = this.newForm();
            this.altaDetalleForm = this.newDetalleForm();
            this.altaDetalles = [];
            this.showForm = false;
            this.saving = false;
            this.abrirDetalle(presupuestoCreado);
          }
        });
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo crear el presupuesto.';
        this.saving = false;
      },
    });
  }

  protected abrirDetalle(row: PresupuestoResponse): void {
    if (this.seleccionado?.idPresupuesto === row.idPresupuesto && !this.editandoSeleccionado) {
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
    this.cargarDetallePresupuesto(row.idPresupuesto);
  }

  protected cerrarDetalle(): void {
    this.seleccionado = null;
    this.detalles = [];
    this.detalleLoading = false;
    this.editandoSeleccionado = false;
    this.usarPrecioPersonalizado = false;
    this.detalleForm = this.newDetalleForm();
    this.detalleEditandoId = null;
    this.showCrearPiezaDetalle = false;
    this.quickPiezaDetalleForm = this.newQuickPiezaForm();
    this.detallePiezaBusqueda = '';
    this.detalleServicioBusqueda = '';
    this.comboAbierto = null;
    this.facturaDePresupuesto = null;
    this.showFacturaPanel = false;
    this.facturaForm = this.newFacturaForm();
  }

  // ── Factura inline ───────────────────────────────────────────────────────────
  protected toggleFacturaPanel(): void {
    this.showFacturaPanel = !this.showFacturaPanel;
    if (!this.showFacturaPanel) {
      this.facturaForm = this.newFacturaForm();
    }
  }

  protected crearFacturaParaPresupuesto(): void {
    if (!this.seleccionado || this.savingFactura) return;
    this.error = '';
    this.success = '';
    this.savingFactura = true;
    this.api.crearFacturaDesdePresupuesto(this.seleccionado.idPresupuesto, this.facturaForm).subscribe({
      next: (factura) => {
        this.facturaDePresupuesto = factura;
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

  protected descargarFacturaPdfPresupuesto(): void {
    if (!this.facturaDePresupuesto || this.downloadingFacturaPdf) return;
    this.error = '';
    this.downloadingFacturaPdf = true;
    this.api.downloadFacturaPdf(this.facturaDePresupuesto.idFactura).subscribe({
      next: async (blob) => {
        try {
          await this.descargarBlob(blob, `Factura-${this.facturaDePresupuesto!.numeroFactura}.pdf`);
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
    this.editandoSeleccionado = true;
    this.seleccionadoForm = this.mapPresupuestoToRequest(this.seleccionado);
  }

  protected cancelarEdicionSeleccionado(): void {
    if (this.seleccionado) {
      this.seleccionadoForm = this.mapPresupuestoToRequest(this.seleccionado);
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

    this.api.updatePresupuesto(this.seleccionado.idPresupuesto, this.seleccionadoForm).subscribe({
      next: (updated) => {
        this.seleccionado = updated;
        this.seleccionadoForm = this.mapPresupuestoToRequest(updated);
        this.reemplazarFila(updated);
        this.editandoSeleccionado = false;
        this.guardandoSeleccionado = false;
        this.success = 'Presupuesto actualizado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo actualizar el presupuesto.';
        this.guardandoSeleccionado = false;
      },
    });
  }

  protected archivarSeleccionado(): void {
    if (!this.seleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.guardandoSeleccionado = true;

    this.api.archivarPresupuesto(this.seleccionado.idPresupuesto).subscribe({
      next: (updated) => {
        this.seleccionado = updated;
        this.seleccionadoForm = this.mapPresupuestoToRequest(updated);
        this.reemplazarFila(updated);
        this.cargar();
        this.guardandoSeleccionado = false;
        this.success = 'Presupuesto archivado.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo archivar el presupuesto.';
        this.guardandoSeleccionado = false;
      },
    });
  }

  protected desarchivarSeleccionado(): void {
    if (!this.seleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.guardandoSeleccionado = true;

    this.api.desarchivarPresupuesto(this.seleccionado.idPresupuesto).subscribe({
      next: (updated) => {
        this.seleccionado = updated;
        this.seleccionadoForm = this.mapPresupuestoToRequest(updated);
        this.reemplazarFila(updated);
        this.cargar();
        this.guardandoSeleccionado = false;
        this.success = 'Presupuesto reactivado (desarchivado).';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo desarchivar el presupuesto.';
        this.guardandoSeleccionado = false;
      },
    });
  }

  protected convertirSeleccionadoAOt(): void {
    if (!this.seleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.guardandoSeleccionado = true;

    this.api.createOrdenTrabajoDesdePresupuesto(this.seleccionado.idPresupuesto).subscribe({
      next: (ot) => {
        this.guardandoSeleccionado = false;
        this.success = `Presupuesto convertido a OT (${ot.numeroOt || 'OT-' + ot.idOt}).`;
        this.cargarDetallePresupuesto(this.seleccionado!.idPresupuesto);
        this.cargar();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo convertir el presupuesto en OT.';
        this.guardandoSeleccionado = false;
      },
    });
  }

  protected descargarPdfSeleccionado(): void {
    if (!this.seleccionado || this.downloadingPdf) {
      return;
    }

    this.error = '';
    this.downloadingPdf = true;
    this.api.downloadPresupuestoPdf(this.seleccionado.idPresupuesto).subscribe({
      next: async (blob) => {
        try {
          await this.descargarBlob(blob, `${this.numeroPresupuestoLabel(this.seleccionado)}.pdf`);
        } finally {
          this.downloadingPdf = false;
        }
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo generar el PDF del presupuesto.';
        this.downloadingPdf = false;
      },
    });
  }

  protected async eliminarSeleccionado(): Promise<void> {
    if (!this.seleccionado) {
      return;
    }

    const presupuesto = this.seleccionado;
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar presupuesto',
      message: `Vas a borrar el presupuesto ${this.numeroPresupuestoLabel(presupuesto)}. Si tiene una OT asociada tambien se eliminara.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.guardandoSeleccionado = true;
    this.api.deletePresupuesto(presupuesto.idPresupuesto).subscribe({
      next: () => {
        this.rows = this.rows.filter((row) => row.idPresupuesto !== presupuesto.idPresupuesto);
        this.cerrarDetalle();
        this.guardandoSeleccionado = false;
        this.cargar();
        this.success = 'Presupuesto eliminado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el presupuesto.';
        this.guardandoSeleccionado = false;
      },
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
      ? this.api.addPresupuestoDetalle(this.seleccionado.idPresupuesto, payload)
      : this.api.updatePresupuestoDetalle(this.detalleEditandoId, payload);

    request$.subscribe({
      next: () => {
        this.success = this.detalleEditandoId == null
          ? 'Item agregado al presupuesto.'
          : 'Item actualizado del presupuesto.';
        this.detalleForm = this.newDetalleForm();
        this.detalleEditandoId = null;
        this.detallePiezaBusqueda = '';
        this.detalleServicioBusqueda = '';
        this.comboAbierto = null;
        this.detalleSaving = false;
        this.cargarDetallePresupuesto(this.seleccionado!.idPresupuesto);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo agregar el item.';
        this.detalleSaving = false;
      },
    });
  }

  protected eliminarDetalle(idDetallePresupuesto: number): void {
    if (!this.seleccionado || !this.editandoSeleccionado) {
      return;
    }

    this.error = '';
    this.success = '';
    this.detalleSaving = true;

    this.api.deletePresupuestoDetalle(idDetallePresupuesto).subscribe({
      next: () => {
        this.success = 'Item eliminado del presupuesto.';
        this.detalleSaving = false;
        this.cargarDetallePresupuesto(this.seleccionado!.idPresupuesto);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el item.';
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
      },
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

  protected subtotalAltaPreview(): number {
    return this.subtotalPreviewFor(this.altaDetalleForm);
  }

  protected editarDetalle(item: PresupuestoDetalleResponse): void {
    if (!this.editandoSeleccionado) {
      return;
    }

    this.detalleEditandoId = item.idDetallePresupuesto;
    this.detalleForm = {
      idPresupuesto: item.idPresupuesto ?? null,
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

  protected badgeClass(estado: EstadoPresupuesto): string {
    if (estado === 'ARCHIVADO') {
      return 'badge badge-warn';
    }
    if (estado === 'ACEPTADO') {
      return 'badge badge-neutral';
    }
    return 'badge badge-success';
  }

  protected aplicarFiltros(): void {
    this.cargar();
  }

  protected limpiarFiltros(): void {
    this.filtroEstado = '';
    this.filtroPatente = '';
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

  protected numeroPresupuestoLabel(row: PresupuestoResponse | null): string {
    if (!row) {
      return '';
    }
    return row.numeroPresupuesto?.trim() || `PRESS-${String(row.idPresupuesto).padStart(7, '0')}`;
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
    this.api.listPresupuestos({
      estado: this.filtroEstado,
      patente: this.filtroPatente,
    }).subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los presupuestos.';
        this.loading = false;
      },
    });
  }

  private cargarDetallePresupuesto(idPresupuesto: number): void {
    this.detalleLoading = true;

    forkJoin({
      presupuesto: this.api.getPresupuesto(idPresupuesto),
      detalles: this.api.listPresupuestoDetalles(idPresupuesto),
    }).subscribe({
      next: ({ presupuesto, detalles }) => {
        this.seleccionado = presupuesto;
        this.seleccionadoForm = this.mapPresupuestoToRequest(presupuesto);
        this.detalles = detalles;
        this.reemplazarFila(presupuesto);
        this.detalleLoading = false;
        this.cargarFacturaDePresupuesto(idPresupuesto);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo cargar el detalle del presupuesto.';
        this.detalleLoading = false;
      },
    });
  }

  private cargarFacturaDePresupuesto(idPresupuesto: number): void {
    this.facturaDePresupuesto = null;
    this.api.listFacturas().subscribe({
      next: (facturas) => {
        this.facturaDePresupuesto = facturas.find((f) => f.idPresupuesto === idPresupuesto) ?? null;
      },
    });
  }

  private guardarItemsIniciales(idPresupuesto: number, itemsAlta: PresupuestoDetalleRequest[]) {
    return from(itemsAlta).pipe(
      concatMap((item) =>
        this.api.addPresupuestoDetalle(idPresupuesto, {
          ...item,
          idPresupuesto,
        })
      ),
      toArray()
    );
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
        // No bloquea la pantalla; si falla el catalogo, quedan inputs manuales/valores previos.
      },
    });
  }

  private normalizarDetalleRequest(form: PresupuestoDetalleRequest): PresupuestoDetalleRequest {
    return this.normalizarDetalleRequestBase(form, this.seleccionado?.idPresupuesto ?? null);
  }

  private normalizarDetalleRequestBase(form: PresupuestoDetalleRequest, idPresupuesto: number | null): PresupuestoDetalleRequest {
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
      idPresupuesto,
      tipoItem,
      idPieza,
      idServicio,
      descripcionItem,
      cantidad,
      precioUnitario,
      subtotal,
    };
  }

  private subtotalPreviewFor(form: PresupuestoDetalleRequest): number {
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

  private reemplazarFila(updated: PresupuestoResponse): void {
    this.rows = this.rows.map((row) => (row.idPresupuesto === updated.idPresupuesto ? updated : row));
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
        this.success = `PDF guardado en la ubicacion seleccionada.`;
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

  private actualizarCatalogoPiezas(pieza: PiezaResponse): void {
    const sinDuplicado = this.piezasCatalogo.filter((item) => item.idPieza !== pieza.idPieza);
    this.piezasCatalogo = [...sinDuplicado, pieza]
      .filter((item) => item.activo)
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  private esEntornoTauri(): boolean {
    const scope = window as Window & { __TAURI__?: unknown; __TAURI_INTERNALS__?: unknown };
    return typeof scope.__TAURI__ !== 'undefined' || typeof scope.__TAURI_INTERNALS__ !== 'undefined';
  }

  private mapPresupuestoToRequest(data: PresupuestoResponse): PresupuestoRequest {
    return {
      numeroPresupuesto: data.numeroPresupuesto ?? '',
      idCliente: data.idCliente,
      idVehiculo: data.idVehiculo,
      resumen: data.resumen ?? '',
      total: data.total ?? 0,
      precioPersonalizado: data.precioPersonalizado ?? null,
      estado: data.estado ?? 'ACTIVO',
      ocultarPreciosItems: data.ocultarPreciosItems ?? false,
    };
  }

  private newForm(): PresupuestoRequest {
    return {
      numeroPresupuesto: '',
      idCliente: 0,
      idVehiculo: 0,
      resumen: '',
      total: 0,
      precioPersonalizado: null,
      estado: 'ACTIVO',
      ocultarPreciosItems: false,
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

  private newDetalleForm(): PresupuestoDetalleRequest {
    return {
      idPresupuesto: null,
      tipoItem: 'PIEZA',
      idPieza: null,
      idServicio: null,
      descripcionItem: '',
      cantidad: 1,
      precioUnitario: null,
      subtotal: null,
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
