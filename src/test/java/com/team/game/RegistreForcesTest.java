package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class RegistreForcesTest
{
    private static final double EPSILON = 0.0001;

    private static Particule particule()
    {
        return new Particule(
            new Vecteur3D(), new Vecteur3D(), new Vecteur3D(), 1.0, 1.0
        );
    }

    private static GenerateurForce constante(double x, double y)
    {
        return (p, dt) -> p.ajouterForce(new Vecteur3D(x, y, 0));
    }

    @Test
    public void testChaqueGenerateurAgitSurSaParticule()
    {
        RegistreForces registre = new RegistreForces();
        Particule a = particule();
        Particule b = particule();

        registre.ajouter(a, constante(1, 0));
        registre.ajouter(b, constante(0, 2));
        registre.mettreAJourForces(0.016);

        assertEquals(1.0, a.getForceAccumulee().getX(), EPSILON);
        assertEquals(0.0, a.getForceAccumulee().getY(), EPSILON);
        assertEquals(0.0, b.getForceAccumulee().getX(), EPSILON);
        assertEquals(2.0, b.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testPlusieursGenerateursSurLaMemeParticule()
    {
        RegistreForces registre = new RegistreForces();
        Particule a = particule();

        registre.ajouter(a, constante(1, 0));
        registre.ajouter(a, constante(2, 3));
        registre.mettreAJourForces(0.016);

        assertEquals(3.0, a.getForceAccumulee().getX(), EPSILON);
        assertEquals(3.0, a.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testUnGenerateurPeutServirPlusieursParticules()
    {
        RegistreForces registre = new RegistreForces();
        GenerateurForce gravite = new Gravite(new Vecteur3D(0, 10, 0));
        Particule legere = particule();
        Particule lourde = particule();
        lourde.setMasse(5.0);

        registre.ajouter(legere, gravite);
        registre.ajouter(lourde, gravite);
        registre.mettreAJourForces(0.016);

        assertEquals(10.0, legere.getForceAccumulee().getY(), EPSILON);
        assertEquals(50.0, lourde.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testRetirer()
    {
        RegistreForces registre = new RegistreForces();
        Particule a = particule();
        GenerateurForce g = constante(1, 0);

        registre.ajouter(a, g);

        assertTrue(registre.retirer(a, g));
        assertFalse(registre.retirer(a, g));
        assertEquals(0, registre.taille());

        registre.mettreAJourForces(0.016);

        assertTrue(a.getForceAccumulee().estNul());
    }

    @Test
    public void testRetirerParticule()
    {
        RegistreForces registre = new RegistreForces();
        Particule a = particule();
        Particule b = particule();

        registre.ajouter(a, constante(1, 0));
        registre.ajouter(a, constante(0, 1));
        registre.ajouter(b, constante(1, 1));
        registre.retirerParticule(a);

        assertEquals(1, registre.taille());

        registre.mettreAJourForces(0.016);

        assertTrue(a.getForceAccumulee().estNul());
        assertEquals(1.0, b.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testViderNeDetruitNiParticuleNiGenerateur()
    {
        RegistreForces registre = new RegistreForces();
        Particule a = particule();

        registre.ajouter(a, constante(1, 0));
        registre.vider();

        assertEquals(0, registre.taille());
        assertNotNull(a.getPosition());
    }

    @Test
    public void testLesGenerateursSontAppelesDansLOrdre()
    {
        RegistreForces registre = new RegistreForces();
        Particule a = particule();
        List<Integer> ordre = new ArrayList<>();

        registre.ajouter(a, (p, dt) -> ordre.add(1));
        registre.ajouter(a, (p, dt) -> ordre.add(2));
        registre.ajouter(a, (p, dt) -> ordre.add(3));
        registre.mettreAJourForces(0.016);

        assertEquals(List.of(1, 2, 3), ordre);
    }

    @Test
    public void testDtEstTransmisAuGenerateur()
    {
        RegistreForces registre = new RegistreForces();
        double[] recu = {0};

        registre.ajouter(particule(), (p, dt) -> recu[0] = dt);
        registre.mettreAJourForces(0.25);

        assertEquals(0.25, recu[0], EPSILON);
    }

    @Test
    public void testArgumentsNulsRefuses()
    {
        RegistreForces registre = new RegistreForces();

        assertThrows(IllegalArgumentException.class,
            () -> registre.ajouter(null, constante(1, 0)));
        assertThrows(IllegalArgumentException.class,
            () -> registre.ajouter(particule(), null));
    }

    @Test
    public void testBoucleCompleteUneFrame()
    {
        // Registre -> intégration -> accumulateur vide.
        RegistreForces registre = new RegistreForces();
        Particule a = particule();
        registre.ajouter(a, new Gravite(new Vecteur3D(0, -10, 0)));

        registre.mettreAJourForces(1.0);
        a.integrer(1.0);

        assertEquals(-10.0, a.getVitesse().getY(), EPSILON);
        assertTrue(a.getForceAccumulee().estNul());
    }
}
