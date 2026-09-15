package com.exemple;

/**
 * Logique du jeu, volontairement séparée de tout code Processing (PApplet).
 * Ça permet de la tester avec JUnit sans avoir besoin d'un contexte graphique.
 *
 * Exemple minimal : un carré qui se déplace horizontalement et rebondit
 * sur les bords de l'écran.
 */
public class GameLogic {

    private final int screenWidth;
    private final int screenHeight;

    private float x;
    private float y;
    private float speedX;

    public GameLogic(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.x = screenWidth / 2f;
        this.y = screenHeight / 2f;
        this.speedX = 2f;
    }

    /**
     * Avance la simulation d'une frame.
     * Déplace le carré et inverse la direction s'il touche un bord.
     */
    public void update() {
        x += speedX;

        if (x <= 0 || x >= screenWidth) {
            speedX = -speedX;
            x = clamp(x, 0, screenWidth);
        }
    }

    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSpeedX() {
        return speedX;
    }
}
