import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { AuditEntry, parseAuditResponse } from '../../../shared/models/audit.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);

  list(): Observable<AuditEntry[]> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/admin/audit`)
      .pipe(map((response) => parseAuditResponse(response)));
  }
}
