package io.github.anka1981.bikerouteplanner.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import io.github.anka1981.bikerouteplanner.ui.i18n.LocalAppStrings
import io.github.anka1981.bikerouteplanner.ui.i18n.aboutSources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val versionText = remember(context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${PackageInfoCompat.getLongVersionCode(packageInfo)})"
        } catch (e: Exception) {
            "?"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.aboutTitle) },
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
            Text("${strings.versionLabel}: $versionText")

            HorizontalDivider()

            Text(strings.routeMapInfo, style = MaterialTheme.typography.bodySmall)

            HorizontalDivider()

            Text(strings.osmAndSetupHeading, style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                strings.osmAndSetupSteps.forEachIndexed { index, step ->
                    Text(
                        "${index + 1}. $step",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            HorizontalDivider()

            Text(strings.sourcesHeading, style = MaterialTheme.typography.titleMedium)
            aboutSources(strings).forEach { source ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.url)))
                        }
                    ) {
                        Text(source.title)
                    }
                    Text(
                        source.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                }
            }

            HorizontalDivider()

            Text(strings.licenseNote, style = MaterialTheme.typography.bodySmall)
        }
    }
}
