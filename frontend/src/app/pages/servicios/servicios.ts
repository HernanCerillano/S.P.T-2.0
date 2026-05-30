import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SptApiService, TipoServicioRequest, TipoServicioResponse } from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

@Component({
  selector: 'app-servicios',
  imports: [CommonModule, FormsModule],
  templateUrl: './servicios.html',
  styleUrl: './servicios.scss',
})
export class Servicios implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected rows: TipoServicioResponse[] = [];
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';
  protected filtroTexto = '';

  protected form: TipoServicioRequest = this.newForm();
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
      ? this.api.createServicio(this.form)
      : this.api.updateServicio(this.editingId, this.form);

    request$.subscribe({
      next: (servicio) => {
        if (this.editingId == null) {
          this.rows = [servicio, ...this.rows];
          this.success = 'Servicio creado correctamente.';
        } else {
          this.rows = this.rows.map((row) => (row.idServicio === servicio.idServicio ? servicio : row));
          this.success = 'Servicio actualizado correctamente.';
        }
        this.form = this.newForm();
        this.showForm = false;
        this.editingId = null;
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar el servicio.';
        this.saving = false;
      }
    });
  }

  protected editar(row: TipoServicioResponse): void {
    this.error = '';
    this.success = '';
    this.editingId = row.idServicio;
    this.form = {
      nombre: row.nombre,
      descripcion: row.descripcion,
      precioBase: Number(row.precioBase ?? 0),
      activo: row.activo
    };
    this.showForm = true;
  }

  protected async eliminar(row: TipoServicioResponse): Promise<void> {
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar servicio',
      message: `Vas a borrar el servicio ${row.nombre}. Se quitará tambien de presupuestos y ordenes donde aparezca.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.api.deleteServicio(row.idServicio).subscribe({
      next: () => {
        this.rows = this.rows.filter((item) => item.idServicio !== row.idServicio);
        if (this.editingId === row.idServicio) {
          this.form = this.newForm();
          this.editingId = null;
          this.showForm = false;
        }
        this.cargar();
        this.success = 'Servicio eliminado correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el servicio.';
      }
    });
  }

  protected rowsFiltradas(): TipoServicioResponse[] {
    const filtro = this.filtroTexto.trim().toLowerCase();
    if (!filtro) {
      return this.rows;
    }
    return this.rows.filter((row) =>
      `${row.nombre} ${row.descripcion} ${row.idServicio}`.toLowerCase().includes(filtro)
    );
  }

  private cargar(): void {
    this.loading = true;
    this.api.listServicios().subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar los servicios.';
        this.loading = false;
      }
    });
  }

  private newForm(): TipoServicioRequest {
    return {
      nombre: '',
      descripcion: '',
      precioBase: 0,
      activo: true
    };
  }
}
