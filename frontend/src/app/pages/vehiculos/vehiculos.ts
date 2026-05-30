import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClienteResponse, SptApiService, VehiculoRequest, VehiculoResponse } from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

@Component({
  selector: 'app-vehiculos',
  imports: [CommonModule, FormsModule],
  templateUrl: './vehiculos.html',
  styleUrl: './vehiculos.scss',
})
export class Vehiculos implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected rows: VehiculoResponse[] = [];
  protected clientesCatalogo: ClienteResponse[] = [];
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';
  protected filtros = {
    texto: '',
    idCliente: 0,
  };

  protected form: VehiculoRequest = this.newForm();
  protected editingId: number | null = null;

  ngOnInit(): void {
    this.cargar();
    this.cargarClientes();
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

    const request$ = this.editingId == null
      ? this.api.createVehiculo(this.form)
      : this.api.updateVehiculo(this.editingId, this.form);

    request$.subscribe({
      next: (vehiculo) => {
        if (this.editingId == null) {
          this.rows = [vehiculo, ...this.rows];
          this.success = 'Vehiculo creado correctamente.';
        } else {
          this.rows = this.rows.map((row) => (row.idVehiculo === vehiculo.idVehiculo ? vehiculo : row));
          this.success = 'Vehiculo actualizado correctamente.';
        }
        this.form = this.newForm();
        this.showForm = false;
        this.editingId = null;
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar el vehiculo.';
        this.saving = false;
      }
    });
  }

  protected editar(row: VehiculoResponse): void {
    this.error = '';
    this.success = '';
    if (this.editingId === row.idVehiculo && this.showForm) {
      this.form = this.newForm();
      this.editingId = null;
      this.showForm = false;
      return;
    }
    this.editingId = row.idVehiculo;
    this.form = {
      idCliente: row.idCliente,
      patente: row.patente,
      marca: row.marca,
      modelo: row.modelo,
      anio: row.anio ?? null,
      kilometraje: row.kilometraje ?? null,
      activo: row.activo,
    };
    this.showForm = true;
  }

  protected async eliminar(row: VehiculoResponse): Promise<void> {
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar vehiculo',
      message: `Vas a borrar el vehiculo ${row.patente} - ${row.marca} ${row.modelo}. Esta accion elimina tambien presupuestos, OT y citas vinculadas.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.api.deleteVehiculo(row.idVehiculo).subscribe({
      next: () => {
        this.rows = this.rows.filter((item) => item.idVehiculo !== row.idVehiculo);
        if (this.editingId === row.idVehiculo) {
          this.form = this.newForm();
          this.editingId = null;
          this.showForm = false;
        }
        this.cargar();
        this.success = 'Vehiculo eliminado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el vehiculo.';
      }
    });
  }

  protected clienteLabel(idCliente: number): string {
    const cliente = this.clientesCatalogo.find((c) => c.idCliente === idCliente);
    return cliente ? `${cliente.idCliente} - ${cliente.nombre} ${cliente.apellido}` : `Cliente ${idCliente}`;
  }

  protected rowsFiltradas(): VehiculoResponse[] {
    const texto = this.filtros.texto.trim().toLowerCase();
    return this.rows.filter((row) => {
      if ((this.filtros.idCliente ?? 0) > 0 && row.idCliente !== this.filtros.idCliente) {
      return false;
      }
      if (!texto) {
        return true;
      }
      return `${row.patente} ${row.marca} ${row.modelo} ${row.anio ?? ''} ${row.kilometraje ?? ''} ${this.clienteLabel(row.idCliente)}`.toLowerCase().includes(texto);
    });
  }

  private cargar(): void {
    this.loading = true;
    this.api.listVehiculos().subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los vehiculos.';
        this.loading = false;
      }
    });
  }

  private cargarClientes(): void {
    this.api.listClientes().subscribe({
      next: (data) => {
        this.clientesCatalogo = data;
      }
    });
  }

  private newForm(): VehiculoRequest {
    return {
      idCliente: 0,
      patente: '',
      marca: '',
      modelo: '',
      anio: null,
      kilometraje: null,
      activo: true
    };
  }
}
