import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClienteResponse, SptApiService } from '../../core/spt-api.service';

/**
 * Combobox reusable de clientes. Mismo patrón que el buscador de piezas/servicios
 * de Presupuestos: click sin escribir despliega la lista completa, tipear filtra
 * localmente (no llama al backend por cada tecla).
 *
 * Carga la lista entera de clientes una sola vez al iniciar (vía
 * {@link SptApiService.listClientes}).
 *
 * Uso:
 *   <app-cliente-buscador
 *     [seleccionado]="cliente"
 *     (seleccionadoChange)="onCliente($event)" />
 */
@Component({
  selector: 'app-cliente-buscador',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cliente-buscador.html',
  styleUrl: './cliente-buscador.scss',
})
export class ClienteBuscador implements OnInit {
  private readonly api = inject(SptApiService);

  @Input() set seleccionado(c: ClienteResponse | null) {
    this._seleccionado.set(c);
    this.query.set('');
  }
  get seleccionado(): ClienteResponse | null { return this._seleccionado(); }
  @Output() seleccionadoChange = new EventEmitter<ClienteResponse | null>();

  protected readonly _seleccionado = signal<ClienteResponse | null>(null);
  protected readonly clientesTotal = signal<ClienteResponse[]>([]);
  protected readonly query = signal('');
  protected readonly abierto = signal(false);

  /** Texto que se ve en el input: query si el operador está tipeando, sino el seleccionado. */
  protected readonly textoMostrado = computed(() => {
    const q = this.query();
    if (q !== '') return q;
    const s = this._seleccionado();
    return s ? `${s.apellido ?? ''} ${s.nombre ?? ''}`.trim() : '';
  });

  /** Lista filtrada localmente (LIKE %q% en nombre o apellido). */
  protected readonly clientesFiltrados = computed(() => {
    const q = this.query().trim().toLowerCase();
    const todos = this.clientesTotal();
    if (q === '') return todos;
    return todos.filter((c) =>
      (c.nombre ?? '').toLowerCase().includes(q) ||
      (c.apellido ?? '').toLowerCase().includes(q)
    );
  });

  ngOnInit(): void {
    this.api.listClientes().subscribe({
      next: (rows) => {
        const activos = rows.filter((c) => c.activo);
        activos.sort((a, b) => {
          const apA = (a.apellido ?? '').toLowerCase();
          const apB = (b.apellido ?? '').toLowerCase();
          if (apA !== apB) return apA.localeCompare(apB);
          return (a.nombre ?? '').toLowerCase().localeCompare((b.nombre ?? '').toLowerCase());
        });
        this.clientesTotal.set(activos);
      },
      error: () => this.clientesTotal.set([]),
    });
  }

  protected onInput(e: Event): void {
    const v = (e.target as HTMLInputElement).value;
    this.query.set(v);
    this.abierto.set(true);
    // Si había selección previa y el operador empieza a tipear, la limpiamos.
    if (this._seleccionado() != null) {
      this._seleccionado.set(null);
      this.seleccionadoChange.emit(null);
    }
  }

  protected abrir(): void { this.abierto.set(true); }

  /** Cierra con micro-delay para dar tiempo al click/mousedown sobre una opción. */
  protected cerrarConDelay(): void {
    setTimeout(() => this.abierto.set(false), 120);
  }

  protected toggleBoton(): void {
    if (this._seleccionado() != null) {
      this.limpiar();
      return;
    }
    this.abierto.update((v) => !v);
  }

  protected seleccionar(c: ClienteResponse): void {
    this._seleccionado.set(c);
    this.query.set('');
    this.abierto.set(false);
    this.seleccionadoChange.emit(c);
  }

  protected limpiar(): void {
    this._seleccionado.set(null);
    this.query.set('');
    this.abierto.set(false);
    this.seleccionadoChange.emit(null);
  }
}
