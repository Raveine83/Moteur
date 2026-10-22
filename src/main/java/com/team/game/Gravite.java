package com.team.game;

/**
 * Gravité : accélération constante g. La force vaut F = m * g, ce qui
 * donne la même accélération à toutes les particules quelle que soit leur
 * masse. Les particules de masse infinie (inverseMasse = 0) y échappent.
 */
public class Gravite implements GenerateurForce
{
    private final Vecteur3D gravite;

    /**
     * @param gravite accélération de la gravité (ex. (0, 400, 0) en pixels
     *                avec l'axe y vers le bas)
     */
    public Gravite(Vecteur3D gravite)
    {
        this.gravite = gravite.copie();
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        if (particule.getInverseMasse() <= 0)
        {
            return;
        }

        // F = m * g
        particule.ajouterForce(gravite.fois(particule.getMasse()));
    }

    public Vecteur3D getGravite()
    {
        return gravite.copie();
    }
}
