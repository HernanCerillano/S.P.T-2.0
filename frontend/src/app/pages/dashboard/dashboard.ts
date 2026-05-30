import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { CalendarioItemDTO, SptApiService } from '../../core/spt-api.service';
import { Citas } from '../citas/citas';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, Citas],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit, OnDestroy {
  private readonly api = inject(SptApiService);
  private citasSub?: Subscription;

  protected metrics = [
    { label: 'Presupuestos activos', value: '0' },
    { label: 'OT en proceso', value: '0' },
    { label: 'Citas', value: '0' },
  ];

  protected semanaOffset = 0;
  protected calendarItems: CalendarioItemDTO[] = [];
  protected calendarLoading = false;

  protected readonly DIAS = ['Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado', 'Domingo'];

  ngOnInit(): void {
    this.cargarResumen();
    this.cargarCalendario();
    // Re-load whenever a cita is created, modified, or deleted anywhere in the app
    this.citasSub = this.api.onCitasChanged().subscribe(() => {
      this.cargarResumen();
      this.cargarCalendario();
    });
  }

  ngOnDestroy(): void {
    this.citasSub?.unsubscribe();
  }

  protected semanaAnterior(): void {
    this.semanaOffset--;
    this.cargarCalendario();
  }

  protected semanaSiguiente(): void {
    this.semanaOffset++;
    this.cargarCalendario();
  }

  protected semanaActual(): void {
    this.semanaOffset = 0;
    this.cargarCalendario();
  }

  /** Returns the Monday of the week at `semanaOffset` weeks from today */
  protected lunesDeSemana(offset: number): Date {
    const hoy = new Date();
    const diaSemana = hoy.getDay(); // 0 Sun … 6 Sat
    const diffLunes = diaSemana === 0 ? -6 : 1 - diaSemana;
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() + diffLunes + offset * 7);
    lunes.setHours(0, 0, 0, 0);
    return lunes;
  }

  protected diasDeLaSemana(): Date[] {
    const lunes = this.lunesDeSemana(this.semanaOffset);
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(lunes);
      d.setDate(lunes.getDate() + i);
      return d;
    });
  }

  protected labelSemana(): string {
    const dias = this.diasDeLaSemana();
    const fmt = (d: Date) => `${d.getDate()}/${d.getMonth() + 1}`;
    return `${fmt(dias[0])} — ${fmt(dias[6])} ${dias[6].getFullYear()}`;
  }

  protected labelDia(d: Date): string {
    return `${this.DIAS[d.getDay() === 0 ? 6 : d.getDay() - 1]} ${d.getDate()}`;
  }

  protected esHoy(d: Date): boolean {
    const hoy = new Date();
    return (
      d.getDate() === hoy.getDate() &&
      d.getMonth() === hoy.getMonth() &&
      d.getFullYear() === hoy.getFullYear()
    );
  }

  protected itemsDelDia(d: Date): CalendarioItemDTO[] {
    const iso = this.toIsoDate(d);
    return this.calendarItems.filter((item) => item.fechaInicio.startsWith(iso));
  }

  protected itemClass(item: CalendarioItemDTO): string {
    return item.origen === 'CITA' ? 'cal-item cal-cita' : 'cal-item cal-evento';
  }

  private toIsoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private cargarCalendario(): void {
    this.calendarLoading = true;
    const dias = this.diasDeLaSemana();
    const desde = this.toIsoDate(dias[0]);
    const hasta = this.toIsoDate(dias[6]);
    this.api.getCalendario(desde, hasta).subscribe({
      next: (items) => {
        this.calendarItems = items;
        this.calendarLoading = false;
      },
      error: () => {
        this.calendarLoading = false;
      },
    });
  }

  private cargarResumen(): void {
    this.api.listPresupuestos({ estado: 'ACTIVO' }).subscribe({
      next: (presupuestos) => {
        this.metrics[0].value = String(presupuestos.length);
      },
    });

    this.api.listOrdenesTrabajo({ estado: 'EN_PROCESO' }).subscribe({
      next: (ots) => {
        this.metrics[1].value = String(ots.length);
      },
    });

    this.api.listCitas().subscribe({
      next: (citas) => {
        this.metrics[2].value = String(citas.length);
      },
    });
  }
}
