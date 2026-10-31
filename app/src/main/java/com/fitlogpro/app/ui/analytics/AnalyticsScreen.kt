package com.fitlogpro.app.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.ui.components.CustomBarChart
import com.fitlogpro.app.ui.components.CustomLineChart
import com.fitlogpro.app.ui.components.MuscleDistributionChart
import com.fitlogpro.app.ui.components.StatCard
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBackground
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    initialExerciseId: Long? = null
) {
    val streakInfo by viewModel.streakInfo.collectAsState()
    val weeklyVolumeBars by viewModel.weeklyVolumeBars.collectAsState()
    val muscleDistribution by viewModel.muscleDistribution.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val selectedExId by viewModel.selectedExerciseId.collectAsState()
    val selectedMetric by viewModel.selectedMetric.collectAsState()

    var showExerciseMenu by remember { mutableStateOf(false) }

    LaunchedEffect(initialExerciseId, allExercises) {
        if (selectedExId == null) {
            val target = initialExerciseId ?: allExercises.firstOrNull()?.id
            if (target != null) {
                viewModel.selectExercise(target)
            }
        }
    }

    val selectedExercise = allExercises.find { it.id == selectedExId }
    val chartPoints = viewModel.getChartDataPoints()

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Analytics & Performance",
                    color = DarkTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Streak & Volume Highlights
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "Active Streak",
                        value = "${streakInfo.currentStreakWeeks} wks",
                        subtitle = "Best: ${streakInfo.bestStreakWeeks} wks",
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = AccentOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "This Week",
                        value = "${streakInfo.workoutsThisWeek}",
                        subtitle = "${streakInfo.totalWorkoutsAllTime} all-time",
                        icon = Icons.Default.FitnessCenter,
                        iconTint = AccentTeal,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Volume",
                        value = "${String.format("%,.0f", streakInfo.totalVolumeKgAllTime)} kg",
                        icon = Icons.Default.ShowChart,
                        iconTint = Color(0xFFFFB300),
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }

            // Weekly Volume Bar Chart
            item {
                Column {
                    Text(
                        text = "Weekly Volume History",
                        color = DarkTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomBarChart(items = weeklyVolumeBars)
                }
            }

            // Muscle Group Distribution
            item {
                MuscleDistributionChart(distributions = muscleDistribution)
            }

            // Exercise Progression Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exercise Progression",
                            color = DarkTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Exercise Dropdown Trigger
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF222731))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { showExerciseMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = selectedExercise?.name ?: "Select Exercise",
                                    color = AccentOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = showExerciseMenu,
                                onDismissRequest = { showExerciseMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                allExercises.forEach { ex ->
                                    DropdownMenuItem(
                                        text = { Text(ex.name, color = DarkTextPrimary, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.selectExercise(ex.id)
                                            showExerciseMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metric Selection Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF222731))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChartMetric.entries.forEach { metric ->
                            val isSel = selectedMetric == metric
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) DarkSurface else Color.Transparent)
                                    .clickable { viewModel.selectMetric(metric) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = metric.title,
                                    color = if (isSel) AccentOrange else DarkTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Canvas Line Chart with Empty State protection
                    CustomLineChart(
                        points = chartPoints,
                        unitSuffix = selectedMetric.unitSuffix
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
