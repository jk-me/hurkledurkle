package com.hurkledurkle.app.ui.screen.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSessionScreen(
    viewModel: LogSessionViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEdit) "Edit Session" else "Log Sleep") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save")
                        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            FieldLabel("Date")
            DateSelector(date = uiState.date, onDateChanged = viewModel::setDate)

            HorizontalDivider()

            FieldLabel("Wind-down Time *")
            TimeSelector(
                entry = uiState.windDownTime,
                placeholder = "When did you start winding down?",
                onTimeSelected = viewModel::setWindDownTime
            )

            FieldLabel("Sleep Time")
            TimeSelector(
                entry = uiState.primarySleepTime,
                placeholder = "When did you fall asleep?",
                onTimeSelected = viewModel::setPrimarySleepTime,
                onClear = { viewModel.setPrimarySleepTime(null) }
            )

            FieldLabel("Wake Time")
            TimeSelector(
                entry = uiState.primaryWakeTime,
                placeholder = "When did you wake up?",
                onTimeSelected = viewModel::setPrimaryWakeTime,
                onClear = { viewModel.setPrimaryWakeTime(null) }
            )

            FieldLabel("Rise Time")
            TimeSelector(
                entry = uiState.riseTime,
                placeholder = "When did you get out of bed?",
                onTimeSelected = viewModel::setRiseTime,
                onClear = { viewModel.setRiseTime(null) }
            )

            HorizontalDivider()

            // Extra interrupted sleep/wake pairs
            if (uiState.extraPairs.isNotEmpty()) {
                FieldLabel("Interrupted Sleep")
                uiState.extraPairs.forEachIndexed { i, pair ->
                    InterruptedPairCard(
                        index = i,
                        pair = pair,
                        onSleepChanged = { viewModel.updateExtraPairSleep(i, it) },
                        onWakeChanged = { viewModel.updateExtraPairWake(i, it) },
                        onRemove = { viewModel.removeExtraPair(i) }
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.addExtraPair() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Interrupted Sleep / Wake")
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("This is a nap", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = uiState.isNap, onCheckedChange = viewModel::setIsNap)
            }

            HorizontalDivider()

            FieldLabel("Timezone")
            TimezoneSelector(timezone = uiState.timezone, onTimezoneSelected = viewModel::setTimezone)

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(date: LocalDate, onDateChanged: (LocalDate) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }

    OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(fmt.format(date))
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        onDateChanged(LocalDate.ofEpochDay(ms / 86_400_000L))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    entry: TimeEntry?,
    placeholder: String,
    onTimeSelected: (TimeEntry) -> Unit,
    onClear: (() -> Unit)? = null
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val pickerState = rememberTimePickerState(
        initialHour = entry?.hour ?: 22,
        initialMinute = entry?.minute ?: 0,
        is24Hour = false
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Schedule, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (entry != null) formatTime(entry) else placeholder,
                color = if (entry != null) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClear != null && entry != null) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Clear, contentDescription = "Clear time")
            }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Select time") },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(TimeEntry(pickerState.hour, pickerState.minute))
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        )
    }
}

private fun formatTime(entry: TimeEntry): String {
    val h = if (entry.hour % 12 == 0) 12 else entry.hour % 12
    val ampm = if (entry.hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(h, entry.minute, ampm)
}

@Composable
private fun InterruptedPairCard(
    index: Int,
    pair: InterruptedPair,
    onSleepChanged: (TimeEntry?) -> Unit,
    onWakeChanged: (TimeEntry?) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Interrupted #${index + 1}", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                }
            }
            TimeSelector(entry = pair.sleepTime, placeholder = "Sleep time", onTimeSelected = onSleepChanged, onClear = { onSleepChanged(null) })
            TimeSelector(entry = pair.wakeTime, placeholder = "Wake time", onTimeSelected = onWakeChanged, onClear = { onWakeChanged(null) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimezoneSelector(timezone: String, onTimezoneSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val all = remember { java.util.TimeZone.getAvailableIDs().sorted() }
    val filtered = remember(search) {
        if (search.isBlank()) all else all.filter { it.contains(search, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = timezone,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                singleLine = true
            )
            filtered.take(80).forEach { tz ->
                DropdownMenuItem(
                    text = { Text(tz) },
                    onClick = {
                        onTimezoneSelected(tz)
                        expanded = false
                        search = ""
                    }
                )
            }
        }
    }
}
