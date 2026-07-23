import { Routes } from '@angular/router';

import { AxesListComponent } from './axes/axes-list.component';
import { MissionsListComponent } from './missions/missions-list.component';
import { NotificationsListComponent } from '../notifications/notifications-list.component';
import { DashboardHomeComponent } from '../../shared/components/dashboard/dashboard-home.component';

export const bureauRoutes: Routes = [
  {
    path: '',
    component: DashboardHomeComponent,
  },
  {
    path: 'axes',
    component: AxesListComponent,
  },
  {
    path: 'missions',
    component: MissionsListComponent,
  },
  {
    path: 'notifications',
    component: NotificationsListComponent,
  },
];
