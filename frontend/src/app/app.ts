import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CitaResponse, ClienteResponse, SptApiService, VehiculoResponse } from './core/spt-api.service';
import { UiConfirmService } from './core/ui-confirm.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit, OnDestroy {
  private readonly api = inject(SptApiService);
  private readonly uiConfirm = inject(UiConfirmService);
  private readonly storageKey = 'spt-citas-vistas';
  private refreshTimer: number | null = null;

  protected readonly navItems = [
    { label: 'Dashboard', path: '/dashboard', emoji: 'D' },
    { label: 'Clientes', path: '/clientes', emoji: 'C' },
    { label: 'Vehiculos', path: '/vehiculos', emoji: 'V' },
    { label: 'Piezas', path: '/piezas', emoji: 'P' },
    { label: 'Servicios', path: '/servicios', emoji: 'S' },
    { label: 'Presupuestos', path: '/presupuestos', emoji: 'PR' },
    { label: 'Ordenes de trabajo', path: '/ordenes-trabajo', emoji: 'OT' },
    { label: 'Pagos', path: '/pagos', emoji: '$' },
    { label: 'Empleados', path: '/empleados', emoji: 'EM' },
    { label: 'Reportes', path: '/reportes', emoji: 'RE' },
    { label: 'Configuracion', path: '/configuracion', emoji: 'CF' }
  ];

  protected notificaciones: CitaResponse[] = [];
  protected readonly confirmState = this.uiConfirm.state;
  protected clientesCatalogo: ClienteResponse[] = [];
  protected vehiculosCatalogo: VehiculoResponse[] = [];
  protected activacionCargando = true;
  protected activado = true;
  protected activationKey = '';
  protected activationError = '';

  ngOnInit(): void {
    this.cargarCatalogos();
    void this.inicializarAplicacion();
  }

  ngOnDestroy(): void {
    if (this.refreshTimer != null) {
      window.clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  protected marcarNotificacionVista(idCita: number): void {
    const vistas = this.obtenerVistas();
    if (!vistas.includes(idCita)) {
      vistas.push(idCita);
      localStorage.setItem(this.storageKey, JSON.stringify(vistas));
    }
    this.notificaciones = this.notificaciones.filter((item) => item.idCita !== idCita);
  }

  protected notificacionTexto(cita: CitaResponse): string {
    const fecha = cita.fechaHora ? new Date(cita.fechaHora) : null;
    const fechaLabel = fecha ? fecha.toLocaleString() : 'sin fecha';
    return `Cita #${cita.idCita} - ${fechaLabel}`;
  }

  protected notificacionDetalle(cita: CitaResponse): string {
    return `${this.clienteLabel(cita.idCliente)} - ${this.vehiculoLabel(cita.idVehiculo)}`;
  }

  protected async activarAplicacion(): Promise<void> {
    this.activationError = '';
    if (!this.activationKey.trim()) {
      this.activationError = 'Ingresa la clave de activacion.';
      return;
    }

    try {
      const ok = await this.invokeTauri<boolean>('activate_app', { key: this.activationKey.trim() });
      if (!ok) {
        this.activationError = 'No se pudo activar la aplicacion.';
        return;
      }
      this.activado = true;
      this.activationKey = '';
      this.iniciarNotificaciones();
    } catch (error) {
      this.activationError = error instanceof Error ? error.message : 'Clave de activacion invalida.';
    }
  }

  protected confirmarDialogo(): void {
    this.uiConfirm.confirm();
  }

  protected cancelarDialogo(): void {
    this.uiConfirm.cancel();
  }

  private async inicializarAplicacion(): Promise<void> {
    if (!this.esEntornoTauri()) {
      this.activado = true;
      this.activacionCargando = false;
      this.iniciarNotificaciones();
      return;
    }

    try {
      this.activado = await this.invokeTauri<boolean>('activation_status');
    } catch {
      this.activado = false;
      this.activationError = 'No se pudo validar la licencia local.';
    } finally {
      this.activacionCargando = false;
    }

    if (this.activado) {
      this.iniciarNotificaciones();
    }
  }

  private iniciarNotificaciones(): void {
    this.cargarNotificaciones();
    if (this.refreshTimer == null) {
      this.refreshTimer = window.setInterval(() => this.cargarNotificaciones(), 60000);
    }
  }

  private cargarNotificaciones(): void {
    this.api.listCitas().subscribe({
      next: (citas) => {
        const ahora = new Date();
        const limite = new Date(ahora.getTime() + 24 * 60 * 60 * 1000);
        const vistas = this.obtenerVistas();
        this.notificaciones = citas
          .filter((cita) => !vistas.includes(cita.idCita))
          .filter((cita) => {
            if (!cita.fechaHora) {
              return false;
            }
            const fecha = new Date(cita.fechaHora);
            return fecha >= ahora && fecha <= limite;
          })
          .sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime());
      },
      error: () => {
        this.notificaciones = [];
      }
    });
  }

  private cargarCatalogos(): void {
    this.api.listClientes().subscribe({
      next: (clientes) => {
        this.clientesCatalogo = clientes;
      }
    });
    this.api.listVehiculos().subscribe({
      next: (vehiculos) => {
        this.vehiculosCatalogo = vehiculos;
      }
    });
  }

  private clienteLabel(idCliente: number): string {
    const cliente = this.clientesCatalogo.find((item) => item.idCliente === idCliente);
    return cliente ? `${cliente.nombre} ${cliente.apellido}`.trim() : `Cliente ${idCliente}`;
  }

  private vehiculoLabel(idVehiculo: number): string {
    const vehiculo = this.vehiculosCatalogo.find((item) => item.idVehiculo === idVehiculo);
    return vehiculo
      ? `${vehiculo.patente} - ${vehiculo.marca} ${vehiculo.modelo}`.trim()
      : `Vehiculo ${idVehiculo}`;
  }

  private obtenerVistas(): number[] {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) {
        return [];
      }
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed.map((value) => Number(value)).filter((value) => !Number.isNaN(value)) : [];
    } catch {
      return [];
    }
  }

  private esEntornoTauri(): boolean {
    const scope = window as Window & { __TAURI__?: unknown; __TAURI_INTERNALS__?: unknown };
    return typeof scope.__TAURI__ !== 'undefined' || typeof scope.__TAURI_INTERNALS__ !== 'undefined';
  }

  private async invokeTauri<T>(command: string, args?: Record<string, unknown>): Promise<T> {
    const { invoke } = await import('@tauri-apps/api/core');
    return invoke<T>(command, args);
  }
}
