package com.fitlogpro.app.domain.calculator

import com.fitlogpro.app.domain.model.DistanceUnit
import com.fitlogpro.app.domain.model.WeightUnit
import kotlin.math.round

object UnitConverter {

    const val LBS_PER_KG = 2.2046226218
    const val KG_PER_LB = 0.45359237
    const val MILES_PER_KM = 0.6213711922
    const val KM_PER_MILE = 1.609344
    const val INCHES_PER_CM = 0.3937007874
    const val CM_PER_INCH = 2.54

    fun kgToLbs(kg: Double): Double {
        if (kg <= 0.0) return 0.0
        return roundToDecimals(kg * LBS_PER_KG, 1)
    }

    fun lbsToKg(lbs: Double): Double {
        if (lbs <= 0.0) return 0.0
        return roundToDecimals(lbs * KG_PER_LB, 1)
    }

    fun kmToMiles(km: Double): Double {
        if (km <= 0.0) return 0.0
        return roundToDecimals(km * MILES_PER_KM, 2)
    }

    fun milesToKm(miles: Double): Double {
        if (miles <= 0.0) return 0.0
        return roundToDecimals(miles * KM_PER_MILE, 2)
    }

    fun cmToInches(cm: Double): Double {
        if (cm <= 0.0) return 0.0
        return roundToDecimals(cm * INCHES_PER_CM, 1)
    }

    fun inchesToCm(inches: Double): Double {
        if (inches <= 0.0) return 0.0
        return roundToDecimals(inches * CM_PER_INCH, 1)
    }

    /**
     * Converts a base weight stored in kg to the target unit for display.
     */
    fun displayWeight(weightInKg: Double, targetUnit: WeightUnit): Double {
        return when (targetUnit) {
            WeightUnit.KG -> roundToDecimals(weightInKg, 1)
            WeightUnit.LBS -> kgToLbs(weightInKg)
        }
    }

    /**
     * Converts user-inputted weight to standard base storage (always in kg).
     */
    fun storageWeight(inputWeight: Double, inputUnit: WeightUnit): Double {
        return when (inputUnit) {
            WeightUnit.KG -> roundToDecimals(inputWeight, 1)
            WeightUnit.LBS -> lbsToKg(inputWeight)
        }
    }

    /**
     * Converts a base distance stored in km to the target unit for display.
     */
    fun displayDistance(distanceInKm: Double, targetUnit: DistanceUnit): Double {
        return when (targetUnit) {
            DistanceUnit.KM -> roundToDecimals(distanceInKm, 2)
            DistanceUnit.MILES -> kmToMiles(distanceInKm)
        }
    }

    /**
     * Converts user-inputted distance to standard base storage (always in km).
     */
    fun storageDistance(inputDistance: Double, inputUnit: DistanceUnit): Double {
        return when (inputUnit) {
            DistanceUnit.KM -> roundToDecimals(inputDistance, 2)
            DistanceUnit.MILES -> milesToKm(inputDistance)
        }
    }

    private fun roundToDecimals(value: Double, decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10.0 }
        return round(value * multiplier) / multiplier
    }
}
