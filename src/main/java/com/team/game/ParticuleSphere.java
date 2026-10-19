package com.team.game;

/**
 * Particule approximée par une sphère (un cercle en 2D) pour la détection
 * de collisions.
 */
public class ParticuleSphere extends Particule
{
    private double rayon;

    public ParticuleSphere(double rayon)
    {
        super();
        setRayon(rayon);
    }

    public ParticuleSphere(
        Vecteur3D position,
        Vecteur3D vitesse,
        Vecteur3D acceleration,
        double masse,
        double damping,
        double rayon)
    {
        super(position, vitesse, acceleration, masse, damping);
        setRayon(rayon);
    }

    public double getRayon()
    {
        return rayon;
    }

    public void setRayon(double rayon)
    {
        if (rayon <= 0)
        {
            throw new IllegalArgumentException(
                "Le rayon doit être strictement positif."
            );
        }

        this.rayon = rayon;
    }
}
