import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import {
  ReporteGananciasDiarias,
  ReporteGananciasSemanales,
  ReporteGananciasMensuales,
  ReporteManoObra,
  ReporteMovimientoCaja,
  SptApiService,
} from '../../core/spt-api.service';

type TabReporte = 'diario' | 'semanal' | 'mensual' | 'mano-obra' | 'movimiento-caja';

@Component({
  selector: 'app-reportes',
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss',
})
export class Reportes implements OnInit {
  private readonly api = inject(SptApiService);

  protected tab: TabReporte = 'mensual';
  protected loading = false;
  protected error = '';

  protected diarias: ReporteGananciasDiarias[] = [];
  protected semanales: ReporteGananciasSemanales[] = [];
  protected mensuales: ReporteGananciasMensuales[] = [];
  protected manoObra: ReporteManoObra[] = [];
  protected movimientoCaja: ReporteMovimientoCaja[] = [];

  ngOnInit(): void {
    this.cargar();
  }

  protected cambiarTab(tab: TabReporte): void {
    this.tab = tab;
    this.cargar();
  }

  protected cargar(): void {
    this.loading = true;
    this.error = '';

    const obs: Observable<any[]> =
      this.tab === 'diario' ? this.api.getReporteGananciasDiarias() :
      this.tab === 'semanal' ? this.api.getReporteGananciasSemanales() :
      this.tab === 'mensual' ? this.api.getReporteGananciasMensuales() :
      this.tab === 'mano-obra' ? this.api.getReporteManoObra() :
      this.api.getReporteMovimientoCaja();

    obs.subscribe({
      next: (data: any[]) => {
        if (this.tab === 'diario') this.diarias = data;
        else if (this.tab === 'semanal') this.semanales = data;
        else if (this.tab === 'mensual') this.mensuales = data;
        else if (this.tab === 'mano-obra') this.manoObra = data;
        else this.movimientoCaja = data;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err?.error?.message ?? 'No se pudo cargar el reporte.';
        this.loading = false;
      },
    });
  }
}
