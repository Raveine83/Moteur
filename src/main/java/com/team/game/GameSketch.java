package com.team.game;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Processing sketch: rendering layer only.
 *
 * All game/physics logic must live in plain Java classes (see {@link GameLogic}),
 * with no dependency on PApplet. This keeps the logic unit-testable in CI,
 * since a headless CI runner cannot fully simulate a real render loop.
 *
 * Uses the P3D renderer since the engine is required to be 3D.
 * P3D depends on JOGL/GlueGen, resolved via the JogAmp repository in pom.xml.
 */
public class GameSketch extends PApplet {

    private GameLogic logic;

    public void settings() {
        size(800, 600, PConstants.P3D);
    }

    public void setup() {
        logic = new GameLogic();
    }

    public void draw() {
        logic.update(1.0f / frameRate > 0 ? 1.0f / 60 : 1.0f / 60);

        background(20);
        lights();

        translate(width / 2f, height / 2f, 0);
        rotateX(logic.getRotationX());
        rotateY(logic.getRotationY());

        fill(0, 160, 220);
        noStroke();
        box(150);
    }
}
