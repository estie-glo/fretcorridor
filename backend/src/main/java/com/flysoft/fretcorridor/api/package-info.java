/**
 * Couche HTTP (contrôleurs REST) du backend FretCorridor.
 *
 * <p>Organisation par client :
 * <ul>
 *   <li>{@link com.flysoft.fretcorridor.api.shared} — endpoints communs (auth, axes…)</li>
 *   <li>{@link com.flysoft.fretcorridor.api.web} — portail Angular (bureau, chargeur, admin)</li>
 *   <li>{@link com.flysoft.fretcorridor.api.mobile} — app Flutter (chauffeur, agent)</li>
 * </ul>
 *
 * <p>La logique métier, les entités et la sécurité restent dans
 * {@code com.flysoft.fretcorridor.common}.
 */
package com.flysoft.fretcorridor.api;
