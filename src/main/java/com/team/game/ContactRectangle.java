package com.team.game;

import java.util.List;

/** Collision cercle/rectangle fixe, y compris quand le centre est à l'intérieur. */
public final class ContactRectangle implements GenerateurContact {
    private final List<? extends ParticuleSphere> particules;
    private final double x, y, largeur, hauteur;
    private boolean actif = true;

    public ContactRectangle(List<? extends ParticuleSphere> particules,
                            double x, double y, double largeur, double hauteur) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(largeur)
            || !Double.isFinite(hauteur) || largeur <= 0 || hauteur <= 0) {
            throw new IllegalArgumentException("Rectangle fini de dimensions positives requis.");
        }
        this.particules = java.util.Objects.requireNonNull(particules);
        this.x = x; this.y = y; this.largeur = largeur; this.hauteur = hauteur;
    }

    public void setActif(boolean actif) { this.actif = actif; }
    public boolean isActif() { return actif; }

    @Override public void genererContacts(List<Contact> contacts) {
        if (!actif) { return; }
        for (ParticuleSphere p : particules) {
            if (p.getInverseMasse() <= 0) { continue; }
            double px = p.getPosition().getX(), py = p.getPosition().getY();
            double procheX = Math.max(x, Math.min(x + largeur, px));
            double procheY = Math.max(y, Math.min(y + hauteur, py));
            double dx = px - procheX, dy = py - procheY;
            double distance = Math.hypot(dx, dy);
            if (distance > p.getRayon()) { continue; }
            Vecteur3D normale;
            double profondeur;
            if (distance > 1e-12) {
                normale = new Vecteur3D(dx / distance, dy / distance, 0);
                profondeur = p.getRayon() - distance;
            } else {
                double[] faces = {px - x, x + largeur - px, py - y, y + hauteur - py};
                int face = 0;
                for (int i = 1; i < faces.length; i++) { if (faces[i] < faces[face]) { face = i; } }
                normale = switch (face) {
                    case 0 -> new Vecteur3D(-1, 0, 0);
                    case 1 -> new Vecteur3D(1, 0, 0);
                    case 2 -> new Vecteur3D(0, -1, 0);
                    default -> new Vecteur3D(0, 1, 0);
                };
                profondeur = p.getRayon() + faces[face];
            }
            contacts.add(new Contact(p, null, normale, profondeur, 0));
        }
    }
}
