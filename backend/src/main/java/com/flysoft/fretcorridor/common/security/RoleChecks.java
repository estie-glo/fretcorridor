package com.flysoft.fretcorridor.common.security;

/**
 * Helpers RBAC légers pour les contrôleurs (MVP).
 */
public final class RoleChecks {

    private RoleChecks() {}

    public static boolean isBackOffice(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.toUpperCase();
        return "AGENT".equals(normalized)
                || "ADMIN".equals(normalized)
                || "OPERATEUR".equals(normalized)
                || "BUREAU".equals(normalized)
                || normalized.contains("BACK_OFFICE")
                || normalized.contains("BACKOFFICE");
    }

    public static boolean isBureauOrBackOffice(String role) {
        return isBackOffice(role) || "CHARGEUR".equalsIgnoreCase(role);
    }

    public static boolean isChargeur(String role) {
        return role != null && "CHARGEUR".equalsIgnoreCase(role);
    }

    /** Chargeur ou back-office : lecture marketplace / offres camion vide. */
    public static boolean canReadOffres(String role) {
        return isBureauOrBackOffice(role);
    }

    /** Bureau / chargeur / back-office : transitions de statut mission (S6). */
    public static boolean canTransitionMission(String role) {
        return isBureauOrBackOffice(role);
    }
}
