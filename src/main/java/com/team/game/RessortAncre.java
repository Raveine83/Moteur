package com.team.game;

/**
 * Ressort entre une particule et un point fixe de l'espace (ancrage) :
 *
 *   F = -k * (longueur - longueurRepos) * direction
 *
 * La direction va du point d'ancrage vers la particule. Seule la particule
 * subit la force, le point d'ancrage ne bouge pas.
 */
public class RessortAncre implements GenerateurForce
{
    private final Vecteur3D ancre;
    private final double constante;
    private final double longueurRepos;

    public RessortAncre(Vecteur3D ancre, double constante, double longueurRepos)
    {
        if (ancre == null)
        {
            throw new IllegalArgumentException(
                "Le point d'ancrage ne peut pas être nul."
            );
        }

        if (constante < 0 || longueurRepos < 0)
        {
            throw new IllegalArgumentException(
                "La constante et la longueur au repos ne peuvent pas être négatives."
            );
        }

        this.ancre = ancre.copie();
        this.constante = constante;
        this.longueurRepos = longueurRepos;
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        Vecteur3D direction = particule.getPosition().moins(ancre);
        double longueur = direction.norme();

        if (longueur == 0)
        {
            return;
        }

        double intensite = -constante * (longueur - longueurRepos);

        particule.ajouterForce(direction.diviserParUnScalaire(longueur).fois(intensite));
    }

    public Vecteur3D getAncre()
    {
        return ancre.copie();
    }
}
