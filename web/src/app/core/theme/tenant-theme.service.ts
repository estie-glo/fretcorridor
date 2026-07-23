import { Injectable, effect, inject } from '@angular/core';

import { AuthService } from '../auth/auth.service';
import { TenantConfig, getResolvedPrimaryColor } from '../../shared/models/tenant-config.model';

@Injectable({ providedIn: 'root' })
export class TenantThemeService {
  private readonly authService = inject(AuthService);

  constructor() {
    effect(() => {
      this.applyTheme(this.authService.tenantConfig());
    });
  }

  private applyTheme(config: TenantConfig | null): void {
    const primary = getResolvedPrimaryColor(config);
    const root = document.documentElement;

    root.style.setProperty('--fc-primary', primary);
    root.style.setProperty('--fc-primary-hover', `color-mix(in srgb, ${primary} 85%, #000000)`);
    root.style.setProperty('--fc-primary-soft', `color-mix(in srgb, ${primary} 8%, #ffffff)`);
    root.style.setProperty('--fc-primary-contrast', '#ffffff');
  }
}
