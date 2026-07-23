import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Hub, parseHubsResponse } from '../models/hub.model';

@Injectable({ providedIn: 'root' })
export class HubsService {
  private readonly http = inject(HttpClient);

  getHubs(): Observable<Hub[]> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/hubs`)
      .pipe(map((response) => parseHubsResponse(response)));
  }
}
