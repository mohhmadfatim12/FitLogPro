package com.fitlogpro.app.ui.activeworkout

import android.app.Activity
import android.view.WindowManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.domain.model.ExerciseType
import com.fitlogpro.app.domain.model.LiveWorkoutExercise
import com.fitlogpro.app.domain.model.LiveWorkoutSet
import com.fitlogpro.app.domain.model.SetType
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.ui.components.GymNumberField
import com.fitlogpro.app.ui.components.PRBadge
import com.fitlogpro.app.ui.components.RestTimerDialog
import com.fitlogpro.app.ui.components.RestTimerFloatingBar
import com.fitlogpro.app.ui.components.SetTypeBadge
import com.fitlogpro.app.ui.theme.AccentGreen
import com.fitlogpro.app.ui.theme.AccentGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    exerciseRepository: ExerciseRepository,
    onBack: () -> Unit,
    onWorkoutFinished: (Long) -> Unit
) {
    val session by viewModel.sessionState.collectAsState()
    val restTimerSnapshot by viewModel.restTimerState.collectAsState()
    val showRestModal by viewModel.showRestTimerModal.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val allExercises by exerciseRepository.getAllExercises().collectAsState(initial = emptyList())

    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddExercisePicker by remember { mutableStateOf(false) }
    var activeSetTypeChooser by remember { mutableStateOf<Pair<String, String>?>(null) } // exerciseId to setId

    // Keep screen on during workout
    DisposableEffect(keepScreenOn) {
        val window = (context as? Activity)?.window
        if (keepScreenOn && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (session == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No active workout",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }
        return
    }

    val activeSession = session!!
    val elapsedMinutes = activeSession.elapsedSeconds / 60
    val elapsedSeconds = activeSession.elapsedSeconds % 60
    val timerText = String.format("%02d:%02d", elapsedMinutes, elapsedSeconds)

    val surfaceColor = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeSession.workoutName,
                            color = textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (activeSession.isPaused) AccentGold else AccentGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (activeSession.isPaused) "Paused • $timerText" else timerText,
                                color = if (activeSession.isPaused) AccentGold else AccentGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Minimize",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    // Pause/Resume button
                    IconButton(onClick = { viewModel.toggleWorkoutPause() }) {
                        Icon(
                            imageVector = if (activeSession.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause Workout",
                            tint = textPrimary
                        )
                    }

                    // Finish Workout button
                    Button(
                        onClick = { viewModel.finishWorkout(onWorkoutFinished) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Discard Menu
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = textPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(surfaceColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Discard Workout", color = Color(0xFFEF4444)) },
                            onClick = {
                                showMenu = false
                                showDiscardDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                RestTimerFloatingBar(
                    snapshot = restTimerSnapshot,
                    onPause = { viewModel.pauseRestTimer() },
                    onResume = { viewModel.resumeRestTimer() },
                    onAddTime = { viewModel.addRestTimerSeconds(it) },
                    onSkip = { viewModel.skipRestTimer() },
                    onOpenModal = { viewModel.setRestTimerModalVisible(true) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activeSession.exercises, key = { it.id }) { exercise ->
                ActiveExerciseCard(
                    exercise = exercise,
                    weightUnit = weightUnit,
                    onAddSet = { viewModel.addSetToExercise(exercise.id) },
                    onRemoveSet = { setId -> viewModel.removeSet(exercise.id, setId) },
                    onRemoveExercise = { viewModel.removeExercise(exercise.id) },
                    onUpdateSet = { setId, w, r, rpe, dur, dist ->
                        viewModel.updateSet(exercise.id, setId, w, r, rpe, null, dur, dist)
                    },
                    onToggleComplete = { setId -> viewModel.toggleSetCompletion(exercise.id, setId) },
                    onOpenSetTypeChooser = { setId -> activeSetTypeChooser = Pair(exercise.id, setId) }
                )
            }

            item {
                Button(
                    onClick = { showAddExercisePicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = surfaceColor,
                        contentColor = primaryColor
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Exercise",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Rest Timer Modal Dialog
    if (showRestModal) {
        RestTimerDialog(
            snapshot = restTimerSnapshot,
            onPause = { viewModel.pauseRestTimer() },
            onResume = { viewModel.resumeRestTimer() },
            onAddTime = { viewModel.addRestTimerSeconds(it) },
            onSubtractTime = { viewModel.subtractRestTimerSeconds(it) },
            onSkip = {
                viewModel.skipRestTimer()
                viewModel.setRestTimerModalVisible(false)
            },
            onDismiss = { viewModel.setRestTimerModalVisible(false) }
        )
    }

    // Set Type Chooser Dialog
    if (activeSetTypeChooser != null) {
        val (exId, setId) = activeSetTypeChooser!!
        SetTypeSelectionDialog(
            onSelect = { selectedType ->
                viewModel.updateSet(exId, setId, setType = selectedType)
                activeSetTypeChooser = null
            },
            onDismiss = { activeSetTypeChooser = null }
        )
    }

    // Add Exercise Picker Dialog
    if (showAddExercisePicker) {
        ActiveExercisePickerDialog(
            exercises = allExercises,
            onSelect = { selected ->
                viewModel.addExerciseToWorkout(selected)
                showAddExercisePicker = false
            },
            onDismiss = { showAddExercisePicker = false }
        )
    }

    // Discard Confirmation Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Workout?", color = textPrimary) },
            text = { Text("Are you sure you want to discard this workout? All logged sets will be deleted.", color = textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.discardWorkout(onBack)
                    }
                ) {
                    Text("Discard", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Workout", color = textPrimary)
                }
            },
            containerColor = surfaceColor
        )
    }
}

@Composable
private fun ActiveExerciseCard(
    exercise: LiveWorkoutExercise,
    weightUnit: WeightUnit,
    onAddSet: () -> Unit,
    onRemoveSet: (String) -> Unit,
    onRemoveExercise: () -> Unit,
    onUpdateSet: (String, Double?, Int?, Double?, Int?, Double?) -> Unit,
    onToggleComplete: (String) -> Unit,
    onOpenSetTypeChooser: (String) -> Unit
) {
    val isSuperset = exercise.supersetGroupId != null
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = if (isSuperset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            // Exercise Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = exercise.exerciseName,
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSuperset) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SUPERSET",
                                    color = primaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "${exercise.muscleGroup.displayName} • ${exercise.equipment.displayName}",
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(onClick = onRemoveExercise, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Exercise",
                        tint = textSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Column Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SET", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text("PREVIOUS", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                Text(weightUnit.symbol.uppercase(), color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                Text("REPS", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                Text("✓", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            }

            // Sets List
            exercise.sets.forEach { set ->
                ActiveSetRow(
                    set = set,
                    exerciseType = exercise.exerciseType,
                    onUpdateWeight = { onUpdateSet(set.id, it, null, null, null, null) },
                    onUpdateReps = { onUpdateSet(set.id, null, it, null, null, null) },
                    onToggleComplete = { onToggleComplete(set.id) },
                    onOpenTypeChooser = { onOpenSetTypeChooser(set.id) },
                    onDeleteSet = { onRemoveSet(set.id) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Add Set Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onAddSet)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("+ Add Set", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActiveSetRow(
    set: LiveWorkoutSet,
    exerciseType: ExerciseType,
    onUpdateWeight: (Double) -> Unit,
    onUpdateReps: (Int) -> Unit,
    onToggleComplete: () -> Unit,
    onOpenTypeChooser: () -> Unit,
    onDeleteSet: () -> Unit
) {
    val rowBackground = if (set.isCompleted) Color(0xFFECFDF5) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowBackground)
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set Badge
        SetTypeBadge(
            setNumber = set.setNumber,
            setType = set.setType,
            onClick = onOpenTypeChooser,
            modifier = Modifier.width(36.dp)
        )

        // Previous Performance
        Text(
            text = set.previousPerformance ?: "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.3f)
        )

        // Weight Input
        GymNumberField(
            value = if (set.weightKg > 0) set.weightKg.toString() else "",
            onValueChange = { onUpdateWeight(it.toDoubleOrNull() ?: 0.0) },
            placeholder = "0",
            modifier = Modifier
                .weight(1.2f)
                .padding(horizontal = 4.dp)
        )

        // Reps Input
        GymNumberField(
            value = if (set.reps > 0) set.reps.toString() else "",
            onValueChange = { onUpdateReps(it.toIntOrNull() ?: 0) },
            placeholder = "0",
            modifier = Modifier
                .weight(1.1f)
                .padding(horizontal = 4.dp)
        )

        // Completion Checkmark / PR Badge
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (set.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (set.isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onToggleComplete),
            contentAlignment = Alignment.Center
        ) {
            if (set.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        }
    }

    if (set.isPr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 38.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PRBadge(prTypes = set.prTypes)
        }
    }
}

@Composable
private fun SetTypeSelectionDialog(
    onSelect: (SetType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Set Type", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SetType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(type) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SetTypeBadge(setNumber = 1, setType = type)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(type.displayName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val note = if (type == SetType.WARMUP) "Excluded from volume and PRs" else "Counts towards volume and PRs"
                            Text(note, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun ActiveExercisePickerDialog(
    exercises: List<ExerciseEntity>,
    onSelect: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = exercises.filter { it.name.contains(query, ignoreCase = true) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Exercise", color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search exercise...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { ex ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(ex) }
                                .padding(vertical = 10.dp)
                        ) {
                            Column {
                                Text(ex.name, color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${ex.muscleGroup.displayName} • ${ex.equipment.displayName}", color = textSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
