import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ConfiguracionTallerRequest,
  ConfiguracionTallerResponse,
  ConfiguracionWhatsappRequest,
  ConfiguracionWhatsappResponse,
  MensajeWhatsappResponse,
  SptApiService,
} from '../../core/spt-api.service';

@Component({
  selector: 'app-configuracion',
  imports: [CommonModule, FormsModule],
  templateUrl: './configuracion.html',
  styleUrl: './configuracion.scss',
})
export class Configuracion implements OnInit {
  private readonly api = inject(SptApiService);

  readonly placeholderPlantilla =
    'Hola {{nombre_cliente}}, le recordamos su cita el {{fecha_cita}}...';
  readonly variablesHint =
    'Variables disponibles: {{nombre_cliente}}, {{fecha_cita}}, {{nombre_taller}}';

  protected loading = false;
  protected saving = false;
  protected error = '';
  protected success = '';
  protected mensajesLoading = false;

  protected form: ConfiguracionTallerRequest = this.newForm();
  protected mensajes: MensajeWhatsappResponse[] = [];

  // ── Mensajes WhatsApp configurables (auto-respuestas / opt-out / template body) ──
  protected formWa: ConfiguracionWhatsappRequest = this.newFormWa();
  protected templateRecordatorioEstado = 'APROBADO';  // readonly, viene del backend
  protected loadingWa = false;
  protected savingWa = false;
  protected errorWa = '';
  protected successWa = '';

  ngOnInit(): void {
    this.cargar();
    this.cargarMensajes();
    this.cargarWhatsapp();
  }

  protected guardar(): void {
    this.error = '';
    this.success = '';
    this.saving = true;

    this.api.updateConfiguracion(this.form).subscribe({
      next: (data: ConfiguracionTallerResponse) => {
        this.form = {
          nombreTaller: data.nombreTaller,
          telefono: data.telefono,
          direccion: data.direccion,
          email: data.email,
          cuit: data.cuit,
          logoPath: data.logoPath,
          plantillaWpRecordatorio: data.plantillaWpRecordatorio,
          horasAnticipacionWp: data.horasAnticipacionWp,
          wpHabilitado: data.wpHabilitado,
        };
        this.success = 'Configuracion guardada correctamente.';
        this.saving = false;
      },
      error: (err: any) => {
        this.error = err?.error?.message ?? 'No se pudo guardar la configuracion.';
        this.saving = false;
      },
    });
  }

  protected estadoBadge(estado: string): string {
    if (estado === 'ENVIADO') return 'badge badge-success';
    if (estado === 'FALLIDO') return 'badge badge-danger';
    if (estado === 'PENDIENTE') return 'badge badge-warn';
    return 'badge badge-neutral';
  }

  private cargar(): void {
    this.loading = true;
    this.api.getConfiguracion().subscribe({
      next: (data: ConfiguracionTallerResponse) => {
        this.form = {
          nombreTaller: data.nombreTaller,
          telefono: data.telefono,
          direccion: data.direccion,
          email: data.email,
          cuit: data.cuit,
          logoPath: data.logoPath,
          plantillaWpRecordatorio: data.plantillaWpRecordatorio,
          horasAnticipacionWp: data.horasAnticipacionWp,
          wpHabilitado: data.wpHabilitado,
        };
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err?.error?.message ?? 'No se pudo cargar la configuracion.';
        this.loading = false;
      },
    });
  }

  private cargarMensajes(): void {
    this.mensajesLoading = true;
    this.api.listMensajesWhatsapp().subscribe({
      next: (data: MensajeWhatsappResponse[]) => {
        this.mensajes = data;
        this.mensajesLoading = false;
      },
      error: () => {
        this.mensajesLoading = false;
      },
    });
  }

  protected guardarWhatsapp(): void {
    this.errorWa = '';
    this.successWa = '';
    this.savingWa = true;

    this.api.updateConfiguracionWhatsapp(this.formWa).subscribe({
      next: (data: ConfiguracionWhatsappResponse) => {
        this.formWa = this.toFormWa(data);
        this.successWa = 'Mensajes de WhatsApp guardados correctamente.';
        this.savingWa = false;
      },
      error: (err: any) => {
        this.errorWa = err?.error?.message ?? 'No se pudieron guardar los mensajes de WhatsApp.';
        this.savingWa = false;
      },
    });
  }

  private cargarWhatsapp(): void {
    this.loadingWa = true;
    this.api.getConfiguracionWhatsapp().subscribe({
      next: (data: ConfiguracionWhatsappResponse) => {
        this.formWa = this.toFormWa(data);
        this.loadingWa = false;
      },
      error: (err: any) => {
        this.errorWa = err?.error?.message ?? 'No se pudo cargar la configuracion de WhatsApp.';
        this.loadingWa = false;
      },
    });
  }

  private toFormWa(data: ConfiguracionWhatsappResponse): ConfiguracionWhatsappRequest {
    this.templateRecordatorioEstado = data.templateRecordatorioEstado ?? 'APROBADO';
    return {
      templateRecordatorioBody: data.templateRecordatorioBody ?? '',
      respuestaConfirmar: data.respuestaConfirmar,
      respuestaCancelar: data.respuestaCancelar,
      respuestaBienvenida: data.respuestaBienvenida,
      respuestaNoEntendido: data.respuestaNoEntendido,
      optOutHabilitado: data.optOutHabilitado,
      optOutPalabrasClave: data.optOutPalabrasClave,
      respuestaOptOut: data.respuestaOptOut,
    };
  }

  private newFormWa(): ConfiguracionWhatsappRequest {
    return {
      templateRecordatorioBody: '',
      respuestaConfirmar: '',
      respuestaCancelar: '',
      respuestaBienvenida: '',
      respuestaNoEntendido: '',
      optOutHabilitado: true,
      optOutPalabrasClave: 'STOP,BAJA,CANCELAR,DESUSCRIBIR',
      respuestaOptOut: '',
    };
  }

  private newForm(): ConfiguracionTallerRequest {
    return {
      nombreTaller: '',
      telefono: '',
      direccion: '',
      email: '',
      cuit: '',
      logoPath: '',
      plantillaWpRecordatorio: '',
      horasAnticipacionWp: 24,
      wpHabilitado: false,
    };
  }
}
