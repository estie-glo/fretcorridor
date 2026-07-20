import { EnvironmentProviders, inject, makeEnvironmentProviders, provideAppInitializer } from '@angular/core';

import { TenantThemeService } from '../theme/tenant-theme.service';

export function provideTenantTheme(): EnvironmentProviders {
  return makeEnvironmentProviders([
    provideAppInitializer(() => {
      inject(TenantThemeService);
    }),
  ]);
}
