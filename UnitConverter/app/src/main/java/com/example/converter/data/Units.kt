package com.example.converter.data

import androidx.annotation.StringRes
import com.example.converter.R

enum class UnitCategory(@StringRes val labelRes: Int) {
    WEIGHT(R.string.category_weight),
    LENGTH(R.string.category_length),
    TEMPERATURE(R.string.category_temp);

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
}