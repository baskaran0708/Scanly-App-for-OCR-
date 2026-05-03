package com.app.ocrscanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Single source of truth for all user preferences, backed by Jetpack DataStore.
 *
 * Values are persisted as raw primitives. Callers are responsible for mapping
 * string keys to their typed enum equivalents (ThemeMode, ImageQuality, etc.)
 * to avoid cross-layer dependencies on UI enums.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store: DataStore<Preferences> = context.dataStore

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_AUTO_GALLERY = booleanPreferencesKey("auto_gallery")
        val KEY_OCR_LANGUAGE = stringPreferencesKey("ocr_language")
        val KEY_EXPORT_FORMAT = stringPreferencesKey("export_format")
        val KEY_IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val KEY_SHOW_CONFIDENCE = booleanPreferencesKey("show_confidence")
        val KEY_AUTO_OCR = booleanPreferencesKey("auto_ocr")
        val KEY_KEEP_ORIGINAL = booleanPreferencesKey("keep_original")
    }

    /**
     * Hot snapshot of all preferences — emits on every write.
     * Upstream errors are recovered by substituting empty defaults.
     */
    val preferences: Flow<UserPreferences> = store.data
        .catch { emit(emptyPreferences()) }
        .map { p ->
            UserPreferences(
                theme = p[KEY_THEME] ?: "SYSTEM",
                autoSaveToGallery = p[KEY_AUTO_GALLERY] ?: true,
                ocrLanguage = p[KEY_OCR_LANGUAGE] ?: "English",
                exportFormat = p[KEY_EXPORT_FORMAT] ?: "PDF",
                imageQuality = p[KEY_IMAGE_QUALITY] ?: "HIGH",
                showConfidenceScore = p[KEY_SHOW_CONFIDENCE] ?: true,
                autoProcessOcr = p[KEY_AUTO_OCR] ?: true,
                keepOriginalImage = p[KEY_KEEP_ORIGINAL] ?: true,
            )
        }

    suspend fun setTheme(value: String) = store.edit { it[KEY_THEME] = value }
    suspend fun setAutoSaveToGallery(v: Boolean) = store.edit { it[KEY_AUTO_GALLERY] = v }
    suspend fun setOcrLanguage(lang: String) = store.edit { it[KEY_OCR_LANGUAGE] = lang }
    suspend fun setExportFormat(f: String) = store.edit { it[KEY_EXPORT_FORMAT] = f }
    suspend fun setImageQuality(q: String) = store.edit { it[KEY_IMAGE_QUALITY] = q }
    suspend fun setShowConfidenceScore(v: Boolean) = store.edit { it[KEY_SHOW_CONFIDENCE] = v }
    suspend fun setAutoProcessOcr(v: Boolean) = store.edit { it[KEY_AUTO_OCR] = v }
    suspend fun setKeepOriginalImage(v: Boolean) = store.edit { it[KEY_KEEP_ORIGINAL] = v }
}

/** Raw-primitive snapshot of every user preference. No UI or domain types. */
data class UserPreferences(
    val theme: String = "SYSTEM",
    val autoSaveToGallery: Boolean = true,
    val ocrLanguage: String = "English",
    val exportFormat: String = "PDF",
    val imageQuality: String = "HIGH",
    val showConfidenceScore: Boolean = true,
    val autoProcessOcr: Boolean = true,
    val keepOriginalImage: Boolean = true,
)
