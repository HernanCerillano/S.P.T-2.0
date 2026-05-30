import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClienteRequest, ClienteResponse, SptApiService } from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

@Component({
  selector: 'app-clientes',
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.html',
  styleUrl: './clientes.scss',
})
export class Clientes implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected rows: ClienteResponse[] = [];
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';
  protected filtroTexto = '';

  protected form: ClienteRequest = this.newForm();
  protected editingId: number | null = null;

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

    const request$ = this.editingId == null
      ? this.api.createCliente(this.form)
      : this.api.updateCliente(this.editingId, this.form);

    request$.subscribe({
      next: (cliente) => {
        if (this.editingId == null) {
          this.rows = [cliente, ...this.rows];
          this.success = 'Cliente creado correctamente.';
        } else {
          this.rows = this.rows.map((row) => (row.idCliente === cliente.idCliente ? cliente : row));
          this.success = 'Cliente actualizado correctamente.';
        }
        this.form = this.newForm();
        this.showForm = false;
        this.editingId = null;
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar el cliente.';
        this.saving = false;
      }
    });
  }

  protected editar(row: ClienteResponse): void {
    this.error = '';
    this.success = '';
    if (this.editingId === row.idCliente && this.showForm) {
      this.form = this.newForm();
      this.editingId = null;
      this.showForm = false;
      return;
    }
    this.editingId = row.idCliente;
    this.form = {
      nombre: row.nombre,
      apellido: row.apellido,
      telefono: row.telefono,
      whatsapp: row.whatsapp ?? '',
      email: row.email,
      direccion: row.direccion,
      activo: row.activo,
    };
    this.showForm = true;
  }

  protected async eliminar(row: ClienteResponse): Promise<void> {
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar cliente',
      message: `Vas a borrar al cliente ${row.nombre} ${row.apellido}. Esta accion elimina tambien sus vehiculos, presupuestos, OT y citas vinculadas.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.api.deleteCliente(row.idCliente).subscribe({
      next: () => {
        this.rows = this.rows.filter((item) => item.idCliente !== row.idCliente);
        if (this.editingId === row.idCliente) {
          this.form = this.newForm();
          this.editingId = null;
          this.showForm = false;
        }
        this.cargar();
        this.success = 'Cliente eliminado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el cliente.';
      }
    });
  }

  protected rowsFiltradas(): ClienteResponse[] {
    const filtro = this.filtroTexto.trim().toLowerCase();
    if (!filtro) {
      return this.rows;
    }
    return this.rows.filter((row) =>
      `${row.nombre} ${row.apellido} ${row.telefono} ${row.whatsapp ?? ''} ${row.email} ${row.direccion}`.toLowerCase().includes(filtro)
    );
  }

  private cargar(): void {
    this.loading = true;
    this.api.listClientes().subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los clientes.';
        this.loading = false;
      }
    });
  }

  private newForm(): ClienteRequest {
    return {
      nombre: '',
      apellido: '',
      telefono: '',
      whatsapp: '',
      email: '',
      direccion: '',
      activo: true
    };
  }
}
