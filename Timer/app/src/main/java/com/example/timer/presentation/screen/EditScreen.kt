package com.example.timer.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhase
import com.example.timer.presentation.viewmodel.EditViewModel
import com.example.timer.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val sequence by viewModel.sequenceState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (sequence == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentSeq = sequence!!
    val colorPresets = listOf(
        Color(0xFFBB86FC), Color(0xFF03DAC5), Color(0xFFCF6679),
        Color(0xFFFFB74D), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFF06292),
        Color.Red, Color.Magenta, Color.Blue,
        Color.Green, Color.Black, Color.White
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    if (currentSeq.id.isEmpty())
                        stringResource(R.string.new_timer)
                    else
                        stringResource(R.string.edit_timer))
                        },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = R.string.back.toString())
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveSequence(onSaved = onNavigateBack) }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = currentSeq.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text(stringResource(R.string.timer_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.color_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(colorPresets) { color ->
                    val isSelected = currentSeq.color == color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updateColor(color.toArgb()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.phases_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentSeq.phases, key = { it.id }) { phase ->
                    PhaseEditItem(
                        phase = phase,
                        onIncreaseTime = { viewModel.updatePhaseDuration(phase.id, 5) },
                        onDecreaseTime = { viewModel.updatePhaseDuration(phase.id, -5) },
                        onTimeChange = { newTimeStr -> viewModel.setPhaseDuration(phase.id, newTimeStr) },
                        onRemove = { viewModel.removePhase(phase.id) }
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhaseType.entries.forEach { type ->
                    Button(onClick = { viewModel.addPhase(type) }) {
                        Text(type.name)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseEditItem(
    phase: TimerPhase,
    onIncreaseTime: () -> Unit,
    onDecreaseTime: () -> Unit,
    onTimeChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val borderColor = MaterialTheme.colorScheme.outline
    val controlHeight = 32.dp

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = phase.type.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1.2f)
            )

            Row(
                modifier = Modifier.weight(2.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    onClick = onDecreaseTime,
                    modifier = Modifier.size(controlHeight),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("-5", style = MaterialTheme.typography.bodySmall, color = contentColor)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                BasicTextField(
                    value = if (phase.durationSeconds == 0) "" else phase.durationSeconds.toString(),
                    onValueChange = { if (it.all { char -> char.isDigit() }) onTimeChange(it) },
                    modifier = Modifier
                        .width(75.dp)
                        .height(controlHeight),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        color = contentColor,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = phase.durationSeconds.toString(),
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember { MutableInteractionSource() },
                            contentPadding = PaddingValues(0.dp),
                            container = {
                                OutlinedTextFieldDefaults.ContainerBox(
                                    enabled = true,
                                    isError = false,
                                    interactionSource = remember { MutableInteractionSource() },
                                    colors = OutlinedTextFieldDefaults.colors(),
                                    shape = MaterialTheme.shapes.extraSmall,
                                )
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    onClick = onIncreaseTime,
                    modifier = Modifier.size(controlHeight),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+5", style = MaterialTheme.typography.bodySmall, color = contentColor)
                    }
                }
            }

            Box(
                modifier = Modifier.weight(0.8f),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(controlHeight)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_phase_title),
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                }
            }
        }
    }
}