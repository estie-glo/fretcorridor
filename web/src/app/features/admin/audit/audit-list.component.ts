import { Component, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { AuditEntry } from '../../../shared/models/audit.model';
import { AuditService } from '../services/audit.service';

@Component({
  selector: 'app-audit-list',
  imports: [TranslatePipe],
  templateUrl: './audit-list.component.html',
  styleUrl: './audit-list.component.scss',
})
export class AuditListComponent {
  private readonly auditService = inject(AuditService);

  readonly entries = signal<AuditEntry[]>([]);
  readonly selectedId = signal<string | null>(null);
  readonly selectedEntry = signal<AuditEntry | null>(null);
  readonly isLoading = signal(true);
  readonly hasError = signal(false);

  constructor() {
    this.reload();
  }

  isSelected(id: string): boolean {
    return this.selectedId() === id;
  }

  select(id: string): void {
    const entry = this.entries().find((e) => e.id === id) ?? null;
    this.selectedId.set(id);
    this.selectedEntry.set(entry);
  }

  label(entry: AuditEntry): string {
    return entry.action || entry.id;
  }

  meta(entry: AuditEntry): string {
    const parts: string[] = [];
    if (entry.ressourceType) {
      parts.push(entry.ressourceType);
    }
    if (entry.horodatage) {
      parts.push(this.formatDate(entry.horodatage));
    }
    return parts.join(' · ');
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
  }

  private reload(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.auditService.list().subscribe({
      next: (list) => {
        this.entries.set(list);
        this.isLoading.set(false);
        if (list.length === 1) {
          this.select(list[0].id);
        }
      },
      error: () => {
        this.entries.set([]);
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }
}
