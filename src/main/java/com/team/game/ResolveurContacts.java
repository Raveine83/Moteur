package com.team.game;

import java.util.List;

/**
 * Résolveur de collisions par impulsions.
 *
 * Les contacts sont résolus séquentiellement. Il n'existe pas d'ordre
 * optimal, on traite donc à chaque itération le contact le plus urgent
 * (vitesse de rapprochement la plus grande, puis interpénétration la plus
 * grande) et on limite le nombre d'itérations (2 fois le nombre de
 * contacts par défaut).
 */
public class ResolveurContacts
{
    private static final double TOLERANCE = 1e-9;

    private int iterationsVitesse;
    private int iterationsPosition;
    private final boolean iterationsAutomatiques;

    /**
     * Nombre d'itérations par défaut : 2 * nombre de contacts.
     */
    public ResolveurContacts()
    {
        this.iterationsAutomatiques = true;
    }

    public ResolveurContacts(int iterationsVitesse, int iterationsPosition)
    {
        if (iterationsVitesse < 0 || iterationsPosition < 0)
        {
            throw new IllegalArgumentException(
                "Le nombre d'itérations ne peut pas être négatif."
            );
        }

        this.iterationsAutomatiques = false;
        this.iterationsVitesse = iterationsVitesse;
        this.iterationsPosition = iterationsPosition;
    }

    /**
     * Résout les contacts : d'abord les vitesses (impulsions), ensuite les
     * interpénétrations.
     *
     * @param dt durée de la frame en secondes
     */
    public void resoudre(List<Contact> contacts, double dt)
    {
        if (contacts == null || contacts.isEmpty() || dt <= 0)
        {
            return;
        }

        if (iterationsAutomatiques)
        {
            iterationsVitesse = 2 * contacts.size();
            iterationsPosition = 2 * contacts.size();
        }

        resoudreVitesses(contacts, dt);
        resoudreInterpenetrations(contacts);
    }

    private void resoudreVitesses(List<Contact> contacts, double dt)
    {
        for (int i = 0; i < iterationsVitesse; i++)
        {
            Contact pire = null;
            double min = Double.MAX_VALUE;

            for (Contact contact : contacts)
            {
                double vitesseSeparation = contact.vitesseSeparation();

                // Contact qui se rapproche, ou qui est encore interpénétré
                // (contact au repos).
                if (vitesseSeparation < min
                    && (vitesseSeparation < -TOLERANCE
                        || contact.getInterpenetration() > TOLERANCE))
                {
                    min = vitesseSeparation;
                    pire = contact;
                }
            }

            if (pire == null)
            {
                return;
            }

            pire.resoudreVitesse(dt);
        }
    }

    private void resoudreInterpenetrations(List<Contact> contacts)
    {
        for (int i = 0; i < iterationsPosition; i++)
        {
            Contact pire = null;
            double max = TOLERANCE;

            for (Contact contact : contacts)
            {
                if (contact.getInterpenetration() > max)
                {
                    max = contact.getInterpenetration();
                    pire = contact;
                }
            }

            if (pire == null)
            {
                return;
            }

            Vecteur3D[] deplacements = pire.resoudreInterpenetration();

            // Les déplacements changent l'interpénétration des autres
            // contacts qui partagent une particule avec celui-ci.
            for (Contact autre : contacts)
            {
                mettreAJourInterpenetration(autre, pire, deplacements);
            }
        }
    }

    private void mettreAJourInterpenetration(
        Contact autre,
        Contact resolu,
        Vecteur3D[] deplacements)
    {
        Vecteur3D normale = autre.getNormale();
        double delta = 0;

        if (autre.getParticule1() == resolu.getParticule1())
        {
            delta -= deplacements[0].produitScalaire(normale);
        }
        else if (autre.getParticule1() != null
            && autre.getParticule1() == resolu.getParticule2())
        {
            delta -= deplacements[1].produitScalaire(normale);
        }

        if (autre.getParticule2() != null)
        {
            if (autre.getParticule2() == resolu.getParticule1())
            {
                delta += deplacements[0].produitScalaire(normale);
            }
            else if (autre.getParticule2() == resolu.getParticule2())
            {
                delta += deplacements[1].produitScalaire(normale);
            }
        }

        autre.setInterpenetration(autre.getInterpenetration() + delta);
    }
}
