package com.team.game;

/** Solution analytique sous-amortie, chapitre 5 p. 8 (m=1, k=196, c=9). */
public final class OscillateurAmorti {
    private double temps;
    private double amplitude;

    public void declencher() { temps = 0; amplitude = -22; }

    public void avancer(double dt) {
        if (Double.isFinite(dt) && dt > 0) { temps += dt; }
    }

    public double getDeplacement() {
        double lambda = 4.5; // c / (2m) = zeta * omega0
        double omegaD = Math.sqrt(196 - lambda * lambda);
        // x(0)=amplitude et v(0)=0.
        return amplitude * Math.exp(-lambda * temps)
            * (Math.cos(omegaD * temps) + lambda / omegaD * Math.sin(omegaD * temps));
    }
}
