import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject, retry, tap } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Enums ──────────────────────────────────────────────────────────────────────
export type EstadoPresupuesto = 'ACTIVO' | 'ACEPTADO' | 'ARCHIVADO';
export type EstadoOT = 'EN_ESPERA' | 'EN_PROCESO' | 'FINALIZADA_COMPLETA' | 'FINALIZADA_INCOMPLETA';
export type TipoItemDetalle = 'PIEZA' | 'SERVICIO';
export type MetodoPago = 'EFECTIVO' | 'TRANSFERENCIA' | 'TARJETA' | 'CHEQUE';
export type TipoCita = 'ENTREGA' | 'VERIFICACION' | 'ADQUISICION';
export type RolUsuario = 'ADMIN' | 'OPERADOR';
export type TipoFactura = 'A' | 'B' | 'C' | 'X' | 'OTRO';
export type EstadoFactura = 'EMITIDA' | 'ANULADA';
export type TipoEvento = 'RECORDATORIO' | 'BLOQUEO' | 'MANTENIMIENTO' | 'FERIADO' | 'OTRO';
export type EstadoMensaje = 'PENDIENTE' | 'ENVIADO' | 'FALLIDO' | 'CANCELADO';

// ── Filtros ────────────────────────────────────────────────────────────────────
export interface PresupuestoFilters {
  estado?: EstadoPresupuesto | '';
  patente?: string;
}

export interface OrdenTrabajoFilters {
  numeroOt?: string;
  idCliente?: number;
  idVehiculo?: number;
  patente?: string;
  desde?: string;
  hasta?: string;
  estado?: EstadoOT;
}

export interface PagoFilters {
  idCliente?: number;
  idVehiculo?: number;
  patente?: string;
  desde?: string;
  hasta?: string;
}

export interface CitaFilters {
  idCliente?: number;
  idVehiculo?: number;
  patente?: string;
  desde?: string;
  hasta?: string;
}

// ── Cliente ────────────────────────────────────────────────────────────────────
export interface ClienteRequest {
  nombre: string;
  apellido: string;
  telefono: string;
  whatsapp: string;
  email: string;
  direccion: string;
  activo: boolean;
}

export interface ClienteResponse extends ClienteRequest {
  idCliente: number;
  saldoPendiente?: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── Vehiculo ───────────────────────────────────────────────────────────────────
export interface VehiculoRequest {
  idCliente: number;
  patente: string;
  marca: string;
  modelo: string;
  anio: number | null;
  kilometraje: number | null;
  activo: boolean;
}

export interface VehiculoResponse extends VehiculoRequest {
  idVehiculo: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── Pieza ──────────────────────────────────────────────────────────────────────
export interface PiezaRequest {
  nombre: string;
  marca: string;
  medidas: string;
  calidad: string;
  precioUnitario: number;
  activo: boolean;
}

export interface PiezaResponse extends PiezaRequest {
  idPieza: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── TipoServicio ───────────────────────────────────────────────────────────────
export interface TipoServicioRequest {
  nombre: string;
  descripcion: string;
  precioBase: number;
  activo: boolean;
}

export interface TipoServicioResponse extends TipoServicioRequest {
  idServicio: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── Presupuesto ────────────────────────────────────────────────────────────────
export interface PresupuestoRequest {
  numeroPresupuesto: string;
  idCliente: number;
  idVehiculo: number;
  resumen: string;
  total: number;
  precioPersonalizado: number | null;
  estado: EstadoPresupuesto;
  ocultarPreciosItems: boolean;
}

export interface PresupuestoResponse extends PresupuestoRequest {
  idPresupuesto: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

export interface PresupuestoDetalleRequest {
  idPresupuesto: number | null;
  tipoItem: TipoItemDetalle;
  idPieza: number | null;
  idServicio: number | null;
  descripcionItem: string;
  cantidad: number;
  precioUnitario: number | null;
  subtotal: number | null;
}

export interface PresupuestoDetalleResponse extends PresupuestoDetalleRequest {
  idDetallePresupuesto: number;
}

// ── OrdenTrabajo ───────────────────────────────────────────────────────────────
export interface OrdenTrabajoRequest {
  numeroOt: string;
  idPresupuesto: number | null;
  idCliente: number;
  idVehiculo: number;
  resumenTrabajo: string;
  estado: EstadoOT;
  total: number;
  precioPersonalizado: number | null;
  ocultarPreciosItems: boolean;
  fechaFinalizacion: string | null;
}

export interface OrdenTrabajoResponse extends OrdenTrabajoRequest {
  idOt: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

export interface OTDetalleRequest {
  idOt: number | null;
  tipoItem: TipoItemDetalle;
  idPieza: number | null;
  idServicio: number | null;
  descripcionItem: string;
  cantidad: number;
  precioUnitario: number | null;
  subtotal: number | null;
}

export interface OTDetalleResponse extends OTDetalleRequest {
  idDetalleOt: number;
}

// ── Pago (cliente) ─────────────────────────────────────────────────────────────
export interface PagoRequest {
  idOt: number;
  monto: number;
  metodoPago: MetodoPago;
  fechaPago: string | null;
  observaciones: string;
}

export interface PagoResponse extends PagoRequest {
  idPago: number;
}

// ── Cita ───────────────────────────────────────────────────────────────────────
export interface CitaRequest {
  idCliente: number;
  idVehiculo: number;
  fechaHora: string;
  tipoCita: TipoCita;
  observaciones: string;
}

export interface CitaResponse extends CitaRequest {
  idCita: number;
  fechaCreacion?: string;
}

// ── Empleado ───────────────────────────────────────────────────────────────────
export interface EmpleadoRequest {
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  email: string;
  rolTaller: string;
  sueldoBase: number | null;
  activo: boolean;
}

export interface EmpleadoResponse extends EmpleadoRequest {
  idEmpleado: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── PagoEmpleado ───────────────────────────────────────────────────────────────
export interface PagoEmpleadoRequest {
  idEmpleado: number;
  monto: number;
  periodoDesde: string | null;
  periodoHasta: string | null;
  fechaPago: string | null;
  metodoPago: MetodoPago;
  observaciones: string;
}

export interface PagoEmpleadoResponse extends PagoEmpleadoRequest {
  idPagoEmpleado: number;
  fechaCreacion?: string;
}

// ── OtEmpleado (mano de obra) ──────────────────────────────────────────────────
export interface OtEmpleadoRequest {
  idEmpleado: number;
  costoManoObra: number;
  horasTrabajadas: number | null;
  observaciones: string;
}

export interface OtEmpleadoResponse extends OtEmpleadoRequest {
  idOtEmpleado: number;
  idOt: number;
  fechaCreacion?: string;
}

// ── Factura ────────────────────────────────────────────────────────────────────
export interface FacturaDesdeOrigenRequest {
  numeroFactura: string;
  tipoFactura: TipoFactura;
  impuestos: number;
  observaciones: string;
}

export interface FacturaResponse {
  idFactura: number;
  numeroFactura: string;
  tipoFactura: TipoFactura;
  idCliente: number;
  idPresupuesto: number | null;
  idOt: number | null;
  subtotal: number;
  impuestos: number;
  total: number;
  estado: EstadoFactura;
  fechaEmision: string;
  observaciones: string;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── Calendario ─────────────────────────────────────────────────────────────────
export type CalendarioOrigen = 'CITA' | 'EVENTO';

export interface CalendarioItemDTO {
  id: number;
  origen: CalendarioOrigen;
  titulo: string;
  descripcion: string;
  fechaInicio: string;
  fechaFin: string | null;
  todoElDia: boolean;
  color: string | null;
  tipo: string;
}

export interface EventoRequest {
  titulo: string;
  descripcion: string;
  tipo: TipoEvento;
  fechaInicio: string;
  fechaFin: string | null;
  todoElDia: boolean;
  color: string;
  idEmpleado: number | null;
}

export interface EventoResponse extends EventoRequest {
  idEvento: number;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// ── Configuracion ──────────────────────────────────────────────────────────────
export interface ConfiguracionTallerRequest {
  nombreTaller: string;
  telefono: string;
  direccion: string;
  email: string;
  cuit: string;
  logoPath: string;
  plantillaWpRecordatorio: string;
  horasAnticipacionWp: number;
  wpHabilitado: boolean;
}

export interface ConfiguracionTallerResponse extends ConfiguracionTallerRequest {
  fechaModificacion?: string;
}

// ── Configuracion WhatsApp (auto-respuestas / opt-out) ───────────────────────────
// templateRecordatorioBody es un proxy a configuracion_taller.plantilla_wp_recordatorio
// (NO está en la tabla configuracion_whatsapp). El estado es cosmético (siempre "APROBADO").
export interface ConfiguracionWhatsappRequest {
  templateRecordatorioBody?: string;
  respuestaConfirmar: string;
  respuestaCancelar: string;
  respuestaBienvenida: string;
  respuestaNoEntendido: string;
  optOutHabilitado: boolean;
  optOutPalabrasClave: string;
  respuestaOptOut: string;
}

export interface ConfiguracionWhatsappResponse extends ConfiguracionWhatsappRequest {
  templateRecordatorioBody: string;
  templateRecordatorioEstado: string;  // readonly desde el cliente
  fechaModificacion?: string;
}

// ── MensajeWhatsapp ────────────────────────────────────────────────────────────
export interface MensajeWhatsappResponse {
  idMensaje: number;
  idCita: number;
  telefonoDestino: string;
  contenido: string;
  estado: EstadoMensaje;
  intentos: number;
  fechaProgramada: string | null;
  fechaEnvio: string | null;
  errorMensaje: string | null;
  fechaCreacion?: string;
}

// ── Reportes ───────────────────────────────────────────────────────────────────
export interface ReporteGananciasDiarias {
  fecha: string;
  ingresosBrutos: number;
  totalSueldos: number;
  gananciaNeta: number;
}

export interface ReporteGananciasSemanales {
  anioSemana: number;
  desde: string;
  hasta: string;
  ingresosBrutos: number;
  totalSueldos: number;
  gananciaNeta: number;
}

export interface ReporteGananciasMensuales {
  anioMes: string;
  ingresosBrutos: number;
  totalSueldos: number;
  gananciaNeta: number;
}

export interface ReporteManoObra {
  idOt: number;
  numeroOt: string | null;
  totalOt: number;
  costoManoObra: number;
  subtotalNetoOt: number;
}

export interface ReporteMovimientoCaja {
  fecha: string;
  monto: number;
  tipo: string;
  concepto: string;
}

// ── Service ────────────────────────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class SptApiService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  // ── Citas change notification ─────────────────────────────────────────────────
  private readonly citasChanged$ = new Subject<void>();

  /** Subscribe to be notified whenever a cita is created, updated or deleted. */
  onCitasChanged(): Observable<void> {
    return this.citasChanged$.asObservable();
  }

  private notifyCitasChanged(): void {
    this.citasChanged$.next();
  }

  private withStartupRetry<T>(request$: Observable<T>): Observable<T> {
    return request$.pipe(retry({ count: 6, delay: 800 }));
  }

  // ── Clientes ────────────────────────────────────────────────────────────────
  listClientes(): Observable<ClienteResponse[]> {
    return this.withStartupRetry(this.http.get<ClienteResponse[]>(`${this.api}/clientes`));
  }

  createCliente(payload: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(`${this.api}/clientes`, payload);
  }

  updateCliente(idCliente: number, payload: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${this.api}/clientes/${idCliente}`, payload);
  }

  deleteCliente(idCliente: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/clientes/${idCliente}`);
  }

  buscarClientes(q: string): Observable<ClienteResponse[]> {
    const params = new HttpParams().set('q', q);
    return this.http.get<ClienteResponse[]>(`${this.api}/clientes/buscar`, { params });
  }

  // ── Vehiculos ───────────────────────────────────────────────────────────────
  listVehiculos(): Observable<VehiculoResponse[]> {
    return this.withStartupRetry(this.http.get<VehiculoResponse[]>(`${this.api}/vehiculos`));
  }

  createVehiculo(payload: VehiculoRequest): Observable<VehiculoResponse> {
    return this.http.post<VehiculoResponse>(`${this.api}/vehiculos`, payload);
  }

  updateVehiculo(idVehiculo: number, payload: VehiculoRequest): Observable<VehiculoResponse> {
    return this.http.put<VehiculoResponse>(`${this.api}/vehiculos/${idVehiculo}`, payload);
  }

  deleteVehiculo(idVehiculo: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/vehiculos/${idVehiculo}`);
  }

  // ── Piezas ──────────────────────────────────────────────────────────────────
  listPiezas(): Observable<PiezaResponse[]> {
    return this.withStartupRetry(this.http.get<PiezaResponse[]>(`${this.api}/piezas`));
  }

  createPieza(payload: PiezaRequest): Observable<PiezaResponse> {
    return this.http.post<PiezaResponse>(`${this.api}/piezas`, payload);
  }

  updatePieza(idPieza: number, payload: PiezaRequest): Observable<PiezaResponse> {
    return this.http.put<PiezaResponse>(`${this.api}/piezas/${idPieza}`, payload);
  }

  deletePieza(idPieza: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/piezas/${idPieza}`);
  }

  // ── Servicios ───────────────────────────────────────────────────────────────
  listServicios(): Observable<TipoServicioResponse[]> {
    return this.withStartupRetry(this.http.get<TipoServicioResponse[]>(`${this.api}/tipos-servicio`));
  }

  createServicio(payload: TipoServicioRequest): Observable<TipoServicioResponse> {
    return this.http.post<TipoServicioResponse>(`${this.api}/tipos-servicio`, payload);
  }

  updateServicio(idServicio: number, payload: TipoServicioRequest): Observable<TipoServicioResponse> {
    return this.http.put<TipoServicioResponse>(`${this.api}/tipos-servicio/${idServicio}`, payload);
  }

  deleteServicio(idServicio: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/tipos-servicio/${idServicio}`);
  }

  // ── Presupuestos ─────────────────────────────────────────────────────────────
  listPresupuestos(filters?: PresupuestoFilters): Observable<PresupuestoResponse[]> {
    let params = new HttpParams();
    if (filters?.estado) params = params.set('estado', filters.estado);
    if (filters?.patente?.trim()) params = params.set('patente', filters.patente.trim());
    return this.withStartupRetry(this.http.get<PresupuestoResponse[]>(`${this.api}/presupuestos`, { params }));
  }

  createPresupuesto(payload: PresupuestoRequest): Observable<PresupuestoResponse> {
    return this.http.post<PresupuestoResponse>(`${this.api}/presupuestos`, payload);
  }

  getPresupuesto(idPresupuesto: number): Observable<PresupuestoResponse> {
    return this.http.get<PresupuestoResponse>(`${this.api}/presupuestos/${idPresupuesto}`);
  }

  updatePresupuesto(idPresupuesto: number, payload: PresupuestoRequest): Observable<PresupuestoResponse> {
    return this.http.put<PresupuestoResponse>(`${this.api}/presupuestos/${idPresupuesto}`, payload);
  }

  deletePresupuesto(idPresupuesto: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/presupuestos/${idPresupuesto}`);
  }

  archivarPresupuesto(idPresupuesto: number): Observable<PresupuestoResponse> {
    return this.http.patch<PresupuestoResponse>(`${this.api}/presupuestos/${idPresupuesto}/archivar`, {});
  }

  desarchivarPresupuesto(idPresupuesto: number): Observable<PresupuestoResponse> {
    return this.http.patch<PresupuestoResponse>(`${this.api}/presupuestos/${idPresupuesto}/desarchivar`, {});
  }

  listPresupuestoDetalles(idPresupuesto: number): Observable<PresupuestoDetalleResponse[]> {
    return this.withStartupRetry(
      this.http.get<PresupuestoDetalleResponse[]>(`${this.api}/presupuestos/${idPresupuesto}/detalles`)
    );
  }

  addPresupuestoDetalle(
    idPresupuesto: number,
    payload: PresupuestoDetalleRequest
  ): Observable<PresupuestoDetalleResponse> {
    return this.http.post<PresupuestoDetalleResponse>(
      `${this.api}/presupuestos/${idPresupuesto}/detalles`,
      payload
    );
  }

  updatePresupuestoDetalle(
    idDetallePresupuesto: number,
    payload: PresupuestoDetalleRequest
  ): Observable<PresupuestoDetalleResponse> {
    return this.http.put<PresupuestoDetalleResponse>(
      `${this.api}/presupuestos/detalles/${idDetallePresupuesto}`,
      payload
    );
  }

  deletePresupuestoDetalle(idDetallePresupuesto: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/presupuestos/detalles/${idDetallePresupuesto}`);
  }

  downloadPresupuestoPdf(idPresupuesto: number): Observable<Blob> {
    return this.http.get(`${this.api}/presupuestos/${idPresupuesto}/pdf`, { responseType: 'blob' });
  }

  // ── Ordenes de trabajo ───────────────────────────────────────────────────────
  listOrdenesTrabajo(filters?: OrdenTrabajoFilters): Observable<OrdenTrabajoResponse[]> {
    let params = new HttpParams();
    if (filters?.numeroOt?.trim()) params = params.set('numeroOt', filters.numeroOt.trim());
    if (filters?.idCliente != null && filters.idCliente > 0) params = params.set('idCliente', filters.idCliente);
    if (filters?.idVehiculo != null && filters.idVehiculo > 0) params = params.set('idVehiculo', filters.idVehiculo);
    if (filters?.patente?.trim()) params = params.set('patente', filters.patente.trim());
    if (filters?.desde) params = params.set('desde', filters.desde);
    if (filters?.hasta) params = params.set('hasta', filters.hasta);
    if (filters?.estado) params = params.set('estado', filters.estado);
    return this.withStartupRetry(
      this.http.get<OrdenTrabajoResponse[]>(`${this.api}/ordenes-trabajo`, { params })
    );
  }

  createOrdenTrabajo(payload: OrdenTrabajoRequest): Observable<OrdenTrabajoResponse> {
    return this.http.post<OrdenTrabajoResponse>(`${this.api}/ordenes-trabajo`, payload);
  }

  createOrdenTrabajoDesdePresupuesto(idPresupuesto: number): Observable<OrdenTrabajoResponse> {
    return this.http.post<OrdenTrabajoResponse>(
      `${this.api}/ordenes-trabajo/desde-presupuesto/${idPresupuesto}`,
      {}
    );
  }

  getOrdenTrabajo(idOt: number): Observable<OrdenTrabajoResponse> {
    return this.http.get<OrdenTrabajoResponse>(`${this.api}/ordenes-trabajo/${idOt}`);
  }

  updateOrdenTrabajo(idOt: number, payload: OrdenTrabajoRequest): Observable<OrdenTrabajoResponse> {
    return this.http.put<OrdenTrabajoResponse>(`${this.api}/ordenes-trabajo/${idOt}`, payload);
  }

  deleteOrdenTrabajo(idOt: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/ordenes-trabajo/${idOt}`);
  }

  cambiarEstadoOrdenTrabajo(idOt: number, estado: EstadoOT): Observable<OrdenTrabajoResponse> {
    return this.http.patch<OrdenTrabajoResponse>(
      `${this.api}/ordenes-trabajo/${idOt}/estado?estado=${estado}`,
      {}
    );
  }

  listOtDetalles(idOt: number): Observable<OTDetalleResponse[]> {
    return this.withStartupRetry(
      this.http.get<OTDetalleResponse[]>(`${this.api}/ordenes-trabajo/${idOt}/detalles`)
    );
  }

  addOtDetalle(idOt: number, payload: OTDetalleRequest): Observable<OTDetalleResponse> {
    return this.http.post<OTDetalleResponse>(`${this.api}/ordenes-trabajo/${idOt}/detalles`, payload);
  }

  updateOtDetalle(idDetalleOt: number, payload: OTDetalleRequest): Observable<OTDetalleResponse> {
    return this.http.put<OTDetalleResponse>(`${this.api}/ordenes-trabajo/detalles/${idDetalleOt}`, payload);
  }

  deleteOtDetalle(idDetalleOt: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/ordenes-trabajo/detalles/${idDetalleOt}`);
  }

  downloadOrdenTrabajoPdf(idOt: number): Observable<Blob> {
    return this.http.get(`${this.api}/ordenes-trabajo/${idOt}/pdf`, { responseType: 'blob' });
  }

  // ── Pagos (cliente) ──────────────────────────────────────────────────────────
  listPagos(filters?: PagoFilters): Observable<PagoResponse[]> {
    let params = new HttpParams();
    if (filters?.idCliente != null && filters.idCliente > 0) params = params.set('idCliente', filters.idCliente);
    if (filters?.idVehiculo != null && filters.idVehiculo > 0) params = params.set('idVehiculo', filters.idVehiculo);
    if (filters?.patente?.trim()) params = params.set('patente', filters.patente.trim());
    if (filters?.desde) params = params.set('desde', filters.desde);
    if (filters?.hasta) params = params.set('hasta', filters.hasta);
    return this.withStartupRetry(this.http.get<PagoResponse[]>(`${this.api}/pagos`, { params }));
  }

  registrarPago(payload: PagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${this.api}/pagos`, payload);
  }

  deletePago(idPago: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/pagos/${idPago}`);
  }

  // ── Citas ────────────────────────────────────────────────────────────────────
  listCitas(filters?: CitaFilters): Observable<CitaResponse[]> {
    let params = new HttpParams();
    if (filters?.idCliente != null && filters.idCliente > 0) params = params.set('idCliente', filters.idCliente);
    if (filters?.idVehiculo != null && filters.idVehiculo > 0) params = params.set('idVehiculo', filters.idVehiculo);
    if (filters?.patente?.trim()) params = params.set('patente', filters.patente.trim());
    if (filters?.desde) params = params.set('desde', filters.desde);
    if (filters?.hasta) params = params.set('hasta', filters.hasta);
    return this.withStartupRetry(this.http.get<CitaResponse[]>(`${this.api}/citas`, { params }));
  }

  createCita(payload: CitaRequest): Observable<CitaResponse> {
    return this.http.post<CitaResponse>(`${this.api}/citas`, payload).pipe(
      tap(() => this.notifyCitasChanged())
    );
  }

  updateCita(idCita: number, payload: CitaRequest): Observable<CitaResponse> {
    return this.http.put<CitaResponse>(`${this.api}/citas/${idCita}`, payload).pipe(
      tap(() => this.notifyCitasChanged())
    );
  }

  deleteCita(idCita: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/citas/${idCita}`).pipe(
      tap(() => this.notifyCitasChanged())
    );
  }

  // ── Empleados ────────────────────────────────────────────────────────────────
  listEmpleados(): Observable<EmpleadoResponse[]> {
    return this.withStartupRetry(this.http.get<EmpleadoResponse[]>(`${this.api}/empleados`));
  }

  createEmpleado(payload: EmpleadoRequest): Observable<EmpleadoResponse> {
    return this.http.post<EmpleadoResponse>(`${this.api}/empleados`, payload);
  }

  updateEmpleado(idEmpleado: number, payload: EmpleadoRequest): Observable<EmpleadoResponse> {
    return this.http.put<EmpleadoResponse>(`${this.api}/empleados/${idEmpleado}`, payload);
  }

  deleteEmpleado(idEmpleado: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/empleados/${idEmpleado}`);
  }

  // ── Pagos empleados ──────────────────────────────────────────────────────────
  listPagosEmpleado(idEmpleado?: number): Observable<PagoEmpleadoResponse[]> {
    let params = new HttpParams();
    if (idEmpleado != null && idEmpleado > 0) params = params.set('idEmpleado', idEmpleado);
    return this.withStartupRetry(
      this.http.get<PagoEmpleadoResponse[]>(`${this.api}/pagos-empleado`, { params })
    );
  }

  registrarPagoEmpleado(payload: PagoEmpleadoRequest): Observable<PagoEmpleadoResponse> {
    return this.http.post<PagoEmpleadoResponse>(`${this.api}/pagos-empleado`, payload);
  }

  deletePagoEmpleado(idPagoEmpleado: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/pagos-empleado/${idPagoEmpleado}`);
  }

  // ── OT Empleados (mano de obra) ──────────────────────────────────────────────
  listOtEmpleados(idOt: number): Observable<OtEmpleadoResponse[]> {
    return this.withStartupRetry(
      this.http.get<OtEmpleadoResponse[]>(`${this.api}/ordenes/${idOt}/empleados`)
    );
  }

  addOtEmpleado(idOt: number, payload: OtEmpleadoRequest): Observable<OtEmpleadoResponse> {
    return this.http.post<OtEmpleadoResponse>(`${this.api}/ordenes/${idOt}/empleados`, payload);
  }

  updateOtEmpleado(idOtEmpleado: number, payload: OtEmpleadoRequest): Observable<OtEmpleadoResponse> {
    return this.http.put<OtEmpleadoResponse>(`${this.api}/ordenes/empleados/${idOtEmpleado}`, payload);
  }

  deleteOtEmpleado(idOtEmpleado: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/ordenes/empleados/${idOtEmpleado}`);
  }

  // ── Facturas ─────────────────────────────────────────────────────────────────
  listFacturas(): Observable<FacturaResponse[]> {
    return this.withStartupRetry(this.http.get<FacturaResponse[]>(`${this.api}/facturas`));
  }

  getFactura(idFactura: number): Observable<FacturaResponse> {
    return this.http.get<FacturaResponse>(`${this.api}/facturas/${idFactura}`);
  }

  crearFacturaDesdePresupuesto(
    idPresupuesto: number,
    payload: FacturaDesdeOrigenRequest
  ): Observable<FacturaResponse> {
    return this.http.post<FacturaResponse>(
      `${this.api}/facturas/desde-presupuesto/${idPresupuesto}`,
      payload
    );
  }

  crearFacturaDesdeOt(idOt: number, payload: FacturaDesdeOrigenRequest): Observable<FacturaResponse> {
    return this.http.post<FacturaResponse>(`${this.api}/facturas/desde-ot/${idOt}`, payload);
  }

  anularFactura(idFactura: number): Observable<FacturaResponse> {
    return this.http.patch<FacturaResponse>(`${this.api}/facturas/${idFactura}/anular`, {});
  }

  proximoNumeroFactura(): Observable<{ numero: string }> {
    return this.http.get<{ numero: string }>(`${this.api}/facturas/proximo-numero`);
  }

  proximoNumeroOt(): Observable<{ numero: string }> {
    return this.http.get<{ numero: string }>(`${this.api}/ordenes-trabajo/proximo-numero`);
  }

  downloadFacturaPdf(idFactura: number): Observable<Blob> {
    return this.http.get(`${this.api}/facturas/${idFactura}/pdf`, { responseType: 'blob' });
  }

  // ── Calendario ───────────────────────────────────────────────────────────────
  getCalendario(desde?: string, hasta?: string): Observable<CalendarioItemDTO[]> {
    let params = new HttpParams();
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    return this.withStartupRetry(
      this.http.get<CalendarioItemDTO[]>(`${this.api}/calendario`, { params })
    );
  }

  // ── Eventos ──────────────────────────────────────────────────────────────────
  listEventos(): Observable<EventoResponse[]> {
    return this.withStartupRetry(this.http.get<EventoResponse[]>(`${this.api}/eventos`));
  }

  createEvento(payload: EventoRequest): Observable<EventoResponse> {
    return this.http.post<EventoResponse>(`${this.api}/eventos`, payload);
  }

  updateEvento(idEvento: number, payload: EventoRequest): Observable<EventoResponse> {
    return this.http.put<EventoResponse>(`${this.api}/eventos/${idEvento}`, payload);
  }

  deleteEvento(idEvento: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/eventos/${idEvento}`);
  }

  // ── Configuracion ─────────────────────────────────────────────────────────────
  getConfiguracion(): Observable<ConfiguracionTallerResponse> {
    return this.withStartupRetry(
      this.http.get<ConfiguracionTallerResponse>(`${this.api}/configuracion`)
    );
  }

  updateConfiguracion(payload: ConfiguracionTallerRequest): Observable<ConfiguracionTallerResponse> {
    return this.http.put<ConfiguracionTallerResponse>(`${this.api}/configuracion`, payload);
  }

  getConfiguracionWhatsapp(): Observable<ConfiguracionWhatsappResponse> {
    return this.withStartupRetry(
      this.http.get<ConfiguracionWhatsappResponse>(`${this.api}/configuracion/whatsapp`)
    );
  }

  updateConfiguracionWhatsapp(
    payload: ConfiguracionWhatsappRequest
  ): Observable<ConfiguracionWhatsappResponse> {
    return this.http.put<ConfiguracionWhatsappResponse>(`${this.api}/configuracion/whatsapp`, payload);
  }

  // ── Mensajes WhatsApp ─────────────────────────────────────────────────────────
  listMensajesWhatsapp(): Observable<MensajeWhatsappResponse[]> {
    return this.withStartupRetry(
      this.http.get<MensajeWhatsappResponse[]>(`${this.api}/mensajes-whatsapp`)
    );
  }

  // ── Reportes ──────────────────────────────────────────────────────────────────
  getReporteGananciasDiarias(): Observable<ReporteGananciasDiarias[]> {
    return this.withStartupRetry(
      this.http.get<ReporteGananciasDiarias[]>(`${this.api}/reportes/ganancias/diarias`)
    );
  }

  getReporteGananciasSemanales(): Observable<ReporteGananciasSemanales[]> {
    return this.withStartupRetry(
      this.http.get<ReporteGananciasSemanales[]>(`${this.api}/reportes/ganancias/semanales`)
    );
  }

  getReporteGananciasMensuales(): Observable<ReporteGananciasMensuales[]> {
    return this.withStartupRetry(
      this.http.get<ReporteGananciasMensuales[]>(`${this.api}/reportes/ganancias/mensuales`)
    );
  }

  getReporteManoObra(): Observable<ReporteManoObra[]> {
    return this.withStartupRetry(
      this.http.get<ReporteManoObra[]>(`${this.api}/reportes/mano-obra/empleados`)
    );
  }

  getReporteMovimientoCaja(): Observable<ReporteMovimientoCaja[]> {
    return this.withStartupRetry(
      this.http.get<ReporteMovimientoCaja[]>(`${this.api}/reportes/caja`)
    );
  }
}
