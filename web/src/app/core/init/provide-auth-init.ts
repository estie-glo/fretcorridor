import { EnvironmentProviders, inject, provideAppInitializer } from '@angular/core';

import { AuthService } from '../auth/auth.service';

export function provideAuthInit(): EnvironmentProviders {
  return provideAppInitializer(() => {
    const authService = inject(AuthService);
    return authService.initialize();
  });
}
