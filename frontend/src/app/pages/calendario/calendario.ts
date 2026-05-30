import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  CalendarioItemDTO,
  ClienteResponse,
  EmpleadoResponse,
  EventoRequest,
  EventoResponse,
  SptApiService,
  TipoEvento,
  VehiculoResponse,
} from '../../core/spt-api.service';
import { UiConfirmService } from '../../core/ui-confirm.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-calendario',
  imports: [CommonModule, FormsModule],
  templateUrl: './calendario.html',
  styleUrl: './calendario.scss',
})
export class Calendario implements OnInit {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);

  protected readonly tiposEvento: TipoEvento[] = [
    'RECORDATORIO', 'BLOQUEO', 'MANTENIMIENTO', 'FERIADO', 'OTRO',
  ];

  protected items: CalendarioItemDTO[] = [];
  protected loading = false;
  protected saving = false;
  protected error = '';
  protected success = '';

  protected desde: string = this.primerDiaMes();
  protected hasta: string = this.ultimoDiaMes();

  protected showEventoForm = false;
  protected editandoEvento: EventoResponse | null = null;
  protected eventoForm: EventoRequest = this.newEventoForm();

  protected clientesCatalogo: ClienteResponse[] = [];
  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected empleadosCatalogo: EmpleadoResponse[] = [];
  protected catalogosCargados = false;

  ngOnInit(): void {
    this.cargar();
    this.cargarCatalogos();
  }

  protected cargar(): void {
    this.loading = true;
    this.error = '';
    this.api.getCalendario(this.desde, this.hasta).subscribe({
      next: (data) => {
        this.items = data.sort(
          (a, b) => new Date(a.fechaInicio).getTime() - new Date(b.fechaInicio).getTime()
        );
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo cargar el calendario.';
        this.loading = false;
      },
    });
  }

  protected toggleEventoForm(): void {
    this.showEventoForm = !this.showEventoForm;
    this.editandoEvento = null;
    this.eventoForm = this.newEventoForm();
    this.error = '';
    this.success = '';
  }

  protected guardarEvento(): void {
    this.error = '';
    this.success = '';
    this.saving = true;

    const request$ =
      this.editandoEvento == null
        ? this.api.createEvento(this.eventoForm)
        : this.api.updateEvento(this.editandoEvento.idEvento, this.eventoForm);

    request$.subscribe({
      next: () => {
        this.success = this.editandoEvento ? 'Evento actualizado.' : 'Evento creado.';
        this.showEventoForm = false;
        this.editandoEvento = null;
        this.eventoForm = this.newEventoForm();
        this.saving = false;
        this.cargar();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo guardar el evento.';
        this.saving = false;
      },
    });
  }

  protected editarEvento(item: CalendarioItemDTO): void {
    if (item.origen !== 'EVENTO') return;
    this.api.listEventos().subscribe({
      next: (eventos) => {
        const ev = eventos.find((e) => e.idEvento === item.id);
        if (!ev) return;
        this.editandoEvento = ev;
        this.eventoForm = {
          titulo: ev.titulo,
          descripcion: ev.descripcion ?? '',
          tipo: ev.tipo as TipoEvento,
          fechaInicio: ev.fechaInicio,
          fechaFin: ev.fechaFin,
          todoElDia: ev.todoElDia,
          color: ev.color ?? '',
          idEmpleado: ev.idEmpleado,
        };
        this.showEventoForm = true;
        this.error = '';
        this.success = '';
      },
    });
  }

  protected async eliminarEvento(item: CalendarioItemDTO): Promise<void> {
    if (item.origen !== 'EVENTO') return;
    this.error = '';
    this.success = '';
    const confirmado = await this.uiConfirm.ask({
      title: 'Borrar evento',
      message: `Vas a borrar "${item.titulo}".`,
      confirmText: 'Borrar',
      tone: 'danger',
    });
    if (!confirmado) return;

    this.api.deleteEvento(item.id).subscribe({
      next: () => {
        this.success = 'Evento eliminado.';
        this.cargar();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'No se pudo eliminar el evento.';
      },
    });
  }

  protected origenBadge(item: CalendarioItemDTO): string {
    return item.origen === 'CITA' ? 'badge badge-success' : 'badge badge-neutral';
  }

  protected clienteLabel(idCliente: number): string {
    const c = this.clientesCatalogo.find((cl) => cl.idCliente === idCliente);
    return c ? `${c.nombre} ${c.apellido}` : `Cliente ${idCliente}`;
  }

  protected vehiculoLabel(idVehiculo: number): string {
    const v = this.vehiculosCatalogo.find((vh) => vh.idVehiculo === idVehiculo);
    return v ? `${v.patente} - ${v.marca} ${v.modelo}` : `Vehiculo ${idVehiculo}`;
  }

  private cargarCatalogos(): void {
    if (this.catalogosCargados) return;
    forkJoin({
      clientes: this.api.listClientes(),
      vehiculos: this.api.listVehiculos(),
      empleados: this.api.listEmpleados(),
    }).subscribe({
      next: ({ clientes, vehiculos, empleados }) => {
        this.clientesCatalogo = clientes;
        this.vehiculosCatalogo = vehiculos;
        this.empleadosCatalogo = empleados;
        this.catalogosCargados = true;
      },
    });
  }

  private primerDiaMes(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`;
  }

  private ultimoDiaMes(): string {
    const d = new Date();
    const ultimo = new Date(d.getFullYear(), d.getMonth() + 1, 0);
    return `${ultimo.getFullYear()}-${String(ultimo.getMonth() + 1).padStart(2, '0')}-${String(ultimo.getDate()).padStart(2, '0')}`;
  }

  private newEventoForm(): EventoRequest {
    return {
      titulo: '',
      descripcion: '',
      tipo: 'OTRO',
      fechaInicio: '',
      fechaFin: null,
      todoElDia: false,
      color: '',
      idEmpleado: null,
    };
  }
}
