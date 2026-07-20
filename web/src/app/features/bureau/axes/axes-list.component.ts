import { Component } from '@angular/core';

import { AxesExplorerComponent } from '../../../shared/components/axes-explorer/axes-explorer.component';

@Component({
  selector: 'app-axes-list',
  imports: [AxesExplorerComponent],
  template: `<app-axes-explorer scope="AXES" />`,
})
export class AxesListComponent {}
