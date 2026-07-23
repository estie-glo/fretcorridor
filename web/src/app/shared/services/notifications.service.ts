import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AppNotification,
  parseNotificationItem,
  parseNotificationsResponse,
} from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private readonly http = inject(HttpClient);

  list(): Observable<AppNotification[]> {
    return this.http
      .get<unknown>(`${environment.apiUrl}/notifications`)
      .pipe(map((response) => parseNotificationsResponse(response)));
  }

  unreadCount(): Observable<number> {
    return this.http
      .get<{ count?: number }>(`${environment.apiUrl}/notifications/non-lues`)
      .pipe(map((r) => r.count ?? 0));
  }

  markRead(id: string): Observable<AppNotification | null> {
    return this.http
      .patch<unknown>(`${environment.apiUrl}/notifications/${id}/lue`, {})
      .pipe(map((response) => parseNotificationItem(response)));
  }
}
