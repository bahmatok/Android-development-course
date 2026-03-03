package com.example.converter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.converter.viewmodel.ConverterViewModel
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment

class NumpadFragment : Fragment() {
    private val viewModel: ConverterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    NumpadScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun NumpadScreen(viewModel: ConverterViewModel) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "+/-", "0", ".")

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.onBackspace()
                },
                modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "<=", style = MaterialTheme.typography.headlineMedium)
            }
            Button(
                onClick = {
                    viewModel.onClear()
                },
                modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "C", style = MaterialTheme.typography.headlineMedium)
            }
        }
        keys.chunked(3).forEach { row ->
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            if (key == "+/-") viewModel.onMinus()
                            else viewModel.onNumberClick(key)
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = key, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}