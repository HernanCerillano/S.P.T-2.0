import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PiezaRequest, PiezaResponse, SptApiService } from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

@Component({
  selector: 'app-piezas',
  imports: [CommonModule, FormsModule],
  templateUrl: './piezas.html',
  styleUrl: './piezas.scss',
})
export class Piezas implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected rows: PiezaResponse[] = [];
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';
  protected filtros = {
    texto: '',
  };

  protected form: PiezaRequest = this.newForm();
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
      ? this.api.createPieza(this.form)
      : this.api.updatePieza(this.editingId, this.form);

    request$.subscribe({
      next: (pieza) => {
        if (this.editingId == null) {
          this.rows = [pieza, ...this.rows];
          this.success = 'Pieza creada correctamente.';
        } else {
          this.rows = this.rows.map((row) => (row.idPieza === pieza.idPieza ? pieza : row));
          this.success = 'Pieza actualizada correctamente.';
        }
        this.form = this.newForm();
        this.showForm = false;
        this.editingId = null;
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar la pieza.';
        this.saving = false;
      }
    });
  }

  protected editar(row: PiezaResponse): void {
    this.error = '';
    this.success = '';
    this.editingId = row.idPieza;
    this.form = {
      nombre: row.nombre,
      marca: row.marca,
      medidas: row.medidas,
      calidad: row.calidad,
      precioUnitario: Number(row.precioUnitario ?? 0),
      activo: row.activo,
    };
    this.showForm = true;
  }

  protected async eliminar(row: PiezaResponse): Promise<void> {
    this.error = '';
    this.success = '';

    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar pieza',
      message: `Vas a borrar la pieza ${row.nombre} - ${row.marca}. Se quitará tambien de presupuestos y ordenes donde aparezca.`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.api.deletePieza(row.idPieza).subscribe({
      next: () => {
        this.rows = this.rows.filter((item) => item.idPieza !== row.idPieza);
        if (this.editingId === row.idPieza) {
          this.form = this.newForm();
          this.editingId = null;
          this.showForm = false;
        }
        this.cargar();
        this.success = 'Pieza eliminada correctamente.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar la pieza.';
      }
    });
  }

  protected rowsFiltradas(): PiezaResponse[] {
    const filtro = this.filtros.texto.trim().toLowerCase();
    if (!filtro) {
      return this.rows;
    }
    return this.rows.filter((row) =>
      `${row.nombre} ${row.marca} ${row.medidas} ${row.idPieza}`.toLowerCase().includes(filtro)
    );
  }

  private cargar(): void {
    this.loading = true;
    this.api.listPiezas().subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar las piezas.';
        this.loading = false;
      }
    });
  }

  private newForm(): PiezaRequest {
    return {
      nombre: '',
      marca: '',
      medidas: '',
      calidad: '',
      precioUnitario: 0,
      activo: true,
    };
  }
}
