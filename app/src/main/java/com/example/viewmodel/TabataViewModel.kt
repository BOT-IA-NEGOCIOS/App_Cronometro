package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioCueManager
import com.example.data.RoutineRepository
import com.example.model.Routine
import com.example.model.TimerPhase
import com.example.model.TimerStep
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN,
    EDITOR,
    ACTIVE_TIMER
}

data class TimerUiState(
    val activeRoutine: Routine? = null,
    val steps: List<TimerStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val remainingSecondsInStep: Int = 0,
    val stepTotalSeconds: Int = 0,
    val elapsedTotalSeconds: Int = 0,
    val overallTotalSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val isMuted: Boolean = false
) {
    val currentStep: TimerStep?
        get() = steps.getOrNull(currentStepIndex)

    val nextStep: TimerStep?
        get() = steps.getOrNull(currentStepIndex + 1)

    val stepProgress: Float
        get() {
            if (stepTotalSeconds <= 0) return 0f
            val elapsed = stepTotalSeconds - remainingSecondsInStep
            return (elapsed.toFloat() / stepTotalSeconds).coerceIn(0f, 1f)
        }

    val overallProgress: Float
        get() {
            if (overallTotalSeconds <= 0) return 0f
            return (elapsedTotalSeconds.toFloat() / overallTotalSeconds).coerceIn(0f, 1f)
        }
}

data class EditorFormState(
    val id: String = "",
    val name: String = "",
    val prepareSeconds: Int = 10,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10,
    val rounds: Int = 8,
    val cycles: Int = 1,
    val cycleRestSeconds: Int = 60,
    val cooldownSeconds: Int = 0,
    val isNew: Boolean = true
) {
    fun toRoutine(): Routine = Routine(
        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
        name = name.ifBlank { "Mi Rutina" },
        prepareSeconds = prepareSeconds.coerceAtLeast(0),
        workSeconds = workSeconds.coerceAtLeast(1),
        restSeconds = restSeconds.coerceAtLeast(0),
        rounds = rounds.coerceAtLeast(1),
        cycles = cycles.coerceAtLeast(1),
        cycleRestSeconds = cycleRestSeconds.coerceAtLeast(0),
        cooldownSeconds = cooldownSeconds.coerceAtLeast(0)
    )

    fun calculateTotalSeconds(): Int = toRoutine().calculateTotalSeconds()

    fun formattedTotalTime(): String = toRoutine().formattedTotalTime()
}

class TabataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)
    val audioCueManager = AudioCueManager(application)

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines.asStateFlow()

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private val _editorForm = MutableStateFlow(EditorFormState())
    val editorForm: StateFlow<EditorFormState> = _editorForm.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadRoutines()
    }

    fun loadRoutines() {
        val list = repository.getRoutines()
        _routines.value = list
    }

    fun openMainScreen() {
        pauseTimer()
        _currentScreen.value = AppScreen.MAIN
    }

    fun openNewTimerEditor() {
        _editorForm.value = EditorFormState(
            id = java.util.UUID.randomUUID().toString(),
            name = "RUTINA NUEVA",
            prepareSeconds = 10,
            workSeconds = 20,
            restSeconds = 10,
            rounds = 8,
            cycles = 1,
            cycleRestSeconds = 60,
            cooldownSeconds = 0,
            isNew = true
        )
        _currentScreen.value = AppScreen.EDITOR
    }

    fun openEditRoutine(routine: Routine) {
        _editorForm.value = EditorFormState(
            id = routine.id,
            name = routine.name,
            prepareSeconds = routine.prepareSeconds,
            workSeconds = routine.workSeconds,
            restSeconds = routine.restSeconds,
            rounds = routine.rounds,
            cycles = routine.cycles,
            cycleRestSeconds = routine.cycleRestSeconds,
            cooldownSeconds = routine.cooldownSeconds,
            isNew = false
        )
        _currentScreen.value = AppScreen.EDITOR
    }

    fun updateEditorForm(update: EditorFormState.() -> EditorFormState) {
        _editorForm.update(update)
    }

    fun saveEditorRoutine() {
        val routine = _editorForm.value.toRoutine()
        val updated = repository.saveRoutine(routine)
        _routines.value = updated
        _feedbackMessage.value = "Rutina '${routine.name}' guardada"
        _currentScreen.value = AppScreen.MAIN
    }

    fun deleteRoutine(routineId: String) {
        val updated = repository.deleteRoutine(routineId)
        _routines.value = updated
        _feedbackMessage.value = "Rutina eliminada"
    }

    fun resetToDefaults() {
        val defaults = repository.resetToDefaults()
        _routines.value = defaults
        _feedbackMessage.value = "Rutinas restauradas por defecto"
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    fun triggerQuickSaveFeedback() {
        _feedbackMessage.value = "Todas las rutinas están guardadas"
    }

    /**
     * Start the active timer for the selected routine
     */
    fun startRoutineTimer(routine: Routine) {
        val steps = routine.generateSteps()
        if (steps.isEmpty()) return

        val totalDuration = routine.calculateTotalSeconds()
        val firstStep = steps[0]

        _timerState.value = TimerUiState(
            activeRoutine = routine,
            steps = steps,
            currentStepIndex = 0,
            remainingSecondsInStep = firstStep.durationSeconds,
            stepTotalSeconds = firstStep.durationSeconds,
            elapsedTotalSeconds = 0,
            overallTotalSeconds = totalDuration,
            isRunning = true,
            isCompleted = false,
            isMuted = audioCueManager.isMuted
        )

        _currentScreen.value = AppScreen.ACTIVE_TIMER

        // Initial phase change beep
        audioCueManager.playPhaseChangeBeep()

        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val state = _timerState.value
                if (!state.isRunning || state.isCompleted) continue

                val currentRemaining = state.remainingSecondsInStep
                if (currentRemaining > 1) {
                    val nextRemaining = currentRemaining - 1
                    val nextElapsed = state.elapsedTotalSeconds + 1

                    // Countdown beeps at 3, 2, 1 seconds
                    if (nextRemaining in 1..3) {
                        audioCueManager.playCountdownBeep()
                    }

                    _timerState.value = state.copy(
                        remainingSecondsInStep = nextRemaining,
                        elapsedTotalSeconds = nextElapsed
                    )
                } else {
                    // Step finished! Advance to next step
                    advanceToNextStep()
                }
            }
        }
    }

    private fun advanceToNextStep() {
        val state = _timerState.value
        val nextIndex = state.currentStepIndex + 1

        if (nextIndex < state.steps.size) {
            val nextStep = state.steps[nextIndex]
            _timerState.value = state.copy(
                currentStepIndex = nextIndex,
                remainingSecondsInStep = nextStep.durationSeconds,
                stepTotalSeconds = nextStep.durationSeconds,
                elapsedTotalSeconds = state.elapsedTotalSeconds + 1
            )
            // Play vibrant sound cue on phase change
            audioCueManager.playPhaseChangeBeep()
        } else {
            // Workout complete!
            _timerState.value = state.copy(
                remainingSecondsInStep = 0,
                elapsedTotalSeconds = state.overallTotalSeconds,
                isRunning = false,
                isCompleted = true
            )
            audioCueManager.playWorkoutCompleteCue()
            timerJob?.cancel()
        }
    }

    fun togglePlayPause() {
        val state = _timerState.value
        if (state.isCompleted) {
            state.activeRoutine?.let { startRoutineTimer(it) }
            return
        }

        val newRunning = !state.isRunning
        _timerState.value = state.copy(isRunning = newRunning)
        if (newRunning && timerJob?.isActive != true) {
            startTimerLoop()
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (!_timerState.value.isCompleted) {
            _timerState.value = _timerState.value.copy(isRunning = true)
            startTimerLoop()
        }
    }

    fun restartCurrentStep() {
        val state = _timerState.value
        val current = state.currentStep ?: return
        _timerState.value = state.copy(
            remainingSecondsInStep = current.durationSeconds,
            stepTotalSeconds = current.durationSeconds
        )
        audioCueManager.playPhaseChangeBeep()
    }

    fun restartWholeRoutine() {
        val routine = _timerState.value.activeRoutine ?: return
        startRoutineTimer(routine)
    }

    fun skipToNextStep() {
        advanceToNextStep()
    }

    fun skipToPreviousStep() {
        val state = _timerState.value
        if (state.currentStepIndex > 0) {
            val prevIndex = state.currentStepIndex - 1
            val prevStep = state.steps[prevIndex]
            _timerState.value = state.copy(
                currentStepIndex = prevIndex,
                remainingSecondsInStep = prevStep.durationSeconds,
                stepTotalSeconds = prevStep.durationSeconds,
                isCompleted = false
            )
            audioCueManager.playPhaseChangeBeep()
        } else {
            restartCurrentStep()
        }
    }

    fun toggleMute() {
        val newMuted = !audioCueManager.isMuted
        audioCueManager.isMuted = newMuted
        _timerState.value = _timerState.value.copy(isMuted = newMuted)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioCueManager.release()
    }
}
