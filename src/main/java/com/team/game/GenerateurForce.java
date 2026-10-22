package com.team.game;

/**
 * Interface commune des générateurs de forces (gravité, friction, ressorts,
 * flottabilité...). Grâce au polymorphisme, le moteur applique n'importe
 * quelle force sans se soucier de sa provenance : le principe de
 * d'Alembert dit que seule la somme vectorielle des forces compte.
 */
public interface GenerateurForce
{
    /**
     * Calcule la force exercée sur la particule et l'ajoute à son
     * accumulateur (particule.ajouterForce). Ne modifie ni la position ni
     * la vitesse.
     *
     * @param particule particule qui subit la force
     * @param dt durée de la frame en secondes (pas toujours utilisée)
     */
    void mettreAJourForce(Particule particule, double dt);
}
