package com.example.converter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.converter.viewmodel.ConverterViewModel

@Composable
fun PremiumTools(viewModel: ConverterViewModel) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { viewModel.swapUnits() },
            modifier = Modifier.weight(1f)
        ) {
            Text("⇅ Swap")
        }

        OutlinedButton(
            onClick = { clipboardManager.setText(AnnotatedString(viewModel.inputAmount)) }
        ) {
            Text("Copy In")
        }

        OutlinedButton(
            onClick = { clipboardManager.setText(AnnotatedString(viewModel.outputAmount)) }
        ) {
            Text("Copy Out")
        }
    }
}