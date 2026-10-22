package com.team.game;

public class Particule
{
    private Vecteur3D position;
    private Vecteur3D vitesse;
    private Vecteur3D acceleration;

    // Somme vectorielle des forces appliquées pendant la frame
    // (principe de d'Alembert). Vidée après chaque intégration.
    private Vecteur3D forceAccumulee;

    // Accélération réellement appliquée lors de la dernière intégration
    // (accélération constante + forces / masse). Utile pour les contacts
    // au repos, car l'accumulateur est déjà vidé à ce moment.
    private Vecteur3D accelerationDerniereFrame;

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
        this.forceAccumulee = new Vecteur3D();
        this.accelerationDerniereFrame = new Vecteur3D();

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
        this.forceAccumulee = new Vecteur3D();
        this.accelerationDerniereFrame = new Vecteur3D();

        setMasse(masse);
        this.damping = damping;
    }

    /* ---------- FORCES ---------- */

    /**
     * Ajoute une force à l'accumulateur : forceAccumulee += force.
     */
    public void ajouterForce(Vecteur3D force)
    {
        forceAccumulee.additionner(force);
    }

    /**
     * Vide l'accumulateur de forces. Appelé à la fin de chaque intégration.
     */
    public void viderAccumulateur()
    {
        forceAccumulee = new Vecteur3D();
    }

    /**
     * Met à jour la position et la vitesse de la particule.
     *
     * L'accélération utilisée est l'accélération constante de la particule
     * plus la somme des forces accumulées divisée par la masse
     * (deuxième loi de Newton : a = F / m = F * inverseMasse).
     * L'accumulateur est vidé ensuite.
     *
     * @param temps durée de la frame en secondes
     */
    public void integrer(double temps)
    {
        if (temps <= 0)
        {
            return;
        }

        // Une particule de masse infinie ne bouge pas : on ignore les forces.
        if (inverseMasse <= 0)
        {
            viderAccumulateur();
            return;
        }

        // Accélération = accélération constante + forces * inverseMasse
        Vecteur3D accelerationTotale =
            acceleration.copie().additionner(
                forceAccumulee.fois(inverseMasse)
            );
        accelerationDerniereFrame = accelerationTotale.copie();

        // Position = Position + Vitesse * dt
        position.additionner(vitesse.fois(temps));

        // Vitesse = Vitesse + Accélération * dt
        vitesse.additionner(accelerationTotale.fois(temps));

        // Application du damping.
        vitesse.multiplierParUnScalaire(
            Math.pow(damping, temps)
        );

        viderAccumulateur();
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

    public Vecteur3D getForceAccumulee()
    {
        return forceAccumulee;
    }

    public Vecteur3D getAccelerationDerniereFrame()
    {
        return accelerationDerniereFrame;
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