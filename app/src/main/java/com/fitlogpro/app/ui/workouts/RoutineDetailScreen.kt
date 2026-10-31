package com.fitlogpro.app.ui.workouts

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.local.entity.RoutineExerciseItem
import com.fitlogpro.app.ui.theme.AccentOrange
import com.fitlogpro.app.ui.theme.AccentTeal
import com.fitlogpro.app.ui.theme.DarkBackground
import com.fitlogpro.app.ui.theme.DarkBorder
import com.fitlogpro.app.ui.theme.DarkSurface
import com.fitlogpro.app.ui.theme.DarkTextPrimary
import com.fitlogpro.app.ui.theme.DarkTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: Long,
    viewModel: RoutinesViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onStartWorkout: (Long) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val routine = routines.find { it.id == routineId }
    val items by viewModel.getRoutineItems(routineId).collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(routine?.name ?: "Routine", color = DarkTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(routineId) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = DarkTextPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onStartWorkout(routineId) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Start Workout", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (routine != null && routine.description.isNotEmpty()) {
                item {
                    Text(
                        text = routine.description,
                        color = DarkTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            item {
                Text(
                    text = "${items.size} Exercises",
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(items, key = { it.crossRef.id }) { item ->
                RoutineExerciseRow(item = item)
            }
        }
    }

    if (showDeleteDialog && routine != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Routine?", color = DarkTextPrimary) },
            text = { Text("Are you sure you want to delete '${routine.name}'? Completed workout history will not be affected.", color = DarkTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRoutine(routine)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = DarkTextPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun RoutineExerciseRow(item: RoutineExerciseItem) {
    val crossRef = item.crossRef
    val exercise = item.exercise
    val isSuperset = !crossRef.supersetGroupId.isNullOrEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                if (isSuperset) AccentTeal.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    color = DarkTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isSuperset) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentTeal.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SUPERSET",
                            color = AccentTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val targetDetails = buildString {
                append("${crossRef.targetSets} sets × ${crossRef.targetReps} reps")
                if (crossRef.targetWeightKg != null && crossRef.targetWeightKg > 0) {
                    append(" @ ${crossRef.targetWeightKg} kg")
                }
                if (crossRef.targetRpe != null) {
                    append(" (RPE ${crossRef.targetRpe})")
                }
            }

            Text(
                text = targetDetails,
                color = AccentOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (crossRef.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = crossRef.notes,
                    color = DarkTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
