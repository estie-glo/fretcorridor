import { Component, computed, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../core/auth/auth.service';
import { getTenantDisplayName } from '../../shared/models/tenant-config.model';
import { BrandLogoComponent } from '../../shared/components/brand-logo/brand-logo.component';
import { ToastHostComponent } from '../../shared/components/toast-host/toast-host.component';
import { ShellNavComponent } from './shell-nav.component';
import { LanguageSwitcherComponent } from '../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    TranslatePipe,
    LanguageSwitcherComponent,
    ShellNavComponent,
    BrandLogoComponent,
    ToastHostComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly tenantName = computed(() => getTenantDisplayName(this.authService.tenantConfig()));

  async logout(): Promise<void> {
    await this.authService.logout();
    await this.router.navigateByUrl('/login');
  }
}
