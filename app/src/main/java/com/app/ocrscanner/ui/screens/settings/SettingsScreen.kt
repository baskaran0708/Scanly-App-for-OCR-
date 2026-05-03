package com.app.ocrscanner.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.Danger
import com.app.ocrscanner.ui.theme.Primary
import com.app.ocrscanner.ui.theme.PrimaryContainer
import com.app.ocrscanner.ui.theme.Success

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDataDialog by remember { mutableStateOf(false) }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear all data?") },
            text = { Text("This will permanently delete all scanned documents. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Clear data", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // App bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                    Text(
                        "LMI Scanly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                // App profile card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Primary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(0.18f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, null, tint = Accent, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("LMI Scanly", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("OCR Document Scanner", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.75f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("v${uiState.appVersion}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.65f))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = Accent, modifier = Modifier.size(12.dp))
                                    Text("Active", style = MaterialTheme.typography.labelSmall, color = Accent)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // Scan & OCR section
                item {
                    SettingsSectionLabel("Scan & OCR")
                    SettingsGroup {
                        SettingsToggleRow(
                            icon = Icons.Outlined.AutoAwesome,
                            iconTint = Color(0xFF7C3AED),
                            title = "Auto-process OCR",
                            subtitle = "Automatically extract text after scanning",
                            checked = uiState.autoProcessOcr,
                            onToggle = viewModel::setAutoProcessOcr,
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 56.dp))
                        SettingsToggleRow(
                            icon = Icons.Outlined.PhotoLibrary,
                            iconTint = Color(0xFF059669),
                            title = "Save to gallery",
                            subtitle = "Copy scanned images to Pictures/LMI Scanly",
                            checked = uiState.autoSaveToGallery,
                            onToggle = viewModel::setAutoSaveToGallery,
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 56.dp))
                        SettingsToggleRow(
                            icon = Icons.Outlined.Image,
                            iconTint = Color(0xFF0891B2),
                            title = "Keep original image",
                            subtitle = "Store unprocessed capture alongside the scan",
                            checked = uiState.keepOriginalImage,
                            onToggle = viewModel::setKeepOriginalImage,
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 56.dp))
                        SettingsToggleRow(
                            icon = Icons.Outlined.Speed,
                            iconTint = Primary,
                            title = "Show confidence score",
                            subtitle = "Display OCR accuracy percentage on results",
                            checked = uiState.showConfidenceScore,
                            onToggle = viewModel::setShowConfidenceScore,
                        )
                    }
                }

                // Image quality section
                item {
                    SettingsSectionLabel("Image Quality")
                    SettingsGroup {
                        ImageQuality.values().forEachIndexed { idx, quality ->
                            val selected = uiState.imageQuality == quality
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setImageQuality(quality) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (selected) Primary else MaterialTheme.colorScheme.outline,
                                            CircleShape,
                                        ),
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        quality.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        "JPEG quality: ${quality.jpegQuality}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (selected) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                            if (idx < ImageQuality.values().size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 38.dp))
                            }
                        }
                    }
                }

                // Export defaults section
                item {
                    SettingsSectionLabel("Default Export Format")
                    SettingsGroup {
                        ExportFormat.values().forEachIndexed { idx, format ->
                            val selected = uiState.defaultExportFormat == format
                            val icon = when (format) {
                                ExportFormat.PDF -> Icons.Outlined.Description
                                ExportFormat.TXT -> Icons.Outlined.TextSnippet
                                ExportFormat.RTF -> Icons.Outlined.Description
                                ExportFormat.JPEG -> Icons.Outlined.Image
                            }
                            val color = when (format) {
                                ExportFormat.PDF -> Color(0xFFDC2626)
                                ExportFormat.TXT -> Color(0xFF2563EB)
                                ExportFormat.RTF -> Color(0xFF7C3AED)
                                ExportFormat.JPEG -> Color(0xFF059669)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setDefaultExportFormat(format) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color.copy(0.12f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    format.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                            if (idx < ExportFormat.values().size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 66.dp))
                            }
                        }
                    }
                }

                // OCR Language section
                item {
                    SettingsSectionLabel("Language")
                    SettingsGroup {
                        listOf("English", "Hindi", "Tamil", "Telugu", "Kannada", "Malayalam", "Bengali", "Arabic", "Chinese", "French", "Spanish", "German").forEach { lang ->
                            val selected = uiState.ocrLanguage == lang
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setOcrLanguage(lang) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.Language, null, tint = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    lang,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outline.copy(0.4f), modifier = Modifier.padding(start = 50.dp))
                        }
                    }
                }

                // Appearance section
                item {
                    SettingsSectionLabel("Appearance")
                    SettingsGroup {
                        ThemeMode.values().forEachIndexed { idx, mode ->
                            val selected = uiState.themeMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Outlined.BrightnessMedium,
                                    null,
                                    tint = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    mode.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                            if (idx < ThemeMode.values().size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 50.dp))
                            }
                        }
                    }
                }

                // Storage section
                item {
                    SettingsSectionLabel("Storage & Data")
                    SettingsGroup {
                        SettingsNavRow(
                            icon = Icons.Outlined.Storage,
                            iconTint = Color(0xFF0891B2),
                            title = "Storage location",
                            subtitle = "Internal storage · Pictures/LMI Scanly",
                            onClick = {},
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 56.dp))
                        SettingsNavRow(
                            icon = Icons.Outlined.CloudDone,
                            iconTint = Success,
                            title = "Cloud backup",
                            subtitle = "Not configured",
                            onClick = {},
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 56.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showClearDataDialog = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Danger.copy(0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Delete, null, tint = Danger, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Clear all data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Danger,
                                )
                                Text(
                                    "Delete all documents permanently",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Sharing section
                item {
                    SettingsSectionLabel("Sharing & Export")
                    SettingsGroup {
                        SettingsNavRow(
                            icon = Icons.Outlined.Share,
                            iconTint = Primary,
                            title = "Share via",
                            subtitle = "WhatsApp, Email, Drive & more",
                            onClick = {},
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.5f), modifier = Modifier.padding(start = 56.dp))
                        SettingsNavRow(
                            icon = Icons.Outlined.FolderOpen,
                            iconTint = Color(0xFFD97706),
                            title = "Export folder",
                            subtitle = "Pictures/LMI Scanly/exports",
                            onClick = {},
                        )
                    }
                }

                // About section
                item {
                    SettingsSectionLabel("About")
                    SettingsGroup {
                        SettingsNavRow(
                            icon = Icons.Outlined.Info,
                            iconTint = Accent,
                            title = "About LMI Scanly",
                            subtitle = "Version ${uiState.appVersion} · Built by Live Medica",
                            onClick = {},
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp).navigationBarsPadding())
                    Text(
                        "LMI Scanly v${uiState.appVersion} · Live Medica",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Primary,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 4.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = PrimaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Outlined.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
    }
}
