import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CitaFilters, CitaRequest, CitaResponse, ClienteResponse, SptApiService, TipoCita, VehiculoResponse } from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';
import { ClienteBuscador } from '../../components/cliente-buscador/cliente-buscador';

@Component({
  selector: 'app-citas',
  imports: [CommonModule, FormsModule, ClienteBuscador],
  templateUrl: './citas.html',
  styleUrl: './citas.scss',
})
export class Citas implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly tipos: TipoCita[] = ['ENTREGA', 'VERIFICACION', 'ADQUISICION'];
  protected rows: CitaResponse[] = [];
  protected filtros: CitaFilters = this.newFiltros();
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';

  protected form: CitaRequest = this.newForm();
  protected editingId: number | null = null;
  protected clientesCatalogo: ClienteResponse[] = [];
  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected clienteSeleccionadoAlta: ClienteResponse | null = null;

  ngOnInit(): void {
    this.cargar();
    this.cargarCatalogos();
  }

  protected toggleForm(): void {
    this.error = '';
    this.success = '';
    if (this.showForm) {
      this.showForm = false;
      this.form = this.newForm();
      this.clienteSeleccionadoAlta = null;
      return;
    }
    this.showForm = true;
    this.editingId = null;
    this.form = this.newForm();
    this.clienteSeleccionadoAlta = null;
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
    const isEditing = this.editingId != null;

    const request$ = !isEditing
      ? this.api.createCita(this.form)
      : this.api.updateCita(this.editingId!, this.form);

    request$.subscribe({
      next: (cita) => {
        if (!isEditing) {
          this.rows = [cita, ...this.rows];
          this.success = 'Cita creada correctamente.';
        } else {
          this.rows = this.rows.map((row) => (row.idCita === cita.idCita ? cita : row));
          this.success = 'Cita actualizada correctamente.';
        }
        this.form = this.newForm();
        this.showForm = false;
        this.editingId = null;
        this.saving = false;
        this.cargar();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar la cita.';
        this.saving = false;
      }
    });
  }

  protected editar(row: CitaResponse): void {
    this.error = '';
    this.success = '';
    this.showForm = false;
    if (this.editingId === row.idCita) {
      this.form = this.newForm();
      this.editingId = null;
      return;
    }
    this.editingId = row.idCita;
    this.form = {
      idCliente: row.idCliente,
      idVehiculo: row.idVehiculo,
      fechaHora: row.fechaHora?.slice(0, 16) ?? this.newForm().fechaHora,
      tipoCita: row.tipoCita,
      observaciones: row.observaciones ?? '',
    };
  }

  protected cancelarEdicion(): void {
    this.form = this.newForm();
    this.editingId = null;
  }

  protected async eliminar(row: CitaResponse): Promise<void> {
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar cita',
      message: `Vas a borrar la cita #${row.idCita}.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.api.deleteCita(row.idCita).subscribe({
      next: () => {
        this.rows = this.rows.filter((item) => item.idCita !== row.idCita);
        if (this.editingId === row.idCita) {
          this.form = this.newForm();
          this.editingId = null;
        }
        this.cargar();
        this.success = 'Cita eliminada correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar la cita.';
      }
    });
  }

  protected eliminarSeleccionada(): void {
    if (this.editingId == null) {
      return;
    }
    const row = this.rows.find((item) => item.idCita === this.editingId);
    if (!row) {
      return;
    }
    void this.eliminar(row);
  }

  protected vehiculosParaCliente(idCliente: number): VehiculoResponse[] {
    if (!idCliente || idCliente <= 0) {
      return this.vehiculosCatalogo;
    }
    return this.vehiculosCatalogo.filter((v) => v.idCliente === idCliente);
  }

  protected onClienteChange(): void {
    if (this.form.idVehiculo > 0) {
      const vehiculoPertenece = this.vehiculosCatalogo.some(
        (v) => v.idVehiculo === this.form.idVehiculo && v.idCliente === this.form.idCliente
      );
      if (!vehiculoPertenece) {
        this.form.idVehiculo = 0;
      }
    }
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

  protected tipoCitaLabel(tipo: TipoCita): string {
    return tipo === 'VERIFICACION' ? 'REVISION' : tipo;
  }

  protected aplicarFiltros(): void {
    this.cargar();
  }

  protected limpiarFiltros(): void {
    this.filtros = this.newFiltros();
    this.cargar();
  }

  private cargar(): void {
    this.loading = true;
    this.api.listCitas(this.filtrosRequest()).subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar las citas.';
        this.loading = false;
      }
    });
  }

  private cargarCatalogos(): void {
    this.api.listClientes().subscribe({ next: (data) => (this.clientesCatalogo = data) });
    this.api.listVehiculos().subscribe({ next: (data) => (this.vehiculosCatalogo = data) });
  }

  private newForm(): CitaRequest {
    const nextHour = new Date();
    nextHour.setMinutes(0, 0, 0);
    nextHour.setHours(nextHour.getHours() + 1);
    const isoLocal = new Date(nextHour.getTime() - nextHour.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16);

    return {
      idCliente: 0,
      idVehiculo: 0,
      fechaHora: isoLocal,
      tipoCita: 'VERIFICACION',
      observaciones: ''
    };
  }

  private newFiltros(): CitaFilters {
    return {
      idCliente: 0,
      idVehiculo: 0,
      patente: '',
      desde: '',
      hasta: '',
    };
  }

  private filtrosRequest(): CitaFilters | undefined {
    const req: CitaFilters = {};
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
    return Object.keys(req).length ? req : undefined;
  }
}
