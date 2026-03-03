package com.example.converter.data

class ConverterService {
    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        if (from::class.java.superclass != to::class.java.superclass) return 0.0

        return when {
            from is ConversionUnit.Temperature && to is ConversionUnit.Temperature -> {
                convertTemperature(value, from, to)
            }
            else -> {
                val valueInBase = value * from.ratioToBase
                valueInBase / to.ratioToBase
            }
        }
    }

    private fun convertTemperature(value: Double, from: ConversionUnit.Temperature, to: ConversionUnit.Temperature): Double {
        val celsius = when (from) {
            ConversionUnit.Temperature.Celsius -> value
            ConversionUnit.Temperature.Kelvin -> value - 273.15
            ConversionUnit.Temperature.Fahrenheit -> (value - 32) * 5 / 9
        }
        return when (to) {
            ConversionUnit.Temperature.Celsius -> celsius
            ConversionUnit.Temperature.Kelvin -> celsius + 273.15
            ConversionUnit.Temperature.Fahrenheit -> (celsius * 9 / 5) + 32
        }
    }
}