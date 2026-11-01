package com.fitlogpro.app.ui.measurements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.domain.calculator.UnitConverter
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.ui.components.CustomLineChart
import com.fitlogpro.app.ui.components.StatCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BodyMeasurementScreen(
    viewModel: BodyMeasurementViewModel
) {
    val weightUnit by viewModel.weightUnit.collectAsState()
    val isMetric = weightUnit == WeightUnit.KG

    val selectedType by viewModel.selectedType.collectAsState()
    val measurements by viewModel.measurementsForType.collectAsState()
    val trendPoints = viewModel.getTrendDataPoints(isMetric)

    var showAddDialog by remember { mutableStateOf(false) }

    val latest = measurements.maxByOrNull { it.recordedEpochMs }
    val latestDisplay = latest?.let { m ->
        val v = if (isMetric) m.value else {
            when (selectedType) {
                MeasurementType.WEIGHT -> UnitConverter.kgToLbs(m.value)
                MeasurementType.BODY_FAT -> m.value
                else -> UnitConverter.cmToInches(m.value)
            }
        }
        "${String.format("%.1f", v)} ${selectedType.getUnit(isMetric)}"
    } ?: "—"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Measurement")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Text(
                text = "Body Measurements",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Measurement Type Horizontal Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(MeasurementType.entries) { type ->
                    val isSel = selectedType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectMeasurementType(type) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.displayName,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Latest Stat Card
                item {
                    StatCard(
                        title = "Latest ${selectedType.displayName}",
                        value = latestDisplay,
                        icon = Icons.Default.MonitorWeight,
                        iconTint = MaterialTheme.colorScheme.primary,
                        subtitle = latest?.let { "Recorded on ${SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(it.recordedEpochMs))}" }
                    )
                }

                // Trend Curve
                item {
                    Column {
                        Text(
                            text = "${selectedType.displayName} Progress Trend",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomLineChart(
                            points = trendPoints,
                            unitSuffix = selectedType.getUnit(isMetric)
                        )
                    }
                }

                // Logs Header
                item {
                    Text(
                        text = "History Logs (${measurements.size})",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Measurement Rows
                items(measurements.sortedByDescending { it.recordedEpochMs }, key = { it.id }) { item ->
                    MeasurementRow(
                        item = item,
                        type = selectedType,
                        isMetric = isMetric,
                        onDelete = { viewModel.deleteMeasurement(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeasurementDialog(
            type = selectedType,
            isMetric = isMetric,
            onSave = { value, notes ->
                viewModel.saveMeasurement(selectedType, value, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun MeasurementRow(
    item: BodyMeasurementEntity,
    type: MeasurementType,
    isMetric: Boolean,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
    val displayVal = if (isMetric) item.value else {
        when (type) {
            MeasurementType.WEIGHT -> UnitConverter.kgToLbs(item.value)
            MeasurementType.BODY_FAT -> item.value
            else -> UnitConverter.cmToInches(item.value)
        }
    }
    val unitLabel = type.getUnit(isMetric)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${String.format("%.1f", displayVal)} $unitLabel",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(item.recordedEpochMs)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = item.notes, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
