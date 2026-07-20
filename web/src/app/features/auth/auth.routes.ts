import { Routes } from '@angular/router';

import { guestGuard } from '../../core/guards/guest.guard';
import { LoginComponent } from './login/login.component';

export const authRoutes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard],
  },
];
