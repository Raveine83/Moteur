package com.team.game;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContactRectangleTest {
    private Contact tester(double x, double y, double rayon) {
        ParticuleSphere p = new ParticuleSphere(rayon);
        p.setPosition(new Vecteur3D(x, y, 0));
        ContactRectangle obstacle = new ContactRectangle(List.of(p), 100, 100, 40, 80);
        List<Contact> contacts = new ArrayList<>(); obstacle.genererContacts(contacts);
        if (contacts.isEmpty()) { return null; }
        Contact c = contacts.get(0); c.resoudreInterpenetration(); return c;
    }
    @Test void facesCoinsEtCentreSontCorrectementSepares() {
        assertNull(tester(80, 140, 10));
        assertEquals(90, tester(95, 140, 10).getParticule1().getPosition().getX(), 1e-9);
        assertEquals(150, tester(145, 140, 10).getParticule1().getPosition().getX(), 1e-9);
        assertEquals(90, tester(120, 95, 10).getParticule1().getPosition().getY(), 1e-9);
        assertEquals(190, tester(120, 185, 10).getParticule1().getPosition().getY(), 1e-9);
        assertEquals(90, tester(105, 130, 10).getParticule1().getPosition().getX(), 1e-9);
        Contact coin = tester(95, 95, 10);
        assertEquals(10, coin.getParticule1().getPosition().moins(new Vecteur3D(100,100,0)).norme(), 1e-9);
    }
    @Test void unePorteDesactiveeNeGenerePlusDeContact() {
        ParticuleSphere p = new ParticuleSphere(13); p.setPosition(new Vecteur3D(110,130,0));
        ContactRectangle porte = new ContactRectangle(List.of(p),100,100,40,80);
        porte.setActif(false); List<Contact> contacts = new ArrayList<>(); porte.genererContacts(contacts);
        assertTrue(contacts.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new ContactRectangle(List.of(p),0,0,0,5));
    }
}
