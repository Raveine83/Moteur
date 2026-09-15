package com.team.game;

/**
 * Pure game logic, deliberately kept free of any Processing (PApplet)
 * dependency so it can be unit-tested with JUnit without a graphics context.
 *
 * This is a placeholder: replace with real engine logic
 * (Vector3D, Particle, integrator, etc.) as the assignment progresses.
 *
 * Minimal example here: two rotation angles advancing over time,
 * used by GameSketch to spin a cube on screen.
 */
public class GameLogic {

    private static final float ROTATION_SPEED_X = 0.8f; // radians per second
    private static final float ROTATION_SPEED_Y = 0.5f; // radians per second

    private float rotationX;
    private float rotationY;

    public GameLogic() {
        this.rotationX = 0f;
        this.rotationY = 0f;
    }

    /**
     * Advances the simulation by one time step.
     *
     * @param deltaTimeSeconds elapsed time since the last frame, in seconds
     */
    public void update(float deltaTimeSeconds) {
        rotationX += ROTATION_SPEED_X * deltaTimeSeconds;
        rotationY += ROTATION_SPEED_Y * deltaTimeSeconds;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }
}
