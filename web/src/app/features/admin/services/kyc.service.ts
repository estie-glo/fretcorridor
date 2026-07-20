import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { KycSummary, parseKycPendingResponse } from '../../../shared/models/kyc.model';

@Injectable({ providedIn: 'root' })
export class KycService {
  private readonly http = inject(HttpClient);

  getPendingKyc(): Observable<KycSummary[]> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/admin/kyc/en-attente`)
      .pipe(map((response) => parseKycPendingResponse(response)));
  }

  validateKyc(kycId: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/admin/kyc/${kycId}/valider`, {});
  }
}
