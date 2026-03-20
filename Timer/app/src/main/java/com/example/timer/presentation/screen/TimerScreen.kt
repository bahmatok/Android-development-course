package com.example.timer.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timer.presentation.viewmodel.TimerViewModel
import com.example.timer.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val timerState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timerState.sequenceName) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelTimer()
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.close_timer)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (timerState.isFinished) {

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.workout_finished),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.cancelTimer()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text(stringResource(R.string.done))
                }

                Spacer(modifier = Modifier.weight(1f))
                return@Column
            }

            Text(
                text = stringResource(
                    R.string.phase_progress,
                    timerState.currentPhaseIndex,
                    timerState.totalPhases
                ),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timerState.currentPhase?.type?.name
                    ?: stringResource(R.string.loading),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {

                val progress =
                    if (timerState.currentPhaseTotalSeconds > 0) {
                        timerState.timeLeftSeconds.toFloat() /
                                timerState.currentPhaseTotalSeconds.toFloat()
                    } else {
                        0f
                    }

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = formatTime(timerState.timeLeftSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = { viewModel.previousPhase() },
                    enabled = timerState.currentPhaseIndex > 1,
                    modifier = Modifier.width(128.dp)
                ) {
                    Text(stringResource(R.string.back))
                }

                FloatingActionButton(
                    onClick = { viewModel.togglePlayPause() },
                    ) {
                    Icon(
                        imageVector =
                            if (timerState.isRunning)
                                Icons.Filled.Close
                            else
                                Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.play_pause)
                    )
                }

                Button(
                    onClick = { viewModel.nextPhase() },
                    modifier = Modifier.width(128.dp)
                ) {
                    Text(stringResource(R.string.next_phase))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.upcoming_phases),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            if (timerState.upcomingPhases.isNotEmpty()) {

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(timerState.upcomingPhases) { phase ->
                        ListItem(
                            headlineContent = { Text(phase.type.name) },
                            trailingContent = {
                                Text(formatTime(phase.durationSeconds))
                            }
                        )
                    }
                }

            } else {

                Text(
                    stringResource(R.string.last_phase),
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}