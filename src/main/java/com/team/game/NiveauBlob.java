package com.team.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Règles du niveau. Aucun dessin ni déplacement direct des particules. */
public final class NiveauBlob {
    public static final int TOTAL = 12;
    public static final double PORTE_X = 825, PORTE_LARGEUR = 24, SORTIE_X = 915;
    public static final double CHARGE_REQUISE = 0.75;
    public static final double MUR_X = 400, MUR_LARGEUR = 50, MUR_HAUTEUR = 120;
    public static final double VITESSE_SAUT = 520;
    public record Plaque(double x, double largeur) { }
    public record Fusion(Vecteur3D position, int nouvellesParticules) { }
    private final Monde monde = Monde.creerAmas(4, 2, 160, 490);
    private final Set<ParticuleSphere> membres = new HashSet<>(monde.getParticules());
    private final List<ParticuleSphere> aRecuperer = new ArrayList<>();
    private final List<Fusion> fusions = new ArrayList<>();
    private final List<Plaque> plaques = List.of(new Plaque(140, 150), new Plaque(555, 150));
    private final boolean[] occupees = new boolean[2];
    private final ContactRectangle porte;
    private double charge, tempsOuverture, delaiSaut;
    private boolean ouverte, gagne;

    public NiveauBlob() {
        aRecuperer.add(monde.ajouter(355, 590, true));
        aRecuperer.add(monde.ajouter(490, 560, true));
        aRecuperer.add(monde.ajouter(670, 480, true));
        aRecuperer.add(monde.ajouter(755, 570, true));
        monde.setSelectionAutorisee(membres::contains);
        monde.activerFrictionSol();
        porte = new ContactRectangle(monde.getParticules(), PORTE_X, 190, PORTE_LARGEUR, Monde.SOL - 190);
        monde.ajouterContact(porte);
        monde.ajouterContact(new ContactRectangle(monde.getParticules(), MUR_X,
            Monde.SOL - MUR_HAUTEUR, MUR_LARGEUR, MUR_HAUTEUR));
        monde.setApresSousPas(this::mettreAJour);
    }

    private void mettreAJour(double dt) {
        delaiSaut = Math.max(0, delaiSaut - dt);
        // Les particules dorées sont recrutées au contact d'un membre du blob.
        for (ParticuleSphere libre : new ArrayList<>(aRecuperer)) {
            ParticuleSphere proche = null;
            double minimum = Double.POSITIVE_INFINITY;
            // Ordre stable de la liste physique, indépendant de l'itération d'un HashSet.
            for (ParticuleSphere p : monde.getParticules()) {
                if (!membres.contains(p)) { continue; }
                double distance = p.getPosition().moins(libre.getPosition()).norme();
                if (distance <= p.getRayon() + libre.getRayon() + 7 && distance < minimum) {
                    minimum = distance; proche = p;
                }
            }
            if (proche != null) {
                monde.relier(proche, libre, 32);
                membres.add(libre); aRecuperer.remove(libre);
                fusions.add(new Fusion(libre.getPosition().copie(), 1));
            }
        }
        List<List<ParticuleSphere>> groupes = groupesRecrutes();
        List<Integer> gauche = occupants(plaques.get(0), groupes);
        List<Integer> droite = occupants(plaques.get(1), groupes);
        occupees[0] = !gauche.isEmpty(); occupees[1] = !droite.isEmpty();
        boolean distincts = gauche.stream().anyMatch(a -> droite.stream().anyMatch(b -> !a.equals(b)));
        if (!ouverte) {
            charge = distincts ? Math.min(CHARGE_REQUISE, charge + dt) : 0;
            if (charge >= CHARGE_REQUISE - 1e-9) { ouverte = true; porte.setActif(false); }
        } else {
            tempsOuverture += dt;
        }
        if (ouverte && membres.size() == TOTAL && groupes.size() == 1
            && membres.stream().allMatch(p -> p.getPosition().getX() - p.getRayon() >= SORTIE_X)) {
            gagne = true;
        }
    }

    private List<Integer> occupants(Plaque plaque, List<List<ParticuleSphere>> groupes) {
        List<Integer> resultat = new ArrayList<>();
        for (int i = 0; i < groupes.size(); i++) {
            if (groupes.get(i).stream().anyMatch(p -> p.getPosition().getX() >= plaque.x() + 5
                && p.getPosition().getX() <= plaque.x() + plaque.largeur() - 5
                && p.getPosition().getY() + p.getRayon() >= Monde.SOL - 1
                && Math.abs(p.getVitesse().getX()) < 45)) { resultat.add(i); }
        }
        return resultat;
    }

    public List<List<ParticuleSphere>> groupesRecrutes() {
        List<List<ParticuleSphere>> groupes = monde.getGroupes();
        groupes.removeIf(g -> g.stream().noneMatch(membres::contains));
        return groupes;
    }

    public boolean fusionner() {
        Set<ParticuleSphere> avant = new HashSet<>(monde.getGroupeSelectionne());
        if (!monde.fusionner()) { return false; }
        List<ParticuleSphere> apres = monde.getGroupeSelectionne();
        int recrutees = 0;
        Vecteur3D point = null;
        for (ParticuleSphere p : apres) {
            if (!avant.contains(p)) { point = p.getPosition().copie(); }
            if (membres.add(p)) { aRecuperer.remove(p); recrutees++; }
        }
        if (point != null) { fusions.add(new Fusion(point, recrutees)); }
        return true;
    }

    /** Une impulsion par pression, seulement si le morceau repose sur le sol ou le mur. */
    public boolean sauter() {
        if (gagne || delaiSaut > 0) { return false; }
        List<ParticuleSphere> groupe = monde.getGroupeSelectionne();
        boolean appui = groupe.stream().anyMatch(p -> {
            double bas = p.getPosition().getY() + p.getRayon();
            boolean auSol = Math.abs(bas - Monde.SOL) < 2;
            boolean surMur = Math.abs(bas - (Monde.SOL - MUR_HAUTEUR)) < 2
                && p.getPosition().getX() >= MUR_X && p.getPosition().getX() <= MUR_X + MUR_LARGEUR;
            return (auSol || surMur) && Math.abs(p.getVitesse().getY()) < 60;
        });
        if (!appui) { return false; }
        for (ParticuleSphere p : groupe) {
            p.appliquerImpulsion(new Vecteur3D(0, -p.getMasse() * VITESSE_SAUT, 0));
        }
        delaiSaut = 0.2;
        return true;
    }

    public List<Fusion> consommerFusions() {
        List<Fusion> resultat = List.copyOf(fusions); fusions.clear(); return resultat;
    }

    public String getObjectif() {
        if (gagne) { return "Niveau terminé ! Toutes les particules sont réunies dans la sortie."; }
        if (!ouverte) { return "Sépare le blob : pose un morceau distinct sur chaque plaque pendant 0,75 s."; }
        if (!aRecuperer.isEmpty()) { return "Porte déverrouillée ! Récupère les particules dorées restantes."; }
        if (groupesRecrutes().size() > 1) { return "Porte déverrouillée ! Rapproche les morceaux et appuie sur F pour les réunir."; }
        return "Le blob est complet : conduis les 12 particules dans la sortie verte.";
    }

    public Monde getMonde() { return monde; }
    public List<Plaque> getPlaques() { return plaques; }
    public boolean isRecrutee(ParticuleSphere p) { return membres.contains(p); }
    public int getNombreRecuperees() { return membres.size() - 8; }
    public boolean isPlaqueOccupee(int index) { return occupees[index]; }
    public double getCharge() { return charge / CHARGE_REQUISE; }
    public boolean isPorteOuverte() { return ouverte; }
    public double getAnimationOuverture() { return Math.min(1, tempsOuverture / 0.6); }
    public boolean isGagne() { return gagne; }
}
