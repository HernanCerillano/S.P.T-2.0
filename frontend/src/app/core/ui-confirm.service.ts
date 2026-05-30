import { Injectable, NgZone, inject, signal } from '@angular/core';

export interface UiConfirmOptions {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  tone?: 'default' | 'danger';
}

export interface UiConfirmState {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  tone: 'default' | 'danger';
}

@Injectable({ providedIn: 'root' })
export class UiConfirmService {
  private readonly ngZone = inject(NgZone);
  readonly state = signal<UiConfirmState | null>(null);
  private resolver: ((value: boolean) => void) | null = null;

  ask(options: UiConfirmOptions): Promise<boolean> {
    if (this.resolver) {
      this.resolver(false);
      this.resolver = null;
    }

    this.state.set({
      title: options.title?.trim() || 'Confirmar accion',
      message: options.message,
      confirmText: options.confirmText?.trim() || 'Confirmar',
      cancelText: options.cancelText?.trim() || 'Cancelar',
      tone: options.tone ?? 'default',
    });

    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  confirm(): void {
    this.resolve(true);
  }

  cancel(): void {
    this.resolve(false);
  }

  private resolve(value: boolean): void {
    const resolver = this.resolver;
    this.ngZone.runTask(() => {
      this.resolver = null;
      this.state.set(null);
      resolver?.(value);
    });
  }
}
