import { Component, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-host',
  imports: [TranslatePipe],
  template: `
    @if (toast.active(); as toastMsg) {
      <div
        class="fc-toast"
        [class.fc-toast--error]="toastMsg.type === 'error'"
        role="status"
        aria-live="polite"
      >
        {{ toastMsg.textKey | translate }}
        <button type="button" class="fc-toast__close" (click)="toast.dismiss()" aria-label="Fermer">
          ×
        </button>
      </div>
    }
  `,
  styles: `
    .fc-toast {
      position: fixed;
      bottom: max(1rem, env(safe-area-inset-bottom, 0));
      right: max(1rem, env(safe-area-inset-right, 0));
      left: auto;
      z-index: var(--fc-z-toast);
      display: flex;
      align-items: center;
      gap: 0.75rem;
      max-width: min(24rem, calc(100vw - 2rem));
      padding: 0.75rem 1rem;
      border-radius: var(--fc-radius-sm);
      background: var(--fc-success-soft);
      color: var(--fc-success);
      border: 1px solid color-mix(in srgb, var(--fc-success) 22%, transparent);
      box-shadow: var(--fc-shadow);
      font-size: 0.875rem;
      font-weight: 600;
      animation: fc-toast-in 200ms var(--fc-ease);
    }

    .fc-toast--error {
      background: var(--fc-danger-soft);
      color: var(--fc-danger);
      border-color: color-mix(in srgb, var(--fc-danger) 22%, transparent);
    }

    .fc-toast__close {
      border: 0;
      background: transparent;
      color: inherit;
      font-size: 1.25rem;
      line-height: 1;
      cursor: pointer;
      padding: 0;
      opacity: 0.7;
    }

    .fc-toast__close:hover {
      opacity: 1;
    }

    @keyframes fc-toast-in {
      from {
        opacity: 0;
        transform: translateY(8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @media (max-width: 480px) {
      .fc-toast {
        left: max(0.85rem, env(safe-area-inset-left, 0));
        right: max(0.85rem, env(safe-area-inset-right, 0));
        max-width: none;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .fc-toast {
        animation: none;
      }
    }
  `,
})
export class ToastHostComponent {
  readonly toast = inject(ToastService);
}
