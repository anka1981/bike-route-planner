package io.github.anka1981.bikerouteplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.anka1981.bikerouteplanner.ui.i18n.AppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(viewModel: RouteViewModel, onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val prefs by viewModel.preferences.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val activeProfileId by viewModel.activeProfileId.collectAsState()
    var newProfileName by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.preferencesTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(strings.preferencesIntro, style = MaterialTheme.typography.bodySmall)

            Column {
                Text(strings.profileLabel, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    LabeledDropdown(
                        title = "",
                        options = profiles.map { it.id to it.name },
                        selected = activeProfileId,
                        onSelected = { viewModel.selectProfile(it) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.deleteActiveProfile() },
                        enabled = profiles.size > 1
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = strings.deleteProfileDesc)
                    }
                }

                if (renaming) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            label = { Text(strings.newProfileNameLabel) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        TextButton(
                            onClick = {
                                if (renameText.isNotBlank()) viewModel.renameActiveProfile(renameText)
                                renaming = false
                            }
                        ) {
                            Text(strings.save)
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            renameText = profiles.find { it.id == activeProfileId }?.name.orEmpty()
                            renaming = true
                        }
                    ) {
                        Text(strings.renameProfile)
                    }
                }

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text(strings.nameForNewProfileLabel) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newProfileName.isNotBlank()) {
                                viewModel.saveCurrentAsNewProfile(newProfileName)
                                newProfileName = ""
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(strings.saveAsNew)
                    }
                }
            }

            HorizontalDivider()

            Column {
                Text(strings.speedLabel(prefs.speedKmh))
                Slider(
                    value = prefs.speedKmh.toFloat(),
                    onValueChange = { viewModel.updatePreferences(prefs.copy(speedKmh = it.toInt())) },
                    valueRange = 8f..40f,
                    steps = 31
                )
            }

            HorizontalDivider()

            LabeledDropdown(
                title = strings.categoryLabel,
                options = strings.categoryOptions,
                selected = prefs.category,
                onSelected = { viewModel.updatePreferences(prefs.copy(category = it)) }
            )

            LabeledDropdown(
                title = strings.qualityLabel,
                options = strings.qualityOptions,
                selected = prefs.quality,
                onSelected = { viewModel.updatePreferences(prefs.copy(quality = it)) }
            )

            LabeledDropdown(
                title = strings.greenRouteLabel,
                options = strings.greenRouteOptions,
                selected = prefs.greenRoute,
                onSelected = { viewModel.updatePreferences(prefs.copy(greenRoute = it)) }
            )

            LabeledDropdown(
                title = strings.specialVehicleLabel,
                options = strings.specialVehicleOptions,
                selected = prefs.specialVehicle,
                onSelected = { viewModel.updatePreferences(prefs.copy(specialVehicle = it)) }
            )

            HorizontalDivider()

            SwitchRow(
                title = strings.allowFerryLabel,
                checked = prefs.allowFerry,
                onCheckedChange = { viewModel.updatePreferences(prefs.copy(allowFerry = it)) }
            )
            SwitchRow(
                title = strings.avoidUnlitLabel,
                checked = prefs.avoidUnlit,
                onCheckedChange = { viewModel.updatePreferences(prefs.copy(avoidUnlit = it)) }
            )
            SwitchRow(
                title = strings.avoidTrafficLightsLabel,
                checked = prefs.avoidTrafficLights,
                onCheckedChange = { viewModel.updatePreferences(prefs.copy(avoidTrafficLights = it)) }
            )
            SwitchRow(
                title = strings.includeUnknownStreetsLabel,
                checked = prefs.includeUnknownStreets,
                onCheckedChange = { viewModel.updatePreferences(prefs.copy(includeUnknownStreets = it)) }
            )
        }
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledDropdown(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selected }?.second ?: selected

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = if (title.isNotBlank()) {
                { Text(title) }
            } else {
                null
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
