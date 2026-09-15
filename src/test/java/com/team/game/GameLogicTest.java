package com.team.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLogicTest {

    private GameLogic logic;

    @BeforeEach
    void setUp() {
        logic = new GameLogic();
    }

    @Test
    void startsWithZeroRotation() {
        assertEquals(0f, logic.getRotationX(), 0.001);
        assertEquals(0f, logic.getRotationY(), 0.001);
    }

    @Test
    void rotationIncreasesOverTime() {
        logic.update(1.0f);

        assertTrue(logic.getRotationX() > 0f, "X rotation should increase after an update");
        assertTrue(logic.getRotationY() > 0f, "Y rotation should increase after an update");
    }

    @Test
    void rotationIsProportionalToElapsedTime() {
        logic.update(2.0f);
        float afterTwoSeconds = logic.getRotationX();

        logic = new GameLogic();
        logic.update(1.0f);
        logic.update(1.0f);
        float afterTwoSteps = logic.getRotationX();

        assertEquals(afterTwoSeconds, afterTwoSteps, 0.001,
                "Two 1-second updates should equal one 2-second update");
    }
}
