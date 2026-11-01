package com.fitlogpro.app.ui.workouts

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlogpro.app.data.local.entity.ExerciseEntity
import com.fitlogpro.app.data.local.entity.RoutineExerciseCrossRef
import com.fitlogpro.app.data.repository.ExerciseRepository
import com.fitlogpro.app.ui.components.GymNumberField

data class EditableRoutineItem(
    val exerciseId: Long,
    val exerciseName: String,
    var targetSets: Int = 3,
    var targetReps: Int = 10,
    var targetWeightKg: Double = 0.0,
    var supersetGroupId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    routineId: Long,
    viewModel: RoutinesViewModel,
    exerciseRepository: ExerciseRepository,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val editableItems = remember { mutableStateListOf<EditableRoutineItem>() }
    var showExercisePicker by remember { mutableStateOf(false) }

    val allExercises by exerciseRepository.getAllExercises().collectAsState(initial = emptyList())
    val existingItems by viewModel.getRoutineItems(routineId).collectAsState()
    val routines by viewModel.routines.collectAsState()

    LaunchedEffect(routineId, existingItems) {
        if (routineId != 0L && editableItems.isEmpty()) {
            val r = routines.find { it.id == routineId }
            if (r != null) {
                name = r.name
                description = r.description
            }
            if (existingItems.isNotEmpty()) {
                editableItems.clear()
                existingItems.sortedBy { it.crossRef.orderIndex }.forEach { item ->
                    editableItems.add(
                        EditableRoutineItem(
                            exerciseId = item.exercise.id,
                            exerciseName = item.exercise.name,
                            targetSets = item.crossRef.targetSets,
                            targetReps = item.crossRef.targetReps,
                            targetWeightKg = item.crossRef.targetWeightKg ?: 0.0,
                            supersetGroupId = item.crossRef.supersetGroupId
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (routineId == 0L) "New Routine" else "Edit Routine", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val crossRefs = editableItems.mapIndexed { idx, item ->
                                    RoutineExerciseCrossRef(
                                        id = 0L,
                                        routineId = routineId,
                                        exerciseId = item.exerciseId,
                                        orderIndex = idx,
                                        targetSets = item.targetSets,
                                        targetReps = item.targetReps,
                                        targetWeightKg = if (item.targetWeightKg > 0) item.targetWeightKg else null,
                                        supersetGroupId = item.supersetGroupId
                                    )
                                }
                                viewModel.saveRoutine(routineId, name.trim(), description.trim(), crossRefs, onSaved)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercises (${editableItems.size})",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showExercisePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Exercise", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            itemsIndexed(editableItems) { index, item ->
                EditableExerciseCard(
                    item = item,
                    index = index,
                    total = editableItems.size,
                    onMoveUp = {
                        if (index > 0) {
                            val temp = editableItems[index]
                            editableItems[index] = editableItems[index - 1]
                            editableItems[index - 1] = temp
                        }
                    },
                    onMoveDown = {
                        if (index < editableItems.size - 1) {
                            val temp = editableItems[index]
                            editableItems[index] = editableItems[index + 1]
                            editableItems[index + 1] = temp
                        }
                    },
                    onToggleSuperset = {
                        item.supersetGroupId = if (item.supersetGroupId == null) "SS_${System.currentTimeMillis() % 1000}" else null
                    },
                    onRemove = { editableItems.removeAt(index) }
                )
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerDialog(
            exercises = allExercises,
            onSelect = { selected ->
                editableItems.add(
                    EditableRoutineItem(
                        exerciseId = selected.id,
                        exerciseName = selected.name,
                        targetSets = 3,
                        targetReps = 10,
                        targetWeightKg = 0.0
                    )
                )
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }
}

@Composable
private fun EditableExerciseCard(
    item: EditableRoutineItem,
    index: Int,
    total: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleSuperset: () -> Unit,
    onRemove: () -> Unit
) {
    val isSuperset = item.supersetGroupId != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isSuperset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}. ${item.exerciseName}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) MaterialTheme.colorScheme.onSurface else Color.LightGray)
                    }
                    IconButton(onClick = onMoveDown, enabled = index < total - 1, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (index < total - 1) MaterialTheme.colorScheme.onSurface else Color.LightGray)
                    }
                    IconButton(onClick = onToggleSuperset, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = "Toggle Superset", tint = if (isSuperset) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sets", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    GymNumberField(
                        value = if (item.targetSets > 0) item.targetSets.toString() else "",
                        onValueChange = { item.targetSets = it.toIntOrNull() ?: 1 },
                        placeholder = "3"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Reps", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    GymNumberField(
                        value = if (item.targetReps > 0) item.targetReps.toString() else "",
                        onValueChange = { item.targetReps = it.toIntOrNull() ?: 1 },
                        placeholder = "10"
                    )
                }

                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Weight (kg)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    GymNumberField(
                        value = if (item.targetWeightKg > 0) item.targetWeightKg.toString() else "",
                        onValueChange = { item.targetWeightKg = it.toDoubleOrNull() ?: 0.0 },
                        placeholder = "0"
                    )
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<ExerciseEntity>,
    onSelect: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Exercise", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercise...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                                Text(ex.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${ex.muscleGroup.displayName} • ${ex.equipment.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
