package com.app.ocrscanner.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ocrscanner.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Enums ──────────────────────────────────────────────────────────────────────

enum class ExportFormat(val label: String) {
    PDF("PDF Document"),
    TXT("Plain Text"),
    RTF("Word Document (.rtf)"),
    JPEG("JPEG Image"),
}

enum class ImageQuality(val label: String, val jpegQuality: Int) {
    LOW("Low (faster)", 60),
    MEDIUM("Medium", 80),
    HIGH("High (recommended)", 90),
    ULTRA("Ultra (large files)", 100),
}

enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("Follow system"),
}

// ── UI state ───────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val autoSaveToGallery: Boolean = true,
    val ocrLanguage: String = "English",
    val defaultExportFormat: ExportFormat = ExportFormat.PDF,
    val imageQuality: ImageQuality = ImageQuality.HIGH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showConfidenceScore: Boolean = true,
    val autoProcessOcr: Boolean = true,
    val keepOriginalImage: Boolean = true,
    val appVersion: String = "1.0.0",
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = prefs.preferences
        .map { p ->
            SettingsUiState(
                themeMode = ThemeMode.entries.firstOrNull { it.name == p.theme }
                    ?: ThemeMode.SYSTEM,
                autoSaveToGallery = p.autoSaveToGallery,
                ocrLanguage = p.ocrLanguage,
                defaultExportFormat = ExportFormat.entries.firstOrNull { it.name == p.exportFormat }
                    ?: ExportFormat.PDF,
                imageQuality = ImageQuality.entries.firstOrNull { it.name == p.imageQuality }
                    ?: ImageQuality.HIGH,
                showConfidenceScore = p.showConfidenceScore,
                autoProcessOcr = p.autoProcessOcr,
                keepOriginalImage = p.keepOriginalImage,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setAutoSaveToGallery(enabled: Boolean) =
        viewModelScope.launch { prefs.setAutoSaveToGallery(enabled) }

    fun setOcrLanguage(lang: String) =
        viewModelScope.launch { prefs.setOcrLanguage(lang) }

    fun setDefaultExportFormat(format: ExportFormat) =
        viewModelScope.launch { prefs.setExportFormat(format.name) }

    fun setImageQuality(quality: ImageQuality) =
        viewModelScope.launch { prefs.setImageQuality(quality.name) }

    fun setThemeMode(mode: ThemeMode) =
        viewModelScope.launch { prefs.setTheme(mode.name) }

    fun setShowConfidenceScore(show: Boolean) =
        viewModelScope.launch { prefs.setShowConfidenceScore(show) }

    fun setAutoProcessOcr(auto: Boolean) =
        viewModelScope.launch { prefs.setAutoProcessOcr(auto) }

    fun setKeepOriginalImage(keep: Boolean) =
        viewModelScope.launch { prefs.setKeepOriginalImage(keep) }
}
