package com.team.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vecteur3DTest {

    @Test
    void testConstructeur() {
        Vecteur3D v = new Vecteur3D(1, 2, 3);

        assertEquals(1, v.getX());
        assertEquals(2, v.getY());
        assertEquals(3, v.getZ());
    }

    @Test
    void testNormeCarre() {
        Vecteur3D v = new Vecteur3D(3, 4, 0);

        assertEquals(25, v.normeCarre());
    }

    @Test
    void testNorme() {
        Vecteur3D v = new Vecteur3D(3, 4, 0);

        assertEquals(5.0f, v.norme(), 0.0001f);
    }

    @Test
    void testNormaliser() {
        Vecteur3D v = new Vecteur3D(3, 4, 0);

        v.normaliser();

        assertEquals(0.6f, v.getX(), 0.0001f);
        assertEquals(0.8f, v.getY(), 0.0001f);
        assertEquals(0.0f, v.getZ(), 0.0001f);
        assertEquals(1.0f, v.norme(), 0.0001f);
    }

    @Test
    void testAdditionner() {
        Vecteur3D v1 = new Vecteur3D(1, 2, 3);
        Vecteur3D v2 = new Vecteur3D(4, 5, 6);

        v1.additionner(v2);

        assertEquals(5, v1.getX());
        assertEquals(7, v1.getY());
        assertEquals(9, v1.getZ());
    }

    @Test
    void testProduitScalaire() {
        Vecteur3D v1 = new Vecteur3D(1, 2, 3);
        Vecteur3D v2 = new Vecteur3D(4, 5, 6);

        assertEquals(32, v1.produitScalaire(v2));
    }

    @Test
    void testProduitVectoriel() {
        Vecteur3D v1 = new Vecteur3D(1, 0, 0);
        Vecteur3D v2 = new Vecteur3D(0, 1, 0);

        Vecteur3D resultat = v1.produitVectoriel(v2);

        assertEquals(0, resultat.getX());
        assertEquals(0, resultat.getY());
        assertEquals(1, resultat.getZ());
    }

    @Test
    void testEstNul() {
        Vecteur3D v = new Vecteur3D();

        assertTrue(v.estNul());

        v = new Vecteur3D(1, 0, 0);

        assertFalse(v.estNul());
    }
}
