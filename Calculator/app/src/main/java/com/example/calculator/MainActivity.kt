package com.example.calculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), InputActionListener {
    companion object {
        private const val STATE_EXPRESSION = "state_expression"
        private const val STATE_RESULT = "state_result"
        private const val STATE_ENGINEERING_VISIBLE = "state_engineering_visible"
    }

    private lateinit var binding: ActivityMainBinding
    private val expression = StringBuilder()
    private var engineeringVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemInsets()

        if (supportFragmentManager.findFragmentById(binding.basicFragmentContainer.id) == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.basicFragmentContainer.id, BasicPadFragment())
                .commit()
        }
        if (BuildConfig.HAS_ENGINEERING_FEATURES &&
            supportFragmentManager.findFragmentById(binding.engineeringFragmentContainer.id) == null
        ) {
            supportFragmentManager.beginTransaction()
                .replace(binding.engineeringFragmentContainer.id, EngineeringPadFragment())
                .commit()
        }

        binding.modeToggleButton.setOnClickListener {
            engineeringVisible = !engineeringVisible
            applyMode()
        }

        restoreState(savedInstanceState)
        applyMode()
        updateTexts()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_EXPRESSION, expression.toString())
        outState.putString(STATE_RESULT, binding.resultTextView.text?.toString().orEmpty())
        outState.putBoolean(STATE_ENGINEERING_VISIBLE, engineeringVisible)
    }

    override fun onInputAction(action: String) {
        when (action) {
            "CLEAR" -> expression.clear()
            "BACKSPACE" -> if (expression.isNotEmpty()) expression.deleteCharAt(expression.length - 1)
            "EQUALS" -> evaluate()
            "SQRT" -> appendToken("√")
            "POW2" -> appendPower2()
            "CONST_PI" -> appendConstant("pi")
            "CONST_E" -> appendConstant("e")
            "FUNC_SIN" -> appendFunction("sin")
            "FUNC_COS" -> appendFunction("cos")
            "FUNC_TAN" -> appendFunction("tan")
            "FUNC_LN" -> appendFunction("ln")
            "FUNC_LOG" -> appendFunction("log")
            "FUNC_EXP" -> appendFunction("exp")
            else -> appendToken(action)
        }
        updateTexts()
    }

    private fun appendToken(token: String) {
        if (!isTokenAllowed(token)) return
        val last = expression.lastOrNull()
        if ((token == "(" || token == "√") && needsImplicitMultiplication()) {
            expression.append('*')
        }
        if (token in setOf("+", "-", "*", "/", "^") && last != null && isBinaryOperator(last)) {
            expression.deleteCharAt(expression.length - 1)
        }
        if (token == "." && (last == null || !last.isDigit())) {
            expression.append("0")
        }
        expression.append(token)
    }

    private fun evaluate() {
        val completedExpression = autoCloseBrackets(expression.toString())
        if (completedExpression.isBlank()) return
        try {
            val result = ExpressionEvaluator.evaluate(completedExpression)
            expression.clear()
            expression.append(ExpressionEvaluator.format(result))
            binding.resultTextView.text = ""
        } catch (_: IllegalArgumentException) {
            binding.resultTextView.text = getString(R.string.error)
        }
    }

    private fun appendFunction(name: String) {
        val last = expression.lastOrNull()
        if (last != null && (last.isDigit() || last == ')' || last == '!' || last == '%' || last == 'e' || last == 'i')) {
            expression.append('*')
        } else if (last != null && !isBinaryOperator(last) && last != '(') {
            return
        }
        expression.append(name).append('(')
    }

    private fun appendConstant(name: String) {
        val last = expression.lastOrNull()
        if (last != null && (last.isDigit() || last == ')' || last == '!' || last == '%' || last == 'e' || last == 'i')) {
            expression.append('*')
        } else if (last != null && !isBinaryOperator(last) && last != '(') {
            return
        }
        expression.append(name)
    }

    private fun appendPower2() {
        val last = expression.lastOrNull() ?: return
        if (last.isDigit() || last == ')' || last == '!' || last == '%' || last == 'e' || last == 'i') {
            expression.append("^2")
        }
    }

    private fun autoCloseBrackets(value: String): String {
        val openCount = value.count { it == '(' }
        val closeCount = value.count { it == ')' }
        return if (openCount > closeCount) {
            value + ")".repeat(openCount - closeCount)
        } else {
            value
        }
    }

    private fun isTokenAllowed(token: String): Boolean {
        if (token in setOf("SQRT", "^", "!") && !BuildConfig.HAS_ENGINEERING_FEATURES) return false
        val current = expression.toString()
        val last = current.lastOrNull()

        if (token.singleOrNull()?.isDigit() == true) {
            if (last == ')') expression.append('*')
            return true
        }
        return when (token) {
            "." -> canAppendDot(current)
            "+", "*", "/", "^" -> last != null && (
                last.isDigit() ||
                    last == ')' ||
                    last == '!' ||
                    last == '%' ||
                    last == 'e' ||
                    last == 'i' ||
                    isBinaryOperator(last)
                )
            "-" -> last == null ||
                last == '(' ||
                isBinaryOperator(last) ||
                last.isDigit() ||
                last == ')' ||
                last == '!' ||
                last == '%' ||
                last == 'e' ||
                last == 'i'
            "(" -> last == null || isBinaryOperator(last) || last == '(' || last == '√'
            ")" -> canCloseBracket(current, last)
            "!" -> last != null && (last.isDigit() || last == ')')
            "%" -> last != null && (last.isDigit() || last == ')')
            "√" -> last == null || isBinaryOperator(last) || last == '('
            else -> false
        }
    }

    private fun canAppendDot(current: String): Boolean {
        val last = current.lastOrNull() ?: return true
        if (!last.isDigit() && last != ')' && !isBinaryOperator(last)) return false
        if (last == ')') return false
        var i = current.length - 1
        while (i >= 0 && (current[i].isDigit() || current[i] == '.')) {
            if (current[i] == '.') return false
            i--
        }
        return true
    }

    private fun canCloseBracket(current: String, last: Char?): Boolean {
        if (last == null) return false
        val open = current.count { it == '(' }
        val close = current.count { it == ')' }
        if (open <= close) return false
        return last.isDigit() || last == ')' || last == '!' || last == '%'
    }

    private fun needsImplicitMultiplication(): Boolean {
        val last = expression.lastOrNull() ?: return false
        return last.isDigit() || last == ')' || last == '!' || last == '%'
    }

    private fun isBinaryOperator(value: Char): Boolean = value in setOf('+', '-', '*', '/', '^')

    private fun applyMode() {
        if (!BuildConfig.HAS_ENGINEERING_FEATURES) {
            binding.modeToggleButton.isEnabled = false
            binding.modeToggleButton.text = getString(R.string.mode_demo)
            binding.engineeringFragmentContainer.visibility = android.view.View.GONE
            return
        }

        val forceEngineering = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val showEngineering = forceEngineering || engineeringVisible
        binding.engineeringFragmentContainer.visibility =
            if (showEngineering) android.view.View.VISIBLE else android.view.View.GONE

        if (forceEngineering) {
            binding.modeToggleButton.isEnabled = false
            binding.modeToggleButton.text = getString(R.string.mode_engineering_locked)
        } else {
            binding.modeToggleButton.isEnabled = true
            binding.modeToggleButton.text =
                if (showEngineering) getString(R.string.mode_basic) else getString(R.string.mode_engineering)
        }
    }

    private fun updateTexts() {
        binding.expressionTextView.text = expression.toString()
        if (binding.resultTextView.text == getString(R.string.error)) return
        if (expression.isBlank()) {
            binding.resultTextView.text = "0"
            return
        }
        val preview = try {
            ExpressionEvaluator.format(ExpressionEvaluator.evaluate(autoCloseBrackets(expression.toString())))
        } catch (_: IllegalArgumentException) {
            ""
        }
        binding.resultTextView.text = preview
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        expression.clear()
        expression.append(savedInstanceState.getString(STATE_EXPRESSION).orEmpty())
        engineeringVisible = savedInstanceState.getBoolean(STATE_ENGINEERING_VISIBLE, false)
        val savedResult = savedInstanceState.getString(STATE_RESULT).orEmpty()
        if (savedResult.isNotBlank()) {
            binding.resultTextView.text = savedResult
        }
    }

    private fun applySystemInsets() {
        val initialBottom = binding.root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                initialBottom + bars.bottom
            )
            insets
        }
    }
}
