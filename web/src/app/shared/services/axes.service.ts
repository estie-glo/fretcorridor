import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AxeStatut,
  AxeSummary,
  parseAxeStatutResponse,
  parseAxesResponse,
} from '../models/axe.model';

@Injectable({ providedIn: 'root' })
export class AxesService {
  private readonly http = inject(HttpClient);

  getAxes(): Observable<AxeSummary[]> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/axes`)
      .pipe(map((response) => parseAxesResponse(response)));
  }

  getAxeStatut(axeId: string): Observable<AxeStatut | null> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/axes/${axeId}/statut`)
      .pipe(map((response) => parseAxeStatutResponse(response)));
  }
}
