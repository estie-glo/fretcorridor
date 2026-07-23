import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error';

export interface ToastMessage {
  textKey: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly active = signal<ToastMessage | null>(null);

  private hideTimer: ReturnType<typeof setTimeout> | null = null;

  show(textKey: string, type: ToastType = 'success'): void {
    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
    }

    this.active.set({ textKey, type });

    this.hideTimer = setTimeout(() => {
      this.active.set(null);
      this.hideTimer = null;
    }, 4000);
  }

  dismiss(): void {
    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = null;
    }
    this.active.set(null);
  }
}
