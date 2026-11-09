package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EditorTopBar
import com.example.ui.components.NumberStepperCard
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
import com.example.viewmodel.EditorFormState

@Composable
fun EditorScreen(
    formState: EditorFormState,
    onFormChange: (EditorFormState.() -> EditorFormState) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    BackHandler(onBack = onCancelClick)

    val totalTimeFormatted = formState.formattedTotalTime()
    val totalSeconds = formState.calculateTotalSeconds()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Cancelar, Title, and Guardar
            EditorTopBar(
                title = if (formState.isNew) "Nuevo Temporizador" else "Editar Rutina",
                onCancelClick = onCancelClick,
                onSaveClick = onSaveClick
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Routine Name Input
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Nombre de la rutina",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = formState.name,
                                onValueChange = { newName ->
                                    onFormChange { copy(name = newName) }
                                },
                                placeholder = {
                                    Text("Ej. BARRAS 2.0, ABDOMEN...", color = TextMuted)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("routine_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = AccentGreen,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurfaceHighlight,
                                    unfocusedContainerColor = DarkSurfaceHighlight
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // 2. Prepárate (segundos) - Yellow
                item {
                    NumberStepperCard(
                        title = "Prepárate",
                        value = formState.prepareSeconds,
                        unitLabel = "seg",
                        minValue = 0,
                        maxValue = 120,
                        step = 5,
                        accentColor = AccentYellow,
                        quickChips = listOf(5, 10, 15),
                        isSeconds = true,
                        testTagPrefix = "prepare",
                        onValueChange = { newVal ->
                            onFormChange { copy(prepareSeconds = newVal) }
                        }
                    )
                }

                // 3. Entrenar (segundos) - Green
                item {
                    NumberStepperCard(
                        title = "Entrenar",
                        value = formState.workSeconds,
                        unitLabel = "seg",
                        minValue = 5,
                        maxValue = 300,
                        step = 5,
                        accentColor = AccentGreen,
                        quickChips = listOf(20, 30, 45),
                        isSeconds = true,
                        testTagPrefix = "work",
                        onValueChange = { newVal ->
                            onFormChange { copy(workSeconds = newVal) }
                        }
                    )
                }

                // 4. Descansar (segundos) - Red
                item {
                    NumberStepperCard(
                        title = "Descansar",
                        value = formState.restSeconds,
                        unitLabel = "seg",
                        minValue = 0,
                        maxValue = 300,
                        step = 5,
                        accentColor = AccentRed,
                        quickChips = listOf(10, 15, 30),
                        isSeconds = true,
                        testTagPrefix = "rest",
                        onValueChange = { newVal ->
                            onFormChange { copy(restSeconds = newVal) }
                        }
                    )
                }

                // 5. Rondas (count) - Yellow/Orange
                item {
                    NumberStepperCard(
                        title = "Rondas",
                        value = formState.rounds,
                        unitLabel = "rondas",
                        minValue = 1,
                        maxValue = 50,
                        step = 1,
                        accentColor = AccentYellow,
                        quickChips = listOf(4, 8, 10),
                        isSeconds = false,
                        testTagPrefix = "rounds",
                        onValueChange = { newVal ->
                            onFormChange { copy(rounds = newVal) }
                        }
                    )
                }

                // 6. Ciclos (count) - Purple
                item {
                    NumberStepperCard(
                        title = "Ciclos",
                        value = formState.cycles,
                        unitLabel = "ciclos",
                        minValue = 1,
                        maxValue = 20,
                        step = 1,
                        accentColor = AccentPurple,
                        quickChips = listOf(1, 2, 3),
                        isSeconds = false,
                        testTagPrefix = "cycles",
                        onValueChange = { newVal ->
                            onFormChange { copy(cycles = newVal) }
                        }
                    )
                }

                // 7. Descanso entre Ciclos (segundos) - Yellow
                item {
                    NumberStepperCard(
                        title = "Descanso entre Ciclos",
                        value = formState.cycleRestSeconds,
                        unitLabel = "seg",
                        minValue = 0,
                        maxValue = 300,
                        step = 10,
                        accentColor = AccentYellow,
                        quickChips = listOf(30, 60, 90),
                        isSeconds = true,
                        testTagPrefix = "cycle_rest",
                        onValueChange = { newVal ->
                            onFormChange { copy(cycleRestSeconds = newVal) }
                        }
                    )
                }

                // 8. Enfriamiento (segundos) - Blue
                item {
                    NumberStepperCard(
                        title = "Enfriamiento",
                        value = formState.cooldownSeconds,
                        unitLabel = "seg",
                        minValue = 0,
                        maxValue = 300,
                        step = 10,
                        accentColor = AccentBlue,
                        quickChips = listOf(15, 30, 60),
                        isSeconds = true,
                        testTagPrefix = "cooldown",
                        onValueChange = { newVal ->
                            onFormChange { copy(cooldownSeconds = newVal) }
                        }
                    )
                }

                // Live Summary Calculation Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Resumen del Entrenamiento",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tiempo Total Estimado",
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = totalTimeFormatted,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val totalIntervals = formState.rounds * formState.cycles
                            Text(
                                text = "$totalIntervals intervalos de trabajo (${formState.workSeconds * totalIntervals}s de esfuerzo activo)",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Big Save Routine Button
                item {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("save_routine_main_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GUARDAR RUTINA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
