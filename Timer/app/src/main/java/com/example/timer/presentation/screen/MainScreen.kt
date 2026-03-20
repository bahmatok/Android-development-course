package com.example.timer.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timer.domain.model.TimerSequence
import com.example.timer.presentation.viewmodel.MainViewModel
import com.example.timer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToTimer: (String) -> Unit,
    onNavigateToEdit: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val sequences by viewModel.sequences.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var sequenceToDelete by remember { mutableStateOf<TimerSequence?>(null) }

    if (sequenceToDelete != null) {
        AlertDialog(
            onDismissRequest = { sequenceToDelete = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = {
                Text(stringResource(R.string.delete_message, sequenceToDelete?.name ?: ""))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sequenceToDelete?.let { viewModel.deleteSequence(it) }
                        sequenceToDelete = null
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { sequenceToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_trainings)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(null) }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_timer)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                sequences.isEmpty() -> {
                    Text(stringResource(R.string.empty_list))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sequences, key = { it.id }) { sequence ->
                            TimerItem(
                                sequence = sequence,
                                onDelete = { sequenceToDelete = sequence },
                                onClick = { onNavigateToTimer(sequence.id) },
                                onEditClick = { onNavigateToEdit(sequence.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerItem(
    sequence: TimerSequence,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(16.dp).background(Color(sequence.color), CircleShape))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = sequence.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.phases_count, sequence.phases.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_timer)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        }
    }
}