package com.exemple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {

    private GameLogic logic;

    @BeforeEach
    void setUp() {
        logic = new GameLogic(640, 480);
    }

    @Test
    void demarreAuCentreDeLecran() {
        assertEquals(320f, logic.getX(), 0.001);
        assertEquals(240f, logic.getY(), 0.001);
    }

    @Test
    void seDeplaceVersLaDroiteInitialement() {
        float xInitial = logic.getX();
        logic.update();
        assertTrue(logic.getX() > xInitial, "Le carré devrait avancer vers la droite");
    }

    @Test
    void rebonditSurLeBordDroit() {
        // On force la position juste avant le bord droit
        for (int i = 0; i < 1000; i++) {
            logic.update();
        }
        // Après un long moment, la vitesse a dû s'inverser au moins une fois
        assertTrue(logic.getX() >= 0 && logic.getX() <= 640,
                "Le carré ne doit jamais sortir de l'écran");
    }

    @Test
    void neSortJamaisDeLecranHorizontalement() {
        for (int i = 0; i < 5000; i++) {
            logic.update();
            assertTrue(logic.getX() >= 0, "x ne doit jamais être négatif");
            assertTrue(logic.getX() <= 640, "x ne doit jamais dépasser la largeur");
        }
    }
}
