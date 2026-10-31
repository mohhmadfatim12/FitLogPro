package com.fitlogpro.app.ui.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.local.entity.WorkoutExerciseWithSets
import com.fitlogpro.app.data.local.entity.WorkoutSetEntity
import com.fitlogpro.app.ui.components.PRBadge
import com.fitlogpro.app.ui.components.SetTypeBadge
import com.fitlogpro.app.ui.components.StatCard
import com.fitlogpro.app.ui.theme.AccentGold
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBackground
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary
import com.fitlogpro.app.ui.theme.DarkTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val workouts by viewModel.workoutsWithDetails.collectAsState()
    val workoutDetail = workouts.find { it.workout.id == workoutId }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (workoutDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Workout not found", color = DarkTextSecondary)
        }
        return
    }

    val workout = workoutDetail.workout
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.US)
    val formattedDate = dateFormat.format(Date(workout.startTimeEpochMs))
    val prCount = workoutDetail.exercises.flatMap { it.sets }.count { it.isPr }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(workout.name, color = DarkTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(workoutId) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Workout", tint = DarkTextPrimary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Workout", tint = Color(0xFFFF5252))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
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
            // Date & Summary Card
            item {
                Text(text = formattedDate, color = DarkTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = "Duration",
                        value = "${workout.durationSeconds / 60}m",
                        icon = Icons.Default.Timer,
                        iconTint = AccentTeal,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Volume",
                        value = "${String.format("%,.0f", workout.totalVolumeKg)} kg",
                        icon = Icons.Default.FitnessCenter,
                        iconTint = AccentOrange,
                        modifier = Modifier.weight(1.3f)
                    )
                    StatCard(
                        title = "PRs",
                        value = "$prCount",
                        icon = Icons.Default.EmojiEvents,
                        iconTint = AccentGold,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }

            // Notes Section
            if (workout.notes.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("Workout Notes", color = DarkTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(workout.notes, color = DarkTextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Exercises (${workoutDetail.exercises.size})",
                    color = DarkTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exercise Detail Cards
            items(workoutDetail.exercises, key = { it.workoutExercise.id }) { weWithSets ->
                DetailExerciseCard(item = weWithSets)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Workout?", color = DarkTextPrimary) },
            text = { Text("Are you sure you want to delete '${workout.name}'? This action cannot be undone.", color = DarkTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkout(workout)
                        showDeleteConfirm = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = DarkTextPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun DetailExerciseCard(item: WorkoutExerciseWithSets) {
    val exercise = item.exercise
    val sets = item.sets

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = exercise.name,
                color = DarkTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${exercise.muscleGroup.displayName} • ${exercise.equipment.displayName}",
                color = DarkTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sets Header
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Text("SET", color = DarkTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text("WEIGHT", color = DarkTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("REPS", color = DarkTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("DETAILS", color = DarkTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
            }

            sets.forEach { set ->
                DetailSetRow(set = set)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DetailSetRow(set: WorkoutSetEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1B1F27))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetTypeBadge(setNumber = set.setNumber, setType = set.setType, modifier = Modifier.width(36.dp))

        Text(
            text = if (set.weightKg > 0) "${set.weightKg} kg" else "—",
            color = DarkTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${set.reps} reps",
            color = DarkTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (set.rpe != null) {
                Text("RPE ${set.rpe}", color = DarkTextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (set.isPr) {
                PRBadge()
            }
        }
    }
}
