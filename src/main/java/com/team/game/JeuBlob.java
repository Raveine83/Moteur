package com.team.game;

import java.util.List;
import processing.core.PApplet;

/** Contrôleur/rendu commun à l'application Java et au sketch Processing Blob. */
public final class JeuBlob {
    private final PApplet app;
    private final java.util.function.LongSupplier horloge;
    private Monde monde;
    private NiveauBlob niveau;
    private final java.util.List<EclatFusion> eclats = new java.util.ArrayList<>();
    private boolean victoireAnnoncee;
    private final OscillateurAmorti compteur = new OscillateurAmorti();
    private final boolean[] touches = new boolean[4];
    private final java.util.Set<Character> enfoncees = new java.util.HashSet<>();
    private int mode = 1;
    private int nombre = -1;
    private long precedent;
    private double dt;
    private boolean pause;
    private String message = "";
    private double messageDuree;
    private ParticuleSphere demoA, demoB;
    private Vecteur3D ancre;
    private final String[] titres = {"", "BLOB", "RESSORT CLASSIQUE", "RESSORT ANCRÉ",
        "BUNGEE", "RESSORT RIGIDE FACTICE", "TIGE", "CÂBLE", "FRICTION SOLIDE", "BLOB LIBRE"};

    public JeuBlob(PApplet app) { this(app, System::nanoTime); }
    JeuBlob(PApplet app, java.util.function.LongSupplier horloge) {
        this.app = app; this.horloge = horloge; reinitialiser();
    }
    Monde getMonde() { return monde; }
    NiveauBlob getNiveau() { return niveau; }

    private void reinitialiser() {
        niveau = mode == 1 ? new NiveauBlob() : null;
        monde = niveau != null ? niveau.getMonde() : mode == 9 ? Monde.creerBlob() : new Monde(false);
        eclats.clear(); victoireAnnoncee = false;
        demoA = null; demoB = null; ancre = null;
        pause = false;
        java.util.Arrays.fill(touches, false);
        nombre = -1;
        faibleDirection = 0;
        precedent = horloge.getAsLong();
        message = "";
        if (mode == 2 || mode == 4) {
            demoA = monde.ajouter(420, 330, false);
            demoB = monde.ajouter(660, 330, false);
            if (mode == 2) {
                monde.ajouterForce(demoA, new Ressort(demoB, 18, 120));
                monde.ajouterForce(demoB, new Ressort(demoA, 18, 120));
            } else {
                monde.ajouterForce(demoA, new RessortBungee(demoB, 18, 120));
                monde.ajouterForce(demoB, new RessortBungee(demoA, 18, 120));
            }
        } else if (mode == 3 || mode == 5) {
            ancre = new Vecteur3D(540, 280, 0);
            demoA = monde.ajouter(680, 420, false);
            monde.ajouterForce(demoA, mode == 3 ? new RessortAncre(ancre, 18, 90)
                : new RessortRigideFactice(ancre, 240, 8));
        } else if (mode == 6 || mode == 7) {
            demoA = monde.ajouter(530, 270, false);
            demoA.setInverseMasse(0);
            demoB = monde.ajouter(680, 320, true);
            double longueur = demoB.getPosition().moins(demoA.getPosition()).norme();
            monde.ajouterContact(mode == 6 ? new ContactTige(demoA, demoB, longueur)
                : new ContactCable(demoA, demoB, longueur, 0));
            monde.selectionner(680, 320);
        } else if (mode == 8) {
            demoA = monde.ajouter(350, Monde.SOL - 13, false);
            monde.ajouterForce(demoA, (p, dt) -> p.ajouterForce(new Vecteur3D(faibleDirection * 130, 0, 0)));
            // Mouvement strictement horizontal : N fourni, comme dans le modèle du cours.
            monde.ajouterForce(demoA, new FrictionSolide(360, 0.6, 0.4));
        }
        verifierCompteur();
    }

    public void dessiner() {
        long maintenant = horloge.getAsLong();
        dt = (maintenant - precedent) / 1e9;
        precedent = maintenant;
        if (!pause) {
            boolean termine = niveau != null && niveau.isGagne();
            monde.commander(termine ? 0 : (touches[1] ? 1 : 0) - (touches[0] ? 1 : 0),
                termine || mode == 8 || niveau != null ? 0 : (touches[3] ? 1 : 0) - (touches[2] ? 1 : 0));
            monde.avancer(dt);
            if (niveau != null) {
                for (NiveauBlob.Fusion f : niveau.consommerFusions()) {
                    eclats.add(new EclatFusion(f.position(), f.nouvellesParticules()));
                }
                if (niveau.isGagne() && !victoireAnnoncee) {
                    victoireAnnoncee = true;
                    informer("Les 12 particules sont sauvées ! R pour rejouer, 9 pour le mode libre.");
                }
            }
            for (EclatFusion e : eclats) { e.age += Math.min(dt, 0.1); }
            eclats.removeIf(e -> e.age > 0.8);
            verifierCompteur();
            compteur.avancer(Math.min(dt, 0.1));
            messageDuree -= dt;
        }
        app.background(19, 27, 43);
        dessinerTerrain();
        dessinerParticules();
        dessinerEclats();
        dessinerHUD();
    }

    private void verifierCompteur() {
        int actuel = monde.getGroupeSelectionne().size();
        if (actuel != nombre) { nombre = actuel; compteur.declencher(); }
    }

    private void dessinerTerrain() {
        app.noStroke();
        if (mode == 1 || mode == 9) {
            app.fill(34, 75, 67);
            float sortie = niveau == null ? 880 : (float) NiveauBlob.SORTIE_X;
            app.rect(sortie, 190, 1075 - sortie, (float) Monde.SOL - 190);
            app.fill(141, 223, 184);
            app.textSize(14);
            app.textAlign(PApplet.CENTER);
            app.text(niveau == null ? "ARRIVÉE" : "SORTIE / 12", 995, 222);
        }
        app.fill(60, 76, 96);
        app.rect(25, (float) Monde.SOL, 1050, 12);
        app.stroke(60, 76, 96);
        app.line(25, 190, 25, (float) Monde.SOL);
        app.line(1075, 190, 1075, (float) Monde.SOL);
        app.line(25, 190, 1075, 190);
        if (niveau != null) { dessinerNiveau(); }
        if (demoA != null && (demoB != null || ancre != null)) {
            Vecteur3D a = demoA.getPosition();
            Vecteur3D b = demoB == null ? ancre : demoB.getPosition();
            app.stroke(241, 191, 105);
            app.strokeWeight(mode == 6 ? 5 : 2);
            app.line((float) a.getX(), (float) a.getY(), (float) b.getX(), (float) b.getY());
            app.strokeWeight(1);
            if (ancre != null) { app.fill(241, 191, 105); app.rect((float) b.getX() - 5, (float) b.getY() - 5, 10, 10); }
        }
    }

    private void dessinerParticules() {
        for (LienBlob lien : monde.getLiens()) {
            float tension = (float) lien.getEtirement();
            app.stroke(app.lerpColor(app.color(80, 143, 159), app.color(255, 152, 75), tension));
            app.strokeWeight(1.5f + tension * 2);
            Vecteur3D a = lien.getA().getPosition(), b = lien.getB().getPosition();
            app.line((float) a.getX(), (float) a.getY(), (float) b.getX(), (float) b.getY());
        }
        List<ParticuleSphere> selection = monde.getGroupeSelectionne();
        for (ParticuleSphere p : monde.getParticules()) {
            Vecteur3D pos = p.getPosition();
            boolean aRecuperer = niveau != null && !niveau.isRecrutee(p);
            app.stroke(selection.contains(p) ? app.color(204, 255, 228) : app.color(151, 173, 223));
            app.strokeWeight(selection.contains(p) ? 2 : 1);
            app.fill(aRecuperer ? app.color(255, 197, 94)
                : selection.contains(p) ? app.color(73, 199, 155) : app.color(100, 124, 185));
            if (p.getInverseMasse() == 0) {
                app.rect((float) pos.getX() - 9, (float) pos.getY() - 9, 18, 18);
            } else {
                app.ellipse((float) pos.getX(), (float) pos.getY(), (float) p.getRayon() * 2, (float) p.getRayon() * 2);
                if (aRecuperer) {
                    app.noFill(); app.stroke(255, 197, 94, 100);
                    app.ellipse((float) pos.getX(), (float) pos.getY(), 38, 38);
                    app.fill(66, 45, 20); app.textSize(16); app.textAlign(PApplet.CENTER, PApplet.CENTER);
                    app.text("+", (float) pos.getX(), (float) pos.getY() - 1);
                    app.textAlign(PApplet.LEFT, PApplet.BASELINE);
                }
            }
        }
        app.strokeWeight(1);
    }

    private void dessinerHUD() {
        app.noStroke();
        app.fill(26, 38, 57); app.rect(0, 0, app.width, 180);
        app.textAlign(PApplet.LEFT);
        app.fill(238, 244, 250); app.textSize(26);
        app.text("PHASE 2 / " + titres[mode], 26, 37);
        app.textSize(13); app.fill(169, 189, 209);
        app.text("[1] Niveau   [2] Ressort   [3] Ancré   [4] Bungee   [5] Rigide factice   [6] Tige   [7] Câble   [8] Friction   [9] Libre", 26, 64);
        app.text(niveau == null
            ? "Flèches : déplacer    Clic / Tab : choisir un morceau    S : séparer    F : fusionner    R : recommencer    P : pause"
            : "Gauche/droite : déplacer    Espace/haut : sauter    Tab/clic : choisir    S : séparer    F : fusionner    R : rejouer    P : pause", 26, 88);
        if (mode == 1 || mode == 9) {
            app.fill(238, 244, 250); app.textSize(16);
            app.text(niveau == null ? "Réunis les 12 particules et conduis tout le blob dans la zone verte."
                : niveau.getObjectif(), 26, 120);
            app.fill(106, 221, 178);
            app.text("Attachées :", 26, 157);
            app.textSize(28);
            app.text(Integer.toString(nombre), 122, 157 + (float) compteur.getDeplacement());
            app.textSize(14); app.fill(169, 189, 209);
            app.text("/ 12     Morceaux : " + (niveau == null ? monde.getGroupes().size() : niveau.groupesRecrutes().size())
                + (niveau == null ? "     Bleu / orange : étirement des liens"
                    : "     Récupérées : " + niveau.getNombreRecuperees() + "/4     Porte : "
                    + (niveau.isPorteOuverte() ? "OUVERTE" : Math.round(niveau.getCharge() * 100) + " %")), 165, 157);
        } else {
            app.fill(238, 244, 250); app.textSize(16);
            String explication = switch (mode) {
                case 2 -> "Hooke : les deux extrémités reçoivent des forces égales et opposées.";
                case 3 -> "Une extrémité fixe ; le ressort tire et pousse autour de sa longueur au repos.";
                case 4 -> "Bungee : il tire lorsqu'il est étiré, puis devient inactif en compression.";
                case 5 -> "Ressort rigide amorti : approximation à partir de la solution analytique.";
                case 6 -> "Tige : distance fixe ; le point carré / fixe ne peut pas se déplacer.";
                case 7 -> "Câble : distance maximale ; le lien peut se détendre. Sélectionne la boule mobile.";
                default -> "Friction : petites poussées bloquées ; maintien d'une flèche pour dépasser le seuil statique.";
            };
            app.text(explication, 26, 120);
            app.fill(169, 189, 209); app.textSize(14);
            app.text(mode == 8 ? "A / D : petite force horizontale (130) ; flèches : force 650 ; seuil statique = 216."
                : "Utilise les flèches pour perturber le système ; R rétablit les conditions initiales.", 26, 154);
        }
        app.textSize(12); app.fill(169, 189, 209);
        app.text(String.format(java.util.Locale.ROOT, "Frame : %.4f s   |   Physique : 1/240 s   |   %s", dt, pause ? "PAUSE" : "En cours"), 26, 682);
        if (niveau != null) {
            app.text("Dorées : touche-les pour les recruter. Bleu / orange : étirement des liens.", 440, 682);
        }
        if ((mode == 9 && monde.objectifAtteint()) || (niveau != null && niveau.isGagne())) {
            app.fill(141, 245, 193); app.textSize(24); app.textAlign(PApplet.CENTER);
            app.text("BRAVO ! Les 12 particules sont arrivées ensemble.", app.width / 2f, 265);
        }
        if (messageDuree > 0) {
            app.fill(255, 206, 134); app.textAlign(PApplet.CENTER); app.textSize(15);
            app.text(message, app.width / 2f, 620);
        }
    }

    public void touche(char key, int code, boolean pressee) {
        if (niveau != null && (key == ' ' || code == PApplet.UP)) {
            char saut = code == PApplet.UP ? '↑' : ' ';
            if (!pressee) { enfoncees.remove(saut); return; }
            if (enfoncees.add(saut) && !pause && !victoireAnnoncee && !niveau.sauter()) {
                informer("Le morceau doit toucher le sol ou le dessus du mur pour sauter.");
            }
            return;
        }
        int index = switch (code) { case PApplet.LEFT -> 0; case PApplet.RIGHT -> 1;
            case PApplet.UP -> 2; case PApplet.DOWN -> 3; default -> -1; };
        if (index >= 0) { touches[index] = pressee; return; }
        char k = Character.toLowerCase(key);
        if (mode == 8 && (k == 'a' || k == 'd')) {
            // Petite commande pour observer le seuil statique, force maintenue par registre.
            faibleDirection = pressee ? (k == 'a' ? -1 : 1) : 0;
            return;
        }
        if (!pressee) { enfoncees.remove(k); return; }
        if (!enfoncees.add(k)) { return; }
        if (k >= '1' && k <= '9') { mode = k - '0'; reinitialiser(); }
        else if (k == 'r') { reinitialiser(); }
        else if (k == 'p') { pause = !pause; }
        else if (!pause && k == '\t') { monde.selectionSuivante(); verifierCompteur(); }
        else if (!pause && (mode == 1 || mode == 9) && k == 's' && !victoireAnnoncee) {
            informer(monde.separer() ? "Liens coupés : choisis un morceau avec Tab ou un clic." : "Ce morceau ne peut plus être séparé.");
            verifierCompteur();
        } else if (!pause && (mode == 1 || mode == 9) && k == 'f' && !victoireAnnoncee) {
            boolean fusion = niveau == null ? monde.fusionner() : niveau.fusionner();
            if (fusion && niveau == null) {
                ParticuleSphere p = monde.getGroupeSelectionne().get(0);
                eclats.add(new EclatFusion(p.getPosition().copie(), 0));
            }
            informer(fusion ? "Morceaux fusionnés : un nouveau ressort et son câble les relient."
                : "Rapproche deux morceaux à moins de 70 pixels, puis appuie sur F.");
            verifierCompteur();
        }
    }

    private int faibleDirection;
    private static final class EclatFusion {
        final double x, y;
        final int ajoutees;
        double age;
        EclatFusion(Vecteur3D position, int ajoutees) {
            x = position.getX(); y = position.getY(); this.ajoutees = ajoutees;
        }
    }

    private void dessinerNiveau() {
        float murX = (float) NiveauBlob.MUR_X, murY = (float) (Monde.SOL - NiveauBlob.MUR_HAUTEUR);
        app.noStroke(); app.fill(76, 94, 120);
        app.rect(murX, murY, (float) NiveauBlob.MUR_LARGEUR, (float) NiveauBlob.MUR_HAUTEUR);
        app.fill(149, 177, 202); app.rect(murX - 2, murY, (float) NiveauBlob.MUR_LARGEUR + 4, 5);
        app.textAlign(PApplet.CENTER); app.textSize(12); app.fill(186, 207, 227);
        app.text("SAUTER", murX + (float) NiveauBlob.MUR_LARGEUR / 2, murY - 13);
        for (int i = 0; i < niveau.getPlaques().size(); i++) {
            NiveauBlob.Plaque plaque = niveau.getPlaques().get(i);
            boolean active = niveau.isPlaqueOccupee(i) || niveau.isPorteOuverte();
            app.noStroke(); app.fill(active ? app.color(106, 221, 178) : app.color(115, 129, 158));
            app.rect((float) plaque.x(), (float) Monde.SOL - 5, (float) plaque.largeur(), 9, 4);
            app.fill(active ? app.color(153, 244, 207) : app.color(169, 189, 209));
            app.textAlign(PApplet.CENTER); app.textSize(12);
            app.text("PLAQUE " + (i + 1) + (active ? "  OK" : ""),
                (float) (plaque.x() + plaque.largeur() / 2), (float) Monde.SOL - 95);
        }
        float x = (float) NiveauBlob.PORTE_X;
        app.stroke(255, 197, 94, 170); app.noFill();
        app.rect(x - 5, 190, (float) NiveauBlob.PORTE_LARGEUR + 10, (float) Monde.SOL - 190);
        app.noStroke();
        if (!niveau.isPorteOuverte()) {
            app.fill(177, 126, 57); app.rect(x, 190, (float) NiveauBlob.PORTE_LARGEUR, (float) Monde.SOL - 190);
            app.fill(255, 206, 134); app.textAlign(PApplet.CENTER); app.textSize(12);
            app.text("PORTE", x + 12, 212);
        } else {
            app.fill(106, 221, 178, (float) (180 * (1 - niveau.getAnimationOuverture())));
            app.rect(x, 190, (float) NiveauBlob.PORTE_LARGEUR, (float) Monde.SOL - 190);
            app.fill(106, 221, 178); app.textAlign(PApplet.CENTER); app.textSize(12);
            app.text("OUVERTE", x + 12, 212);
        }
        if (!niveau.isPorteOuverte() && niveau.getCharge() > 0) {
            app.fill(106, 221, 178);
            app.rect(x - 3, (float) (Monde.SOL - 400 * niveau.getCharge()), 30, (float) (400 * niveau.getCharge()));
        }
        app.textAlign(PApplet.LEFT, PApplet.BASELINE);
    }

    private void dessinerEclats() {
        for (EclatFusion e : eclats) {
            float t = (float) (e.age / 0.8);
            app.noFill(); app.strokeWeight(2);
            app.stroke(e.ajoutees > 0 ? app.color(255, 206, 134, 255 * (1 - t))
                : app.color(106, 221, 178, 255 * (1 - t)));
            float diametre = 28 + 85 * t;
            app.ellipse((float) e.x, (float) e.y, diametre, diametre);
            app.fill(238, 244, 250, 255 * (1 - t)); app.textSize(15); app.textAlign(PApplet.CENTER);
            app.text(e.ajoutees > 0 ? "+" + e.ajoutees : "Fusion", (float) e.x, (float) e.y - 24 - t * 24);
        }
        app.strokeWeight(1); app.textAlign(PApplet.LEFT, PApplet.BASELINE);
    }
    private void informer(String texte) { message = texte; messageDuree = 3; }
    public void cliquer(float x, float y) { if (!pause) { monde.selectionner(x, y); verifierCompteur(); } }
    public void perdreFocus() { java.util.Arrays.fill(touches, false); enfoncees.clear(); faibleDirection = 0; }
}
