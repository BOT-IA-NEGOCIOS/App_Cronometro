package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import java.util.UUID

enum class TimerPhase(
    val title: String,
    val color: Color,
    val darkColor: Color
) {
    PREPARE(
        title = "PREPÁRATE",
        color = Color(0xFFF59E0B), // Amarillo / Amber
        darkColor = Color(0xFF451A03)
    ),
    WORK(
        title = "ENTRENAR",
        color = Color(0xFF22C55E), // Verde
        darkColor = Color(0xFF052E16)
    ),
    REST(
        title = "DESCANSAR",
        color = Color(0xFFEF4444), // Rojo
        darkColor = Color(0xFF450A0A)
    ),
    CYCLE_REST(
        title = "DESCANSO DE CICLO",
        color = Color(0xFFF59E0B), // Amarillo para descanso de ciclos
        darkColor = Color(0xFF451A03)
    ),
    COOLDOWN(
        title = "ENFRIAMIENTO",
        color = Color(0xFF3B82F6), // Azul
        darkColor = Color(0xFF172554)
    ),
    COMPLETED(
        title = "¡COMPLETADO!",
        color = Color(0xFF8B5CF6),
        darkColor = Color(0xFF2E1065)
    )
}

data class TimerStep(
    val phase: TimerPhase,
    val durationSeconds: Int,
    val currentRound: Int,
    val totalRounds: Int,
    val currentCycle: Int,
    val totalCycles: Int,
    val nextPhaseName: String = "",
    val nextPhaseDuration: Int = 0
)

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val prepareSeconds: Int = 10,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10,
    val rounds: Int = 8,
    val cycles: Int = 1,
    val cycleRestSeconds: Int = 60,
    val cooldownSeconds: Int = 0
) {
    fun calculateTotalSeconds(): Int {
        var total = 0
        if (prepareSeconds > 0) total += prepareSeconds
        for (c in 1..cycles) {
            for (r in 1..rounds) {
                total += workSeconds
                if (r < rounds && restSeconds > 0) {
                    total += restSeconds
                }
            }
            if (c < cycles && cycleRestSeconds > 0) {
                total += cycleRestSeconds
            }
        }
        if (cooldownSeconds > 0) total += cooldownSeconds
        return total
    }

    fun formattedTotalTime(): String {
        val totalSec = calculateTotalSeconds()
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun generateSteps(): List<TimerStep> {
        val rawSteps = mutableListOf<TimerStep>()

        // 1. Prepare phase
        if (prepareSeconds > 0) {
            rawSteps.add(
                TimerStep(
                    phase = TimerPhase.PREPARE,
                    durationSeconds = prepareSeconds,
                    currentRound = 0,
                    totalRounds = rounds,
                    currentCycle = 1,
                    totalCycles = cycles
                )
            )
        }

        // 2. Cycles & Rounds
        for (c in 1..cycles) {
            for (r in 1..rounds) {
                // Work
                if (workSeconds > 0) {
                    rawSteps.add(
                        TimerStep(
                            phase = TimerPhase.WORK,
                            durationSeconds = workSeconds,
                            currentRound = r,
                            totalRounds = rounds,
                            currentCycle = c,
                            totalCycles = cycles
                        )
                    )
                }

                // Rest between rounds (only if not last round in cycle)
                if (r < rounds && restSeconds > 0) {
                    rawSteps.add(
                        TimerStep(
                            phase = TimerPhase.REST,
                            durationSeconds = restSeconds,
                            currentRound = r,
                            totalRounds = rounds,
                            currentCycle = c,
                            totalCycles = cycles
                        )
                    )
                }
            }

            // Cycle rest between cycles (only if not last cycle)
            if (c < cycles && cycleRestSeconds > 0) {
                rawSteps.add(
                    TimerStep(
                        phase = TimerPhase.CYCLE_REST,
                        durationSeconds = cycleRestSeconds,
                        currentRound = rounds,
                        totalRounds = rounds,
                        currentCycle = c,
                        totalCycles = cycles
                    )
                )
            }
        }

        // 3. Cooldown phase
        if (cooldownSeconds > 0) {
            rawSteps.add(
                TimerStep(
                    phase = TimerPhase.COOLDOWN,
                    durationSeconds = cooldownSeconds,
                    currentRound = rounds,
                    totalRounds = rounds,
                    currentCycle = cycles,
                    totalCycles = cycles
                )
            )
        }

        // Now link next phase information for each step
        return rawSteps.mapIndexed { index, step ->
            val nextStep = rawSteps.getOrNull(index + 1)
            step.copy(
                nextPhaseName = nextStep?.phase?.title ?: "FIN",
                nextPhaseDuration = nextStep?.durationSeconds ?: 0
            )
        }
    }

    companion object {
        fun getDefaultRoutines(): List<Routine> = listOf(
            Routine(
                id = "default_barras_2",
                name = "BARRAS 2.0",
                prepareSeconds = 10,
                workSeconds = 30,
                restSeconds = 15,
                rounds = 8,
                cycles = 3,
                cycleRestSeconds = 60,
                cooldownSeconds = 30
            ),
            Routine(
                id = "default_barras",
                name = "BARRAS",
                prepareSeconds = 10,
                workSeconds = 20,
                restSeconds = 10,
                rounds = 8,
                cycles = 1,
                cycleRestSeconds = 0,
                cooldownSeconds = 0
            ),
            Routine(
                id = "default_abdomen",
                name = "ABDOMEN",
                prepareSeconds = 10,
                workSeconds = 25,
                restSeconds = 10,
                rounds = 6,
                cycles = 2,
                cycleRestSeconds = 45,
                cooldownSeconds = 20
            ),
            Routine(
                id = "default_abdomen_3",
                name = "ABDOMEN 3",
                prepareSeconds = 10,
                workSeconds = 20,
                restSeconds = 10,
                rounds = 8,
                cycles = 3,
                cycleRestSeconds = 60,
                cooldownSeconds = 30
            )
        )
    }
}
