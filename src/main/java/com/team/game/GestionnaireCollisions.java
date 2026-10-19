package com.team.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entrée du système de collisions, à appeler une fois par frame
 * après l'intégration des particules :
 *
 *   gestionnaire.traiter(dt);
 *
 * Collecte les contacts de tous les générateurs, puis les passe au
 * résolveur.
 */
public class GestionnaireCollisions
{
    private final List<GenerateurContact> generateurs = new ArrayList<>();
    private static final int PASSES_PAR_DEFAUT = 4;

    private final ResolveurContacts resolveur;
    private final int passes;

    public GestionnaireCollisions()
    {
        this(new ResolveurContacts(), PASSES_PAR_DEFAUT);
    }

    public GestionnaireCollisions(ResolveurContacts resolveur)
    {
        this(resolveur, PASSES_PAR_DEFAUT);
    }

    /**
     * @param passes nombre maximal de passes détection + résolution par
     *               frame. Résoudre un contact déplace les particules et
     *               peut en créer de nouveaux (une boule poussée dans le
     *               sol par une autre) : on détecte donc à nouveau. On
     *               s'arrête dès qu'une passe ne trouve plus de contact.
     */
    public GestionnaireCollisions(ResolveurContacts resolveur, int passes)
    {
        if (passes < 1)
        {
            throw new IllegalArgumentException(
                "Il faut au moins une passe."
            );
        }

        this.resolveur = resolveur;
        this.passes = passes;
    }

    public void ajouter(GenerateurContact generateur)
    {
        generateurs.add(generateur);
    }

    public void retirer(GenerateurContact generateur)
    {
        generateurs.remove(generateur);
    }

    public void vider()
    {
        generateurs.clear();
    }

    /**
     * @param dt durée de la frame en secondes
     * @return les contacts détectés à la première passe de cette frame
     */
    public List<Contact> traiter(double dt)
    {
        List<Contact> premiere = new ArrayList<>();

        for (int passe = 0; passe < passes; passe++)
        {
            List<Contact> contacts = new ArrayList<>();

            for (GenerateurContact generateur : generateurs)
            {
                generateur.genererContacts(contacts);
            }

            if (passe == 0)
            {
                premiere = contacts;
            }

            if (contacts.isEmpty())
            {
                break;
            }

            resolveur.resoudre(contacts, dt);
        }

        return premiere;
    }
}
