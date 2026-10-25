package com.team.game;

/**
 * Friction entre une particule et une surface (sol, paroi...), avec les
 * deux régimes vus en classe :
 *
 * Friction statique (la particule est au repos) :
 *   si |F_appliquée| <= mu_s * |N|, la particule ne bouge pas : on annule
 *   la force appliquée.
 *
 * Friction cinétique (la particule est en mouvement, ou la force appliquée
 * dépasse le seuil statique) :
 *   |F| = mu_k * |N|, dans la direction opposée au mouvement.
 *   On a en général mu_k < mu_s.
 *
 * |N| est la force normale (réaction de la surface, souvent m * g pour un
 * sol horizontal). On la fournit au constructeur.
 *
 * La force appliquée est la somme des forces déjà accumulées sur la
 * particule : ce générateur doit donc être enregistré APRÈS les autres
 * dans le registre. Il s'utilise pour un mouvement le long de la surface.
 */
public class FrictionSolide implements GenerateurForce
{
    private static final double SEUIL_VITESSE_DEFAUT = 1e-3;

    private final double forceNormale;
    private final double muStatique;
    private final double muCinetique;
    private final double seuilVitesse;

    public FrictionSolide(
        double forceNormale,
        double muStatique,
        double muCinetique)
    {
        this(forceNormale, muStatique, muCinetique, SEUIL_VITESSE_DEFAUT);
    }

    /**
     * @param seuilVitesse vitesse en dessous de laquelle la particule est
     *                     considérée au repos
     */
    public FrictionSolide(
        double forceNormale,
        double muStatique,
        double muCinetique,
        double seuilVitesse)
    {
        if (forceNormale < 0 || muStatique < 0 || muCinetique < 0
            || seuilVitesse < 0)
        {
            throw new IllegalArgumentException(
                "Les paramètres de friction ne peuvent pas être négatifs."
            );
        }

        this.forceNormale = forceNormale;
        this.muStatique = muStatique;
        this.muCinetique = muCinetique;
        this.seuilVitesse = seuilVitesse;
    }

    @Override
    public void mettreAJourForce(Particule particule, double dt)
    {
        double vitesse = particule.getVitesse().norme();
        Vecteur3D forceAppliquee = particule.getForceAccumulee();
        double intensiteAppliquee = forceAppliquee.norme();

        if (vitesse <= seuilVitesse)
        {
            if (intensiteAppliquee <= muStatique * forceNormale)
            {
                // Friction statique : la particule reste immobile. On
                // annule la force appliquée et la petite vitesse restante.
                Vecteur3D arret = forceAppliquee.copie().negation();

                if (dt > 0)
                {
                    arret.additionner(
                        particule.getVitesse().fois(-particule.getMasse() / dt)
                    );
                }

                particule.ajouterForce(arret);
                return;
            }

            // La force dépasse le seuil : la particule va partir dans la
            // direction de la force, la friction cinétique s'y oppose.
            particule.ajouterForce(
                forceAppliquee.copie().normaliser()
                    .fois(-muCinetique * forceNormale)
            );
            return;
        }

        // Friction cinétique : opposée à la vitesse.
        double intensite = muCinetique * forceNormale;
        if (dt > 0 && particule.getInverseMasse() > 0) {
            // Ne pas inverser artificiellement la vitesse en une seule frame.
            double forceParallele = forceAppliquee.produitScalaire(
                particule.getVitesse().fois(1 / vitesse));
            intensite = Math.min(intensite, Math.max(0,
                particule.getMasse() * vitesse / dt + forceParallele));
        }
        particule.ajouterForce(particule.getVitesse().fois(-intensite / vitesse));
    }
}
