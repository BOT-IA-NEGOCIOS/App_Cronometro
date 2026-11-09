package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ActiveTimerScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.TabataViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TabataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    TabataApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TabataApp(viewModel: TabataViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val editorForm by viewModel.editorForm.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    Crossfade(
        targetState = currentScreen,
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.MAIN -> {
                MainScreen(
                    routines = routines,
                    feedbackMessage = feedbackMessage,
                    onClearFeedback = { viewModel.clearFeedbackMessage() },
                    onAddNewTimerClick = { viewModel.openNewTimerEditor() },
                    onStartRoutineClick = { routine -> viewModel.startRoutineTimer(routine) },
                    onEditRoutineClick = { routine -> viewModel.openEditRoutine(routine) },
                    onDeleteRoutineClick = { id -> viewModel.deleteRoutine(id) },
                    onResetDefaultsClick = { viewModel.resetToDefaults() },
                    onQuickSaveClick = { viewModel.triggerQuickSaveFeedback() }
                )
            }
            AppScreen.EDITOR -> {
                EditorScreen(
                    formState = editorForm,
                    onFormChange = { update -> viewModel.updateEditorForm(update) },
                    onSaveClick = { viewModel.saveEditorRoutine() },
                    onCancelClick = { viewModel.openMainScreen() }
                )
            }
            AppScreen.ACTIVE_TIMER -> {
                ActiveTimerScreen(
                    timerState = timerState,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onRestartClick = { viewModel.restartWholeRoutine() },
                    onSkipNextClick = { viewModel.skipToNextStep() },
                    onSkipPreviousClick = { viewModel.skipToPreviousStep() },
                    onBackToListClick = { viewModel.openMainScreen() },
                    onToggleMuteClick = { viewModel.toggleMute() }
                )
            }
        }
    }
}
