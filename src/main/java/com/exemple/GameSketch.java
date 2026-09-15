package com.exemple;

import processing.core.PApplet;

/**
 * Sketch Processing : ne contient que le rendu.
 * Toute la logique vit dans GameLogic (testable indépendamment).
 */
public class GameSketch extends PApplet {

    private GameLogic logic;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        logic = new GameLogic(width, height);
        rectMode(CENTER);
    }

    public void draw() {
        logic.update();

        background(30);
        fill(0, 200, 120);
        noStroke();
        rect(logic.getX(), logic.getY(), 40, 40);
    }
}
