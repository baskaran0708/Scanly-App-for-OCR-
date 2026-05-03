package com.app.ocrscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.ocrscanner.ui.MainViewModel
import com.app.ocrscanner.ui.navigation.ScanlyNavigation
import com.app.ocrscanner.ui.theme.ScanlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // null = respect system setting; true/false = user override
            val isDarkOverride by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val darkTheme = isDarkOverride ?: isSystemInDarkTheme()

            ScanlyTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScanlyNavigation()
                }
            }
        }
    }
}
