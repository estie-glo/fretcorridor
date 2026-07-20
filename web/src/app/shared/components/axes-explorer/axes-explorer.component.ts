import { Component, inject, input, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { AxeSummary, getAxeDisplayLabel } from '../../models/axe.model';
import { AxesService } from '../../services/axes.service';
import { toRecordEntries } from '../../utils/record-display';

@Component({
  selector: 'app-axes-explorer',
  imports: [TranslatePipe],
  templateUrl: './axes-explorer.component.html',
  styleUrl: './axes-explorer.component.scss',
})
export class AxesExplorerComponent {
  private readonly axesService = inject(AxesService);

  /** Préfixe i18n, ex. AXES ou CHARGEUR */
  readonly scope = input.required<string>();

  readonly axes = signal<AxeSummary[]>([]);
  readonly selectedAxeId = signal<string | null>(null);
  readonly statutEntries = signal<{ key: string; value: string }[]>([]);
  readonly isLoadingList = signal(true);
  readonly isLoadingStatut = signal(false);
  readonly listError = signal(false);
  readonly statutError = signal(false);

  constructor() {
    this.loadAxes();
  }

  translationKey(suffix: string): string {
    return `${this.scope()}.${suffix}`;
  }

  getAxeLabel(axe: AxeSummary): string {
    return getAxeDisplayLabel(axe);
  }

  isSelected(axeId: string): boolean {
    return this.selectedAxeId() === axeId;
  }

  selectAxe(axeId: string): void {
    if (this.selectedAxeId() === axeId) {
      return;
    }

    this.selectedAxeId.set(axeId);
    this.statutEntries.set([]);
    this.statutError.set(false);
    this.loadStatut(axeId);
  }

  private loadAxes(): void {
    this.isLoadingList.set(true);
    this.listError.set(false);

    this.axesService.getAxes().subscribe({
      next: (axes) => {
        this.axes.set(axes);
        this.isLoadingList.set(false);

        if (axes.length === 1) {
          this.selectAxe(axes[0].id);
        }
      },
      error: () => {
        this.axes.set([]);
        this.listError.set(true);
        this.isLoadingList.set(false);
      },
    });
  }

  private loadStatut(axeId: string): void {
    this.isLoadingStatut.set(true);
    this.statutError.set(false);

    this.axesService.getAxeStatut(axeId).subscribe({
      next: (statut) => {
        this.statutEntries.set(toRecordEntries(statut));
        this.isLoadingStatut.set(false);

        if (!statut) {
          this.statutError.set(true);
        }
      },
      error: () => {
        this.statutEntries.set([]);
        this.statutError.set(true);
        this.isLoadingStatut.set(false);
      },
    });
  }
}
