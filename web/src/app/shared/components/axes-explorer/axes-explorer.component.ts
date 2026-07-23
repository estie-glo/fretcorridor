import { Component, inject, input, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { catchError, of } from 'rxjs';

import { AxeSummary, getAxeDisplayLabel, getAxeDisplayMeta } from '../../models/axe.model';
import { Hub } from '../../models/hub.model';
import { AxesService } from '../../services/axes.service';
import { HubsService } from '../../services/hubs.service';
import { toRecordEntries } from '../../utils/record-display';
import { CorridorMapComponent } from '../corridor-map/corridor-map.component';

@Component({
  selector: 'app-axes-explorer',
  imports: [TranslatePipe, CorridorMapComponent],
  templateUrl: './axes-explorer.component.html',
  styleUrl: './axes-explorer.component.scss',
})
export class AxesExplorerComponent {
  private readonly axesService = inject(AxesService);
  private readonly hubsService = inject(HubsService);

  /** Préfixe i18n, ex. AXES ou CHARGEUR */
  readonly scope = input.required<string>();

  readonly axes = signal<AxeSummary[]>([]);
  readonly hubs = signal<Hub[]>([]);
  readonly selectedAxeId = signal<string | null>(null);
  readonly statutEntries = signal<{ key: string; value: string }[]>([]);
  readonly isLoadingList = signal(true);
  readonly isLoadingMap = signal(true);
  readonly isLoadingStatut = signal(false);
  readonly listError = signal(false);
  readonly mapError = signal(false);
  readonly statutError = signal(false);

  constructor() {
    this.loadNetwork();
  }

  translationKey(suffix: string): string {
    return `${this.scope()}.${suffix}`;
  }

  getAxeLabel(axe: AxeSummary): string {
    return getAxeDisplayLabel(axe);
  }

  getAxeMeta(axe: AxeSummary): string {
    return getAxeDisplayMeta(axe);
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

  private loadNetwork(): void {
    this.isLoadingList.set(true);
    this.isLoadingMap.set(true);
    this.listError.set(false);
    this.mapError.set(false);

    this.axesService.getAxes().subscribe({
      next: (axes) => {
        this.axes.set(axes);
        this.isLoadingList.set(false);
        this.isLoadingMap.set(false);

        if (axes.length === 1) {
          this.selectAxe(axes[0].id);
        }
      },
      error: () => {
        this.axes.set([]);
        this.listError.set(true);
        this.mapError.set(true);
        this.isLoadingList.set(false);
        this.isLoadingMap.set(false);
      },
    });

    this.hubsService
      .getHubs()
      .pipe(catchError(() => of([] as Hub[])))
      .subscribe({
        next: (hubs) => this.hubs.set(hubs),
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
