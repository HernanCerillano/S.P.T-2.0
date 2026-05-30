import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  EmpleadoRequest,
  EmpleadoResponse,
  MetodoPago,
  PagoEmpleadoRequest,
  PagoEmpleadoResponse,
  SptApiService,
} from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

@Component({
  selector: 'app-empleados',
  imports: [CommonModule, FormsModule],
  templateUrl: './empleados.html',
  styleUrl: './empleados.scss',
})
export class Empleados implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly metodosPago: MetodoPago[] = ['EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'CHEQUE'];

  protected rows: EmpleadoResponse[] = [];
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';
  protected filtroTexto = '';

  protected form: EmpleadoRequest = this.newForm();
  protected editingId: number | null = null;

  // Panel de pagos por empleado
  protected empleadoSeleccionado: EmpleadoResponse | null = null;
  protected pagosRows: PagoEmpleadoResponse[] = [];
  protected pagosLoading = false;
  protected showPagoForm = false;
  protected pagoForm: PagoEmpleadoRequest = this.newPagoForm();
  protected pagoSaving = false;

  ngOnInit(): void {
    this.cargar();
  }

  protected toggleForm(): void {
    this.showForm = !this.showForm;
    this.error = '';
    this.success = '';
    if (!this.showForm) {
      this.form = this.newForm();
      this.editingId = null;
    }
  }

  protected guardar(): void {
    this.error = '';
    this.success = '';
    this.saving = true;

    const request$ =
      this.editingId == null
        ? this.api.createEmpleado(this.form)
        : this.api.updateEmpleado(this.editingId, this.form);

    request$.subscribe({
      next: (empleado) => {
        if (this.editingId == null) {
          this.rows = [empleado, ...this.rows];
          this.success = 'Empleado creado correctamente.';
        } else {
          this.rows = this.rows.map((r) => (r.idEmpleado === empleado.idEmpleado ? empleado : r));
          if (this.empleadoSeleccionado?.idEmpleado === empleado.idEmpleado) {
            this.empleadoSeleccionado = empleado;
          }
          this.success = 'Empleado actualizado correctamente.';
        }
        this.form = this.newForm();
        this.showForm = false;
        this.editingId = null;
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar el empleado.';
        this.saving = false;
      },
    });
  }

  protected editar(row: EmpleadoResponse): void {
    this.error = '';
    this.success = '';
    if (this.editingId === row.idEmpleado && this.showForm) {
      this.form = this.newForm();
      this.editingId = null;
      this.showForm = false;
      return;
    }
    this.editingId = row.idEmpleado;
    this.form = {
      nombre: row.nombre,
      apellido: row.apellido,
      dni: row.dni ?? '',
      telefono: row.telefono ?? '',
      email: row.email ?? '',
      rolTaller: row.rolTaller ?? '',
      sueldoBase: row.sueldoBase,
      activo: row.activo,
    };
    this.showForm = true;
  }

  protected async eliminar(row: EmpleadoResponse): Promise<void> {
    this.error = '';
    this.success = '';
    const confirmado = await this.uiConfirm.ask({
      title: 'Dar de baja empleado',
      message: `Vas a eliminar a ${row.nombre} ${row.apellido}. Esta accion no se puede deshacer.`,
      confirmText: 'Eliminar',
      tone: 'danger',
    });
    if (!confirmado) return;

    this.api.deleteEmpleado(row.idEmpleado).subscribe({
      next: () => {
        this.rows = this.rows.filter((r) => r.idEmpleado !== row.idEmpleado);
        if (this.empleadoSeleccionado?.idEmpleado === row.idEmpleado) {
          this.empleadoSeleccionado = null;
          this.pagosRows = [];
        }
        this.success = 'Empleado eliminado.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el empleado.';
      },
    });
  }

  protected abrirPagos(row: EmpleadoResponse): void {
    if (this.empleadoSeleccionado?.idEmpleado === row.idEmpleado) {
      this.empleadoSeleccionado = null;
      this.pagosRows = [];
      this.showPagoForm = false;
      return;
    }
    this.empleadoSeleccionado = row;
    this.showPagoForm = false;
    this.pagoForm = this.newPagoForm();
    this.cargarPagos(row.idEmpleado);
  }

  protected togglePagoForm(): void {
    this.showPagoForm = !this.showPagoForm;
    if (!this.showPagoForm) {
      this.pagoForm = this.newPagoForm();
    }
  }

  protected registrarPago(): void {
    if (!this.empleadoSeleccionado) return;
    this.error = '';
    this.success = '';
    this.pagoSaving = true;
    const payload = { ...this.pagoForm, idEmpleado: this.empleadoSeleccionado.idEmpleado };

    this.api.registrarPagoEmpleado(payload).subscribe({
      next: (pago) => {
        this.pagosRows = [pago, ...this.pagosRows];
        this.showPagoForm = false;
        this.pagoForm = this.newPagoForm();
        this.pagoSaving = false;
        this.success = 'Pago registrado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo registrar el pago.';
        this.pagoSaving = false;
      },
    });
  }

  protected async eliminarPago(pago: PagoEmpleadoResponse): Promise<void> {
    this.error = '';
    this.success = '';
    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar pago',
      message: `Vas a borrar el pago #${pago.idPagoEmpleado} por $${pago.monto}.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) return;

    this.api.deletePagoEmpleado(pago.idPagoEmpleado).subscribe({
      next: () => {
        this.pagosRows = this.pagosRows.filter((p) => p.idPagoEmpleado !== pago.idPagoEmpleado);
        this.success = 'Pago eliminado.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el pago.';
      },
    });
  }

  protected rowsFiltradas(): EmpleadoResponse[] {
    const f = this.filtroTexto.trim().toLowerCase();
    if (!f) return this.rows;
    return this.rows.filter((r) =>
      `${r.nombre} ${r.apellido} ${r.dni ?? ''} ${r.rolTaller ?? ''}`.toLowerCase().includes(f)
    );
  }

  protected empleadoLabel(row: EmpleadoResponse): string {
    return `${row.nombre} ${row.apellido}`;
  }

  private cargar(): void {
    this.loading = true;
    this.api.listEmpleados().subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los empleados.';
        this.loading = false;
      },
    });
  }

  private cargarPagos(idEmpleado: number): void {
    this.pagosLoading = true;
    this.api.listPagosEmpleado(idEmpleado).subscribe({
      next: (data) => {
        this.pagosRows = data;
        this.pagosLoading = false;
      },
      error: () => {
        this.pagosRows = [];
        this.pagosLoading = false;
      },
    });
  }

  private newForm(): EmpleadoRequest {
    return {
      nombre: '',
      apellido: '',
      dni: '',
      telefono: '',
      email: '',
      rolTaller: '',
      sueldoBase: null,
      activo: true,
    };
  }

  private newPagoForm(): PagoEmpleadoRequest {
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
}
