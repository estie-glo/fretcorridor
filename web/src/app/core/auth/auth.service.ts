import { computed, Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RefreshRequest } from '../../shared/models/auth.dto';
import { TenantConfig } from '../../shared/models/tenant-config.model';
import {
  getHomeRouteForRole,
  normalizeRole,
  UserRole,
} from '../../shared/models/user-role.enum';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);

  private accessToken: string | null = null;
  private refreshPromise: Promise<boolean> | null = null;
  private initPromise: Promise<void> | null = null;

  readonly role = signal<string | null>(null);
  readonly tenantId = signal<string | null>(null);
  readonly tenantConfig = signal<TenantConfig | null>(null);
  readonly isAuthenticated = signal(false);
  readonly isLoading = signal(false);
  readonly initComplete = signal(false);

  readonly normalizedRole = computed(() => normalizeRole(this.role()));

  getAccessToken(): string | null {
    return this.accessToken;
  }

  waitForInit(): Promise<void> {
    return this.initPromise ?? Promise.resolve();
  }

  initialize(): Promise<void> {
    if (!this.initPromise) {
      this.initPromise = this.restoreSession();
    }

    return this.initPromise;
  }

  async login(telephone: string, pin: string): Promise<void> {
    this.isLoading.set(true);

    try {
      const payload: LoginRequest = { telephone, pin };
      const response = await firstValueFrom(
        this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, payload),
      );
      this.applyAuthResponse(response);
    } finally {
      this.isLoading.set(false);
    }
  }

  async logout(): Promise<void> {
    const token = this.accessToken;

    this.clearSession();

    if (!token) {
      return;
    }

    try {
      await firstValueFrom(this.http.post<void>(`${environment.apiUrl}/auth/logout`, {}));
    } catch {
      // La session locale est déjà effacée.
    }
  }

  async refreshSession(): Promise<boolean> {
    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    const refreshToken = this.tokenStorage.getRefreshToken();

    if (!refreshToken) {
      this.clearSession();
      return false;
    }

    this.refreshPromise = this.performRefresh(refreshToken).finally(() => {
      this.refreshPromise = null;
    });

    return this.refreshPromise;
  }

  getHomeRoute(): string {
    return getHomeRouteForRole(this.normalizedRole());
  }

  private async restoreSession(): Promise<void> {
    const refreshToken = this.tokenStorage.getRefreshToken();

    if (!refreshToken) {
      this.initComplete.set(true);
      return;
    }

    try {
      await this.refreshSession();
    } catch {
      this.clearSession();
    } finally {
      this.initComplete.set(true);
    }
  }

  private async performRefresh(refreshToken: string): Promise<boolean> {
    try {
      const payload: RefreshRequest = { refreshToken };
      const response = await firstValueFrom(
        this.http.post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, payload),
      );
      this.applyAuthResponse(response);
      return true;
    } catch {
      this.clearSession();
      return false;
    }
  }

  private applyAuthResponse(response: AuthResponse): void {
    this.accessToken = response.accessToken;
    this.tokenStorage.setRefreshToken(response.refreshToken);
    this.role.set(response.role);
    this.tenantId.set(response.tenantId);
    this.tenantConfig.set(response.configTenant);
    this.isAuthenticated.set(true);
  }

  clearSession(): void {
    this.accessToken = null;
    this.tokenStorage.clearRefreshToken();
    this.role.set(null);
    this.tenantId.set(null);
    this.tenantConfig.set(null);
    this.isAuthenticated.set(false);
  }

  hasRole(allowedRoles: UserRole[]): boolean {
    return allowedRoles.includes(this.normalizedRole());
  }
}
