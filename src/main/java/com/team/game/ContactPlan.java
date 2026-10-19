package com.team.game;

import java.util.List;

/**
 * Plan fixe (sol, mur, plafond) de masse infinie, défini par un point et
 * une normale unitaire qui pointe vers l'espace libre. Une sphère touche le
 * plan quand la distance de son centre au plan est inférieure à son rayon.
 */
public class ContactPlan implements GenerateurContact
{
    private final List<? extends ParticuleSphere> particules;
    private final Vecteur3D point;
    private final Vecteur3D normale;
    private final double restitution;

    public ContactPlan(
        List<? extends ParticuleSphere> particules,
        Vecteur3D point,
        Vecteur3D normale,
        double restitution)
    {
        if (normale == null || normale.normeCarre() == 0)
        {
            throw new IllegalArgumentException(
                "La normale du plan doit être non nulle."
            );
        }

        this.particules = particules;
        this.point = point.copie();
        this.normale = normale.copie().normaliser();
        this.restitution = restitution;
    }

    @Override
    public void genererContacts(List<Contact> contacts)
    {
        for (ParticuleSphere particule : particules)
        {
            // Distance signée du centre au plan, moins le rayon.
            double distance = particule.getPosition()
                .moins(point)
                .produitScalaire(normale) - particule.getRayon();

            // "<=" : une sphère posée sur le plan (distance nulle) génère
            // un contact au repos, ce qui évite qu'elle vibre au sol.
            if (distance <= 0)
            {
                contacts.add(new Contact(
                    particule, null, normale, -distance, restitution
                ));
            }
        }
    }
}
