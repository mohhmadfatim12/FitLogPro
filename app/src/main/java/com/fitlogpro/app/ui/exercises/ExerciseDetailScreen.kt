package com.fitlogpro.app.ui.exercises

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.fitlogpro.app.data.local.entity.PersonalRecordEntity
import com.fitlogpro.app.domain.calculator.OneRepMaxCalculator
import com.fitlogpro.app.domain.model.PRType
import com.fitlogpro.app.ui.components.GymNumberField
import com.fitlogpro.app.ui.components.StatCard
import com.fitlogpro.app.ui.theme.AccentGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    viewModel: ExerciseViewModel,
    onBack: () -> Unit,
    onViewCharts: (Long) -> Unit
) {
    val exercises by viewModel.filteredExercises.collectAsState()
    val exercise = exercises.find { it.id == exerciseId }
    val prs by viewModel.getPrsForExercise(exerciseId).collectAsState()

    var calcWeight by remember { mutableStateOf("100") }
    var calcReps by remember { mutableStateOf("5") }

    val calculated1Rm = remember(calcWeight, calcReps) {
        val w = calcWeight.toDoubleOrNull() ?: 0.0
        val r = calcReps.toIntOrNull() ?: 0
        OneRepMaxCalculator.calculateEpley(w, r)
    }

    val percentageTable = remember(calculated1Rm) {
        OneRepMaxCalculator.calculatePercentageTable(calculated1Rm)
    }

    val heaviestPr = prs.filter { it.prType == PRType.HEAVIEST_WEIGHT }.maxByOrNull { it.value }
    val best1RmPr = prs.filter { it.prType == PRType.BEST_1RM }.maxByOrNull { it.value }
    val bestVolPr = prs.filter { it.prType == PRType.BEST_SET_VOLUME }.maxByOrNull { it.value }
    val mostRepsPr = prs.filter { it.prType == PRType.MOST_REPS }.maxByOrNull { it.value }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise Details", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { onViewCharts(exerciseId) }) {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = "Charts", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Info Card
            if (exercise != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Muscle Group", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(exercise.muscleGroup.displayName, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Equipment", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(exercise.equipment.displayName, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Default Rest", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text("${exercise.defaultRestSeconds}s", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (exercise.instructions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Instructions", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                Text(exercise.instructions, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            // Personal Records Section
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Personal Records", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "Heaviest Weight",
                        value = heaviestPr?.let { "${it.value} kg" } ?: "—",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = AccentGold,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Best 1RM",
                        value = best1RmPr?.let { "${it.value} kg" } ?: "—",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = AccentGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "Best Set Volume",
                        value = bestVolPr?.let { "${it.value} kg" } ?: "—",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Most Reps",
                        value = mostRepsPr?.let { "${it.value.toInt()} reps" } ?: "—",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive 1RM Calculator Widget
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Estimated 1RM Calculator (Epley)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Weight (kg)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                GymNumberField(value = calcWeight, onValueChange = { calcWeight = it }, placeholder = "100")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reps Completed", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                GymNumberField(value = calcReps, onValueChange = { calcReps = it }, placeholder = "5")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated 1RM:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Text("$calculated1Rm kg", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Percentage breakdown table
                        Text("Repetition Percentage Brackets", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))

                        percentageTable.entries.chunked(2).forEach { rowEntries ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                rowEntries.forEach { (pct, weight) ->
                                    Text(
                                        text = "$pct%: $weight kg",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f).padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // View Progress Charts Button
            item {
                Button(
                    onClick = { onViewCharts(exerciseId) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Progress Charts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
