import { Component, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import {
  ChauffeurSummary,
  getChauffeurDisplayLabel,
  getChauffeurDisplayMeta,
} from '../../../shared/models/chauffeur.model';
import { toRecordEntries } from '../../../shared/utils/record-display';
import { ChauffeursService } from '../services/chauffeurs.service';

@Component({
  selector: 'app-chauffeurs-list',
  imports: [TranslatePipe],
  templateUrl: './chauffeurs-list.component.html',
  styleUrl: './chauffeurs-list.component.scss',
})
export class ChauffeursListComponent {
  private readonly chauffeursService = inject(ChauffeursService);

  readonly chauffeurs = signal<ChauffeurSummary[]>([]);
  readonly selectedChauffeurId = signal<string | null>(null);
  readonly detailEntries = signal<{ key: string; value: string }[]>([]);
  readonly isLoadingList = signal(true);
  readonly isLoadingDetail = signal(false);
  readonly listError = signal(false);
  readonly detailError = signal(false);

  constructor() {
    this.loadChauffeurs();
  }

  getChauffeurLabel(chauffeur: ChauffeurSummary): string {
    return getChauffeurDisplayLabel(chauffeur);
  }

  getChauffeurMeta(chauffeur: ChauffeurSummary): string {
    return getChauffeurDisplayMeta(chauffeur);
  }

  isSelected(chauffeurId: string): boolean {
    return this.selectedChauffeurId() === chauffeurId;
  }

  selectChauffeur(chauffeurId: string): void {
    if (this.selectedChauffeurId() === chauffeurId) {
      return;
    }

    this.selectedChauffeurId.set(chauffeurId);
    this.detailEntries.set([]);
    this.detailError.set(false);
    this.loadChauffeurDetail(chauffeurId);
  }

  private loadChauffeurs(): void {
    this.isLoadingList.set(true);
    this.listError.set(false);

    this.chauffeursService.getChauffeurs().subscribe({
      next: (items) => {
        this.chauffeurs.set(items);
        this.isLoadingList.set(false);

        if (items.length === 1) {
          this.selectChauffeur(items[0].id);
        }
      },
      error: () => {
        this.chauffeurs.set([]);
        this.listError.set(true);
        this.isLoadingList.set(false);
      },
    });
  }

  private loadChauffeurDetail(chauffeurId: string): void {
    this.isLoadingDetail.set(true);
    this.detailError.set(false);

    this.chauffeursService.getChauffeur(chauffeurId).subscribe({
      next: (detail) => {
        this.detailEntries.set(toRecordEntries(detail));
        this.isLoadingDetail.set(false);

        if (!detail) {
          this.detailError.set(true);
        }
      },
      error: () => {
        this.detailEntries.set([]);
        this.detailError.set(true);
        this.isLoadingDetail.set(false);
      },
    });
  }
}
