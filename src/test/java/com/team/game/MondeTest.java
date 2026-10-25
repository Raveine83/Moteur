package com.team.game;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class MondeTest {
    @Test void blobInitialConnecteEtSansChevauchement() {
        Monde m = Monde.creerBlob();
        assertEquals(12, m.getGroupeSelectionne().size());
        assertEquals(1, m.getGroupes().size());
        for (int i = 0; i < 12; i++) {
            for (int j = i + 1; j < 12; j++) {
                assertTrue(m.getParticules().get(i).getPosition()
                    .moins(m.getParticules().get(j).getPosition()).norme() >= 26);
            }
        }
    }

    @Test void separationEtFusionModifientLeGrapheEtLeCompteur() {
        Monde m = Monde.creerBlob();
        int liensAvant = m.getLiens().size();
        assertTrue(m.separer());
        assertEquals(2, m.getGroupes().size());
        assertEquals(6, m.getGroupeSelectionne().size());
        assertTrue(m.getLiens().size() < liensAvant);
        List<ParticuleSphere> premier = m.getGroupeSelectionne();
        m.selectionSuivante();
        assertFalse(m.getGroupeSelectionne().contains(premier.get(0)));
        assertTrue(m.fusionner());
        assertEquals(1, m.getGroupes().size());
        assertEquals(12, m.getGroupeSelectionne().size());
    }

    @Test void fusionNeTeleportePasUnGroupeLointain() {
        Monde m = Monde.creerBlob();
        m.separer();
        for (ParticuleSphere p : m.getGroupeSelectionne()) {
            p.getPosition().additionner(new Vecteur3D(-500, 0, 0));
        }
        assertFalse(m.fusionner());
        assertEquals(2, m.getGroupes().size());
    }

    @Test void simulationProlongeeStableEtCablesRespectes() {
        Monde m = Monde.creerBlob();
        for (int i = 0; i < 1200; i++) {
            m.commander(i < 300 ? 1 : 0, i >= 300 && i < 360 ? -1 : 0);
            m.avancer(1.0 / 60);
        }
        for (ParticuleSphere p : m.getParticules()) {
            assertTrue(Double.isFinite(p.getPosition().norme()));
            assertTrue(p.getPosition().getY() <= Monde.SOL - p.getRayon() + 0.1);
            assertTrue(p.getPosition().getX() >= 25 + p.getRayon() - 0.1);
            assertTrue(p.getPosition().getX() <= 1075 - p.getRayon() + 0.1);
        }
        for (LienBlob l : m.getLiens()) { assertTrue(l.getLongueur() <= l.getMaximum() + 0.2); }
    }

    @Test void pasFixeIndependantDeLaFrequenceDeRendu() {
        Monde a = Monde.creerBlob(), b = Monde.creerBlob();
        a.commander(1, -0.5); b.commander(1, -0.5);
        for (int i = 0; i < 60; i++) { a.avancer(1.0 / 60); }
        for (int i = 0; i < 144; i++) { b.avancer(1.0 / 144); }
        for (int i = 0; i < 12; i++) {
            assertEquals(0, a.getParticules().get(i).getPosition()
                .moins(b.getParticules().get(i).getPosition()).norme(), 1e-8);
        }
    }

    @Test void contactsAuReposUtilisentLaGraviteDuGenerateur() {
        Monde m = new Monde(false);
        ParticuleSphere p = m.ajouter(400, Monde.SOL - 13, true);
        for (int i = 0; i < 600; i++) { m.avancer(1.0 / 60); }
        assertEquals(Monde.SOL - 13, p.getPosition().getY(), 1e-6);
        assertEquals(0, p.getVitesse().getY(), 1e-6);
    }

    @Test void limiteTempsEtDureesInvalides() {
        Monde a = Monde.creerBlob(), b = Monde.creerBlob();
        a.avancer(Double.NaN); a.avancer(-1); a.avancer(Double.POSITIVE_INFINITY);
        a.avancer(30); b.avancer(0.1);
        assertEquals(0, a.getParticules().get(0).getPosition()
            .moins(b.getParticules().get(0).getPosition()).norme(), 1e-10);
    }

    @Test void objectifAccessibleParLesCommandes() {
        Monde m = Monde.creerBlob();
        assertFalse(m.objectifAtteint());
        m.commander(1, 0);
        for (int i = 0; i < 600; i++) { m.avancer(1.0 / 60); }
        assertTrue(m.objectifAtteint());
    }

    @Test void oscillationAmortieChangeDeSigneEtConverge() {
        OscillateurAmorti o = new OscillateurAmorti();
        o.declencher();
        assertEquals(-22, o.getDeplacement(), 1e-10);
        o.avancer(0.25);
        assertTrue(o.getDeplacement() > 0);
        o.avancer(5);
        assertEquals(0, o.getDeplacement(), 1e-7);
        o.declencher();
        assertEquals(-22, o.getDeplacement(), 1e-10);
    }

    @Test void frictionCinetiqueNeRenversePasLaVitesse() {
        Particule p = new Particule(new Vecteur3D(), new Vecteur3D(0.01, 0, 0), new Vecteur3D(), 1, 1);
        new FrictionSolide(360, 0.6, 0.4).mettreAJourForce(p, 1.0 / 60);
        p.integrer(1.0 / 60);
        assertEquals(0, p.getVitesse().getX(), 1e-10);
    }
}
