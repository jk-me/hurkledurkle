package com.hurkledurkle.app.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimezonePicker by rememberSaveable { mutableStateOf(false) }
    var showResetTimePicker by rememberSaveable { mutableStateOf(false) }

    val resetPickerState = rememberTimePickerState(
        initialHour = uiState.bedtimeResetHour,
        initialMinute = uiState.bedtimeResetMinute,
        is24Hour = false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Default Timezone", style = MaterialTheme.typography.titleSmall)
            Text(
                "Used as the default when logging a new session.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = { showTimezonePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.timezone)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Bedtime Reset Time", style = MaterialTheme.typography.titleSmall)
            Text(
                "If you start winding down before this time, the session is bucketed to the previous calendar day. Default: 12:00 PM.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = { showResetTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                val h = uiState.bedtimeResetHour
                val m = uiState.bedtimeResetMinute
                val display = "%d:%02d %s".format(
                    if (h % 12 == 0) 12 else h % 12,
                    m,
                    if (h < 12) "AM" else "PM"
                )
                Text("Reset at $display")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Graph Range: ${uiState.graphDays} days",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(
                value = uiState.graphDays.toFloat(),
                onValueChange = { viewModel.setGraphDays(it.toInt()) },
                valueRange = 7f..90f,
                steps = 16,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("7 days", style = MaterialTheme.typography.labelSmall)
                Text("90 days", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTimezonePicker) {
        TimezonePickerDialog(
            current = uiState.timezone,
            onSelected = { viewModel.setTimezone(it); showTimezonePicker = false },
            onDismiss = { showTimezonePicker = false }
        )
    }

    if (showResetTimePicker) {
        AlertDialog(
            onDismissRequest = { showResetTimePicker = false },
            title = { Text("Bedtime Reset Time") },
            text = { TimePicker(state = resetPickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setBedtimeReset(resetPickerState.hour, resetPickerState.minute)
                    showResetTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showResetTimePicker = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun TimezonePickerDialog(
    current: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val all = remember { java.util.TimeZone.getAvailableIDs().sorted() }
    val filtered = remember(search) {
        if (search.isBlank()) all else all.filter { it.contains(search, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Timezone") },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                filtered.take(60).forEach { tz ->
                    TextButton(onClick = { onSelected(tz) }, modifier = Modifier.fillMaxWidth()) {
                        Text(tz, color = if (tz == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
