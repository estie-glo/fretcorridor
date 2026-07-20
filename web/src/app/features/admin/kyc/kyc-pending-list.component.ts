import { Component, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { KycSummary, getKycDisplayLabel } from '../../../shared/models/kyc.model';
import { toRecordEntries } from '../../../shared/utils/record-display';
import { KycService } from '../services/kyc.service';

@Component({
  selector: 'app-kyc-pending-list',
  imports: [TranslatePipe],
  templateUrl: './kyc-pending-list.component.html',
  styleUrl: './kyc-pending-list.component.scss',
})
export class KycPendingListComponent {
  private readonly kycService = inject(KycService);

  readonly pendingKyc = signal<KycSummary[]>([]);
  readonly selectedKycId = signal<string | null>(null);
  readonly selectedEntries = signal<{ key: string; value: string }[]>([]);
  readonly validatingId = signal<string | null>(null);
  readonly isLoading = signal(true);
  readonly loadError = signal(false);
  readonly validateError = signal(false);
  readonly validateSuccess = signal(false);

  constructor() {
    this.loadPendingKyc();
  }

  getKycLabel(kyc: KycSummary): string {
    return getKycDisplayLabel(kyc);
  }

  isSelected(kycId: string): boolean {
    return this.selectedKycId() === kycId;
  }

  isValidating(kycId: string): boolean {
    return this.validatingId() === kycId;
  }

  selectKyc(kycId: string): void {
    const kyc = this.pendingKyc().find((item) => item.id === kycId);

    this.selectedKycId.set(kycId);
    this.selectedEntries.set(kyc ? toRecordEntries(kyc.raw) : []);
    this.validateError.set(false);
    this.validateSuccess.set(false);
  }

  validateSelected(): void {
    const kycId = this.selectedKycId();

    if (!kycId || this.validatingId()) {
      return;
    }

    this.validatingId.set(kycId);
    this.validateError.set(false);
    this.validateSuccess.set(false);

    this.kycService.validateKyc(kycId).subscribe({
      next: () => {
        this.validatingId.set(null);
        this.validateSuccess.set(true);
        this.pendingKyc.update((items) => items.filter((item) => item.id !== kycId));
        this.selectedKycId.set(null);
        this.selectedEntries.set([]);
      },
      error: () => {
        this.validatingId.set(null);
        this.validateError.set(true);
      },
    });
  }

  private loadPendingKyc(): void {
    this.isLoading.set(true);
    this.loadError.set(false);

    this.kycService.getPendingKyc().subscribe({
      next: (items) => {
        this.pendingKyc.set(items);
        this.isLoading.set(false);

        if (items.length === 1) {
          this.selectKyc(items[0].id);
        }
      },
      error: () => {
        this.pendingKyc.set([]);
        this.loadError.set(true);
        this.isLoading.set(false);
      },
    });
  }
}
