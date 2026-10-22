package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests des ressorts (Hooke, ancré, bungee, rigide factice) et de la
 * flottabilité.
 */
public class RessortsTest
{
    private static final double EPSILON = 0.0001;

    private static Particule particule(double x, double y, double masse)
    {
        return new Particule(
            new Vecteur3D(x, y, 0),
            new Vecteur3D(),
            new Vecteur3D(),
            masse,
            1.0
        );
    }

    /* ---------- RESSORT (HOOKE) ---------- */

    @Test
    public void testRessortEtireTireVersLAutre()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(3, 0, 1);

        // Étirement 3 - 1 = 2, F = k * 2 = 20 vers b (+x)
        new Ressort(b, 10, 1).mettreAJourForce(a, 0.016);

        assertEquals(20.0, a.getForceAccumulee().getX(), EPSILON);
        assertEquals(0.0, a.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testRessortComprimePousse()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(0.5, 0, 1);

        // Compression 0.5 - 1 = -0.5, F = 10 * 0.5 = 5 loin de b (-x)
        new Ressort(b, 10, 1).mettreAJourForce(a, 0.016);

        assertEquals(-5.0, a.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testRessortALaLongueurAuReposNeFaitRien()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(1, 0, 1);

        new Ressort(b, 10, 1).mettreAJourForce(a, 0.016);

        assertTrue(a.getForceAccumulee().estNul());
    }

    @Test
    public void testRessortEnDiagonale()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(3, 4, 1);

        // Longueur 5, étirement 5, F = 5 dans la direction (0.6, 0.8)
        new Ressort(b, 1, 0).mettreAJourForce(a, 0.016);

        assertEquals(3.0, a.getForceAccumulee().getX(), EPSILON);
        assertEquals(4.0, a.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testRessortForcesEgalesEtOpposees()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(3, 1, 7);
        RegistreForces registre = new RegistreForces();

        registre.ajouter(a, new Ressort(b, 10, 1));
        registre.ajouter(b, new Ressort(a, 10, 1));
        registre.mettreAJourForces(0.016);

        assertEquals(0.0, a.getForceAccumulee().getX() + b.getForceAccumulee().getX(), EPSILON);
        assertEquals(0.0, a.getForceAccumulee().getY() + b.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testRessortSurParticulesConfonduesNeFaitRien()
    {
        Particule a = particule(2, 2, 1);
        Particule b = particule(2, 2, 1);

        new Ressort(b, 10, 1).mettreAJourForce(a, 0.016);

        assertTrue(a.getForceAccumulee().estNul());
    }

    @Test
    public void testRessortLongueurActuelle()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(3, 4, 1);

        assertEquals(5.0, new Ressort(b, 1, 1).longueurActuelle(a), EPSILON);
    }

    @Test
    public void testRessortOscilleALaPeriodeTheorique()
    {
        // Ancre simulée par une particule de masse infinie.
        Particule ancre = particule(0, 0, 1);
        ancre.setInverseMasse(0);
        Particule masse = particule(1.5, 0, 1); // l0 = 1 -> étirement initial 0,5

        RegistreForces registre = new RegistreForces();
        registre.ajouter(masse, new Ressort(ancre, 100, 1));

        // T = 2 pi sqrt(m / k) = 0.6283 s
        double dt = 0.0001;
        int pas = (int) Math.round(2 * Math.PI * Math.sqrt(1.0 / 100) / dt);

        for (int i = 0; i < pas; i++)
        {
            registre.mettreAJourForces(dt);
            masse.integrer(dt);
        }

        // Après une période, retour à la position de départ.
        assertEquals(1.5, masse.getPosition().getX(), 0.02);
    }

    @Test
    public void testRessortArgumentsInvalides()
    {
        Particule a = particule(0, 0, 1);

        assertThrows(IllegalArgumentException.class, () -> new Ressort(null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Ressort(a, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Ressort(a, 1, -1));
    }

    /* ---------- RESSORT ANCRÉ ---------- */

    @Test
    public void testRessortAncreTireVersLAncre()
    {
        Particule p = particule(5, 0, 1);

        // Étirement 5 - 1 = 4, F = 2 * 4 = 8 vers l'ancre (-x)
        new RessortAncre(new Vecteur3D(0, 0, 0), 2, 1)
            .mettreAJourForce(p, 0.016);

        assertEquals(-8.0, p.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testRessortAncreConserveUneCopieDeLAncre()
    {
        Vecteur3D ancre = new Vecteur3D(0, 0, 0);
        RessortAncre ressort = new RessortAncre(ancre, 2, 1);
        ancre.additionner(new Vecteur3D(100, 0, 0));

        assertEquals(0.0, ressort.getAncre().getX(), EPSILON);
    }

    @Test
    public void testRessortAncrePousseQuandComprime()
    {
        Particule p = particule(0.5, 0, 1);

        new RessortAncre(new Vecteur3D(), 2, 1).mettreAJourForce(p, 0.016);

        assertEquals(1.0, p.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testRessortAncreArgumentsInvalides()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new RessortAncre(null, 1, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new RessortAncre(new Vecteur3D(), -1, 1));
    }

    /* ---------- BUNGEE ---------- */

    @Test
    public void testBungeeDetenduNeFaitRien()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(0.5, 0, 1);

        new RessortBungee(b, 10, 1).mettreAJourForce(a, 0.016);

        assertTrue(a.getForceAccumulee().estNul());
    }

    @Test
    public void testBungeeEtireTireCommeUnRessort()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(3, 0, 1);

        new RessortBungee(b, 10, 1).mettreAJourForce(a, 0.016);

        assertEquals(20.0, a.getForceAccumulee().getX(), EPSILON);
    }

    @Test
    public void testBungeeNePousseJamais()
    {
        Particule a = particule(0, 0, 1);
        Particule b = particule(0.1, 0, 1);

        new RessortBungee(b, 1000, 5).mettreAJourForce(a, 0.016);

        assertTrue(a.getForceAccumulee().estNul());
    }

    @Test
    public void testBungeeArgumentsInvalides()
    {
        Particule a = particule(0, 0, 1);

        assertThrows(IllegalArgumentException.class, () -> new RessortBungee(null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new RessortBungee(a, -1, 1));
    }

    /* ---------- FLOTTABILITÉ ---------- */

    private static Flottabilite flottabilite()
    {
        // Surface à y = 0, profondeur max 1, volume 2, densité 1000, y vers le haut.
        return new Flottabilite(1, 2, 0, 1000, new Vecteur3D(0, 1, 0));
    }

    @Test
    public void testFlottabiliteHorsDuLiquide()
    {
        Particule p = particule(0, 2, 1);

        flottabilite().mettreAJourForce(p, 0.016);

        assertTrue(p.getForceAccumulee().estNul());
    }

    @Test
    public void testFlottabiliteTotalementImmergee()
    {
        Particule p = particule(0, -2, 1);

        flottabilite().mettreAJourForce(p, 0.016);

        assertEquals(2000.0, p.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testFlottabilitePartielleProportionnelleALImmersion()
    {
        Particule moitie = particule(0, 0, 1);
        Particule quart = particule(0, 0.5, 1);

        flottabilite().mettreAJourForce(moitie, 0.016);
        flottabilite().mettreAJourForce(quart, 0.016);

        assertEquals(1000.0, moitie.getForceAccumulee().getY(), EPSILON);
        assertEquals(500.0, quart.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testFlottabiliteAvecAxeYVersLeBas()
    {
        // Processing : le "haut" est (0, -1, 0). Surface à hauteur 0.
        Flottabilite f = new Flottabilite(1, 2, 0, 1000, new Vecteur3D(0, -1, 0));
        // y = 2 en pixels -> hauteur -2 : sous la surface
        Particule p = particule(0, 2, 1);

        f.mettreAJourForce(p, 0.016);

        assertEquals(-2000.0, p.getForceAccumulee().getY(), EPSILON);
    }

    @Test
    public void testObjetFlotteALEquilibre()
    {
        // Poids = m g = 1 * 10 ; la poussée max est 20 : l'objet remonte et
        // se stabilise là où la poussée vaut 10, soit à 50 % d'immersion.
        RegistreForces registre = new RegistreForces();
        Particule p = new Particule(
            new Vecteur3D(0, -3, 0),
            new Vecteur3D(),
            new Vecteur3D(),
            1.0,
            0.5
        );
        registre.ajouter(p, new Gravite(new Vecteur3D(0, -10, 0)));
        registre.ajouter(p, new Flottabilite(1, 0.02, 0, 1000, new Vecteur3D(0, 1, 0)));

        for (int i = 0; i < 20000; i++)
        {
            registre.mettreAJourForces(0.001);
            p.integrer(0.001);
        }

        // 50 % d'immersion : au niveau de la surface.
        assertEquals(0.0, p.getPosition().getY(), 0.01);
    }

    @Test
    public void testFlottabiliteArgumentsInvalides()
    {
        Vecteur3D haut = new Vecteur3D(0, 1, 0);

        assertThrows(IllegalArgumentException.class,
            () -> new Flottabilite(0, 1, 0, 1000, haut));
        assertThrows(IllegalArgumentException.class,
            () -> new Flottabilite(1, 0, 0, 1000, haut));
        assertThrows(IllegalArgumentException.class,
            () -> new Flottabilite(1, 1, 0, 1000, new Vecteur3D()));
    }

    /* ---------- RESSORT RIGIDE FACTICE ---------- */

    @Test
    public void testRessortClassiqueTresRaideExplose()
    {
        // Sans amortissement, l'Euler explicite fait croître l'amplitude
        // de ~13 % par frame à 60 images/s avec k = 1000 : elle diverge.
        RegistreForces registre = new RegistreForces();
        Particule p = particule(10, 0, 1);
        registre.ajouter(p, new RessortAncre(new Vecteur3D(), 1000, 0));

        double maxAbs = 0;

        for (int i = 0; i < 120; i++)
        {
            registre.mettreAJourForces(1.0 / 60);
            p.integrer(1.0 / 60);
            maxAbs = Math.max(maxAbs, Math.abs(p.getPosition().getX()));
        }

        assertTrue(maxAbs > 1000);
    }

    @Test
    public void testRessortRigideFacticeResteStableEtConverge()
    {
        RegistreForces registre = new RegistreForces();
        Particule p = particule(10, 0, 1);
        registre.ajouter(p, new RessortRigideFactice(new Vecteur3D(), 1000, 20));

        double maxAbs = 0;

        for (int i = 0; i < 120; i++)
        {
            registre.mettreAJourForces(1.0 / 60);
            p.integrer(1.0 / 60);
            maxAbs = Math.max(maxAbs, Math.abs(p.getPosition().getX()));
        }

        assertTrue(maxAbs <= 10.0 + 1e-6);
        assertEquals(0.0, p.getPosition().getX(), 0.01);
    }

    @Test
    public void testRessortRigideFacticeSurAmortiNeFaitRien()
    {
        // 4k <= d^2 : pas de solution oscillante.
        Particule p = particule(10, 0, 1);

        new RessortRigideFactice(new Vecteur3D(), 1, 10)
            .mettreAJourForce(p, 0.016);

        assertTrue(p.getForceAccumulee().estNul());
    }

    @Test
    public void testRessortRigideFacticeArgumentsInvalides()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new RessortRigideFactice(null, 1, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new RessortRigideFactice(new Vecteur3D(), 0, 1));
        assertThrows(IllegalArgumentException.class,
            () -> new RessortRigideFactice(new Vecteur3D(), 1, -1));
    }
}
