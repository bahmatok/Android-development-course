package com.example.converter.data

import androidx.annotation.StringRes
import com.example.converter.R

enum class UnitCategory(@StringRes val labelRes: Int) {
    WEIGHT(R.string.category_weight),
    LENGTH(R.string.category_length),
    TEMPERATURE(R.string.category_temp),
    SPEED(R.string.category_speed),
    VOLUME(R.string.category_volume),
    AREA(R.string.category_area);

    fun getUnits(): List<ConversionUnit> {
        return when (this) {
            LENGTH -> listOf(
                ConversionUnit.Length.Meter,
                ConversionUnit.Length.Foot,
                ConversionUnit.Length.Inch
            )
            WEIGHT -> listOf(
                ConversionUnit.Weight.Kilogram,
                ConversionUnit.Weight.Pound,
                ConversionUnit.Weight.Ounce
            )
            TEMPERATURE -> listOf(
                ConversionUnit.Temperature.Celsius,
                ConversionUnit.Temperature.Kelvin,
                ConversionUnit.Temperature.Fahrenheit
            )
            SPEED -> listOf(
                ConversionUnit.Speed.MetersPerSecond,
                ConversionUnit.Speed.KilometersPerHour,
                ConversionUnit.Speed.MilesPerHour,
                ConversionUnit.Speed.Knots
            )
            VOLUME -> listOf(
                ConversionUnit.Volume.Liter,
                ConversionUnit.Volume.Milliliter,
                ConversionUnit.Volume.CubicMeter,
                ConversionUnit.Volume.GallonUS,
                ConversionUnit.Volume.GallonImperial
            )
            AREA -> listOf(
                ConversionUnit.Area.SquareMeter,
                ConversionUnit.Area.SquareKilometer,
                ConversionUnit.Area.Hectare,
                ConversionUnit.Area.Acre,
                ConversionUnit.Area.SquareFoot
            )
        }
    }
}

sealed class ConversionUnit(
    @StringRes val nameRes: Int,
    val ratioToBase: Double
) {
    sealed class Length(@StringRes res: Int, ratio: Double) : ConversionUnit(res, ratio) {
        object Meter : Length(R.string.unit_meter, 1.0)
        object Foot : Length(R.string.unit_foot, 0.3048)
        object Inch : Length(R.string.unit_inch, 0.0254)
    }

    sealed class Weight(@StringRes res: Int, ratio: Double) : ConversionUnit(res, ratio) {
        object Kilogram : Weight(R.string.unit_kg, 1.0)
        object Pound : Weight(R.string.unit_lb, 0.453592)
        object Ounce : Weight(R.string.unit_oz, 0.0283495)
    }

    sealed class Temperature(@StringRes res: Int) : ConversionUnit(res, 1.0) {
        object Celsius : Temperature(R.string.unit_celsius)
        object Kelvin : Temperature(R.string.unit_kelvin)
        object Fahrenheit : Temperature(R.string.unit_fahrenheit)
    }
    sealed class Speed(@StringRes res: Int, ratio: Double) : ConversionUnit(res, ratio) {
        object MetersPerSecond : Speed(R.string.unit_mps, 1.0)
        object KilometersPerHour : Speed(R.string.unit_kmh, 0.2777778)
        object MilesPerHour : Speed(R.string.unit_mph, 0.44704)
        object Knots : Speed(R.string.unit_knot, 0.514444)
    }
    sealed class Volume(@StringRes res: Int, ratio: Double) : ConversionUnit(res, ratio) {
        object Liter : Volume(R.string.unit_liter, 1.0)
        object Milliliter : Volume(R.string.unit_ml, 0.001)
        object CubicMeter : Volume(R.string.unit_cubic_meter, 1000.0)
        object GallonUS : Volume(R.string.unit_gallon_us, 3.78541)
        object GallonImperial : Volume(R.string.unit_gallon_uk, 4.54609)
    }
    sealed class Area(@StringRes res: Int, ratio: Double) : ConversionUnit(res, ratio) {
        object SquareMeter : Area(R.string.unit_sq_meter, 1.0)
        object SquareKilometer : Area(R.string.unit_sq_km, 1_000_000.0)
        object Hectare : Area(R.string.unit_hectare, 10_000.0)
        object Acre : Area(R.string.unit_acre, 4046.856)
        object SquareFoot : Area(R.string.unit_sq_foot, 0.092903)
    }
}