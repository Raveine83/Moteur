package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DetecteurCollisionsTest
{
    private static final double EPSILON = 0.0001;

    private static ParticuleSphere sphere(double x, double y, double rayon)
    {
        return new ParticuleSphere(
            new Vecteur3D(x, y, 0),
            new Vecteur3D(),
            new Vecteur3D(),
            1.0,
            1.0,
            rayon
        );
    }

    @Test
    public void testSpheresQuiSeChevauchent()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        ParticuleSphere a = sphere(0, 0, 1);
        ParticuleSphere b = sphere(1.5, 0, 1);
        particules.add(a);
        particules.add(b);

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 0.5).genererContacts(contacts);

        assertEquals(1, contacts.size());

        Contact contact = contacts.get(0);

        assertEquals(0.5, contact.getInterpenetration(), EPSILON);
        // La normale va de b (particule2) vers a (particule1).
        assertEquals(-1.0, contact.getNormale().getX(), EPSILON);
        assertEquals(0.0, contact.getNormale().getY(), EPSILON);
        assertSame(a, contact.getParticule1());
        assertSame(b, contact.getParticule2());
        assertEquals(0.5, contact.getRestitution(), EPSILON);
    }

    @Test
    public void testSpheresSeulementEnContactNeCollisionnentPas()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 0, 1));
        particules.add(sphere(2, 0, 1));

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 1).genererContacts(contacts);

        assertTrue(contacts.isEmpty());
    }

    @Test
    public void testSpheresEloigneesNeCollisionnentPas()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 0, 1));
        particules.add(sphere(10, 10, 1));

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 1).genererContacts(contacts);

        assertTrue(contacts.isEmpty());
    }

    @Test
    public void testRayonsDifferents()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 0, 3));
        particules.add(sphere(0, 4, 2));

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 1).genererContacts(contacts);

        assertEquals(1, contacts.size());
        assertEquals(1.0, contacts.get(0).getInterpenetration(), EPSILON);
    }

    @Test
    public void testTousLesCouplesSontTestes()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 0, 1));
        particules.add(sphere(1, 0, 1));
        particules.add(sphere(2, 0, 1));

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 1).genererContacts(contacts);

        // (0,1) et (1,2) se chevauchent, (0,2) se touchent seulement.
        assertEquals(2, contacts.size());
    }

    @Test
    public void testCentresConfondusDonnentUneNormaleValide()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(3, 3, 1));
        particules.add(sphere(3, 3, 1));

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 1).genererContacts(contacts);

        assertEquals(1, contacts.size());
        assertEquals(1.0, contacts.get(0).getNormale().norme(), EPSILON);
        assertEquals(2.0, contacts.get(0).getInterpenetration(), EPSILON);
    }

    @Test
    public void testDeuxObjetsFixesIgnores()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        ParticuleSphere a = sphere(0, 0, 1);
        ParticuleSphere b = sphere(1, 0, 1);
        a.setInverseMasse(0);
        b.setInverseMasse(0);
        particules.add(a);
        particules.add(b);

        List<Contact> contacts = new ArrayList<>();
        new DetecteurCollisions(particules, 1).genererContacts(contacts);

        assertTrue(contacts.isEmpty());
    }

    @Test
    public void testListeVivanteEstRelueAChaqueAppel()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 0, 1));
        DetecteurCollisions detecteur = new DetecteurCollisions(particules, 1);

        List<Contact> contacts = new ArrayList<>();
        detecteur.genererContacts(contacts);
        assertTrue(contacts.isEmpty());

        particules.add(sphere(1, 0, 1));
        detecteur.genererContacts(contacts);
        assertEquals(1, contacts.size());
    }

    @Test
    public void testRayonInvalide()
    {
        assertThrows(IllegalArgumentException.class, () -> new ParticuleSphere(0));
    }
}
