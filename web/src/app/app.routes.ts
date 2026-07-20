import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { authRoutes } from './features/auth/auth.routes';
import { bureauRoutes } from './features/bureau/bureau.routes';
import { adminRoutes } from './features/admin/admin.routes';
import { chargeurRoutes } from './features/chargeur/chargeur.routes';
import { ShellComponent } from './layout/shell/shell.component';
import { UserRole } from './shared/models/user-role.enum';

function createShellRoute(allowedRoles: UserRole[], children: Routes) {
  return {
    component: ShellComponent,
    canActivate: [authGuard, roleGuard(allowedRoles)],
    children,
  };
}

export const routes: Routes = [
  ...authRoutes,
  {
    path: 'bureau',
    ...createShellRoute([UserRole.Bureau], bureauRoutes),
  },
  {
    path: 'chargeur',
    ...createShellRoute([UserRole.Chargeur], chargeurRoutes),
  },
  {
    path: 'admin',
    ...createShellRoute([UserRole.Admin], adminRoutes),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
