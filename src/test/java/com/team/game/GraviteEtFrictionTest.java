package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests de la gravité et des générateurs de friction.
 */
public class GraviteEtFrictionTest
{
    private static final double EPSILON = 0.0001;

    private static Particule particule(double masse, double vx, double vy)
    {
        return new Particule(
            new Vecteur3D(),
            new Vecteur3D(vx, vy, 0),
            new Vecteur3D(),
            masse,
            1.0
        );
    }

    /* ---------- GRAVITÉ ---------- */

    @Test
    public void testForceDeGraviteEgaleMasseFoisG()
    {
        Particule p = particule(3.0, 0, 0);

        new Gravite(new Vecteur3D(0, -10, 0)).mettreAJourForce(p, 0.016);

        assertEquals(-30.0, p.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testToutesLesMassesTombentALaMemeVitesse()
    {
        Gravite gravite = new Gravite(new Vecteur3D(0, -10, 0));
        Particule legere = particule(0.1, 0, 0);
        Particule lourde = particule(100.0, 0, 0);

        for (int i = 0; i < 100; i++)
        {
            gravite.mettreAJourForce(legere, 0.01);
            gravite.mettreAJourForce(lourde, 0.01);
            legere.integrer(0.01);
            lourde.integrer(0.01);
        }

        assertEquals(legere.getVitesse().getY(), lourde.getVitesse().getY(), EPSILON);
        assertEquals(-10.0, legere.getVitesse().getY(), 0.01);
    }

    @Test
    public void testChuteLibreDistanceParcourue()
    {
        Gravite gravite = new Gravite(new Vecteur3D(0, -10, 0));
        Particule p = particule(1.0, 0, 0);

        // d = 1/2 g t^2 = 5 m après 1 s (petit dt pour limiter l'erreur d'Euler).
        for (int i = 0; i < 10000; i++)
        {
            gravite.mettreAJourForce(p, 0.0001);
            p.integrer(0.0001);
        }

        assertEquals(-5.0, p.getPosition().getY(), 0.01);
    }

    @Test
    public void testParticuleDeMasseInfinieNeSubitPasLaGravite()
    {
        Particule p = particule(1.0, 0, 0);
        p.setInverseMasse(0);

        new Gravite(new Vecteur3D(0, -10, 0)).mettreAJourForce(p, 0.016);

        assertTrue(p.getForceAccumulee().estNul());
    }

    @Test
    public void testGraviteNeConserveQueUneCopie()
    {
        Vecteur3D g = new Vecteur3D(0, -10, 0);
        Gravite gravite = new Gravite(g);

        g.multiplierParUnScalaire(100);

        assertEquals(-10.0, gravite.getGravite().getY(), EPSILON);
    }

    /* ---------- TRAÎNÉE ---------- */

    @Test
    public void testTraineeValeur()
    {
        Particule p = particule(1.0, 10, 0);

        // F = -(k1 |v| + k2 |v|^2) = -(0.5*10 + 0.1*100) = -15
        new FrictionTrainee(0.5, 0.1).mettreAJourForce(p, 0.016);

        assertEquals(-15.0, p.getForceAccumulee().getX(), EPSILON);
        assertEquals(0.0, p.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testTraineeOpposeAuMouvementEnDiagonale()
    {
        Particule p = particule(1.0, 3, 4);

        new FrictionTrainee(1.0, 0).mettreAJourForce(p, 0.016);

        // |v| = 5, F = -5 dans la direction (0.6, 0.8)
        assertEquals(-3.0, p.getForceAccumulee().getX(), EPSILON);
        assertEquals(-4.0, p.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testTraineeSansVitesseEstNulle()
    {
        Particule p = particule(1.0, 0, 0);

        new FrictionTrainee(1.0, 1.0).mettreAJourForce(p, 0.016);

        assertTrue(p.getForceAccumulee().estNul());
    }

    @Test
    public void testVitesseLimiteAvecTrainee()
    {
        // v_limite = m g / k1 = 1 * 10 / 2 = 5
        RegistreForces registre = new RegistreForces();
        Particule p = particule(1.0, 0, 0);
        registre.ajouter(p, new Gravite(new Vecteur3D(0, -10, 0)));
        registre.ajouter(p, new FrictionTrainee(2.0, 0));

        for (int i = 0; i < 20000; i++)
        {
            registre.mettreAJourForces(0.001);
            p.integrer(0.001);
        }

        assertEquals(-5.0, p.getVitesse().getY(), 0.05);
    }

    @Test
    public void testTraineeCoefficientsNegatifsRefuses()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new FrictionTrainee(-1, 0));
        assertThrows(IllegalArgumentException.class,
            () -> new FrictionTrainee(0, -1));
    }

    /* ---------- FRICTION SOLIDE ---------- */

    @Test
    public void testFrictionStatiqueRetientLObjet()
    {
        // N = 10, mu_s = 0.5 : seuil de 5. Une poussée de 3 ne le fait pas bouger.
        RegistreForces registre = new RegistreForces();
        Particule p = particule(1.0, 0, 0);
        registre.ajouter(p, (q, dt) -> q.ajouterForce(new Vecteur3D(3, 0, 0)));
        registre.ajouter(p, new FrictionSolide(10, 0.5, 0.3));

        for (int i = 0; i < 100; i++)
        {
            registre.mettreAJourForces(0.01);
            p.integrer(0.01);
        }

        assertEquals(0.0, p.getVitesse().getX(), EPSILON);
        assertEquals(0.0, p.getPosition().getX(), EPSILON);
    }

    @Test
    public void testPousseeAuDessusDuSeuilStatiqueMetLObjetEnMouvement()
    {
        // Poussée 8 > seuil 5 : friction cinétique 0.3 * 10 = 3, force nette 5.
        Particule p = particule(1.0, 0, 0);
        p.ajouterForce(new Vecteur3D(8, 0, 0));

        new FrictionSolide(10, 0.5, 0.3).mettreAJourForce(p, 0.01);

        assertEquals(5.0, p.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testFrictionCinetiqueOpposeALaVitesse()
    {
        Particule p = particule(1.0, 4, 0);

        new FrictionSolide(10, 0.5, 0.3).mettreAJourForce(p, 0.01);

        // -mu_k * N = -3
        assertEquals(-3.0, p.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testFrictionCinetiqueArreteLObjetMaisNeLInverseJamais()
    {
        // Glisse à 6 m/s, décélération mu_k N / m = 3 m/s^2 : arrêt en ~2 s.
        RegistreForces registre = new RegistreForces();
        Particule p = particule(1.0, 6, 0);
        registre.ajouter(p, new FrictionSolide(10, 0.5, 0.3, 0.05));

        double vitessePrecedente = p.getVitesse().getX();

        for (int i = 0; i < 1000; i++)
        {
            registre.mettreAJourForces(0.01);
            p.integrer(0.01);

            assertTrue(p.getVitesse().getX() >= -0.05);
            assertTrue(p.getVitesse().getX() <= vitessePrecedente + EPSILON);
            vitessePrecedente = p.getVitesse().getX();
        }

        assertEquals(0.0, p.getVitesse().getX(), 0.05);
        // Distance d'arrêt : v^2 / (2 a) = 36 / 6 = 6 m
        assertEquals(6.0, p.getPosition().getX(), 0.1);
    }

    @Test
    public void testFrictionSolideParametresNegatifsRefuses()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new FrictionSolide(-1, 0.5, 0.3));
        assertThrows(IllegalArgumentException.class,
            () -> new FrictionSolide(1, -0.5, 0.3));
        assertThrows(IllegalArgumentException.class,
            () -> new FrictionSolide(1, 0.5, -0.3));
    }
}
