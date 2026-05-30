import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  EstadoFactura,
  FacturaDesdeOrigenRequest,
  FacturaResponse,
  OrdenTrabajoResponse,
  PresupuestoResponse,
  SptApiService,
  TipoFactura,
} from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';

type OrigenFactura = 'PRESUPUESTO' | 'OT';

@Component({
  selector: 'app-facturas',
  imports: [CommonModule, FormsModule],
  templateUrl: './facturas.html',
  styleUrl: './facturas.scss',
})
export class Facturas implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly tiposFactura: TipoFactura[] = ['A', 'B', 'C', 'X', 'OTRO'];
  protected readonly origenes: OrigenFactura[] = ['PRESUPUESTO', 'OT'];

  protected rows: FacturaResponse[] = [];
  protected loading = false;
  protected saving = false;
  protected showForm = false;
  protected error = '';
  protected success = '';
  protected downloadingPdf = false;

  protected origenSeleccionado: OrigenFactura = 'OT';
  protected form: FacturaDesdeOrigenRequest = this.newForm();
  protected idOrigenSeleccionado: number = 0;
  protected proximoNumero = '';

  protected presupuestasCatalogo: PresupuestoResponse[] = [];
  protected otsCatalogo: OrdenTrabajoResponse[] = [];
  protected catalogosCargados = false;

  protected filtroEstado: EstadoFactura | '' = '';
  protected filtroTexto = '';

  ngOnInit(): void {
    this.cargar();
  }

  protected toggleForm(): void {
    this.showForm = !this.showForm;
    this.error = '';
    this.success = '';
    if (this.showForm && !this.catalogosCargados) {
      this.cargarCatalogos();
    }
    if (this.showForm) {
      this.cargarProximoNumero();
    }
    if (!this.showForm) {
      this.form = this.newForm();
      this.idOrigenSeleccionado = 0;
    }
  }

  private cargarProximoNumero(): void {
    this.api.proximoNumeroFactura().subscribe({
      next: (resp) => {
        this.proximoNumero = resp.numero;
        // El backend respeta lo que viene; aún así reflejamos el valor en el form para auditar.
        this.form.numeroFactura = resp.numero;
      },
      error: () => { /* si falla, el backend igual auto-asigna al guardar */ },
    });
  }

  protected guardar(): void {
    if (this.idOrigenSeleccionado <= 0) {
      this.error = 'Selecciona un presupuesto u OT de origen.';
      return;
    }
    this.error = '';
    this.success = '';
    this.saving = true;

    const request$ =
      this.origenSeleccionado === 'PRESUPUESTO'
        ? this.api.crearFacturaDesdePresupuesto(this.idOrigenSeleccionado, this.form)
        : this.api.crearFacturaDesdeOt(this.idOrigenSeleccionado, this.form);

    request$.subscribe({
      next: (factura) => {
        this.rows = [factura, ...this.rows];
        this.success = `Factura ${factura.numeroFactura} creada correctamente.`;
        this.form = this.newForm();
        this.idOrigenSeleccionado = 0;
        this.showForm = false;
        this.saving = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo crear la factura.';
        this.saving = false;
      },
    });
  }

  protected async anular(row: FacturaResponse): Promise<void> {
    this.error = '';
    this.success = '';
    const confirmado = await this.uiConfirm.ask({
      title: 'Anular factura',
      message: `Vas a anular la factura ${row.numeroFactura}. Esta accion no se puede deshacer.`,
      confirmText: 'Anular',
      tone: 'danger',
    });
    if (!confirmado) return;

    this.api.anularFactura(row.idFactura).subscribe({
      next: (updated) => {
        this.rows = this.rows.map((r) => (r.idFactura === updated.idFactura ? updated : r));
        this.success = 'Factura anulada.';
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo anular la factura.';
      },
    });
  }

  protected descargarPdf(row: FacturaResponse): void {
    if (this.downloadingPdf) return;
    this.downloadingPdf = true;
    this.error = '';
    this.api.downloadFacturaPdf(row.idFactura).subscribe({
      next: async (blob) => {
        try {
          await this.descargarBlob(blob, `Factura-${row.numeroFactura}.pdf`);
        } finally {
          this.downloadingPdf = false;
        }
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo generar el PDF.';
        this.downloadingPdf = false;
      },
    });
  }

  protected rowsFiltradas(): FacturaResponse[] {
    let data = this.rows;
    if (this.filtroEstado) {
      data = data.filter((r) => r.estado === this.filtroEstado);
    }
    const f = this.filtroTexto.trim().toLowerCase();
    if (f) {
      data = data.filter((r) =>
        `${r.numeroFactura} ${r.tipoFactura} ${r.estado}`.toLowerCase().includes(f)
      );
    }
    return data;
  }

  protected estadoBadgeClass(estado: EstadoFactura): string {
    return estado === 'ANULADA' ? 'badge badge-danger' : 'badge badge-success';
  }

  private cargar(): void {
    this.loading = true;
    this.api.listFacturas().subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudieron cargar las facturas.';
        this.loading = false;
      },
    });
  }

  private cargarCatalogos(): void {
    forkJoin({
      presupuestos: this.api.listPresupuestos(),
      ots: this.api.listOrdenesTrabajo(),
    }).subscribe({
      next: ({ presupuestos, ots }) => {
        this.presupuestasCatalogo = presupuestos;
        this.otsCatalogo = ots;
        this.catalogosCargados = true;
      },
    });
  }

  private newForm(): FacturaDesdeOrigenRequest {
    return {
      numeroFactura: '',
      tipoFactura: 'B',
      impuestos: 0,
      observaciones: '',
    };
  }

  private async descargarBlob(blob: Blob, filename: string): Promise<void> {
    try {
      const [{ save }, { writeFile }] = await Promise.all([
        import('@tauri-apps/plugin-dialog'),
        import('@tauri-apps/plugin-fs'),
      ]);
      const selectedPath = await save({
        defaultPath: filename,
        filters: [{ name: 'PDF', extensions: ['pdf'] }],
      });
      if (typeof selectedPath === 'string' && selectedPath) {
        await writeFile(selectedPath, new Uint8Array(await blob.arrayBuffer()));
        this.success = 'PDF guardado.';
        return;
      }
      if (selectedPath === null) return;
    } catch { /* no Tauri, continua */ }

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
