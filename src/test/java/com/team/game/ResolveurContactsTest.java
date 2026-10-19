package com.team.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ResolveurContactsTest
{
    private static final double EPSILON = 0.0001;
    private static final double DT = 1.0 / 60.0;

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

    @Test
    public void testListeVideOuDtInvalideNeFaitRien()
    {
        ParticuleSphere a = sphere(0, 0, 5, 0);
        ParticuleSphere b = sphere(1, 0, 0, 0);
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(a, b, new Vecteur3D(-1, 0, 0), 1, 1));

        ResolveurContacts resolveur = new ResolveurContacts();
        resolveur.resoudre(new ArrayList<>(), DT);
        resolveur.resoudre(null, DT);
        resolveur.resoudre(contacts, 0);

        assertEquals(5.0, a.getVitesse().getX(), EPSILON);
        assertEquals(0.0, a.getPosition().getX(), EPSILON);
    }

    @Test
    public void testCollisionSpheresSepareEtRebondit()
    {
        ParticuleSphere a = sphere(0, 0, 5, 0);
        ParticuleSphere b = sphere(1.5, 0, -5, 0);
        List<ParticuleSphere> liste = List.of(a, b);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new DetecteurCollisions(liste, 1));
        gestionnaire.traiter(DT);

        // Choc élastique, masses égales : les vitesses sont échangées.
        assertEquals(-5.0, a.getVitesse().getX(), EPSILON);
        assertEquals(5.0, b.getVitesse().getX(), EPSILON);
        // Plus d'interpénétration.
        double distance = a.getPosition().moins(b.getPosition()).norme();
        assertEquals(2.0, distance, EPSILON);
    }

    @Test
    public void testPireContactEstResoluEnPremier()
    {
        // Une seule itération : seul le choc le plus rapide est traité.
        ParticuleSphere murFixe = sphere(0, 0, 0, 0);
        murFixe.setInverseMasse(0);
        ParticuleSphere lente = sphere(10, 0, -1, 0);
        ParticuleSphere rapide = sphere(-10, 0, 20, 0);

        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(lente, murFixe, new Vecteur3D(1, 0, 0), 0, 0));
        contacts.add(new Contact(rapide, murFixe, new Vecteur3D(-1, 0, 0), 0, 0));

        new ResolveurContacts(1, 0).resoudre(contacts, DT);

        assertEquals(0.0, rapide.getVitesse().getX(), EPSILON);
        assertEquals(-1.0, lente.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testIterationsParDefautResolventTousLesContacts()
    {
        ParticuleSphere murFixe = sphere(0, 0, 0, 0);
        murFixe.setInverseMasse(0);
        ParticuleSphere lente = sphere(10, 0, -1, 0);
        ParticuleSphere rapide = sphere(-10, 0, 20, 0);

        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(lente, murFixe, new Vecteur3D(1, 0, 0), 0, 0));
        contacts.add(new Contact(rapide, murFixe, new Vecteur3D(-1, 0, 0), 0, 0));

        new ResolveurContacts().resoudre(contacts, DT);

        assertEquals(0.0, rapide.getVitesse().getX(), EPSILON);
        assertEquals(0.0, lente.getVitesse().getX(), EPSILON);
    }

    @Test
    public void testInterpenetrationsEnChaineSontCorrigees()
    {
        // Trois sphères alignées qui se chevauchent : corriger un contact
        // modifie l'interpénétration de l'autre.
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(1.5, 0, 0, 0);
        ParticuleSphere c = sphere(3.0, 0, 0, 0);
        List<ParticuleSphere> liste = List.of(a, b, c);

        // Une seule passe : on teste la mise à jour des interpénétrations
        // dans le résolveur (la solution est approchée, pas exacte).
        GestionnaireCollisions gestionnaire =
            new GestionnaireCollisions(new ResolveurContacts(), 1);
        gestionnaire.ajouter(new DetecteurCollisions(liste, 0));
        gestionnaire.traiter(DT);

        double ab = b.getPosition().moins(a.getPosition()).norme();
        double bc = c.getPosition().moins(b.getPosition()).norme();

        assertEquals(2.0, ab, 0.15);
        assertEquals(2.0, bc, 0.15);

        // Avec plusieurs passes, on converge vers l'écart exact.
        ParticuleSphere d = sphere(0, 0, 0, 0);
        ParticuleSphere e = sphere(1.5, 0, 0, 0);
        ParticuleSphere f = sphere(3.0, 0, 0, 0);
        List<ParticuleSphere> autre = List.of(d, e, f);

        GestionnaireCollisions precis = new GestionnaireCollisions();
        precis.ajouter(new DetecteurCollisions(autre, 0));
        precis.traiter(DT);

        assertEquals(2.0,
            e.getPosition().moins(d.getPosition()).norme(), 0.05);
        assertEquals(2.0,
            f.getPosition().moins(e.getPosition()).norme(), 0.05);
    }

    @Test
    public void testEmpilementReposeSurLeSol()
    {
        double dt = 1.0 / 60.0;
        ParticuleSphere bas = new ParticuleSphere(
            new Vecteur3D(0, 1, 0), new Vecteur3D(),
            new Vecteur3D(0, -400, 0), 1.0, 1.0, 1.0
        );
        ParticuleSphere haut = new ParticuleSphere(
            new Vecteur3D(0, 3, 0), new Vecteur3D(),
            new Vecteur3D(0, -400, 0), 1.0, 1.0, 1.0
        );
        List<ParticuleSphere> liste = new ArrayList<>(List.of(bas, haut));

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        gestionnaire.ajouter(new DetecteurCollisions(liste, 0.2));
        gestionnaire.ajouter(new ContactPlan(
            liste, new Vecteur3D(0, 0, 0), new Vecteur3D(0, 1, 0), 0.2
        ));

        for (int i = 0; i < 600; i++)
        {
            bas.integrer(dt);
            haut.integrer(dt);
            gestionnaire.traiter(dt);
        }

        assertEquals(1.0, bas.getPosition().getY(), 0.05);
        assertEquals(3.0, haut.getPosition().getY(), 0.1);
        assertEquals(0.0, haut.getVitesse().getY(), 0.5);
    }

    @Test
    public void testGestionnaireRetourneLesContactsTraites()
    {
        ParticuleSphere a = sphere(0, 0, 0, 0);
        ParticuleSphere b = sphere(1, 0, 0, 0);
        List<ParticuleSphere> liste = List.of(a, b);

        GestionnaireCollisions gestionnaire = new GestionnaireCollisions();
        DetecteurCollisions detecteur = new DetecteurCollisions(liste, 1);
        gestionnaire.ajouter(detecteur);

        assertEquals(1, gestionnaire.traiter(DT).size());

        gestionnaire.retirer(detecteur);

        assertEquals(0, gestionnaire.traiter(DT).size());
    }

    @Test
    public void testIterationsNegativesInvalides()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new ResolveurContacts(-1, 0));
    }
}
