import type { TenantConfig } from './tenant-config.model';

export interface LoginRequest {
  telephone: string;
  /** Aligné sur le backend (`codePin`). */
  codePin: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  role: string;
  tenantId: string;
  configTenant: TenantConfig;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface AuthErrorResponse {
  code?: string;
  message?: string;
  tentativesRestantes?: number;
}

export class AuthError extends Error {
  constructor(
    readonly code: string,
    message?: string,
    readonly tentativesRestantes?: number,
  ) {
    super(message ?? code);
    this.name = 'AuthError';
  }
}
