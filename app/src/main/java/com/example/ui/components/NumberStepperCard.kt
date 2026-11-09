package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NumberStepperCard(
    title: String,
    value: Int,
    unitLabel: String = "s",
    minValue: Int = 0,
    maxValue: Int = 3600,
    step: Int = 5,
    accentColor: Color = Color.White,
    quickChips: List<Int> = listOf(5, 10, 30),
    isSeconds: Boolean = true,
    testTagPrefix: String,
    onValueChange: (Int) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small accent indicator dot
                    Spacer(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = if (isSeconds) formatSeconds(value) else "$value $unitLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stepper Row: [-] [ BIG NUMBER ] [+]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newValue = (value - step).coerceAtLeast(minValue)
                        onValueChange(newValue)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceHighlight)
                        .testTag("${testTagPrefix}_decrement"),
                    enabled = value > minValue
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir $title",
                        tint = if (value > minValue) TextPrimary else TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$value",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    if (unitLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unitLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val newValue = (value + step).coerceAtMost(maxValue)
                        onValueChange(newValue)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceHighlight)
                        .testTag("${testTagPrefix}_increment"),
                    enabled = value < maxValue
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar $title",
                        tint = if (value < maxValue) TextPrimary else TextMuted
                    )
                }
            }

            // Quick increment chips
            if (quickChips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickChips.forEach { chipStep ->
                        SuggestionChip(
                            onClick = {
                                val newValue = (value + chipStep).coerceAtMost(maxValue)
                                onValueChange(newValue)
                            },
                            label = {
                                Text(
                                    text = if (isSeconds) "+${chipStep}s" else "+$chipStep",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = DarkSurfaceHighlight
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = DarkBorder
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }

                    // Reset to 0 / min chip if current value is above min
                    if (value > minValue && isSeconds && minValue == 0) {
                        SuggestionChip(
                            onClick = { onValueChange(0) },
                            label = { Text("0s", fontSize = 12.sp, color = TextMuted) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = DarkSurfaceHighlight
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = DarkBorder
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
