package com.team.game;

public class Particule
{
    private Vecteur3D position;
    private Vecteur3D vitesse;
    private Vecteur3D acceleration;

    private double inverseMasse;
    private double damping;

    /**
     * Constructeur par défaut.
     */
    public Particule()
    {
        this.position = new Vecteur3D();
        this.vitesse = new Vecteur3D();
        this.acceleration = new Vecteur3D();

        this.inverseMasse = 1.0;
        this.damping = 0.999;
    }

    /**
     * Constructeur complet.
     */
    public Particule(
        Vecteur3D position,
        Vecteur3D vitesse,
        Vecteur3D acceleration,
        double masse,
        double damping)
    {
        this.position = position;
        this.vitesse = vitesse;
        this.acceleration = acceleration;

        setMasse(masse);
        this.damping = damping;
    }

    /**
     * Met à jour la position et la vitesse de la particule.
     *
     * @param temps durée de la frame en secondes
     */
    public void integrer(double temps)
    {
        if (temps <= 0)
        {
            return;
        }

        if (inverseMasse <= 0)
        {
            return;
        }

        // Position = Position + Vitesse * dt
        Vecteur3D deplacement = new Vecteur3D(
            vitesse.getX(),
            vitesse.getY(),
            vitesse.getZ()
        );

        deplacement.multiplierParUnScalaire(temps);
        position.additionner(deplacement);

        // Vitesse = Vitesse + Accélération * dt
        Vecteur3D variationVitesse = new Vecteur3D(
            acceleration.getX(),
            acceleration.getY(),
            acceleration.getZ()
        );

        variationVitesse.multiplierParUnScalaire(temps);
        vitesse.additionner(variationVitesse);

        // Application du damping.
        vitesse.multiplierParUnScalaire(
            Math.pow(damping, temps)
        );
    }

    /* ---------- MASSE ---------- */

    public double getInverseMasse()
    {
        return inverseMasse;
    }

    public void setInverseMasse(double inverseMasse)
    {
        if (inverseMasse < 0)
        {
            throw new IllegalArgumentException(
                "L'inverse de la masse ne peut pas être négatif."
            );
        }

        this.inverseMasse = inverseMasse;
    }

    public double getMasse()
    {
        if (inverseMasse == 0)
        {
            return Double.POSITIVE_INFINITY;
        }

        return 1.0 / inverseMasse;
    }

    public void setMasse(double masse)
    {
        if (masse <= 0)
        {
            throw new IllegalArgumentException(
                "La masse doit être strictement positive."
            );
        }

        this.inverseMasse = 1.0 / masse;
    }

    /* ---------- GETTERS ---------- */

    public Vecteur3D getPosition()
    {
        return position;
    }

    public Vecteur3D getVitesse()
    {
        return vitesse;
    }

    public Vecteur3D getAcceleration()
    {
        return acceleration;
    }

    public double getDamping()
    {
        return damping;
    }

    /* ---------- SETTERS ---------- */

    public void setPosition(Vecteur3D position)
    {
        this.position = position;
    }

    public void setVitesse(Vecteur3D vitesse)
    {
        this.vitesse = vitesse;
    }

    public void setAcceleration(Vecteur3D acceleration)
    {
        this.acceleration = acceleration;
    }

    public void setDamping(double damping)
    {
        if (damping < 0 || damping > 1)
        {
            throw new IllegalArgumentException(
                "Le damping doit être compris entre 0 et 1."
            );
        }

        this.damping = damping;
    }
}
