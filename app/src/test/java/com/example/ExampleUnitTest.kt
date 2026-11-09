package com.example

import com.example.model.Routine
import com.example.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDefaultRoutinesLoaded() {
        val defaults = Routine.getDefaultRoutines()
        assertEquals(4, defaults.size)
        assertEquals("BARRAS 2.0", defaults[0].name)
        assertEquals("BARRAS", defaults[1].name)
        assertEquals("ABDOMEN", defaults[2].name)
        assertEquals("ABDOMEN 3", defaults[3].name)
    }

    @Test
    fun testRoutineStepGeneration() {
        val routine = Routine(
            name = "Test Routine",
            prepareSeconds = 5,
            workSeconds = 20,
            restSeconds = 10,
            rounds = 2,
            cycles = 1,
            cycleRestSeconds = 0,
            cooldownSeconds = 15
        )

        val steps = routine.generateSteps()
        // Expected steps:
        // 1: PREPARE (5s)
        // 2: WORK round 1 (20s)
        // 3: REST round 1 (10s)
        // 4: WORK round 2 (20s)
        // 5: COOLDOWN (15s)
        assertEquals(5, steps.size)
        assertEquals(TimerPhase.PREPARE, steps[0].phase)
        assertEquals(5, steps[0].durationSeconds)
        assertEquals(TimerPhase.WORK, steps[1].phase)
        assertEquals(20, steps[1].durationSeconds)
        assertEquals(TimerPhase.REST, steps[2].phase)
        assertEquals(10, steps[2].durationSeconds)
        assertEquals(TimerPhase.WORK, steps[3].phase)
        assertEquals(20, steps[3].durationSeconds)
        assertEquals(TimerPhase.COOLDOWN, steps[4].phase)
        assertEquals(15, steps[4].durationSeconds)

        val totalExpected = 5 + 20 + 10 + 20 + 15
        assertEquals(totalExpected, routine.calculateTotalSeconds())
    }

    @Test
    fun testMultiCycleCalculation() {
        val routine = Routine(
            name = "Cycle Test",
            prepareSeconds = 10,
            workSeconds = 20,
            restSeconds = 10,
            rounds = 2,
            cycles = 2,
            cycleRestSeconds = 30,
            cooldownSeconds = 0
        )
        // Cycle 1: Work(20) + Rest(10) + Work(20) = 50
        // Rest between cycles: 30
        // Cycle 2: Work(20) + Rest(10) + Work(20) = 50
        // Prep: 10
        // Total: 10 + 50 + 30 + 50 = 140s
        assertEquals(140, routine.calculateTotalSeconds())
    }
}
