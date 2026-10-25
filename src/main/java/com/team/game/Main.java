package com.team.game;

import processing.core.PApplet;

/** Point d'entrée de l'application distribuable. */
public final class Main extends PApplet {
    private JeuBlob jeu;
    public void settings() { size(1100, 700); }
    public void setup() { surface.setTitle("Phase 2 - Blob"); frameRate(60); jeu = new JeuBlob(this); }
    public void draw() { if (!focused) { jeu.perdreFocus(); } jeu.dessiner(); }
    public void keyPressed() { jeu.touche(key, keyCode, true); if (key == ESC) { key = 0; } }
    public void keyReleased() { jeu.touche(key, keyCode, false); }
    public void mousePressed() { jeu.cliquer(mouseX, mouseY); }
    public static void main(String[] args) { PApplet.main(Main.class.getName()); }
}
