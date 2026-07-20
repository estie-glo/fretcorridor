import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';

export const guestGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  await authService.waitForInit();

  if (!authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree([authService.getHomeRoute()]);
};
