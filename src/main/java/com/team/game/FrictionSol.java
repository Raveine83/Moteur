package com.team.game;

/** Friction de Coulomb tangentielle au sol horizontal, après les autres forces. */
final class FrictionSol implements GenerateurForce {
    private final double sol, muStatique, muCinetique;

    FrictionSol(double sol, double muStatique, double muCinetique) {
        this.sol = sol;
        this.muStatique = muStatique;
        this.muCinetique = muCinetique;
    }

    @Override public void mettreAJourForce(Particule particule, double dt) {
        if (!(particule instanceof ParticuleSphere p) || p.getInverseMasse() <= 0 || dt <= 0
            || p.getPosition().getY() + p.getRayon() < sol - 0.2) { return; }
        // n=(0,-1,0) : N compense la composante qui pousse vers le sol.
        double normale = Math.max(0, p.getForceAccumulee().getY()
            + p.getMasse() * p.getAcceleration().getY());
        double vx = p.getVitesse().getX();
        double fx = p.getForceAccumulee().getX() + p.getMasse() * p.getAcceleration().getX();
        double friction;
        if (Math.abs(vx) < 0.5 && Math.abs(fx) <= muStatique * normale) {
            friction = -fx - p.getMasse() * vx / dt;
        } else {
            double sens = Math.abs(vx) >= 0.5 ? Math.signum(vx) : Math.signum(fx);
            double arret = Math.max(0, p.getMasse() * Math.abs(vx) / dt + fx * sens);
            friction = -sens * Math.min(muCinetique * normale, arret);
        }
        p.ajouterForce(new Vecteur3D(friction, 0, 0));
    }
}
