package com.example.converter.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.converter.data.ConversionUnit
import com.example.converter.data.ConverterService
import com.example.converter.data.UnitCategory

class ConverterViewModel : ViewModel() {
    private val converter = ConverterService()


    var selectedCategory by mutableStateOf(UnitCategory.LENGTH)
        private set

    var unitFrom by mutableStateOf<ConversionUnit>(ConversionUnit.Length.Meter)
    var unitTo by mutableStateOf<ConversionUnit>(ConversionUnit.Length.Foot)

    var inputAmount by mutableStateOf("0")
        private set

    val outputAmount: String
        get() {
            val amount = inputAmount.toDoubleOrNull() ?: 0.0
            val result = converter.convert(amount, unitFrom, unitTo)
            return "%.4f".format(result)
        }


    fun onCategorySelected(category: UnitCategory) {
        selectedCategory = category
        val units = category.getUnits()
        unitFrom = units[0]
        unitTo = units[1]
        inputAmount = "0"
    }

    fun onNumberClick(char: String) {
        val digitsCount = inputAmount.count { it.isDigit() }
        if (digitsCount >= 10 && char != ".") return

        inputAmount = when {
            char == "." && inputAmount.contains(".") -> inputAmount

            (inputAmount == "0" || inputAmount == "-0") && char != "." -> {
                if (inputAmount.startsWith("-")) "-$char" else char
            }

            else -> inputAmount + char
        }
    }

    fun onBackspace() {
        inputAmount = if (inputAmount.length > 1) inputAmount.dropLast(1) else "0"
    }

    fun onClear() {
        inputAmount = "0"
    }

    fun onMinus() {
        if (inputAmount[0] == '-') inputAmount = inputAmount.drop(1)
        else inputAmount = "-$inputAmount"
    }

    fun swapUnits() {
        val unitTemp = unitFrom
        val valTemp = outputAmount
        unitFrom = unitTo
        unitTo = unitTemp
        inputAmount = valTemp
    }
}