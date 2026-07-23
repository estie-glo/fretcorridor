import { Routes } from '@angular/router';

import { ChargeurDistributionComponent } from './distribution/distribution.component';
import { OffresListComponent } from './offres/offres-list.component';
import { NotificationsListComponent } from '../notifications/notifications-list.component';
import { DashboardHomeComponent } from '../../shared/components/dashboard/dashboard-home.component';

export const chargeurRoutes: Routes = [
  {
    path: '',
    component: DashboardHomeComponent,
  },
  {
    path: 'distribution',
    component: ChargeurDistributionComponent,
  },
  {
    path: 'offres',
    component: OffresListComponent,
  },
  {
    path: 'notifications',
    component: NotificationsListComponent,
  },
];
