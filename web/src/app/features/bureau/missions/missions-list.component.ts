import { Component, DestroyRef, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { Subscription, forkJoin, interval, of, startWith, switchMap } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { MissionTrackingMapComponent } from '../../../shared/components/mission-tracking-map/mission-tracking-map.component';
import {
  missionStatusVariant,
  StatusBadgeComponent,
} from '../../../shared/components/status-badge/status-badge.component';
import {
  EtaInfo,
  MissionSummary,
  TrackingInfo,
  getMissionDisplayLabel,
} from '../../../shared/models/mission.model';
import { AxeSummary, getAxeDisplayLabel } from '../../../shared/models/axe.model';
import { AxesService } from '../../../shared/services/axes.service';
import { MissionsService } from '../../../shared/services/missions.service';
import { ToastService } from '../../../shared/services/toast.service';
import { toRecordEntries } from '../../../shared/utils/record-display';

const POLL_MS = 20_000;

@Component({
  selector: 'app-missions-list',
  imports: [TranslatePipe, MissionTrackingMapComponent, StatusBadgeComponent],
  templateUrl: './missions-list.component.html',
  styleUrl: './missions-list.component.scss',
})
export class MissionsListComponent {
  private readonly missionsService = inject(MissionsService);
  private readonly axesService = inject(AxesService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly missions = signal<MissionSummary[]>([]);
  readonly axes = signal<AxeSummary[]>([]);
  readonly filterStatut = signal('');
  readonly filterAxeId = signal('');
  readonly selectedMissionId = signal<string | null>(null);
  readonly selectedMission = signal<MissionSummary | null>(null);
  readonly detailEntries = signal<{ key: string; value: string }[]>([]);
  readonly tracking = signal<TrackingInfo | null>(null);
  readonly eta = signal<EtaInfo | null>(null);
  readonly trackingUpdatedAt = signal<Date | null>(null);
  readonly isLoadingList = signal(true);
  readonly isLoadingDetail = signal(false);
  readonly isLoadingTracking = signal(false);
  readonly isTransitioning = signal(false);
  readonly listError = signal(false);
  readonly detailError = signal(false);
  readonly trackingError = signal(false);
  readonly actionError = signal<string | null>(null);

  private pollSub: Subscription | null = null;

  constructor() {
    this.loadAxes();
    this.loadMissions();
    this.destroyRef.onDestroy(() => this.stopPolling());
  }

  missionStatusVariant = missionStatusVariant;

  getAxeLabel(axe: AxeSummary): string {
    return getAxeDisplayLabel(axe);
  }

  onFilterChange(): void {
    this.loadMissions();
  }

  getMissionLabel(mission: MissionSummary): string {
    return getMissionDisplayLabel(mission);
  }

  isSelected(missionId: string): boolean {
    return this.selectedMissionId() === missionId;
  }

  canAccepter(): boolean {
    const s = this.selectedMission()?.statut;
    return s === 'CAMION_VIDE_DECLARE' || s === 'MATCH_PROPOSE';
  }

  canDemarrer(): boolean {
    return this.selectedMission()?.statut === 'MISSION_ACCEPTEE';
  }

  canTerminer(): boolean {
    return this.selectedMission()?.statut === 'EN_COURS';
  }

  canAnnuler(): boolean {
    const s = this.selectedMission()?.statut;
    return (
      s === 'CAMION_VIDE_DECLARE' ||
      s === 'MATCH_PROPOSE' ||
      s === 'MISSION_ACCEPTEE' ||
      s === 'EN_COURS'
    );
  }

  accepter(): void {
    this.runTransition((id) => this.missionsService.accepter(id));
  }

  demarrer(): void {
    this.runTransition((id) => this.missionsService.demarrer(id));
  }

  terminer(): void {
    this.runTransition((id) => this.missionsService.terminer(id));
  }

  annuler(): void {
    this.runTransition((id) => this.missionsService.annuler(id));
  }

  private runTransition(
    action: (id: string) => ReturnType<MissionsService['accepter']>,
  ): void {
    const id = this.selectedMissionId();
    if (!id || this.isTransitioning()) {
      return;
    }

    this.isTransitioning.set(true);
    this.actionError.set(null);

    action(id).subscribe({
      next: (updated) => {
        this.isTransitioning.set(false);
        if (!updated) {
          this.actionError.set('TRANSITION_INVALIDE');
          return;
        }
        this.selectedMission.set(updated);
        this.missions.update((list) =>
          list.map((m) => (m.id === updated.id ? { ...m, ...updated } : m)),
        );
        this.toastService.show('MISSIONS.ACTION_SUCCESS', 'success');
        this.loadMissionDetail(id);
      },
      error: () => {
        this.isTransitioning.set(false);
        this.actionError.set('TRANSITION_INVALIDE');
        this.toastService.show('MISSIONS.ERROR_ACTION', 'error');
      },
    });
  }

  selectMission(missionId: string): void {
    if (this.selectedMissionId() === missionId) {
      return;
    }

    this.stopPolling();
    this.selectedMissionId.set(missionId);
    this.selectedMission.set(this.missions().find((m) => m.id === missionId) ?? null);
    this.detailEntries.set([]);
    this.tracking.set(null);
    this.eta.set(null);
    this.trackingUpdatedAt.set(null);
    this.detailError.set(false);
    this.trackingError.set(false);
    this.actionError.set(null);
    this.loadMissionDetail(missionId);
  }

  private loadAxes(): void {
    this.axesService.getAxes().subscribe({
      next: (items) => this.axes.set(items),
      error: () => this.axes.set([]),
    });
  }

  private loadMissions(): void {
    this.isLoadingList.set(true);
    this.listError.set(false);

    const statut = this.filterStatut().trim() || undefined;
    const axeId = this.filterAxeId().trim() || undefined;

    this.missionsService.getMissions({ statut, axeId }).subscribe({
      next: (items) => {
        this.missions.set(items);
        this.isLoadingList.set(false);

        if (items.length === 1) {
          this.selectMission(items[0].id);
        }
      },
      error: () => {
        this.missions.set([]);
        this.listError.set(true);
        this.isLoadingList.set(false);
      },
    });
  }

  private loadMissionDetail(missionId: string): void {
    this.isLoadingDetail.set(true);
    this.detailError.set(false);

    this.missionsService.getMission(missionId).subscribe({
      next: (detail) => {
        this.detailEntries.set(toRecordEntries(detail?.raw ?? {}));
        this.isLoadingDetail.set(false);

        if (!detail) {
          this.detailError.set(true);
          return;
        }

        this.selectedMission.set(detail);
        this.startLiveTracking(missionId, detail.statut);
      },
      error: () => {
        this.detailEntries.set([]);
        this.detailError.set(true);
        this.isLoadingDetail.set(false);
      },
    });
  }

  private startLiveTracking(missionId: string, statut?: string): void {
    this.stopPolling();
    this.isLoadingTracking.set(true);

    const refresh$ = () =>
      forkJoin({
        tracking: this.missionsService.getTracking(missionId).pipe(catchError(() => of(null))),
        eta: this.missionsService.getEta(missionId).pipe(catchError(() => of(null))),
      });

    const apply = (result: { tracking: TrackingInfo | null; eta: EtaInfo | null }) => {
      this.tracking.set(result.tracking);
      this.eta.set(result.eta);
      this.trackingUpdatedAt.set(new Date());
      this.isLoadingTracking.set(false);
      this.trackingError.set(result.tracking === null);
    };

    const onError = () => {
      this.tracking.set(null);
      this.eta.set(null);
      this.isLoadingTracking.set(false);
      this.trackingError.set(true);
    };

    if (statut === 'EN_COURS') {
      this.pollSub = interval(POLL_MS)
        .pipe(
          startWith(0),
          switchMap(() => refresh$()),
        )
        .subscribe({ next: apply, error: onError });
      return;
    }

    refresh$().subscribe({ next: apply, error: onError });
  }

  private stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }
}
