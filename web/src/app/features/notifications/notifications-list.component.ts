import { Component, inject, signal } from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import {
  AppNotification,
  getNotificationBody,
  getNotificationTitle,
} from '../../shared/models/notification.model';
import { NotificationsService } from '../../shared/services/notifications.service';

@Component({
  selector: 'app-notifications-list',
  imports: [TranslatePipe],
  templateUrl: './notifications-list.component.html',
  styleUrl: './notifications-list.component.scss',
})
export class NotificationsListComponent {
  private readonly notificationsService = inject(NotificationsService);
  private readonly translate = inject(TranslateService);

  readonly items = signal<AppNotification[]>([]);
  readonly selectedId = signal<string | null>(null);
  readonly selected = signal<AppNotification | null>(null);
  readonly isLoading = signal(true);
  readonly hasError = signal(false);
  readonly unreadCount = signal(0);

  constructor() {
    this.reload();
  }

  title(n: AppNotification): string {
    return getNotificationTitle(n, this.translate.getCurrentLang() || 'fr');
  }

  body(n: AppNotification): string {
    return getNotificationBody(n, this.translate.getCurrentLang() || 'fr');
  }

  isSelected(id: string): boolean {
    return this.selectedId() === id;
  }

  select(id: string): void {
    const item = this.items().find((n) => n.id === id) ?? null;
    this.selectedId.set(id);
    this.selected.set(item);
    if (item && !item.lue) {
      this.markRead(item);
    }
  }

  private markRead(item: AppNotification): void {
    this.notificationsService.markRead(item.id).subscribe({
      next: (updated) => {
        if (!updated) {
          return;
        }
        this.items.update((list) =>
          list.map((n) => (n.id === updated.id ? updated : n)),
        );
        this.selected.set(updated);
        this.unreadCount.update((c) => Math.max(0, c - 1));
      },
    });
  }

  private reload(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    this.notificationsService.list().subscribe({
      next: (list) => {
        this.items.set(list);
        this.unreadCount.set(list.filter((n) => !n.lue && n.canal === 'IN_APP').length);
        this.isLoading.set(false);
        if (list.length === 1) {
          this.select(list[0].id);
        }
      },
      error: () => {
        this.items.set([]);
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }
}
