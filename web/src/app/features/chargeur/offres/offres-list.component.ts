import { Component, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import {
  MissionSummary,
  getMissionDisplayLabel,
} from '../../../shared/models/mission.model';
import { MissionsService } from '../../../shared/services/missions.service';
import { toRecordEntries } from '../../../shared/utils/record-display';

@Component({
  selector: 'app-offres-list',
  imports: [TranslatePipe],
  templateUrl: './offres-list.component.html',
  styleUrl: './offres-list.component.scss',
})
export class OffresListComponent {
  private readonly missionsService = inject(MissionsService);

  readonly offres = signal<MissionSummary[]>([]);
  readonly selectedOffreId = signal<string | null>(null);
  readonly detailEntries = signal<{ key: string; value: string }[]>([]);
  readonly isLoadingList = signal(true);
  readonly isLoadingDetail = signal(false);
  readonly listError = signal(false);
  readonly detailError = signal(false);

  constructor() {
    this.loadOffres();
  }

  getOffreLabel(offre: MissionSummary): string {
    return getMissionDisplayLabel(offre);
  }

  getOffreMeta(offre: MissionSummary): string {
    const parts: string[] = [];
    if (offre.typeCamion) {
      parts.push(offre.typeCamion);
    }
    if (offre.capaciteTonnes !== undefined) {
      parts.push(`${offre.capaciteTonnes} t`);
    }
    if (offre.chauffeurNom) {
      parts.push(offre.chauffeurNom);
    }
    return parts.join(' · ');
  }

  isSelected(offreId: string): boolean {
    return this.selectedOffreId() === offreId;
  }

  selectOffre(offreId: string): void {
    if (this.selectedOffreId() === offreId) {
      return;
    }

    this.selectedOffreId.set(offreId);
    this.detailEntries.set([]);
    this.detailError.set(false);
    this.loadOffreDetail(offreId);
  }

  private loadOffres(): void {
    this.isLoadingList.set(true);
    this.listError.set(false);

    this.missionsService.getOffres().subscribe({
      next: (items) => {
        this.offres.set(items);
        this.isLoadingList.set(false);

        if (items.length === 1) {
          this.selectOffre(items[0].id);
        }
      },
      error: () => {
        this.offres.set([]);
        this.listError.set(true);
        this.isLoadingList.set(false);
      },
    });
  }

  private loadOffreDetail(offreId: string): void {
    this.isLoadingDetail.set(true);
    this.detailError.set(false);

    this.missionsService.getMission(offreId).subscribe({
      next: (detail) => {
        this.detailEntries.set(toRecordEntries(detail?.raw ?? {}));
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
