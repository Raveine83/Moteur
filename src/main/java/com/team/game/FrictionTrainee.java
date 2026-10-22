package com.team.game;

/**
 * Traînée (frottement d'un fluide comme l'air ou l'eau) :
 *
 *   F = -v_unitaire * (k1 * |v| + k2 * |v|^2)
 *
 * k1 donne un frottement proportionnel à la vitesse, k2 un frottement
 * proportionnel au carré de la vitesse (important quand l'objet va vite).
 * La force s'oppose toujours à la vitesse.
 */
public class FrictionTrainee implements GenerateurForce
{
    private final double k1;
    private final double k2;

    public FrictionTrainee(double k1, double k2)
    {
        if (k1 < 0 || k2 < 0)
        {
            throw new IllegalArgumentException(
                "Les coefficients de traînée ne peuvent pas être négatifs."
            );
        }

        this.k1 = k1;
        this.k2 = k2;
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        double vitesse = particule.getVitesse().norme();

        if (vitesse == 0)
        {
            return;
        }

        double intensite = k1 * vitesse + k2 * vitesse * vitesse;

        // -v_unitaire * intensité
        particule.ajouterForce(
            particule.getVitesse().copie().normaliser().fois(-intensite)
        );
    }
}
