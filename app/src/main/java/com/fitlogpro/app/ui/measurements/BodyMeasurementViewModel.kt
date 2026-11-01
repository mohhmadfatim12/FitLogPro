package com.fitlogpro.app.ui.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitlogpro.app.data.local.entity.BodyMeasurementEntity
import com.fitlogpro.app.data.local.preferences.UserPreferencesManager
import com.fitlogpro.app.data.repository.BodyMeasurementRepository
import com.fitlogpro.app.domain.calculator.UnitConverter
import com.fitlogpro.app.domain.model.MeasurementType
import com.fitlogpro.app.domain.model.WeightUnit
import com.fitlogpro.app.ui.components.ChartDataPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class BodyMeasurementViewModel(
    private val measurementRepository: BodyMeasurementRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("MMM d", Locale.US)

    val weightUnit: StateFlow<WeightUnit> = userPreferencesManager.weightUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUnit.KG)

    val userHeightCm: StateFlow<Float> = userPreferencesManager.userHeightCmFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 175.0f)

    val latestWeightEntity: StateFlow<BodyMeasurementEntity?> = measurementRepository
        .getMeasurementsByType(MeasurementType.WEIGHT)
        .map { list -> list.maxByOrNull { it.recordedEpochMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedType = MutableStateFlow(MeasurementType.WEIGHT)
    val selectedType: StateFlow<MeasurementType> = _selectedType.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val measurementsForType: StateFlow<List<BodyMeasurementEntity>> = _selectedType
        .flatMapLatest { type -> measurementRepository.getMeasurementsByType(type) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMeasurementType(type: MeasurementType) {
        _selectedType.value = type
    }

    fun updateHeight(heightCm: Float) {
        viewModelScope.launch {
            userPreferencesManager.setUserHeightCm(heightCm)
        }
    }

    fun saveMeasurement(type: MeasurementType, rawValue: Double, notes: String) {
        viewModelScope.launch {
            // Convert to base metric storage
            val isMetric = weightUnit.value == WeightUnit.KG
            val baseValue = when {
                type == MeasurementType.WEIGHT && !isMetric -> UnitConverter.lbsToKg(rawValue)
                type != MeasurementType.WEIGHT && type != MeasurementType.BODY_FAT && !isMetric -> UnitConverter.inchesToCm(rawValue)
                else -> rawValue
            }
            measurementRepository.saveMeasurement(type, baseValue, notes)
        }
    }

    fun deleteMeasurement(entity: BodyMeasurementEntity) {
        viewModelScope.launch {
            measurementRepository.deleteMeasurement(entity)
        }
    }

    fun getTrendDataPoints(isMetric: Boolean): List<ChartDataPoint> {
        val list = measurementsForType.value.sortedBy { it.recordedEpochMs }
        val type = _selectedType.value

        return list.map { m ->
            val displayVal = if (isMetric) m.value else {
                when (type) {
                    MeasurementType.WEIGHT -> UnitConverter.kgToLbs(m.value)
                    MeasurementType.BODY_FAT -> m.value
                    else -> UnitConverter.cmToInches(m.value)
                }
            }
            ChartDataPoint(
                label = dateFormat.format(m.recordedEpochMs),
                value = displayVal
            )
        }
    }

    class Factory(
        private val measurementRepository: BodyMeasurementRepository,
        private val userPreferencesManager: UserPreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BodyMeasurementViewModel(measurementRepository, userPreferencesManager) as T
        }
    }
}
