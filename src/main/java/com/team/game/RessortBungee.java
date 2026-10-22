package com.team.game;

/**
 * Ressort bungee (élastique) entre deux particules : il tire quand il est
 * étiré au-delà de sa longueur au repos, mais ne pousse jamais quand il
 * est comprimé (comme une corde élastique qui se détend).
 *
 *   F = -k * (longueur - longueurRepos) * direction   si longueur > l0
 *   F = 0                                             sinon
 *
 * Comme pour Ressort, la force est appliquée à une seule particule :
 * enregistrer un générateur par extrémité.
 */
public class RessortBungee implements GenerateurForce
{
    private final Particule autre;
    private final double constante;
    private final double longueurRepos;

    public RessortBungee(Particule autre, double constante, double longueurRepos)
    {
        if (autre == null)
        {
            throw new IllegalArgumentException(
                "L'autre extrémité du bungee ne peut pas être nulle."
            );
        }

        if (constante < 0 || longueurRepos < 0)
        {
            throw new IllegalArgumentException(
                "La constante et la longueur au repos ne peuvent pas être négatives."
            );
        }

        this.autre = autre;
        this.constante = constante;
        this.longueurRepos = longueurRepos;
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        Vecteur3D direction = particule.getPosition().moins(autre.getPosition());
        double longueur = direction.norme();

        // Détendu : aucune force.
        if (longueur <= longueurRepos)
        {
            return;
        }

        double intensite = -constante * (longueur - longueurRepos);

        particule.ajouterForce(direction.diviserParUnScalaire(longueur).fois(intensite));
    }
}
