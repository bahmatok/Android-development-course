package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.sin

object ExpressionEvaluator {
    private val mathContext = MathContext(34, RoundingMode.HALF_UP)

    fun evaluate(expression: String): BigDecimal {
        val parser = Parser(expression)
        val result = parser.parseExpression()
        if (!parser.isAtEnd()) {
            throw IllegalArgumentException("Unexpected token")
        }
        return result.stripTrailingZeros()
    }

    fun format(value: BigDecimal): String {
        val normalized = value.stripTrailingZeros()
        return if (normalized.scale() < 0) normalized.setScale(0).toPlainString() else normalized.toPlainString()
    }

    private class Parser(expression: String) {
        private val input = expression.replace(" ", "")
        private var index = 0

        fun isAtEnd(): Boolean = index >= input.length

        fun parseExpression(): BigDecimal {
            var value = parseTerm()
            while (!isAtEnd()) {
                when (peek()) {
                    '+' -> {
                        index++
                        value = value.add(parseTerm(), mathContext)
                    }
                    '-' -> {
                        index++
                        value = value.subtract(parseTerm(), mathContext)
                    }
                    else -> return value
                }
            }
            return value
        }

        private fun parseTerm(): BigDecimal {
            var value = parsePower()
            while (!isAtEnd()) {
                when (peek()) {
                    '*' -> {
                        index++
                        value = value.multiply(parsePower(), mathContext)
                    }
                    '/' -> {
                        index++
                        val right = parsePower()
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            throw IllegalArgumentException("Division by zero")
                        }
                        value = value.divide(right, mathContext)
                    }
                    else -> return value
                }
            }
            return value
        }

        private fun parsePower(): BigDecimal {
            val base = parseUnary()
            if (!isAtEnd() && peek() == '^') {
                index++
                val exponent = parsePower()
                return pow(base, exponent)
            }
            return base
        }

        private fun parseUnary(): BigDecimal {
            if (isAtEnd()) throw IllegalArgumentException("Unexpected end")
            return when (peek()) {
                '-' -> {
                    index++
                    parseUnary().negate(mathContext)
                }
                '√' -> {
                    index++
                    val operand = parseUnary()
                    if (operand < BigDecimal.ZERO) {
                        throw IllegalArgumentException("Sqrt from negative")
                    }
                    sqrtBigDecimal(operand)
                }
                else -> parsePostfix()
            }
        }

        private fun parsePostfix(): BigDecimal {
            var value = parsePrimary()
            while (!isAtEnd()) {
                when (peek()) {
                    '!' -> {
                        index++
                        value = factorial(value)
                    }
                    '%' -> {
                        index++
                        value = value.divide(BigDecimal("100"), mathContext)
                    }
                    else -> return value
                }
            }
            return value
        }

        private fun parsePrimary(): BigDecimal {
            if (peek().isLetter()) {
                return parseIdentifier()
            }
            if (peek() == '(') {
                index++
                val value = parseExpression()
                if (isAtEnd() || peek() != ')') {
                    throw IllegalArgumentException("Missing closing bracket")
                }
                index++
                return value
            }
            return parseNumber()
        }

        private fun parseIdentifier(): BigDecimal {
            val start = index
            while (!isAtEnd() && peek().isLetter()) {
                index++
            }
            val name = input.substring(start, index)
            return if (!isAtEnd() && peek() == '(') {
                index++
                val arg = parseExpression()
                if (isAtEnd() || peek() != ')') {
                    throw IllegalArgumentException("Missing closing bracket")
                }
                index++
                applyFunction(name, arg)
            } else {
                when (name) {
                    "pi" -> BigDecimal("3.141592653589793238462643383279503")
                    "e" -> BigDecimal("2.718281828459045235360287471352662")
                    else -> throw IllegalArgumentException("Unknown identifier")
                }
            }
        }

        private fun parseNumber(): BigDecimal {
            val start = index
            var hasDot = false
            while (!isAtEnd()) {
                val c = peek()
                if (c.isDigit()) {
                    index++
                    continue
                }
                if (c == '.' && !hasDot) {
                    hasDot = true
                    index++
                    continue
                }
                break
            }
            if (start == index || input.substring(start, index) == ".") {
                throw IllegalArgumentException("Invalid number")
            }
            return input.substring(start, index).toBigDecimal()
        }

        private fun peek(): Char = input[index]
    }

    private fun pow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        val normalizedExponent = exponent.stripTrailingZeros()
        if (normalizedExponent.scale() > 0) {
            throw IllegalArgumentException("Only integer powers are supported")
        }
        val exp = normalizedExponent.toInt()
        return if (exp >= 0) {
            base.pow(exp, mathContext)
        } else {
            BigDecimal.ONE.divide(base.pow(-exp, mathContext), mathContext)
        }
    }

    private fun factorial(value: BigDecimal): BigDecimal {
        val normalized = value.stripTrailingZeros()
        if (normalized.scale() > 0 || normalized < BigDecimal.ZERO) {
            throw IllegalArgumentException("Factorial requires non-negative integer")
        }
        val n = normalized.toInt()
        var result = BigDecimal.ONE
        for (i in 2..n) {
            result = result.multiply(i.toBigDecimal(), mathContext)
        }
        return result
    }

    private fun sqrtBigDecimal(value: BigDecimal): BigDecimal {
        val asDouble = value.toDouble()
        val root = sqrt(asDouble)
        return root.toBigDecimal(mathContext)
    }

    private fun applyFunction(name: String, arg: BigDecimal): BigDecimal {
        return when (name) {
            "sin" -> sin(arg.toDouble()).toBigDecimal(mathContext)
            "cos" -> cos(arg.toDouble()).toBigDecimal(mathContext)
            "tan" -> tan(arg.toDouble()).toBigDecimal(mathContext)
            "ln" -> {
                if (arg <= BigDecimal.ZERO) throw IllegalArgumentException("ln requires positive value")
                ln(arg.toDouble()).toBigDecimal(mathContext)
            }
            "log" -> {
                if (arg <= BigDecimal.ZERO) throw IllegalArgumentException("log requires positive value")
                log10(arg.toDouble()).toBigDecimal(mathContext)
            }
            "exp" -> exp(arg.toDouble()).toBigDecimal(mathContext)
            else -> throw IllegalArgumentException("Unknown function")
        }
    }
}
