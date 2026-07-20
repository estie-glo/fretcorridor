import { Injectable } from '@angular/core';

const REFRESH_TOKEN_KEY = 'fc_refresh_token';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  }

  clearRefreshToken(): void {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
