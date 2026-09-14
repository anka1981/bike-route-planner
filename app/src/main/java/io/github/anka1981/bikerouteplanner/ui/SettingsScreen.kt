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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.availableUiLanguages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RouteViewModel,
    onOpenAbout: () -> Unit,
    onOpenHelp: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val settings by viewModel.settings.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val cityRefreshStatus by viewModel.cityRefreshStatus.collectAsState()
    var appIdVisible by remember { mutableStateOf(false) }
    var citySearchExpanded by remember { mutableStateOf(false) }

    val cityMatches = remember(settings.citySlug, cities) {
        val query = settings.citySlug.trim()
        if (query.isEmpty()) {
            emptyList()
        } else {
            cities.filter {
                it.label.contains(query, ignoreCase = true) || it.slug.contains(query, ignoreCase = true)
            }.take(8)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHelp) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = strings.helpButton)
                    }
                    IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Filled.Info, contentDescription = strings.aboutButton)
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
            Column {
                OutlinedTextField(
                    value = settings.citySlug,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(citySlug = it))
                        citySearchExpanded = true
                    },
                    label = { Text(strings.citySlugLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (citySearchExpanded && cityMatches.isNotEmpty()) {
                    Column {
                        cityMatches.forEach { city ->
                            TextButton(
                                onClick = {
                                    viewModel.updateSettings(settings.copy(citySlug = city.slug))
                                    citySearchExpanded = false
                                }
                            ) {
                                Text("${city.label} (${city.slug})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Text(strings.citySlugHelp, style = MaterialTheme.typography.bodySmall)

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedButton(onClick = { viewModel.refreshCities() }) {
                    Text(strings.refreshCitiesButton)
                }
            }
            cityRefreshStatus?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = settings.appId,
                onValueChange = { viewModel.updateSettings(settings.copy(appId = it)) },
                label = { Text(strings.appIdLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (appIdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { appIdVisible = !appIdVisible }) {
                        Icon(
                            if (appIdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (appIdVisible) strings.appIdHideDesc else strings.appIdShowDesc
                        )
                    }
                }
            )
            Text(strings.appIdHelp, style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = settings.routingLanguage,
                onValueChange = { viewModel.updateSettings(settings.copy(routingLanguage = it)) },
                label = { Text(strings.routingLanguageLabel) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LabeledOptionPicker(
                label = strings.uiLanguageLabel,
                options = availableUiLanguages.map { it.languageCode to it.languageDisplayName },
                selected = settings.uiLanguage,
                onSelected = { viewModel.updateSettings(settings.copy(uiLanguage = it)) }
            )

            LabeledOptionPicker(
                label = strings.colorThemeLabel,
                options = strings.colorThemeOptions,
                selected = settings.colorTheme,
                onSelected = { viewModel.updateSettings(settings.copy(colorTheme = it)) }
            )

            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.routeEventsLabel, modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.loadRouteEvents,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(loadRouteEvents = it)) }
                )
            }
            Text(strings.routeEventsHelp, style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onOpenHelp) {
                    Text(strings.helpButton)
                }
                Button(onClick = onOpenAbout) {
                    Text(strings.aboutButton)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledOptionPicker(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selected }?.second ?: selected

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, optionLabel) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
