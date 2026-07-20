import { Routes } from '@angular/router';

import { AdminPageComponent } from './admin-page/admin-page.component';
import { ChauffeursListComponent } from './chauffeurs/chauffeurs-list.component';
import { KycPendingListComponent } from './kyc/kyc-pending-list.component';

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminPageComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'kyc',
      },
      {
        path: 'kyc',
        component: KycPendingListComponent,
      },
      {
        path: 'chauffeurs',
        component: ChauffeursListComponent,
      },
    ],
  },
];
