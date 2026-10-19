package com.team.game;

import java.util.List;

/**
 * Détection des interpénétrations entre particules sphériques.
 *
 * Deux sphères de centres A et B et de rayons a et b sont en collision si
 * dist(A, B) < a + b. L'interpénétration vaut a + b - dist, la normale est
 * le vecteur unitaire entre les centres. Tous les couples sont testés à
 * chaque frame : O(n^2).
 */
public class DetecteurCollisions implements GenerateurContact
{
    private final List<? extends ParticuleSphere> particules;
    private final double restitution;

    /**
     * @param particules liste vivante des particules du monde (lue à
     *                   chaque appel)
     * @param restitution coefficient de restitution des contacts générés
     */
    public DetecteurCollisions(
        List<? extends ParticuleSphere> particules,
        double restitution)
    {
        this.particules = particules;
        this.restitution = restitution;
    }

    @Override
    public void genererContacts(List<Contact> contacts)
    {
        for (int i = 0; i < particules.size(); i++)
        {
            for (int j = i + 1; j < particules.size(); j++)
            {
                Contact contact = tester(particules.get(i), particules.get(j));

                if (contact != null)
                {
                    contacts.add(contact);
                }
            }
        }
    }

    /**
     * Teste deux sphères.
     *
     * @return le contact, ou null s'il n'y a pas d'interpénétration
     */
    Contact tester(ParticuleSphere a, ParticuleSphere b)
    {
        // Deux objets de masse infinie ne réagissent pas.
        if (a.getInverseMasse() <= 0 && b.getInverseMasse() <= 0)
        {
            return null;
        }

        Vecteur3D centres =
            a.getPosition().moins(b.getPosition());
        double sommeRayons = a.getRayon() + b.getRayon();
        double distanceCarre = centres.normeCarre();

        if (distanceCarre >= sommeRayons * sommeRayons)
        {
            return null;
        }

        double distance = Math.sqrt(distanceCarre);

        // Centres confondus : normale arbitraire.
        Vecteur3D normale = distance > 0
            ? centres.diviserParUnScalaire(distance)
            : new Vecteur3D(0, 1, 0);

        return new Contact(a, b, normale, sommeRayons - distance, restitution);
    }
}
