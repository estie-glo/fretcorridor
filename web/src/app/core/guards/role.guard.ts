import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { UserRole } from '../../shared/models/user-role.enum';

export function roleGuard(allowedRoles: UserRole[]): CanActivateFn {
  return async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    await authService.waitForInit();

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }

    if (authService.hasRole(allowedRoles)) {
      return true;
    }

    return router.createUrlTree([authService.getHomeRoute()]);
  };
}
