package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ParticuleTest
{
    private static final double EPSILON = 0.0001;

    @Test
    public void testSetMasse()
    {
        Particule particule = new Particule();

        particule.setMasse(10.0);

        assertEquals(
            0.1,
            particule.getInverseMasse(),
            EPSILON
        );
    }

    @Test
    public void testSetInverseMasse()
    {
        Particule particule = new Particule();

        particule.setInverseMasse(0.5);

        assertEquals(
            0.5,
            particule.getInverseMasse(),
            EPSILON
        );

        assertEquals(
            2.0,
            particule.getMasse(),
            EPSILON
        );
    }

    @Test
    public void testIntegrationPosition()
    {
        Particule particule = new Particule(
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(10, 0, 0),
            new Vecteur3D(0, 0, 0),
            1.0,
            1.0
        );

        particule.integrer(1.0);

        assertEquals(
            10.0,
            particule.getPosition().getX(),
            EPSILON
        );

        assertEquals(
            0.0,
            particule.getPosition().getY(),
            EPSILON
        );

        assertEquals(
            0.0,
            particule.getPosition().getZ(),
            EPSILON
        );
    }

    @Test
    public void testIntegrationVitesseAvecAcceleration()
    {
        Particule particule = new Particule(
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(0, -9.81, 0),
            1.0,
            1.0
        );

        particule.integrer(1.0);

        assertEquals(
            -9.81,
            particule.getVitesse().getY(),
            EPSILON
        );
    }

    @Test
    public void testDamping()
    {
        Particule particule = new Particule(
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(10, 0, 0),
            new Vecteur3D(0, 0, 0),
            1.0,
            0.5
        );

        particule.integrer(1.0);

        assertEquals(
            5.0,
            particule.getVitesse().getX(),
            EPSILON
        );
    }

    @Test
    public void testMasseInvalide()
    {
        Particule particule = new Particule();

        assertThrows(
            IllegalArgumentException.class,
            () -> particule.setMasse(0)
        );
    }

    @Test
    public void testTempsNegatifNeModifiePasParticule()
    {
        Particule particule = new Particule(
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(10, 0, 0),
            new Vecteur3D(0, -9.81, 0),
            1.0,
            1.0
        );

        particule.integrer(-1.0);

        assertEquals(
            0.0,
            particule.getPosition().getX(),
            EPSILON
        );

        assertEquals(
            10.0,
            particule.getVitesse().getX(),
            EPSILON
        );
    }
}
