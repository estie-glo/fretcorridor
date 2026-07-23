import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../../environments/environment';
import {
  ChauffeurDetail,
  ChauffeurSummary,
  parseChauffeurDetailResponse,
  parseChauffeursResponse,
} from '../../../shared/models/chauffeur.model';

@Injectable({ providedIn: 'root' })
export class ChauffeursService {
  private readonly http = inject(HttpClient);

  getChauffeurs(): Observable<ChauffeurSummary[]> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/admin/chauffeurs`)
      .pipe(map((response) => parseChauffeursResponse(response)));
  }

  getChauffeur(id: string): Observable<ChauffeurDetail | null> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/chauffeurs/${id}`)
      .pipe(map((response) => parseChauffeurDetailResponse(response)));
  }
}
