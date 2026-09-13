package io.github.anka1981.bikerouteplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.anka1981.bikerouteplanner.ui.i18n.HelpItem
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.helpSections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.helpTitle) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            helpSections(strings).forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(section.heading, style = MaterialTheme.typography.titleMedium)
                    section.items.forEach { item -> HelpItemRow(item) }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun HelpItemRow(item: HelpItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (item.icon != null) {
            Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp))
        } else {
            Spacer(modifier = Modifier.size(22.dp))
        }
        Column {
            Text(item.title, style = MaterialTheme.typography.bodyMedium)
            Text(item.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
