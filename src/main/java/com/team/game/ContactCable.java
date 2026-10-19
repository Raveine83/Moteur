package com.team.game;

import java.util.List;

/**
 * Câble : relie deux particules et les empêche de s'éloigner au-delà d'une
 * longueur maximale. Tant que la longueur n'est pas atteinte, il n'a aucun
 * effet. Au-delà, on génère un contact dont la normale ramène les
 * particules l'une vers l'autre.
 */
public class ContactCable implements GenerateurContact
{
    private final Particule particule1;
    private final Particule particule2;
    private final double longueurMax;
    private final double restitution;

    public ContactCable(
        Particule particule1,
        Particule particule2,
        double longueurMax,
        double restitution)
    {
        if (particule1 == null || particule2 == null)
        {
            throw new IllegalArgumentException(
                "Un câble relie deux particules."
            );
        }

        if (longueurMax <= 0)
        {
            throw new IllegalArgumentException(
                "La longueur maximale doit être strictement positive."
            );
        }

        this.particule1 = particule1;
        this.particule2 = particule2;
        this.longueurMax = longueurMax;
        this.restitution = restitution;
    }

    @Override
    public void genererContacts(List<Contact> contacts)
    {
        Vecteur3D vers2 =
            particule2.getPosition().moins(particule1.getPosition());
        double longueur = vers2.norme();

        if (longueur < longueurMax)
        {
            return;
        }

        // Contact "inversé" : la normale va de 1 vers 2, donc deux
        // particules qui s'éloignent ont une vitesse de séparation négative
        // et l'impulsion les ramène l'une vers l'autre.
        Vecteur3D normale = vers2.diviserParUnScalaire(longueur);

        contacts.add(new Contact(
            particule1,
            particule2,
            normale,
            longueur - longueurMax,
            restitution
        ));
    }

    public Particule getParticule1()
    {
        return particule1;
    }

    public Particule getParticule2()
    {
        return particule2;
    }

    public double getLongueurMax()
    {
        return longueurMax;
    }
}
