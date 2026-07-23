import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { UserRole } from '../../shared/models/user-role.enum';
import { NotificationsService } from '../../shared/services/notifications.service';

interface ShellNavItem {
  path: string;
  labelKey: string;
  exact?: boolean;
  badge?: number;
}

@Component({
  selector: 'app-shell-nav',
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './shell-nav.component.html',
  styleUrl: './shell-nav.component.scss',
})
export class ShellNavComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly notificationsService = inject(NotificationsService);

  readonly unreadCount = signal(0);

  readonly navItems = computed((): ShellNavItem[] => {
    const badge = this.unreadCount();
    const notifBadge = badge > 0 ? badge : undefined;

    switch (this.authService.normalizedRole()) {
      case UserRole.Bureau:
        return [
          { path: '/bureau', labelKey: 'NAV.DASHBOARD', exact: true },
          { path: '/bureau/axes', labelKey: 'NAV.BUREAU_AXES', exact: false },
          { path: '/bureau/missions', labelKey: 'NAV.BUREAU_MISSIONS', exact: false },
          { path: '/bureau/notifications', labelKey: 'NAV.NOTIFICATIONS', exact: false, badge: notifBadge },
        ];
      case UserRole.Chargeur:
        return [
          { path: '/chargeur', labelKey: 'NAV.DASHBOARD', exact: true },
          { path: '/chargeur/distribution', labelKey: 'NAV.CHARGEUR_DISTRIBUTION', exact: false },
          { path: '/chargeur/offres', labelKey: 'NAV.CHARGEUR_OFFRES', exact: false },
          { path: '/chargeur/notifications', labelKey: 'NAV.NOTIFICATIONS', exact: false, badge: notifBadge },
        ];
      case UserRole.Admin:
        return [
          { path: '/admin', labelKey: 'NAV.DASHBOARD', exact: true },
          { path: '/admin/kyc', labelKey: 'ADMIN.KYC_NAV', exact: false },
          { path: '/admin/chauffeurs', labelKey: 'ADMIN.CHAUFFEURS_NAV', exact: false },
          { path: '/admin/missions', labelKey: 'NAV.BUREAU_MISSIONS', exact: false },
          { path: '/admin/notifications', labelKey: 'NAV.NOTIFICATIONS', exact: false, badge: notifBadge },
          { path: '/admin/audit', labelKey: 'ADMIN.AUDIT_NAV', exact: false },
        ];
      default:
        return [];
    }
  });

  ngOnInit(): void {
    this.refreshUnreadCount();
  }

  private refreshUnreadCount(): void {
    this.notificationsService.unreadCount().subscribe({
      next: (count) => this.unreadCount.set(count),
      error: () => this.unreadCount.set(0),
    });
  }
}
