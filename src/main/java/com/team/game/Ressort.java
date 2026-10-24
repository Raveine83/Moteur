package com.team.game;

/**
 * Ressort classique entre deux particules (loi de Hooke) :
 *
 *   F = -k * (longueur - longueurRepos) * direction
 *
 * Il se laisse étirer et comprimer. La direction est le vecteur unitaire
 * allant de l'autre extrémité vers la particule qui subit la force.
 *
 * Attention : ce générateur n'applique la force qu'à UNE particule. Pour
 * relier A et B, il faut enregistrer deux générateurs, un par particule :
 *
 *   registre.ajouter(a, new Ressort(b, k, l0));
 *   registre.ajouter(b, new Ressort(a, k, l0));
 *
 * Les deux forces sont égales et opposées (troisième loi de Newton).
 */
public class Ressort implements GenerateurForce
{
    private final Particule autre;
    private final double constante;
    private final double longueurRepos;

    /**
     * @param autre autre extrémité du ressort
     * @param constante constante d'élasticité k (N/m, doit être >= 0)
     * @param longueurRepos longueur au repos l0 (>= 0)
     */
    public Ressort(Particule autre, double constante, double longueurRepos)
    {
        if (autre == null)
        {
            throw new IllegalArgumentException(
                "L'autre extrémité du ressort ne peut pas être nulle."
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

        if (longueur == 0)
        {
            return;
        }

        double intensite = -constante * (longueur - longueurRepos);

        particule.ajouterForce(direction.diviserParUnScalaire(longueur).fois(intensite));
    }

    /**
     * Longueur actuelle du ressort vue depuis la particule donnée.
     */
    public double longueurActuelle(Particule particule)
    {
        return particule.getPosition().moins(autre.getPosition()).norme();
    }

    public Particule getAutre()
    {
        return autre;
    }

    public double getConstante()
    {
        return constante;
    }

    public double getLongueurRepos()
    {
        return longueurRepos;
    }
}
