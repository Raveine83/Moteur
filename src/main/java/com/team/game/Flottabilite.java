package com.team.game;

/**
 * Flottabilité (poussée d'Archimède) : force vers le haut égale au poids
 * du liquide déplacé, F = densité * volume immergé.
 *
 * - Totalement hors du liquide : aucune force.
 * - Partiellement immergée : force proportionnelle à la profondeur.
 * - Totalement immergée : force maximale densité * volume.
 *
 * L'objet est décrit par sa profondeur maximale (la moitié de sa hauteur)
 * et son volume. La hauteur est mesurée le long du vecteur "haut".
 */
public class Flottabilite implements GenerateurForce
{
    private final double profondeurMax;
    private final double volume;
    private final double hauteurLiquide;
    private final double densiteLiquide;
    private final Vecteur3D haut;

    /**
     * @param profondeurMax profondeur à laquelle l'objet est entièrement
     *                      immergé (> 0)
     * @param volume volume de l'objet (> 0)
     * @param hauteurLiquide hauteur de la surface du liquide le long de
     *                       l'axe "haut"
     * @param densiteLiquide densité du liquide (1000 pour l'eau en SI)
     * @param haut direction opposée à la gravité (sera normalisée) ; avec
     *             l'axe y vers le bas de Processing, c'est (0, -1, 0)
     */
    public Flottabilite(
        double profondeurMax,
        double volume,
        double hauteurLiquide,
        double densiteLiquide,
        Vecteur3D haut)
    {
        if (profondeurMax <= 0 || volume <= 0 || densiteLiquide < 0)
        {
            throw new IllegalArgumentException(
                "La profondeur et le volume doivent être strictement positifs "
                + "et la densité ne peut pas être négative."
            );
        }

        if (haut == null || haut.normeCarre() == 0)
        {
            throw new IllegalArgumentException(
                "La direction \"haut\" doit être non nulle."
            );
        }

        this.profondeurMax = profondeurMax;
        this.volume = volume;
        this.hauteurLiquide = hauteurLiquide;
        this.densiteLiquide = densiteLiquide;
        this.haut = haut.copie().normaliser();
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        double hauteurParticule = particule.getPosition().produitScalaire(haut);

        // Hors du liquide.
        if (hauteurParticule >= hauteurLiquide + profondeurMax)
        {
            return;
        }

        double intensite;

        if (hauteurParticule <= hauteurLiquide - profondeurMax)
        {
            // Totalement immergée.
            intensite = densiteLiquide * volume;
        }
        else
        {
            // Partiellement immergée : fraction du volume sous la surface.
            double fraction =
                (hauteurLiquide + profondeurMax - hauteurParticule)
                / (2 * profondeurMax);
            intensite = densiteLiquide * volume * fraction;
        }

        particule.ajouterForce(haut.fois(intensite));
    }
}
