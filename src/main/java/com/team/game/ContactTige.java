package com.team.game;

import java.util.List;

/**
 * Tige : maintient deux particules à une distance fixe. C'est un contact
 * permanent : trop loin, on les rapproche ; trop près, on les écarte. La
 * restitution est nulle pour que la tige ne rebondisse pas.
 */
public class ContactTige implements GenerateurContact
{
    private final Particule particule1;
    private final Particule particule2;
    private final double longueur;

    public ContactTige(
        Particule particule1,
        Particule particule2,
        double longueur)
    {
        if (particule1 == null || particule2 == null)
        {
            throw new IllegalArgumentException(
                "Une tige relie deux particules."
            );
        }

        if (longueur <= 0)
        {
            throw new IllegalArgumentException(
                "La longueur doit être strictement positive."
            );
        }

        this.particule1 = particule1;
        this.particule2 = particule2;
        this.longueur = longueur;
    }

    @Override
    public void genererContacts(List<Contact> contacts)
    {
        Vecteur3D vers2 =
            particule2.getPosition().moins(particule1.getPosition());
        double distance = vers2.norme();

        // Centres confondus : pas de direction pour la tige.
        if (distance == 0)
        {
            return;
        }

        Vecteur3D normale = vers2.diviserParUnScalaire(distance);

        if (distance >= longueur)
        {
            // Trop loin (ou exactement tendue, ce qui bloque
            // l'éloignement) : même cas que le câble.
            contacts.add(new Contact(
                particule1, particule2, normale, distance - longueur, 0
            ));
        }
        else
        {
            // Trop près : on écarte (normale de 2 vers 1).
            contacts.add(new Contact(
                particule1, particule2, normale.negation(), longueur - distance, 0
            ));
        }
    }

    public Particule getParticule1()
    {
        return particule1;
    }

    public Particule getParticule2()
    {
        return particule2;
    }

    public double getLongueur()
    {
        return longueur;
    }
}
