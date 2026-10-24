package com.team.game;

/**
 * Ressort rigide factice, ancré en un point fixe.
 *
 * Un ressort raide avec l'intégrateur d'Euler devient instable : la force
 * est calculée à la position du début de la frame et fait osciller la
 * particule de plus en plus fort. Ici on utilise plutôt la solution
 * exacte du mouvement harmonique simple amorti (chapitre 5) :
 *
 *   x(t) = (position cos(g t) + c sin(g t)) * e^(-d t / 2)
 *   g = sqrt(4k - d^2) / 2        c = position d / (2g) + vitesse / g
 *
 * (k et d sont divisés par la masse.) On calcule la vitesse exacte que la
 * particule devrait avoir à la fin de la frame, puis l'accélération qui
 * la lui donne. L'oscillation est ainsi amortie comme dans la solution
 * analytique au lieu de croître.
 *
 * Limites : le ressort est ancré. L'intégrateur déplace la particule avec
 * la vitesse du début de frame, donc seule la vitesse de fin de frame est
 * corrigée : la méthode reste stable tant que la pulsation sqrt(k/m) fois
 * dt n'est pas beaucoup plus grande que 1 (environ k jusqu'à 1000 avec
 * m = 1 à 60 images par seconde). Au-delà, il faut un dt plus petit. Si
 * 4k <= d^2 (sur-amortissement), la solution oscillante n'existe pas et on
 * n'applique aucune force.
 */
public class RessortRigideFactice implements GenerateurForce
{
    private final Vecteur3D ancre;
    private final double constante;
    private final double amortissement;

    /**
     * @param ancre point fixe
     * @param constante constante d'élasticité k (> 0)
     * @param amortissement coefficient d'amortissement d (>= 0)
     */
    public RessortRigideFactice(
        Vecteur3D ancre,
        double constante,
        double amortissement)
    {
        if (ancre == null)
        {
            throw new IllegalArgumentException(
                "Le point d'ancrage ne peut pas être nul."
            );
        }

        if (constante <= 0 || amortissement < 0)
        {
            throw new IllegalArgumentException(
                "La constante doit être strictement positive et "
                + "l'amortissement ne peut pas être négatif."
            );
        }

        this.ancre = ancre.copie();
        this.constante = constante;
        this.amortissement = amortissement;
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        if (dt <= 0 || particule.getInverseMasse() <= 0)
        {
            return;
        }

        // Par unité de masse : k/m (rad^2/s^2) et d/m (1/s).
        double k = constante * particule.getInverseMasse();
        double amortissement = this.amortissement * particule.getInverseMasse();

        // Fréquence angulaire réelle d'oscillation.
        double discriminant = 4 * k - amortissement * amortissement;

        if (discriminant <= 0)
        {
            return;
        }

        double gamma = 0.5 * Math.sqrt(discriminant);

        Vecteur3D position = particule.getPosition().moins(ancre);
        Vecteur3D vitesse = particule.getVitesse();

        // c = position * d / (2 gamma) + vitesse / gamma
        Vecteur3D c = position.fois(amortissement / (2 * gamma))
            .additionner(vitesse.fois(1 / gamma));

        double cos = Math.cos(gamma * dt);
        double sin = Math.sin(gamma * dt);
        double decroissance = Math.exp(-0.5 * dt * amortissement);

        // Position exacte à la fin de la frame :
        //   x(dt) = (position cos + c sin) * e^(-d dt / 2)
        Vecteur3D cible = position.fois(cos)
            .additionner(c.fois(sin))
            .multiplierParUnScalaire(decroissance);

        // Vitesse exacte à la fin de la frame (dérivée de x) :
        //   v(dt) = (-position gamma sin + c gamma cos) * e^(-d dt / 2)
        //           - (d / 2) * x(dt)
        Vecteur3D vitesseCible = position.fois(-gamma * sin)
            .additionner(c.fois(gamma * cos))
            .multiplierParUnScalaire(decroissance)
            .soustraire(cible.fois(0.5 * amortissement));

        // Accélération qui donne à la particule cette vitesse en dt.
        // (L'intégrateur déplace la particule avec la vitesse du début de
        // frame : on ne peut donc corriger que la vitesse de fin de frame.)
        Vecteur3D acceleration = vitesseCible.moins(vitesse).fois(1 / dt);

        particule.ajouterForce(acceleration.fois(particule.getMasse()));
    }
}
