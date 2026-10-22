package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests de l'accumulateur de forces de Particule.
 */
public class ParticuleForcesTest
{
    private static final double EPSILON = 0.0001;

    private static Particule particule(double masse)
    {
        return new Particule(
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(0, 0, 0),
            masse,
            1.0
        );
    }

    @Test
    public void testDeuxiemeLoiDeNewton()
    {
        Particule p = particule(2.0);

        p.ajouterForce(new Vecteur3D(10, 0, 0));
        p.integrer(1.0);

        // a = F / m = 5
        assertEquals(5.0, p.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testLesForcesSAdditionnent()
    {
        Particule p = particule(1.0);

        p.ajouterForce(new Vecteur3D(3, 0, 0));
        p.ajouterForce(new Vecteur3D(0, 4, 0));
        p.ajouterForce(new Vecteur3D(-1, 0, 0));

        assertEquals(2.0, p.getForceAccumulee().getX(), EPSILON);
        assertEquals(4.0, p.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testAccumulateurVideApresIntegration()
    {
        Particule p = particule(1.0);

        p.ajouterForce(new Vecteur3D(10, 0, 0));
        p.integrer(1.0);

        assertTrue(p.getForceAccumulee().estNul());

        // Sans nouvelle force, la vitesse ne change plus.
        p.integrer(1.0);
        assertEquals(10.0, p.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testViderAccumulateur()
    {
        Particule p = particule(1.0);

        p.ajouterForce(new Vecteur3D(10, 10, 10));
        p.viderAccumulateur();

        assertTrue(p.getForceAccumulee().estNul());
    }

    @Test
    public void testAccelerationConstanteEtForcesSAdditionnent()
    {
        Particule p = new Particule(
            new Vecteur3D(),
            new Vecteur3D(),
            new Vecteur3D(0, -9.81, 0),
            2.0,
            1.0
        );

        p.ajouterForce(new Vecteur3D(4, 0, 0));
        p.integrer(1.0);

        assertEquals(2.0, p.getVitesse().getX(), EPSILON);
        assertEquals(-9.81, p.getVitesse().getY(), EPSILON);
    }

    @Test
    public void testParticuleDeMasseInfinieIgnoreLesForces()
    {
        Particule p = particule(1.0);
        p.setInverseMasse(0);

        p.ajouterForce(new Vecteur3D(100, 0, 0));
        p.integrer(1.0);

        assertEquals(0.0, p.getVitesse().getX(), EPSILON);
        assertEquals(0.0, p.getPosition().getX(), EPSILON);
        assertTrue(p.getForceAccumulee().estNul());
    }

    @Test
    public void testTempsInvalideConserveLesForces()
    {
        Particule p = particule(1.0);

        p.ajouterForce(new Vecteur3D(5, 0, 0));
        p.integrer(0);

        assertEquals(5.0, p.getForceAccumulee().getX(), EPSILON);
        assertEquals(0.0, p.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testAccelerationDerniereFrame()
    {
        Particule p = new Particule(
            new Vecteur3D(),
            new Vecteur3D(),
            new Vecteur3D(0, 1, 0),
            2.0,
            1.0
        );

        p.ajouterForce(new Vecteur3D(6, 0, 0));
        p.integrer(0.5);

        assertEquals(3.0, p.getAccelerationDerniereFrame().getX(), EPSILON);
        assertEquals(1.0, p.getAccelerationDerniereFrame().getY(), EPSILON);
    }

    @Test
    public void testPositionNeDependPasDeLaForceDeLaFrameCourante()
    {
        // Euler explicite : la position utilise l'ancienne vitesse.
        Particule p = particule(1.0);

        p.ajouterForce(new Vecteur3D(10, 0, 0));
        p.integrer(1.0);

        assertEquals(0.0, p.getPosition().getX(), EPSILON);

        p.integrer(1.0);

        assertEquals(10.0, p.getPosition().getX(), EPSILON);
    }
}
