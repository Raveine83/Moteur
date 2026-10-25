package com.team.game;

/**
 * Contact entre deux particules (ou entre une particule et un objet fixe).
 *
 * Convention : la normale est un vecteur unitaire qui pointe de la
 * particule2 vers la particule1. La vitesse de séparation
 * (v1 - v2) . n est donc négative quand les particules se rapprochent.
 *
 * Si particule2 est null, la particule1 touche un objet fixe de masse
 * infinie (sol, mur...).
 */
public class Contact
{
    private static final double EPSILON = 1e-12;

    private final Particule particule1;
    private final Particule particule2;
    private final Vecteur3D normale;
    private final double restitution;
    private double interpenetration;

    public Contact(
        Particule particule1,
        Particule particule2,
        Vecteur3D normale,
        double interpenetration,
        double restitution)
    {
        if (particule1 == null)
        {
            throw new IllegalArgumentException(
                "La particule1 d'un contact ne peut pas être nulle."
            );
        }

        if (normale == null || normale.normeCarre() < EPSILON)
        {
            throw new IllegalArgumentException(
                "La normale d'un contact doit être non nulle."
            );
        }

        if (restitution < 0 || restitution > 1)
        {
            throw new IllegalArgumentException(
                "Le coefficient de restitution doit être compris entre 0 et 1."
            );
        }

        this.particule1 = particule1;
        this.particule2 = particule2;
        this.normale = normale.copie().normaliser();
        this.interpenetration = interpenetration;
        this.restitution = restitution;
    }

    /* ---------- VITESSE ---------- */

    /**
     * Vitesse relative des deux particules projetée sur la normale :
     * (v1 - v2) . n. Négative si elles se rapprochent.
     */
    public double vitesseSeparation()
    {
        Vecteur3D vitesseRelative = particule1.getVitesse().copie();

        if (particule2 != null)
        {
            vitesseRelative.soustraire(particule2.getVitesse());
        }

        return vitesseRelative.produitScalaire(normale);
    }

    /**
     * Résout la collision par une impulsion dans la direction de la normale.
     *
     * Nouvelle vitesse de séparation : -e * vitesse de séparation.
     * L'impulsion appliquée vaut (e + 1) * v_rel . n / (1/m1 + 1/m2) :
     * la particule1 reçoit +j/m1 et la particule2 reçoit -j/m2.
     *
     * Contact au repos : si la vitesse de séparation vient seulement de
     * l'accélération appliquée pendant cette frame (gravité par exemple),
     * on la retire pour que l'objet ne rebondisse pas à l'infini.
     *
     * @param dt durée de la frame en secondes
     */
    public void resoudreVitesse(double dt)
    {
        double vitesseSeparation = vitesseSeparation();

        // Les particules s'éloignent déjà : rien à faire.
        if (vitesseSeparation > 0)
        {
            return;
        }

        // Vitesse de séparation due uniquement à l'accélération de la frame.
        Vecteur3D accelerationRelative =
            particule1.getAccelerationDerniereFrame().copie();

        if (particule2 != null)
        {
            accelerationRelative.soustraire(particule2.getAccelerationDerniereFrame());
        }

        double separationDueAcceleration =
            accelerationRelative.produitScalaire(normale) * dt;

        double restitutionEffective = restitution;
        double nouvelleVitesseSeparation;

        if (separationDueAcceleration < 0
            && -vitesseSeparation <= 3 * -separationDueAcceleration)
        {
            // Contact au repos : l'impact ne vaut pas plus de trois frames
            // d'accélération, donc ce n'est pas un vrai choc. Sans ce seuil,
            // l'objet vibre indéfiniment au sol.
            restitutionEffective = 0;
        }

        nouvelleVitesseSeparation = -vitesseSeparation * restitutionEffective;

        if (separationDueAcceleration < 0)
        {
            nouvelleVitesseSeparation +=
                restitutionEffective * separationDueAcceleration;

            if (nouvelleVitesseSeparation < 0)
            {
                nouvelleVitesseSeparation = 0;
            }
        }

        double delta = nouvelleVitesseSeparation - vitesseSeparation;

        double inverseMasseTotale = particule1.getInverseMasse();

        if (particule2 != null)
        {
            inverseMasseTotale += particule2.getInverseMasse();
        }

        // Deux objets de masse infinie : impulsion sans effet.
        if (inverseMasseTotale <= 0)
        {
            return;
        }

        double impulsion = delta / inverseMasseTotale;
        Vecteur3D impulsionParInverseMasse =
            normale.fois(impulsion);

        particule1.getVitesse().additionner(
            impulsionParInverseMasse.fois(particule1.getInverseMasse())
        );

        if (particule2 != null)
        {
            particule2.getVitesse().additionner(
                impulsionParInverseMasse.fois(-particule2.getInverseMasse())
            );
        }
    }

    /* ---------- INTERPÉNÉTRATION ---------- */

    /**
     * Sépare les particules le long de la normale. Le déplacement de chaque
     * particule est inversement proportionnel à sa masse :
     * dP1 = d * m2 / (m1 + m2) = d * (1/m1) / (1/m1 + 1/m2).
     *
     * @return {déplacement de la particule1, déplacement de la particule2}
     *         (la particule2 est nulle ou immobile : vecteur nul)
     */
    public Vecteur3D[] resoudreInterpenetration()
    {
        Vecteur3D[] deplacements = {new Vecteur3D(), new Vecteur3D()};

        if (interpenetration <= 0)
        {
            return deplacements;
        }

        double inverseMasseTotale = particule1.getInverseMasse();

        if (particule2 != null)
        {
            inverseMasseTotale += particule2.getInverseMasse();
        }

        if (inverseMasseTotale <= 0)
        {
            return deplacements;
        }

        double parInverseMasse = interpenetration / inverseMasseTotale;

        deplacements[0] =
            normale.fois(parInverseMasse * particule1.getInverseMasse());
        particule1.getPosition().additionner(deplacements[0]);

        if (particule2 != null)
        {
            deplacements[1] =
                normale.fois(-parInverseMasse * particule2.getInverseMasse());
            particule2.getPosition().additionner(deplacements[1]);
        }

        return deplacements;
    }

    /* ---------- GETTERS ---------- */

    public Particule getParticule1()
    {
        return particule1;
    }

    public Particule getParticule2()
    {
        return particule2;
    }

    public Vecteur3D getNormale()
    {
        return normale;
    }

    public double getInterpenetration()
    {
        return interpenetration;
    }

    public void setInterpenetration(double interpenetration)
    {
        this.interpenetration = interpenetration;
    }

    public double getRestitution()
    {
        return restitution;
    }
}
