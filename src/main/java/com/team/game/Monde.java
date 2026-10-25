package com.team.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Orchestration indépendante du rendu. Unités : px, s, masse conventionnelle. */
public final class Monde {
    public static final double PAS = 1.0 / 240.0;
    public static final double SOL = 640;
    private final List<ParticuleSphere> particules = new ArrayList<>();
    private final List<LienBlob> liens = new ArrayList<>();
    private final RegistreForces forces = new RegistreForces();
    private final GestionnaireCollisions collisions = new GestionnaireCollisions();
    private ParticuleSphere selection;
    private double accumulateur;
    private double directionX;
    private double directionY;
    private double tempsSeparation;
    private final List<ParticuleSphere> gauche = new ArrayList<>();
    private final List<ParticuleSphere> droite = new ArrayList<>();
    private final boolean blob;
    private java.util.function.Predicate<ParticuleSphere> selectionAutorisee = p -> true;
    private java.util.function.DoubleConsumer apresSousPas = dt -> { };
    private FrictionSol frictionSol;

    public Monde(boolean blob) {
        this.blob = blob;
        collisions.ajouter(new DetecteurCollisions(particules, 0.15));
        ajouterPlan(0, SOL, 0, -1);
        ajouterPlan(25, 0, 1, 0);
        ajouterPlan(1075, 0, -1, 0);
        ajouterPlan(0, 190, 0, 1);
    }

    private void ajouterPlan(double x, double y, double nx, double ny) {
        collisions.ajouter(new ContactPlan(particules,
            new Vecteur3D(x, y, 0), new Vecteur3D(nx, ny, 0), 0.15));
    }

    public ParticuleSphere ajouter(double x, double y, boolean gravite) {
        ParticuleSphere p = new ParticuleSphere(new Vecteur3D(x, y, 0),
            new Vecteur3D(), new Vecteur3D(), 1, 0.995, 13);
        particules.add(p);
        if (selection == null) { selection = p; }
        if (gravite) { forces.ajouter(p, new Gravite(new Vecteur3D(0, 360, 0))); }
        forces.ajouter(p, new FrictionTrainee(0.8, 0.002));
        return p;
    }

    public void ajouterForce(Particule p, GenerateurForce force) { forces.ajouter(p, force); }
    public void ajouterContact(GenerateurContact contact) { collisions.ajouter(contact); }

    void setSelectionAutorisee(java.util.function.Predicate<ParticuleSphere> autorisee) {
        selectionAutorisee = java.util.Objects.requireNonNull(autorisee);
    }

    void setApresSousPas(java.util.function.DoubleConsumer action) {
        apresSousPas = java.util.Objects.requireNonNull(action);
    }

    void activerFrictionSol() { frictionSol = new FrictionSol(SOL, 0.65, 0.55); }

    public void relier(ParticuleSphere a, ParticuleSphere b, double repos) {
        if (a == b || !particules.contains(a) || !particules.contains(b)) {
            throw new IllegalArgumentException("Les deux particules doivent appartenir au monde.");
        }
        for (LienBlob l : liens) {
            if ((l.getA() == a && l.getB() == b) || (l.getA() == b && l.getB() == a)) { return; }
        }
        LienBlob lien = new LienBlob(a, b, repos, repos * 1.55);
        liens.add(lien);
        lien.enregistrer(forces, collisions);
    }

    public static Monde creerBlob() {
        return creerAmas(4, 3, 200, 420);
    }

    static Monde creerAmas(int colonnes, int lignes, double x, double y) {
        Monde m = new Monde(true);
        for (int ligne = 0; ligne < lignes; ligne++) {
            for (int colonne = 0; colonne < colonnes; colonne++) {
                m.ajouter(x + colonne * 35 + (ligne % 2) * 17.5, y + ligne * 31, true);
            }
        }
        for (int i = 0; i < m.particules.size(); i++) {
            for (int j = i + 1; j < m.particules.size(); j++) {
                ParticuleSphere a = m.particules.get(i), b = m.particules.get(j);
                double distance = a.getPosition().moins(b.getPosition()).norme();
                if (distance < 40) { m.relier(a, b, distance); }
            }
        }
        return m;
    }

    public void commander(double x, double y) {
        double norme = Math.max(1, Math.hypot(x, y));
        directionX = x / norme;
        directionY = y / norme;
    }

    /** Temps réel accumulé, sous-pas fixes. Après une pause longue, max 0,1 s simulée. */
    public void avancer(double dt) {
        if (!Double.isFinite(dt) || dt <= 0) { return; }
        accumulateur += Math.min(dt, 0.1);
        while (accumulateur + 1e-12 >= PAS) {
            List<ParticuleSphere> groupe = getGroupeSelectionne();
            for (ParticuleSphere p : groupe) {
                p.ajouterForce(new Vecteur3D(directionX * 650, directionY * 650, 0));
            }
            if (tempsSeparation > 0) {
                for (ParticuleSphere p : gauche) { p.ajouterForce(new Vecteur3D(-450, -100, 0)); }
                for (ParticuleSphere p : droite) { p.ajouterForce(new Vecteur3D(450, -100, 0)); }
                tempsSeparation -= PAS;
            }
            forces.mettreAJourForces(PAS);
            if (frictionSol != null) {
                for (ParticuleSphere p : particules) { frictionSol.mettreAJourForce(p, PAS); }
            }
            for (ParticuleSphere p : particules) { p.integrer(PAS); }
            collisions.traiter(PAS);
            apresSousPas.accept(PAS);
            accumulateur -= PAS;
        }
    }

    /** Composantes connexes du graphe des liens, indépendamment de la proximité visuelle. */
    public List<List<ParticuleSphere>> getGroupes() {
        List<List<ParticuleSphere>> groupes = new ArrayList<>();
        List<ParticuleSphere> restants = new ArrayList<>(particules);
        while (!restants.isEmpty()) {
            List<ParticuleSphere> groupe = new ArrayList<>();
            groupe.add(restants.remove(0));
            for (int i = 0; i < groupe.size(); i++) {
                ParticuleSphere p = groupe.get(i);
                for (LienBlob l : liens) {
                    ParticuleSphere voisin = l.getA() == p ? l.getB() : l.getB() == p ? l.getA() : null;
                    if (voisin != null && restants.remove(voisin)) { groupe.add(voisin); }
                }
            }
            groupes.add(groupe);
        }
        return groupes;
    }

    public List<ParticuleSphere> getGroupeSelectionne() {
        for (List<ParticuleSphere> g : getGroupes()) { if (g.contains(selection)) { return g; } }
        return Collections.emptyList();
    }

    public void selectionSuivante() {
        List<List<ParticuleSphere>> groupes = getGroupes();
        groupes.removeIf(g -> g.stream().noneMatch(selectionAutorisee));
        for (int i = 0; i < groupes.size(); i++) {
            if (groupes.get(i).contains(selection)) {
                selection = groupes.get((i + 1) % groupes.size()).get(0);
                return;
            }
        }
    }

    public void selectionner(double x, double y) {
        for (ParticuleSphere p : particules) {
            if (selectionAutorisee.test(p)
                && Math.hypot(p.getPosition().getX() - x, p.getPosition().getY() - y) <= p.getRayon() + 6) {
                selection = p;
                return;
            }
        }
    }

    public boolean separer() {
        if (!blob) { return false; }
        List<ParticuleSphere> groupe = new ArrayList<>(getGroupeSelectionne());
        if (groupe.size() < 2) { return false; }
        groupe.sort((a, b) -> Double.compare(a.getPosition().getX(), b.getPosition().getX()));
        gauche.clear(); droite.clear();
        gauche.addAll(groupe.subList(0, groupe.size() / 2));
        droite.addAll(groupe.subList(groupe.size() / 2, groupe.size()));
        liens.removeIf(l -> {
            boolean couper = (gauche.contains(l.getA()) && droite.contains(l.getB()))
                || (droite.contains(l.getA()) && gauche.contains(l.getB()));
            if (couper) { l.retirer(forces, collisions); }
            return couper;
        });
        tempsSeparation = 0.25;
        return true;
    }

    /** Fusion explicite : un pont entre deux composantes proches, sans téléportation. */
    public boolean fusionner() {
        if (!blob) { return false; }
        List<ParticuleSphere> groupe = getGroupeSelectionne();
        ParticuleSphere aProche = null, bProche = null;
        double distanceMin = 70;
        for (ParticuleSphere a : groupe) {
            for (ParticuleSphere b : particules) {
                if (groupe.contains(b)) { continue; }
                double d = a.getPosition().moins(b.getPosition()).norme();
                if (d < distanceMin) { distanceMin = d; aProche = a; bProche = b; }
            }
        }
        if (aProche == null) { return false; }
        relier(aProche, bProche, Math.max(30, Math.min(38, distanceMin)));
        tempsSeparation = 0;
        return true;
    }

    public boolean objectifAtteint() {
        return blob && getGroupeSelectionne().size() == particules.size()
            && particules.stream().allMatch(p -> p.getPosition().getX() > 880);
    }

    public List<ParticuleSphere> getParticules() { return Collections.unmodifiableList(particules); }
    public List<LienBlob> getLiens() { return Collections.unmodifiableList(liens); }
}
