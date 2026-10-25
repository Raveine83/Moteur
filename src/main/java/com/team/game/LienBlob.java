package com.team.game;

/** Deux forces de Hooke opposées, avec un câble limitant l'allongement. */
public final class LienBlob {
    private final ParticuleSphere a;
    private final ParticuleSphere b;
    private final Ressort ressortA;
    private final Ressort ressortB;
    private final ContactCable cable;

    public LienBlob(ParticuleSphere a, ParticuleSphere b, double repos, double maximum) {
        if (repos <= 0 || maximum <= repos) {
            throw new IllegalArgumentException("Le maximum doit dépasser la longueur au repos.");
        }
        this.a = a;
        this.b = b;
        ressortA = new Ressort(b, 85, repos);
        ressortB = new Ressort(a, 85, repos);
        cable = new ContactCable(a, b, maximum, 0);
    }

    void enregistrer(RegistreForces forces, GestionnaireCollisions collisions) {
        forces.ajouter(a, ressortA);
        forces.ajouter(b, ressortB);
        collisions.ajouter(cable);
    }

    void retirer(RegistreForces forces, GestionnaireCollisions collisions) {
        forces.retirer(a, ressortA);
        forces.retirer(b, ressortB);
        collisions.retirer(cable);
    }

    public ParticuleSphere getA() { return a; }
    public ParticuleSphere getB() { return b; }
    public double getLongueur() { return a.getPosition().moins(b.getPosition()).norme(); }
    public double getMaximum() { return cable.getLongueurMax(); }
    /** 0 au repos ou en compression, 1 lorsque le câble atteint sa limite. */
    public double getEtirement() {
        return Math.max(0, Math.min(1, (getLongueur() - ressortA.getLongueurRepos())
            / (getMaximum() - ressortA.getLongueurRepos())));
    }
}
