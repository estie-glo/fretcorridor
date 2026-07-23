import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { catchError, forkJoin, of } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { UserRole } from '../../../shared/models/user-role.enum';
import { AxesService } from '../../../shared/services/axes.service';
import { MissionsService } from '../../../shared/services/missions.service';
import { NotificationsService } from '../../../shared/services/notifications.service';
import { KycService } from '../../../features/admin/services/kyc.service';
import { AuditService } from '../../../features/admin/services/audit.service';
import { ChauffeursService } from '../../../features/admin/services/chauffeurs.service';

interface KpiTile {
  labelKey: string;
  value: number | string;
  link?: string;
  accent?: 'primary' | 'success' | 'warning';
}

interface StatBar {
  label: string;
  count: number;
  variant: 'primary' | 'success' | 'warning' | 'neutral';
}

@Component({
  selector: 'app-dashboard-home',
  imports: [TranslatePipe, RouterLink],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.scss',
})
export class DashboardHomeComponent {
  private readonly authService = inject(AuthService);
  private readonly axesService = inject(AxesService);
  private readonly missionsService = inject(MissionsService);
  private readonly notificationsService = inject(NotificationsService);
  private readonly kycService = inject(KycService);
  private readonly auditService = inject(AuditService);
  private readonly chauffeursService = inject(ChauffeursService);

  readonly isLoading = signal(true);
  readonly hasError = signal(false);
  readonly kpis = signal<KpiTile[]>([]);
  readonly missionBars = signal<StatBar[]>([]);
  readonly axeBars = signal<StatBar[]>([]);

  readonly role = computed(() => this.authService.normalizedRole());
  readonly isAdmin = computed(() => this.role() === UserRole.Admin);
  readonly isChargeur = computed(() => this.role() === UserRole.Chargeur);

  constructor() {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    const role = this.role();

    if (role === UserRole.Admin) {
      this.loadAdminDashboard();
      return;
    }

    if (role === UserRole.Chargeur) {
      this.loadChargeurDashboard();
      return;
    }

    this.loadBureauDashboard();
  }

  private loadBureauDashboard(): void {
    forkJoin({
      axes: this.axesService.getAxes().pipe(catchError(() => of([]))),
      missions: this.missionsService.getMissions().pipe(catchError(() => of([]))),
      notifs: this.notificationsService.unreadCount().pipe(catchError(() => of(0))),
    }).subscribe({
      next: ({ axes, missions, notifs }) => {
        const actifs = axes.filter((a) => a.etatActivation === 'ACTIF').length;
        const verrouilles = axes.filter((a) => a.etatActivation === 'VERROUILLE').length;
        const enCours = missions.filter((m) => m.statut === 'EN_COURS').length;
        const offres = missions.filter((m) => m.statut === 'CAMION_VIDE_DECLARE').length;

        this.kpis.set([
          { labelKey: 'DASHBOARD.KPI_AXES_ACTIFS', value: actifs, link: '/bureau/axes', accent: 'success' },
          { labelKey: 'DASHBOARD.KPI_AXES_VERROUILLES', value: verrouilles, link: '/bureau/axes', accent: 'warning' },
          { labelKey: 'DASHBOARD.KPI_MISSIONS_EN_COURS', value: enCours, link: '/bureau/missions', accent: 'primary' },
          { labelKey: 'DASHBOARD.KPI_OFFRES_VIDES', value: offres, link: '/bureau/missions', accent: 'warning' },
          { labelKey: 'DASHBOARD.KPI_NOTIFS', value: notifs, link: '/bureau/notifications' },
        ]);

        this.missionBars.set(this.buildMissionBars(missions.map((m) => m.statut ?? '')));
        this.axeBars.set([
          { label: 'ACTIF', count: actifs, variant: 'success' },
          { label: 'VERROUILLE', count: verrouilles, variant: 'warning' },
          { label: 'INACTIF', count: axes.length - actifs - verrouilles, variant: 'neutral' },
        ]);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }

  private loadAdminDashboard(): void {
    forkJoin({
      kyc: this.kycService.getPendingKyc().pipe(catchError(() => of([]))),
      chauffeurs: this.chauffeursService.getChauffeurs().pipe(catchError(() => of([]))),
      missions: this.missionsService.getMissions().pipe(catchError(() => of([]))),
      audit: this.auditService.list().pipe(catchError(() => of([]))),
      notifs: this.notificationsService.unreadCount().pipe(catchError(() => of(0))),
    }).subscribe({
      next: ({ kyc, chauffeurs, missions, audit, notifs }) => {
        const actives = missions.filter((m) =>
          ['EN_COURS', 'MISSION_ACCEPTEE', 'CAMION_VIDE_DECLARE', 'MATCH_PROPOSE'].includes(
            m.statut ?? '',
          ),
        ).length;

        this.kpis.set([
          { labelKey: 'DASHBOARD.KPI_KYC_PENDING', value: kyc.length, link: '/admin/kyc', accent: 'warning' },
          { labelKey: 'DASHBOARD.KPI_CHAUFFEURS', value: chauffeurs.length, link: '/admin/chauffeurs' },
          { labelKey: 'DASHBOARD.KPI_MISSIONS_ACTIVES', value: actives, link: '/admin/missions', accent: 'primary' },
          { labelKey: 'DASHBOARD.KPI_AUDIT', value: audit.length, link: '/admin/audit' },
          { labelKey: 'DASHBOARD.KPI_NOTIFS', value: notifs, link: '/admin/notifications' },
        ]);

        this.missionBars.set(this.buildMissionBars(missions.map((m) => m.statut ?? '')));
        this.axeBars.set([]);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }

  private loadChargeurDashboard(): void {
    forkJoin({
      axes: this.axesService.getAxes().pipe(catchError(() => of([]))),
      offres: this.missionsService.getOffres().pipe(catchError(() => of([]))),
      notifs: this.notificationsService.unreadCount().pipe(catchError(() => of(0))),
    }).subscribe({
      next: ({ axes, offres, notifs }) => {
        const actifs = axes.filter((a) => a.etatActivation === 'ACTIF').length;

        this.kpis.set([
          { labelKey: 'DASHBOARD.KPI_AXES_ACTIFS', value: actifs, link: '/chargeur/distribution', accent: 'success' },
          { labelKey: 'DASHBOARD.KPI_OFFRES_DISPONIBLES', value: offres.length, link: '/chargeur/offres', accent: 'primary' },
          { labelKey: 'DASHBOARD.KPI_NOTIFS', value: notifs, link: '/chargeur/notifications' },
        ]);

        this.missionBars.set([]);
        this.axeBars.set([
          { label: 'ACTIF', count: actifs, variant: 'success' },
          { label: 'VERROUILLE', count: axes.filter((a) => a.etatActivation === 'VERROUILLE').length, variant: 'warning' },
          { label: 'INACTIF', count: axes.filter((a) => a.etatActivation === 'INACTIF').length, variant: 'neutral' },
        ]);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }

  private buildMissionBars(statuts: string[]): StatBar[] {
    const counts = new Map<string, number>();
    for (const statut of statuts) {
      const key = statut || 'INCONNU';
      counts.set(key, (counts.get(key) ?? 0) + 1);
    }

    return [...counts.entries()]
      .sort((a, b) => b[1] - a[1])
      .slice(0, 6)
      .map(([label, count]) => ({
        label,
        count,
        variant: label === 'EN_COURS' || label === 'MISSION_ACCEPTEE'
          ? 'primary'
          : label === 'TERMINEE'
            ? 'success'
            : label === 'CAMION_VIDE_DECLARE'
              ? 'warning'
              : 'neutral',
      }));
  }

  barWidth(count: number, bars: StatBar[]): number {
    const max = Math.max(...bars.map((b) => b.count), 1);
    return Math.round((count / max) * 100);
  }
}
