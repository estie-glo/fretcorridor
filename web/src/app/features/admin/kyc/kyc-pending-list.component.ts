import { Component, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { KycSummary, getKycDisplayLabel, getKycDisplayMeta } from '../../../shared/models/kyc.model';
import { ToastService } from '../../../shared/services/toast.service';
import { toRecordEntries } from '../../../shared/utils/record-display';
import { KycNiveau, KycService } from '../services/kyc.service';

@Component({
  selector: 'app-kyc-pending-list',
  imports: [TranslatePipe],
  templateUrl: './kyc-pending-list.component.html',
  styleUrl: './kyc-pending-list.component.scss',
})
export class KycPendingListComponent {
  private readonly kycService = inject(KycService);
  private readonly toastService = inject(ToastService);

  readonly pendingKyc = signal<KycSummary[]>([]);
  readonly selectedKycId = signal<string | null>(null);
  readonly selectedEntries = signal<{ key: string; value: string }[]>([]);
  readonly validatingId = signal<string | null>(null);
  readonly isLoading = signal(true);
  readonly loadError = signal(false);
  readonly validateError = signal(false);
  readonly validateSuccess = signal(false);
  readonly docsMissing = signal(false);

  constructor() {
    this.loadPendingKyc();
  }

  getKycLabel(kyc: KycSummary): string {
    return getKycDisplayLabel(kyc);
  }

  getKycMeta(kyc: KycSummary): string {
    return getKycDisplayMeta(kyc);
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
    this.docsMissing.set(false);
  }

  validateSelected(niveau: KycNiveau): void {
    const kycId = this.selectedKycId();

    if (!kycId || this.validatingId()) {
      return;
    }

    this.validatingId.set(kycId);
    this.validateError.set(false);
    this.validateSuccess.set(false);
    this.docsMissing.set(false);

    this.kycService.validateKyc(kycId, niveau).subscribe({
      next: () => {
        this.validatingId.set(null);
        this.validateSuccess.set(true);
        this.toastService.show('KYC.VALIDATE_SUCCESS', 'success');
        this.pendingKyc.update((items) => items.filter((item) => item.id !== kycId));
        this.selectedKycId.set(null);
        this.selectedEntries.set([]);
      },
      error: (err) => {
        this.validatingId.set(null);
        if (err?.status === 400) {
          this.docsMissing.set(true);
          this.toastService.show('KYC.DOCS_MISSING', 'error');
        } else {
          this.validateError.set(true);
          this.toastService.show('KYC.VALIDATE_ERROR', 'error');
        }
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
