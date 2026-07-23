package com.flysoft.fretcorridor.common;

import com.flysoft.fretcorridor.common.security.RoleChecks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleChecksTest {

    @Test
    void isBackOffice_accepteOperateurAgentAdmin() {
        assertTrue(RoleChecks.isBackOffice("OPERATEUR"));
        assertTrue(RoleChecks.isBackOffice("AGENT"));
        assertTrue(RoleChecks.isBackOffice("ADMIN"));
        assertTrue(RoleChecks.isBackOffice("BACK_OFFICE"));
        assertFalse(RoleChecks.isBackOffice("CHAUFFEUR"));
        assertFalse(RoleChecks.isBackOffice(null));
    }

    @Test
    void isBureauOrBackOffice_inclutChargeur() {
        assertTrue(RoleChecks.isBureauOrBackOffice("CHARGEUR"));
        assertTrue(RoleChecks.isBureauOrBackOffice("OPERATEUR"));
        assertFalse(RoleChecks.isBureauOrBackOffice("CHAUFFEUR"));
    }

    @Test
    void canReadOffres_chargeurEtBureau() {
        assertTrue(RoleChecks.canReadOffres("CHARGEUR"));
        assertTrue(RoleChecks.canReadOffres("OPERATEUR"));
        assertTrue(RoleChecks.isChargeur("CHARGEUR"));
        assertFalse(RoleChecks.canReadOffres("CHAUFFEUR"));
        assertFalse(RoleChecks.isChargeur("AGENT"));
    }

    @Test
    void canTransitionMission_bureauChargeur() {
        assertTrue(RoleChecks.canTransitionMission("OPERATEUR"));
        assertTrue(RoleChecks.canTransitionMission("CHARGEUR"));
        assertFalse(RoleChecks.canTransitionMission("CHAUFFEUR"));
    }
}
