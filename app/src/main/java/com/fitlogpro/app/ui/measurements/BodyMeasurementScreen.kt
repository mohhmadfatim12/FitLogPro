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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fitlogpro.app.domain.calculator.BmiCalculator
import com.fitlogpro.app.domain.calculator.BmiCategory
import com.fitlogpro.app.domain.calculator.UnitConverter
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.ui.components.CustomLineChart
import com.fitlogpro.app.ui.components.StatCard
import com.fitlogpro.app.ui.theme.ElectricLime
import com.fitlogpro.app.ui.theme.ElectricLimeText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BodyScreenTab {
    MEASUREMENTS,
    BMI_CALCULATOR
}

@Composable
fun BodyMeasurementScreen(
    viewModel: BodyMeasurementViewModel
) {
    var currentTab by remember { mutableStateOf(BodyScreenTab.MEASUREMENTS) }

    val weightUnit by viewModel.weightUnit.collectAsState()
    val isMetric = weightUnit == WeightUnit.KG

    val userHeightCm by viewModel.userHeightCm.collectAsState()
    val latestWeightEntity by viewModel.latestWeightEntity.collectAsState()
    val latestWeightKg = latestWeightEntity?.value

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
            if (currentTab == BodyScreenTab.MEASUREMENTS) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ElectricLime,
                    contentColor = ElectricLimeText
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Measurement")
                }
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
                text = "Body Tracking",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Segmented Top Switch: Measurements vs BMI Calculator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isMeas = currentTab == BodyScreenTab.MEASUREMENTS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isMeas) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { currentTab = BodyScreenTab.MEASUREMENTS }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Measurements",
                        fontSize = 13.sp,
                        fontWeight = if (isMeas) FontWeight.Bold else FontWeight.Medium,
                        color = if (isMeas) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isBmi = currentTab == BodyScreenTab.BMI_CALCULATOR
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isBmi) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { currentTab = BodyScreenTab.BMI_CALCULATOR }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = if (isBmi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BMI Calculator",
                            fontSize = 13.sp,
                            fontWeight = if (isBmi) FontWeight.Bold else FontWeight.Medium,
                            color = if (isBmi) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Screen Content
            if (currentTab == BodyScreenTab.BMI_CALCULATOR) {
                BmiCalculatorView(
                    userHeightCm = userHeightCm,
                    latestWeightKg = latestWeightKg,
                    initialUnitIsMetric = isMetric,
                    onHeightChanged = { viewModel.updateHeight(it) },
                    onSaveWeight = { kg, notes ->
                        viewModel.saveMeasurement(MeasurementType.WEIGHT, kg, notes)
                    }
                )
            } else {
                // Measurements Tab
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
                                color = if (isSel) ElectricLimeText else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                val sortedMeasurements = remember(measurements) {
                    measurements.sortedByDescending { it.recordedEpochMs }
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
                            subtitle = latest?.let { "Recorded on ${MEASUREMENT_DAY_FORMAT.format(Date(it.recordedEpochMs))}" }
                        )
                    }

                    // Quick BMI Glance Card (when viewing Body Weight)
                    if (selectedType == MeasurementType.WEIGHT && latestWeightKg != null && latestWeightKg > 0.0) {
                        item {
                            val currentBmi = BmiCalculator.calculateBmi(latestWeightKg, userHeightCm.toDouble())
                            val cat = BmiCategory.fromBmi(currentBmi)
                            val catColor = Color(cat.colorHex)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .clickable { currentTab = BodyScreenTab.BMI_CALCULATOR }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(catColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Calculate,
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Current BMI: ${String.format("%.1f", currentBmi)} • ${cat.label}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Based on latest weight & ${String.format("%.0f", userHeightCm)} cm",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Calculate →",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
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
                    items(sortedMeasurements, key = { it.id }) { item ->
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

private val MEASUREMENT_DATE_FORMAT = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
private val MEASUREMENT_DAY_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.US)

@Composable
private fun MeasurementRow(
    item: BodyMeasurementEntity,
    type: MeasurementType,
    isMetric: Boolean,
    onDelete: () -> Unit
) {
    val dateText = remember(item.recordedEpochMs) {
        synchronized(MEASUREMENT_DATE_FORMAT) {
            MEASUREMENT_DATE_FORMAT.format(Date(item.recordedEpochMs))
        }
    }
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
                    text = dateText,
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
