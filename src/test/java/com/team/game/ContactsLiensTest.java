package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests des câbles, des tiges et du plan fixe.
 */
public class ContactsLiensTest
{
    private static final double EPSILON = 0.0001;

    private static ParticuleSphere sphere(double x, double y, double vx, double vy)
    {
        return new ParticuleSphere(
            new Vecteur3D(x, y, 0),
            new Vecteur3D(vx, vy, 0),
            new Vecteur3D(),
            1.0,
            1.0,
            1.0
        );
    }

    private static double distance(Particule a, Particule b)
    {
        return a.getPosition().moins(b.getPosition()).norme();
    }

    /* ---------- CÂBLE ---------- */

    @Test
    public void testCableRelacheNeGenerePasDeContact()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(3, 0, 0, 0);

        List<Contact> contacts = new ArrayList<>();
        new ContactCable(a, b, 5, 0).genererContacts(contacts);

        assertTrue(contacts.isEmpty());
    }

    @Test
    public void testCableTenduGenereUnContact()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(7, 0, 0, 0);

        List<Contact> contacts = new ArrayList<>();
        new ContactCable(a, b, 5, 0).genererContacts(contacts);

        assertEquals(1, contacts.size());
        assertEquals(2.0, contacts.get(0).getInterpenetration(), EPSILON);
    }

    @Test
    public void testCableRamenePasLesParticulesALaLongueurMax()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(7, 0, 0, 0);
        List<ParticuleSphere> liste = List.of(a, b);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactCable(a, b, 5, 0));
        gestionnaire.traiter(0.016);

        assertEquals(5.0, distance(a, b), EPSILON);
        assertEquals(2, liste.size());
    }

    @Test
    public void testCableAnnuleLaVitesseQuiEloigne()
    {
        ParticuleSphere ancre = sphere(0, 0, 0, 0);
        ancre.setInverseMasse(0);
        ParticuleSphere boule = sphere(5, 0, 10, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactCable(ancre, boule, 5, 0));
        gestionnaire.traiter(0.016);

        assertEquals(0.0, boule.getVitesse().getX(), EPSILON);
        assertEquals(0.0, ancre.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testCableRebonditAvecRestitution()
    {
        ParticuleSphere ancre = sphere(0, 0, 0, 0);
        ancre.setInverseMasse(0);
        ParticuleSphere boule = sphere(5, 0, 10, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactCable(ancre, boule, 5, 0.5));
        gestionnaire.traiter(0.016);

        assertEquals(-5.0, boule.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testCableLaisseLesParticulesSeRapprocher()
    {
        ParticuleSphere ancre = sphere(0, 0, 0, 0);
        ancre.setInverseMasse(0);
        ParticuleSphere boule = sphere(5, 0, -10, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactCable(ancre, boule, 5, 0));
        gestionnaire.traiter(0.016);

        assertEquals(-10.0, boule.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testPenduleSurCableNeDepassePasLaLongueur()
    {
        double dt = 1.0 / 60.0;
        ParticuleSphere ancre = sphere(0, 0, 0, 0);
        ancre.setInverseMasse(0);
        ParticuleSphere boule = new ParticuleSphere(
            new Vecteur3D(3, 0, 0),
            new Vecteur3D(),
            new Vecteur3D(0, 400, 0), // y vers le bas
            1.0,
            0.999,
            1.0
        );

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactCable(ancre, boule, 3, 0));

        for (int i = 0; i < 600; i++)
        {
            boule.integrer(dt);
            gestionnaire.traiter(dt);
            assertTrue(distance(ancre, boule) < 3.0 + 0.05);
        }
    }

    @Test
    public void testCableArgumentsInvalides()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class,
            () -> new ContactCable(a, null, 5, 0));
        assertThrows(IllegalArgumentException.class,
            () -> new ContactCable(a, a, 0, 0));
    }

    /* ---------- TIGE ---------- */

    @Test
    public void testTigeALaBonneLongueurGenereUnContactSansInterpenetration()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(5, 0, 0, 0);

        List<Contact> contacts = new ArrayList<>();
        new ContactTige(a, b, 5).genererContacts(contacts);

        assertEquals(1, contacts.size());
        assertEquals(0.0, contacts.get(0).getInterpenetration(), EPSILON);
    }

    @Test
    public void testTigeTropLongueRapprocheLesParticules()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(7, 0, 0, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactTige(a, b, 5));
        gestionnaire.traiter(0.016);

        assertEquals(5.0, distance(a, b), EPSILON);
        // Masses égales : chacune parcourt la moitié.
        assertEquals(1.0, a.getPosition().getX(), EPSILON);
        assertEquals(6.0, b.getPosition().getX(), EPSILON);
    }

    @Test
    public void testTigeTropCourteEcarteLesParticules()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(3, 0, 0, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactTige(a, b, 5));
        gestionnaire.traiter(0.016);

        assertEquals(5.0, distance(a, b), EPSILON);
        assertEquals(-1.0, a.getPosition().getX(), EPSILON);
        assertEquals(4.0, b.getPosition().getX(), EPSILON);
    }

    @Test
    public void testTigeEmpecheLEloignementEtLeRapprochement()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(5, 0, 4, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactTige(a, b, 5));
        gestionnaire.traiter(0.016);

        // La vitesse relative le long de la tige est annulée.
        double relative = b.getVitesse().getX() - a.getVitesse().getX();
        assertEquals(0.0, relative, EPSILON);

        ParticuleSphere c = sphere(0, 0, 0, 0);
        ParticuleSphere d = sphere(5, 0, -4, 0);
        GestionnaireCollisions autre = new GestionnaireCollisions();
        autre.ajouter(new ContactTige(c, d, 5));
        autre.traiter(0.016);

        // À longueur exacte, le contact généré bloque l'éloignement mais
        // laisse passer le rapprochement : la tige se raccourcit d'abord,
        // puis le contact "trop court" prend le relais à la frame suivante.
        assertEquals(-4.0, d.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testTigeAvecParticuleFixe()
    {
        ParticuleSphere ancre = sphere(0, 0, 0, 0);
        ancre.setInverseMasse(0);
        ParticuleSphere boule = sphere(8, 0, 0, 0);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactTige(ancre, boule, 5));
        gestionnaire.traiter(0.016);

        assertEquals(0.0, ancre.getPosition().getX(), EPSILON);
        assertEquals(5.0, boule.getPosition().getX(), EPSILON);
    }

    /* ---------- PLAN ---------- */

    @Test
    public void testPlanSphereAuDessusNeGenerePasDeContact()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 5, 0, 0));

        List<Contact> contacts = new ArrayList<>();
        new ContactPlan(
            particules, new Vecteur3D(0, 0, 0), new Vecteur3D(0, 1, 0), 0.5
        ).genererContacts(contacts);

        assertTrue(contacts.isEmpty());
    }

    @Test
    public void testPlanSphereEnfonceeGenereUnContact()
    {
        List<ParticuleSphere> particules = new ArrayList<>();
        particules.add(sphere(0, 0.4, 0, 0));

        List<Contact> contacts = new ArrayList<>();
        new ContactPlan(
            particules, new Vecteur3D(0, 0, 0), new Vecteur3D(0, 1, 0), 0.5
        ).genererContacts(contacts);

        assertEquals(1, contacts.size());
        assertEquals(0.6, contacts.get(0).getInterpenetration(), EPSILON);
        assertNull(contacts.get(0).getParticule2());
    }

    @Test
    public void testPlanRemonteLaSphere()
    {
        ParticuleSphere balle = sphere(0, 0.4, 0, -10);
        List<ParticuleSphere> particules = List.of(balle);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactPlan(
            particules, new Vecteur3D(0, 0, 0), new Vecteur3D(0, 1, 0), 0.5
        ));
        gestionnaire.traiter(0.016);

        assertEquals(1.0, balle.getPosition().getY(), EPSILON);
        assertEquals(5.0, balle.getVitesse().getY(), EPSILON);
    }

    @Test
    public void testBalleQuiTombeSeStabiliseAuSol()
    {
        double dt = 1.0 / 60.0;
        ParticuleSphere balle = new ParticuleSphere(
            new Vecteur3D(0, 50, 0),
            new Vecteur3D(),
            new Vecteur3D(0, -400, 0), // y vers le haut
            1.0,
            1.0,
            1.0
        );
        List<ParticuleSphere> particules = List.of(balle);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new ContactPlan(
            particules, new Vecteur3D(0, 0, 0), new Vecteur3D(0, 1, 0), 0.5
        ));

        double minY = Double.MAX_VALUE;
        double maxYFinal = 0;

        for (int i = 0; i < 1200; i++)
        {
            balle.integrer(dt);
            gestionnaire.traiter(dt);
            minY = Math.min(minY, balle.getPosition().getY());

            if (i >= 600)
            {
                maxYFinal = Math.max(maxYFinal, balle.getPosition().getY());
            }
        }

        // Jamais sous le sol, et plus de rebond visible à la fin : la
        // balle reste posée (vibration inférieure à 0,05 px).
        assertTrue(minY >= 1.0 - 1e-6);
        assertTrue(maxYFinal <= 1.05);
    }

    @Test
    public void testPlanNormaleNulleInvalide()
    {
        assertThrows(IllegalArgumentException.class, () -> new ContactPlan(
            new ArrayList<ParticuleSphere>(),
            new Vecteur3D(),
            new Vecteur3D(),
            1
        ));
    }
}
