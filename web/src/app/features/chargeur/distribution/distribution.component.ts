import { Component } from '@angular/core';

import { AxesExplorerComponent } from '../../../shared/components/axes-explorer/axes-explorer.component';

@Component({
  selector: 'app-chargeur-distribution',
  imports: [AxesExplorerComponent],
  template: `<app-axes-explorer scope="CHARGEUR" />`,
})
export class ChargeurDistributionComponent {}
