import { TenantConfig } from './tenant-config.model';

export interface LoginRequest {
  telephone: string;
  pin: string;
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
