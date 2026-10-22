package com.team.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Registre des forces : il indique quel générateur agit sur quelle
 * particule. Une même particule peut avoir plusieurs générateurs, et un
 * même générateur peut servir plusieurs particules.
 *
 * Chaque frame :
 *   1. registre.mettreAJourForces(dt)   remplit les accumulateurs
 *   2. chaque particule s'intègre       (vide son accumulateur)
 *
 * Les générateurs sont appelés dans l'ordre d'enregistrement. Un
 * générateur qui dépend des autres forces (la friction solide) doit donc
 * être enregistré après eux.
 */
public class RegistreForces
{
    private static class Enregistrement
    {
        final Particule particule;
        final GenerateurForce generateur;

        Enregistrement(Particule particule, GenerateurForce generateur)
        {
            this.particule = particule;
            this.generateur = generateur;
        }
    }

    private final List<Enregistrement> enregistrements = new ArrayList<>();

    public void ajouter(Particule particule, GenerateurForce generateur)
    {
        if (particule == null || generateur == null)
        {
            throw new IllegalArgumentException(
                "La particule et le générateur ne peuvent pas être nuls."
            );
        }

        enregistrements.add(new Enregistrement(particule, generateur));
    }

    /**
     * Retire un couple particule / générateur.
     *
     * @return true si le couple était enregistré
     */
    public boolean retirer(Particule particule, GenerateurForce generateur)
    {
        return enregistrements.removeIf(
            e -> e.particule == particule && e.generateur == generateur
        );
    }

    /**
     * Retire tous les générateurs d'une particule (par exemple quand elle
     * disparaît du jeu).
     */
    public void retirerParticule(Particule particule)
    {
        enregistrements.removeIf(e -> e.particule == particule);
    }

    /**
     * Vide le registre. Les particules et les générateurs ne sont pas
     * détruits.
     */
    public void vider()
    {
        enregistrements.clear();
    }

    public int taille()
    {
        return enregistrements.size();
    }

    /**
     * Demande à chaque générateur d'ajouter sa force à sa particule.
     */
    public void mettreAJourForces(double dt)
    {
        for (Enregistrement e : enregistrements)
        {
            e.generateur.mettreAJourForce(e.particule, dt);
        }
    }
}
