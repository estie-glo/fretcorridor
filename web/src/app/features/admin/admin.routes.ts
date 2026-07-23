import { Routes } from '@angular/router';

import { AdminPageComponent } from './admin-page/admin-page.component';
import { ChauffeursListComponent } from './chauffeurs/chauffeurs-list.component';
import { KycPendingListComponent } from './kyc/kyc-pending-list.component';
import { AuditListComponent } from './audit/audit-list.component';
import { MissionsListComponent } from '../bureau/missions/missions-list.component';
import { NotificationsListComponent } from '../notifications/notifications-list.component';
import { DashboardHomeComponent } from '../../shared/components/dashboard/dashboard-home.component';

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminPageComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: DashboardHomeComponent,
      },
      {
        path: 'kyc',
        component: KycPendingListComponent,
      },
      {
        path: 'chauffeurs',
        component: ChauffeursListComponent,
      },
      {
        path: 'missions',
        component: MissionsListComponent,
      },
      {
        path: 'notifications',
        component: NotificationsListComponent,
      },
      {
        path: 'audit',
        component: AuditListComponent,
      },
    ],
  },
];
