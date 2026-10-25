package com.team.game;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;

class JeuBlobTest {
    @Test void maintenirHautNePermetNiVolNiRepetitionDuSaut() {
        PApplet app=new PApplet();app.width=1100;app.height=700;
        PGraphicsJava2D g=new PGraphicsJava2D();g.setParent(app);g.setSize(1100,700);app.g=g;
        AtomicLong temps=new AtomicLong();JeuBlob jeu=new JeuBlob(app,temps::get);
        try {
            for(int i=0;i<120;i++){temps.addAndGet(16_666_667);g.beginDraw();jeu.dessiner();g.endDraw();}
            jeu.touche((char)0,PApplet.UP,true);
            double vy=jeu.getMonde().getGroupeSelectionne().get(0).getVitesse().getY();
            assertTrue(vy < -450);
            jeu.touche((char)0,PApplet.UP,true);
            assertEquals(vy,jeu.getMonde().getGroupeSelectionne().get(0).getVitesse().getY(),1e-9);
            for(int i=0;i<180;i++){
                jeu.touche((char)0,PApplet.UP,true);
                temps.addAndGet(16_666_667);g.beginDraw();jeu.dessiner();g.endDraw();
            }
            for(ParticuleSphere p:jeu.getMonde().getGroupeSelectionne()) {assertTrue(p.getPosition().getY()>520);}
        } finally {g.dispose();}
    }
    @Test void renduDesNeufScenesEtCommandesDuBlob() throws Exception {
        PApplet app = new PApplet();
        app.width = 1100; app.height = 700;
        PGraphicsJava2D g = new PGraphicsJava2D();
        g.setParent(app); g.setSize(1100, 700); app.g = g;
        AtomicLong horloge = new AtomicLong();
        JeuBlob jeu = new JeuBlob(app, horloge::get);
        Path dossier = Path.of("target", "verification-visuelle").toAbsolutePath();
        Files.createDirectories(dossier);
        for (char mode = '1'; mode <= '9'; mode++) {
            jeu.touche(mode, 0, true); jeu.touche(mode, 0, false);
            for (int frame = 0; frame < 120; frame++) {
                horloge.addAndGet(16_666_667);
                g.beginDraw(); jeu.dessiner(); g.endDraw();
            }
            g.save(dossier.resolve("scene-" + mode + ".png").toString());
            for (ParticuleSphere p : jeu.getMonde().getParticules()) {
                assertTrue(Double.isFinite(p.getPosition().norme()));
            }
        }
        jeu.touche('1', 0, true); jeu.touche('1', 0, false);
        jeu.touche('s', 0, true);
        assertEquals(2, jeu.getNiveau().groupesRecrutes().size());
        jeu.touche('s', 0, true); // Répétition clavier ignorée.
        assertEquals(2, jeu.getNiveau().groupesRecrutes().size());
        jeu.touche('s', 0, false);
        jeu.touche('f', 0, true); jeu.touche('f', 0, false);
        assertEquals(1, jeu.getNiveau().groupesRecrutes().size());
        jeu.touche('p', 0, true); jeu.touche('p', 0, false);
        Vecteur3D avant = jeu.getMonde().getParticules().get(0).getPosition().copie();
        horloge.addAndGet(1_000_000_000);
        g.beginDraw(); jeu.dessiner(); g.endDraw();
        assertEquals(0, avant.moins(jeu.getMonde().getParticules().get(0).getPosition()).norme(), 1e-10);
        g.dispose();
    }
}
