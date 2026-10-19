package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ContactTest
{
    private static final double EPSILON = 0.0001;

    private static Particule particule(double x, double y, double vx, double vy, double masse)
    {
        return new Particule(
            new Vecteur3D(x, y, 0),
            new Vecteur3D(vx, vy, 0),
            new Vecteur3D(0, 0, 0),
            masse,
            1.0
        );
    }

    @Test
    public void testVitesseSeparationNegativeQuandRapprochement()
    {
        Particule p1 = particule(0, 0, 10, 0, 1);
        Particule p2 = particule(1, 0, 0, 0, 1);

        // Normale de p2 vers p1.
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0, 1);

        assertEquals(-10.0, contact.vitesseSeparation(), EPSILON);
    }

    @Test
    public void testNormaleEstNormalisee()
    {
        Particule p1 = particule(0, 0, 0, 0, 1);

        Contact contact = new Contact(p1, null, new Vecteur3D(0, 5, 0), 0, 1);

        assertEquals(1.0, contact.getNormale().norme(), EPSILON);
    }

    @Test
    public void testChocElastiqueMassesEgalesEchangeLesVitesses()
    {
        Particule p1 = particule(0, 0, 10, 0, 1);
        Particule p2 = particule(1, 0, 0, 0, 1);
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0, 1);

        contact.resoudreVitesse(0.016);

        assertEquals(0.0, p1.getVitesse().getX(), EPSILON);
        assertEquals(10.0, p2.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testChocParfaitementInelastiqueLesObjetsSeFusionnent()
    {
        Particule p1 = particule(0, 0, 10, 0, 1);
        Particule p2 = particule(1, 0, 0, 0, 1);
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0, 0);

        contact.resoudreVitesse(0.016);

        assertEquals(5.0, p1.getVitesse().getX(), EPSILON);
        assertEquals(5.0, p2.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testConservationDuMomentum()
    {
        Particule p1 = particule(0, 0, 8, 3, 2);
        Particule p2 = particule(1, 0, -2, 1, 5);
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0, 0.6);

        double avantX = 2 * 8 + 5 * -2;
        double avantY = 2 * 3 + 5 * 1;

        contact.resoudreVitesse(0.016);

        double apresX = 2 * p1.getVitesse().getX() + 5 * p2.getVitesse().getX();
        double apresY = 2 * p1.getVitesse().getY() + 5 * p2.getVitesse().getY();

        assertEquals(avantX, apresX, EPSILON);
        assertEquals(avantY, apresY, EPSILON);
    }

    @Test
    public void testVitesseRelativeApresChocVautMoinsERelative()
    {
        Particule p1 = particule(0, 0, 8, 0, 2);
        Particule p2 = particule(1, 0, -2, 0, 5);
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0, 0.5);

        double avant = contact.vitesseSeparation();

        contact.resoudreVitesse(0.016);

        assertEquals(-0.5 * avant, contact.vitesseSeparation(), EPSILON);
    }

    @Test
    public void testRebondSurObjetFixe()
    {
        Particule balle = particule(0, 0, 0, 10, 1);
        Contact contact = new Contact(balle, null, new Vecteur3D(0, -1, 0), 0, 0.5);

        contact.resoudreVitesse(0.016);

        assertEquals(-5.0, balle.getVitesse().getY(), EPSILON);
    }

    @Test
    public void testParticulesQuiSEloignentNeSontPasModifiees()
    {
        Particule p1 = particule(0, 0, -3, 0, 1);
        Particule p2 = particule(1, 0, 3, 0, 1);
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0.1, 1);

        contact.resoudreVitesse(0.016);

        assertEquals(-3.0, p1.getVitesse().getX(), EPSILON);
        assertEquals(3.0, p2.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testObjetPlusLourdBougeMoins()
    {
        Particule leger = particule(0, 0, 10, 0, 1);
        Particule lourd = particule(1, 0, 0, 0, 100);
        Contact contact = new Contact(leger, lourd, new Vecteur3D(-1, 0, 0), 0, 1);

        contact.resoudreVitesse(0.016);

        assertTrue(Math.abs(lourd.getVitesse().getX()) < 0.25);
        assertTrue(leger.getVitesse().getX() < -9.0);
    }

    @Test
    public void testDeuxMassesInfiniesSansEffet()
    {
        Particule p1 = particule(0, 0, 1, 0, 1);
        Particule p2 = particule(1, 0, 0, 0, 1);
        p1.setInverseMasse(0);
        p2.setInverseMasse(0);
        Contact contact = new Contact(p1, p2, new Vecteur3D(-1, 0, 0), 0.5, 1);

        contact.resoudreVitesse(0.016);
        contact.resoudreInterpenetration();

        assertEquals(1.0, p1.getVitesse().getX(), EPSILON);
        assertEquals(0.0, p1.getPosition().getX(), EPSILON);
        assertEquals(1.0, p2.getPosition().getX(), EPSILON);
    }

    @Test
    public void testContactAuRepos()
    {
        double dt = 0.01;
        double g = 100;

        // Une balle posée sur le sol (y vers le bas) dont la vitesse vient
        // uniquement de la gravité appliquée pendant la frame.
        Particule balle = new Particule(
            new Vecteur3D(0, 0, 0),
            new Vecteur3D(0, g * dt, 0),
            new Vecteur3D(0, g, 0),
            1.0,
            1.0
        );
        Contact contact = new Contact(balle, null, new Vecteur3D(0, -1, 0), 0, 0.9);

        contact.resoudreVitesse(dt);

        assertEquals(0.0, balle.getVitesse().getY(), EPSILON);
    }

    @Test
    public void testInterpenetrationInverseProportionnelleALaMasse()
    {
        Particule leger = particule(0, 0, 0, 0, 1);
        Particule lourd = particule(0.5, 0, 0, 0, 3);
        Contact contact = new Contact(leger, lourd, new Vecteur3D(1, 0, 0), 4, 0);

        Vecteur3D[] deplacements = contact.resoudreInterpenetration();

        // Le plus léger (m=1) fait 3/4 du chemin, le plus lourd (m=3) 1/4.
        assertEquals(3.0, deplacements[0].getX(), EPSILON);
        assertEquals(-1.0, deplacements[1].getX(), EPSILON);
        assertEquals(3.0, leger.getPosition().getX(), EPSILON);
        assertEquals(-0.5, lourd.getPosition().getX(), EPSILON);
    }

    @Test
    public void testInterpenetrationContreObjetFixe()
    {
        Particule balle = particule(0, 0, 0, 0, 1);
        Contact contact = new Contact(balle, null, new Vecteur3D(0, -1, 0), 2, 0);

        contact.resoudreInterpenetration();

        assertEquals(-2.0, balle.getPosition().getY(), EPSILON);
    }

    @Test
    public void testArgumentsInvalides()
    {
        Particule p = particule(0, 0, 0, 0, 1);

        assertThrows(IllegalArgumentException.class,
            () -> new Contact(null, p, new Vecteur3D(1, 0, 0), 0, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new Contact(p, null, new Vecteur3D(0, 0, 0), 0, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new Contact(p, null, new Vecteur3D(1, 0, 0), 0, 1.5));
    }
}
