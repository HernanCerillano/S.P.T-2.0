import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ClienteResponse,
  EmpleadoResponse,
  MetodoPago,
  OrdenTrabajoResponse,
  PagoEmpleadoRequest,
  PagoEmpleadoResponse,
  PagoFilters,
  PagoRequest,
  PagoResponse,
  SptApiService,
  VehiculoResponse,
} from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

@Component({
  selector: 'app-pagos',
  imports: [CommonModule, FormsModule],
  templateUrl: './pagos.html',
  styleUrl: './pagos.scss',
})
export class Pagos implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly metodos: MetodoPago[] = ['EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'CHEQUE'];

  // ── tab ──────────────────────────────────────────────────────────────────────
  protected tabPagos: 'clientes' | 'empleados' = 'clientes';

  // ── Pagos de clientes ────────────────────────────────────────────────────────
  protected rows: PagoResponse[] = [];
  protected filtros: PagoFilters = this.newFiltros();
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';

  protected form: PagoRequest = this.newForm();
  protected clientesCatalogo: ClienteResponse[] = [];
  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected otsCatalogo: OrdenTrabajoResponse[] = [];
  protected clienteSeleccionado = 0;
  protected vehiculoSeleccionado = 0;
  protected pagoSeleccionado: PagoResponse | null = null;

  // ── Pagos a empleados ────────────────────────────────────────────────────────
  protected rowsEmpleado: PagoEmpleadoResponse[] = [];
  protected loadingEmpleado = false;
  protected savingEmpleado = false;
  protected showFormEmpleado = false;
  protected empleadosCatalogo: EmpleadoResponse[] = [];
  protected pagoEmpleadoForm: PagoEmpleadoRequest = this.newPagoEmpleadoForm();

  ngOnInit(): void {
    this.cargar();
    this.cargarCatalogos();
  }

  // ── tab ──────────────────────────────────────────────────────────────────────
  protected cambiarTab(tab: 'clientes' | 'empleados'): void {
    this.tabPagos = tab;
    this.error = '';
    this.success = '';
    if (tab === 'empleados' && this.rowsEmpleado.length === 0) {
      this.cargarPagosEmpleado();
    }
  }

  // ── Pagos de clientes ────────────────────────────────────────────────────────
  protected toggleForm(): void {
    this.showForm = !this.showForm;
    this.error = '';
    this.success = '';
    if (!this.showForm) {
      this.form = this.newForm();
      this.clienteSeleccionado = 0;
      this.vehiculoSeleccionado = 0;
    }
  }

  protected guardar(): void {
    this.error = '';
    this.success = '';
    this.saving = true;

    this.api.registrarPago(this.form).subscribe({
      next: (pago) => {
        this.rows = [pago, ...this.rows];
        this.success = 'Pago registrado correctamente.';
        this.form = this.newForm();
        this.clienteSeleccionado = 0;
        this.vehiculoSeleccionado = 0;
        this.showForm = false;
        this.saving = false;
        this.cargar();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo registrar el pago.';
        this.saving = false;
      }
    });
  }

  protected onClienteChange(): void {
    this.vehiculoSeleccionado = 0;
    this.form.idOt = 0;
  }

  protected onVehiculoChange(): void {
    this.form.idOt = 0;
  }

  protected onOtChange(): void {
    const ot = this.otsCatalogo.find((x) => x.idOt === this.form.idOt);
    if (!ot) return;
    this.clienteSeleccionado = ot.idCliente;
    this.vehiculoSeleccionado = ot.idVehiculo;
  }

  protected vehiculosParaCliente(idCliente: number): VehiculoResponse[] {
    if (!idCliente || idCliente <= 0) return this.vehiculosCatalogo;
    return this.vehiculosCatalogo.filter((v) => v.idCliente === idCliente);
  }

  protected otsFiltradas(): OrdenTrabajoResponse[] {
    return this.otsCatalogo.filter((ot) => {
      if (this.clienteSeleccionado > 0 && ot.idCliente !== this.clienteSeleccionado) return false;
      if (this.vehiculoSeleccionado > 0 && ot.idVehiculo !== this.vehiculoSeleccionado) return false;
      return true;
    });
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

  protected otLabel(idOt: number): string {
    const ot = this.otsCatalogo.find((o) => o.idOt === idOt);
    if (!ot) return `OT-${String(idOt).padStart(4, '0')}`;
    const numero = ot.numeroOt?.trim() || `OT-${String(ot.idOt).padStart(4, '0')}`;
    return `${numero} - ${this.vehiculoLabel(ot.idVehiculo)}`;
  }

  protected abrirDetalle(row: PagoResponse): void {
    this.pagoSeleccionado = this.pagoSeleccionado?.idPago === row.idPago ? null : row;
  }

  protected async eliminar(row: PagoResponse): Promise<void> {
    this.error = '';
    this.success = '';
    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar pago',
      message: `Vas a borrar el pago #${row.idPago}.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) return;

    this.api.deletePago(row.idPago).subscribe({
      next: () => {
        this.rows = this.rows.filter((item) => item.idPago !== row.idPago);
        if (this.pagoSeleccionado?.idPago === row.idPago) this.pagoSeleccionado = null;
        this.cargar();
        this.success = 'Pago eliminado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el pago.';
      }
    });
  }

  protected aplicarFiltros(): void { this.cargar(); }

  protected limpiarFiltros(): void {
    this.filtros = this.newFiltros();
    this.cargar();
  }

  // ── Pagos a empleados ────────────────────────────────────────────────────────
  protected toggleFormEmpleado(): void {
    this.showFormEmpleado = !this.showFormEmpleado;
    this.error = '';
    this.success = '';
    if (!this.showFormEmpleado) {
      this.pagoEmpleadoForm = this.newPagoEmpleadoForm();
    }
  }

  protected guardarPagoEmpleado(): void {
    this.error = '';
    this.success = '';
    this.savingEmpleado = true;

    this.api.registrarPagoEmpleado(this.pagoEmpleadoForm).subscribe({
      next: (pago) => {
        this.rowsEmpleado = [pago, ...this.rowsEmpleado];
        this.success = 'Pago a empleado registrado.';
        this.pagoEmpleadoForm = this.newPagoEmpleadoForm();
        this.showFormEmpleado = false;
        this.savingEmpleado = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo registrar el pago.';
        this.savingEmpleado = false;
      }
    });
  }

  protected async eliminarPagoEmpleado(row: PagoEmpleadoResponse): Promise<void> {
    this.error = '';
    this.success = '';
    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar pago a empleado',
      message: `Vas a borrar el pago #${row.idPagoEmpleado}.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) return;

    this.api.deletePagoEmpleado(row.idPagoEmpleado).subscribe({
      next: () => {
        this.rowsEmpleado = this.rowsEmpleado.filter((r) => r.idPagoEmpleado !== row.idPagoEmpleado);
        this.success = 'Pago eliminado.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el pago.';
      }
    });
  }

  protected empleadoLabel(idEmpleado: number): string {
    const emp = this.empleadosCatalogo.find((e) => e.idEmpleado === idEmpleado);
    return emp ? `${emp.nombre} ${emp.apellido}` : `Empleado ${idEmpleado}`;
  }

  // ── private ──────────────────────────────────────────────────────────────────
  private cargar(): void {
    this.loading = true;
    this.api.listPagos(this.filtrosRequest()).subscribe({
      next: (data) => { this.rows = data; this.loading = false; },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los pagos.';
        this.loading = false;
      }
    });
  }

  private cargarPagosEmpleado(): void {
    this.loadingEmpleado = true;
    this.api.listPagosEmpleado().subscribe({
      next: (data) => { this.rowsEmpleado = data; this.loadingEmpleado = false; },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los pagos.';
        this.loadingEmpleado = false;
      }
    });
  }

  private cargarCatalogos(): void {
    this.api.listClientes().subscribe({ next: (data) => (this.clientesCatalogo = data) });
    this.api.listVehiculos().subscribe({ next: (data) => (this.vehiculosCatalogo = data) });
    this.api.listOrdenesTrabajo().subscribe({ next: (data) => (this.otsCatalogo = data) });
    this.api.listEmpleados().subscribe({ next: (data) => (this.empleadosCatalogo = data) });
  }

  private newForm(): PagoRequest {
    return { idOt: 0, monto: 0, metodoPago: 'EFECTIVO', fechaPago: null, observaciones: '' };
  }

  private newPagoEmpleadoForm(): PagoEmpleadoRequest {
    return {
      idEmpleado: 0,
      monto: 0,
      periodoDesde: null,
      periodoHasta: null,
      fechaPago: null,
      metodoPago: 'EFECTIVO',
      observaciones: '',
    };
  }

  private newFiltros(): PagoFilters {
    return { idCliente: 0, idVehiculo: 0, patente: '', desde: '', hasta: '' };
  }

  private filtrosRequest(): PagoFilters | undefined {
    const req: PagoFilters = {};
    if ((this.filtros.idCliente ?? 0) > 0) req.idCliente = this.filtros.idCliente;
    if ((this.filtros.idVehiculo ?? 0) > 0) req.idVehiculo = this.filtros.idVehiculo;
    if (this.filtros.patente?.trim()) req.patente = this.filtros.patente.trim();
    if (this.filtros.desde) req.desde = this.filtros.desde;
    if (this.filtros.hasta) req.hasta = this.filtros.hasta;
    return Object.keys(req).length ? req : undefined;
  }
}
