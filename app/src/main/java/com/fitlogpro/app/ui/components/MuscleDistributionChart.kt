package com.fitlogpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.domain.model.MuscleGroupDistribution

val MuscleGroupColors = listOf(
    Color(0xFF2563EB), // Primary Royal Blue (Chest)
    Color(0xFF10B981), // Vibrant Emerald (Back)
    Color(0xFFF59E0B), // Trophy Gold (Legs)
    Color(0xFF8B5CF6), // Violet (Shoulders)
    Color(0xFFEC4899), // Pink (Arms)
    Color(0xFF06B6D4), // Cyan (Core)
    Color(0xFF64748B)  // Slate (Other)
)

@Composable
fun MuscleDistributionChart(
    distributions: List<MuscleGroupDistribution>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, outlineColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (distributions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Muscle Balance",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "0 sets logged",
                            color = textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Multi-segment preview bar in soft tints
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    listOf(0.28f, 0.24f, 0.20f, 0.16f, 0.12f).forEachIndexed { idx, frac ->
                        Box(
                            modifier = Modifier
                                .weight(frac)
                                .height(12.dp)
                                .background(MuscleGroupColors[idx % MuscleGroupColors.size].copy(alpha = 0.35f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Circular Muscle Category Pills
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    listOf("Chest", "Back", "Legs", "Arms", "Core").forEachIndexed { idx, name ->
                        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MuscleGroupColors[idx % MuscleGroupColors.size])
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = name,
                                color = textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            Column {
                Text(
                    text = "Muscle Group Distribution",
                    color = textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Multi-segment horizontal bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    distributions.forEachIndexed { index, dist ->
                        val color = MuscleGroupColors[index % MuscleGroupColors.size]
                        val weightFraction = (dist.percentage / 100f).coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(weightFraction)
                                .height(14.dp)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed Legend Grid
                Column {
                    distributions.forEachIndexed { index, dist ->
                        val color = MuscleGroupColors[index % MuscleGroupColors.size]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dist.muscleGroup.displayName,
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${dist.totalSets} sets (${String.format("%.1f", dist.percentage)}%)",
                                color = textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
