import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { UserRole } from '../../shared/models/user-role.enum';

interface ShellNavItem {
  path: string;
  labelKey: string;
  exact?: boolean;
}

@Component({
  selector: 'app-shell-nav',
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  template: `
    @if (navItems().length > 0) {
      <nav class="shell-nav" [attr.aria-label]="'NAV.LABEL' | translate">
        @for (item of navItems(); track item.path) {
          <a
            class="shell-nav__link"
            routerLinkActive="shell-nav__link--active"
            [routerLinkActiveOptions]="{ exact: item.exact ?? false }"
            [routerLink]="item.path"
          >
            {{ item.labelKey | translate }}
          </a>
        }
      </nav>
    }
  `,
  styles: `
    .shell-nav {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
      border-bottom: 1px solid var(--fc-border);
      background: color-mix(in srgb, var(--fc-primary) 4%, var(--fc-surface));
    }

    .shell-nav__link {
      display: inline-flex;
      align-items: center;
      padding: 0.4rem 0.85rem;
      border-radius: 999px;
      border: 1px solid transparent;
      color: var(--fc-muted);
      font-size: 0.875rem;
      font-weight: 600;
      text-decoration: none;
      transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease;
    }

    .shell-nav__link:hover {
      color: var(--fc-text);
      background: color-mix(in srgb, var(--fc-primary) 8%, transparent);
    }

    .shell-nav__link--active {
      color: var(--fc-primary);
      border-color: color-mix(in srgb, var(--fc-primary) 30%, var(--fc-border));
      background: color-mix(in srgb, var(--fc-primary) 10%, var(--fc-surface));
    }
  `,
})
export class ShellNavComponent {
  private readonly authService = inject(AuthService);

  readonly navItems = computed(() => {
    switch (this.authService.normalizedRole()) {
      case UserRole.Bureau:
        return [{ path: '/bureau', labelKey: 'NAV.BUREAU_AXES', exact: true }];
      case UserRole.Chargeur:
        return [{ path: '/chargeur', labelKey: 'NAV.CHARGEUR_DISTRIBUTION', exact: true }];
      case UserRole.Admin:
        return [
          { path: '/admin/kyc', labelKey: 'ADMIN.KYC_NAV', exact: false },
          { path: '/admin/chauffeurs', labelKey: 'ADMIN.CHAUFFEURS_NAV', exact: false },
        ];
      default:
        return [];
    }
  });
}
