export enum UserRole {
  Bureau = 'BUREAU',
  Chargeur = 'CHARGEUR',
  Admin = 'ADMIN',
}

const ADMIN_ROLE_PATTERNS = ['ADMIN', 'BACK_OFFICE', 'BACKOFFICE', 'OPERATEUR', 'OPERATOR'];
const CHARGEUR_ROLE_PATTERNS = ['CHARGEUR', 'SHIPPER', 'CLIENT'];

export function normalizeRole(role: string | null | undefined): UserRole {
  if (!role) {
    return UserRole.Bureau;
  }

  const normalized = role.trim().toUpperCase();

  if (ADMIN_ROLE_PATTERNS.some((pattern) => normalized.includes(pattern))) {
    return UserRole.Admin;
  }

  if (CHARGEUR_ROLE_PATTERNS.some((pattern) => normalized.includes(pattern))) {
    return UserRole.Chargeur;
  }

  return UserRole.Bureau;
}

export function getHomeRouteForRole(role: UserRole): string {
  switch (role) {
    case UserRole.Admin:
      return '/admin';
    case UserRole.Chargeur:
      return '/chargeur';
    default:
      return '/bureau';
  }
}
