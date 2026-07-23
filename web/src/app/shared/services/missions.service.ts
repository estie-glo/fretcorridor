import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  EtaInfo,
  MissionSummary,
  TrackingInfo,
  parseEtaResponse,
  parseMissionItem,
  parseMissionsResponse,
  parseTrackingResponse,
} from '../models/mission.model';

@Injectable({ providedIn: 'root' })
export class MissionsService {
  private readonly http = inject(HttpClient);

  getMissions(filters?: { axeId?: string; statut?: string }): Observable<MissionSummary[]> {
    let params = new HttpParams();
    if (filters?.axeId) {
      params = params.set('axeId', filters.axeId);
    }
    if (filters?.statut) {
      params = params.set('statut', filters.statut);
    }

    return this.http
      .get<unknown>(`${environment.apiUrl}/missions`, { params })
      .pipe(map((response) => parseMissionsResponse(response)));
  }

  /** S4 — offres marketplace (missions CAMION_VIDE_DECLARE). */
  getOffres(filters?: { axeId?: string }): Observable<MissionSummary[]> {
    let params = new HttpParams();
    if (filters?.axeId) {
      params = params.set('axeId', filters.axeId);
    }

    return this.http
      .get<unknown>(`${environment.apiUrl}/missions/offres`, { params })
      .pipe(map((response) => parseMissionsResponse(response)));
  }

  getMission(id: string): Observable<MissionSummary | null> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/missions/${id}`)
      .pipe(map((response) => parseMissionItem(response)));
  }

  getTracking(id: string): Observable<TrackingInfo | null> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/missions/${id}/tracking`)
      .pipe(map((response) => parseTrackingResponse(response)));
  }

  getEta(id: string): Observable<EtaInfo | null> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/missions/${id}/eta`)
      .pipe(map((response) => parseEtaResponse(response)));
  }

  accepter(id: string): Observable<MissionSummary | null> {
    return this.http
      .post<unknown>(`${environment.apiUrl}/missions/${id}/accepter`, {})
      .pipe(map((response) => parseMissionItem(response)));
  }

  demarrer(id: string): Observable<MissionSummary | null> {
    return this.http
      .post<unknown>(`${environment.apiUrl}/missions/${id}/demarrer`, {})
      .pipe(map((response) => parseMissionItem(response)));
  }

  terminer(id: string): Observable<MissionSummary | null> {
    return this.http
      .post<unknown>(`${environment.apiUrl}/missions/${id}/terminer`, {})
      .pipe(map((response) => parseMissionItem(response)));
  }

  annuler(id: string): Observable<MissionSummary | null> {
    return this.http
      .post<unknown>(`${environment.apiUrl}/missions/${id}/annuler`, {})
      .pipe(map((response) => parseMissionItem(response)));
  }
}
