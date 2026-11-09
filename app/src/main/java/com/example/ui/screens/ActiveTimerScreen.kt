package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimerPhase
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TimerUiState

@Composable
fun ActiveTimerScreen(
    timerState: TimerUiState,
    onPlayPauseClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSkipNextClick: () -> Unit,
    onSkipPreviousClick: () -> Unit,
    onBackToListClick: () -> Unit,
    onToggleMuteClick: () -> Unit
) {
    // Keep screen turned on while timer is active
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (timerState.isRunning) {
            showExitDialog = true
        } else {
            onBackToListClick()
        }
    }

    val currentStep = timerState.currentStep
    val currentPhase = if (timerState.isCompleted) TimerPhase.COMPLETED else (currentStep?.phase ?: TimerPhase.PREPARE)

    // Dynamic phase colors:
    // Amarillo: PREPARE, CYCLE_REST
    // Verde: WORK
    // Rojo: REST
    // Azul: COOLDOWN
    val targetBgColor = when (currentPhase) {
        TimerPhase.PREPARE -> Color(0xFF451A03) // Warm deep amber
        TimerPhase.WORK -> Color(0xFF052E16)    // Deep forest green
        TimerPhase.REST -> Color(0xFF450A0A)    // Deep crimson red
        TimerPhase.CYCLE_REST -> Color(0xFF451A03) // Warm deep amber
        TimerPhase.COOLDOWN -> Color(0xFF172554)  // Deep cobalt blue
        TimerPhase.COMPLETED -> Color(0xFF2E1065) // Deep victory violet
    }

    val targetAccentColor = when (currentPhase) {
        TimerPhase.PREPARE -> Color(0xFFF59E0B) // Bright yellow/amber
        TimerPhase.WORK -> Color(0xFF22C55E)    // Bright green
        TimerPhase.REST -> Color(0xFFEF4444)    // Bright red
        TimerPhase.CYCLE_REST -> Color(0xFFF59E0B) // Bright yellow/amber
        TimerPhase.COOLDOWN -> Color(0xFF3B82F6)  // Bright blue
        TimerPhase.COMPLETED -> Color(0xFFA855F7) // Bright purple
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bgColorAnim"
    )

    val animatedAccentColor by animateColorAsState(
        targetValue = targetAccentColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "accentColorAnim"
    )

    // Pulse animation during countdown 3, 2, 1
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (timerState.remainingSecondsInStep in 1..3 && timerState.isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAnim"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    // Step circular progress
    val animatedProgress by animateFloatAsState(
        targetValue = timerState.stepProgress,
        animationSpec = tween(durationMillis = 300),
        label = "progressAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedBgColor,
                        DarkBackground,
                        DarkBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Routine Name, Mute Toggle, and Exit / Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button to return to routine list
                IconButton(
                    onClick = {
                        if (timerState.isRunning) {
                            showExitDialog = true
                        } else {
                            onBackToListClick()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated.copy(alpha = 0.8f))
                        .testTag("timer_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver a la lista",
                        tint = TextPrimary
                    )
                }

                // Routine Title Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerState.activeRoutine?.name ?: "TABATA TIMER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Total: ${formatTime(timerState.elapsedTotalSeconds)} / ${formatTime(timerState.overallTotalSeconds)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Mute / Sound Toggle
                IconButton(
                    onClick = onToggleMuteClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated.copy(alpha = 0.8f))
                        .testTag("timer_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (timerState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = if (timerState.isMuted) "Activar sonido" else "Silenciar",
                        tint = if (timerState.isMuted) AccentRed else TextPrimary
                    )
                }
            }

            // OVERALL WORKOUT PROGRESS BAR
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                LinearProgressIndicator(
                    progress = { timerState.overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = animatedAccentColor,
                    trackColor = DarkSurfaceHighlight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // COUNTERS: Rondas & Ciclos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Round Counter Card
                CounterCard(
                    title = "RONDA",
                    current = currentStep?.currentRound ?: 0,
                    total = currentStep?.totalRounds ?: (timerState.activeRoutine?.rounds ?: 8),
                    accentColor = animatedAccentColor
                )

                // Cycle Counter Card
                CounterCard(
                    title = "CICLO",
                    current = currentStep?.currentCycle ?: 1,
                    total = currentStep?.totalCycles ?: (timerState.activeRoutine?.cycles ?: 1),
                    accentColor = animatedAccentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CENTER: HUGE TIMER & PHASE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (timerState.isCompleted) {
                    // Celebration View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡ENTRENAMIENTO COMPLETADO!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGreen,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gran trabajo. Has finalizado toda la rutina.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    // Main Timer Display with circular gauge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(pulseScale)
                    ) {
                        // Current Phase Name in Big Bold Letters
                        Text(
                            text = currentPhase.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = animatedAccentColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("current_phase_title")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Circular Progress with GIANT Numbers inside
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(260.dp)
                        ) {
                            // Circular Canvas Progress Ring
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                val radius = (size.minDimension - strokeWidth) / 2
                                val center = Offset(size.width / 2, size.height / 2)

                                // Track background ring
                                drawCircle(
                                    color = Color(0x33FFFFFF),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )

                                // Progress sweep arc
                                val sweepAngle = (1f - animatedProgress) * 360f
                                drawArc(
                                    color = animatedAccentColor,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            // GIANT Numbers
                            AnimatedContent(
                                targetState = timerState.remainingSecondsInStep,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                                },
                                label = "giantNumberAnim"
                            ) { remaining ->
                                Text(
                                    text = if (remaining >= 100) formatSeconds(remaining) else "$remaining",
                                    fontSize = if (remaining >= 100) 68.sp else 96.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("giant_timer_text")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next Phase Preview Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("next_phase_pill")
                        ) {
                            Text(
                                text = if (currentStep?.nextPhaseName.isNullOrBlank()) {
                                    "SIGUIENTE: FINALIZAR"
                                } else {
                                    "SIGUIENTE: ${currentStep?.nextPhaseName} (${currentStep?.nextPhaseDuration}s)"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM: LARGE PLAYBACK CONTROLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart / Replay Button
                IconButton(
                    onClick = onRestartClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHighlight)
                        .testTag("timer_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reiniciar",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Previous Phase Button
                IconButton(
                    onClick = onSkipPreviousClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHighlight)
                        .testTag("timer_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Fase anterior",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // HUGE Play / Pause Central Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(animatedAccentColor)
                        .testTag("timer_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (timerState.isCompleted) {
                                Icons.Default.Refresh
                            } else if (timerState.isRunning) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (timerState.isRunning) "Pausar" else "Iniciar",
                            tint = Color.Black,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Next Phase Button
                IconButton(
                    onClick = onSkipNextClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHighlight)
                        .testTag("timer_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Fase siguiente",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Return to List Button
                IconButton(
                    onClick = {
                        if (timerState.isRunning) {
                            showExitDialog = true
                        } else {
                            onBackToListClick()
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHighlight)
                        .testTag("timer_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terminar y volver a la lista",
                        tint = AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Exit confirmation dialog when timer is currently active
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text(
                    text = "¿Salir del entrenamiento?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "El temporizador está en marcha. ¿Deseas pausar y volver a la lista de ejercicios?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onBackToListClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Salir a la lista", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continuar entrenando", color = AccentGreen)
                }
            }
        )
    }
}

@Composable
private fun CounterCard(
    title: String,
    current: Int,
    total: Int,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.85f))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$current",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = " / $total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
