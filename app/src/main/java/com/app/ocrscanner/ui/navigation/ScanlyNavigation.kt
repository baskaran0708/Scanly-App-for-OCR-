package com.app.ocrscanner.ui.navigation

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.ocrscanner.ui.screens.camera.CameraScreen
import com.app.ocrscanner.ui.screens.crop.CropScreen
import com.app.ocrscanner.ui.screens.document.DocumentDetailScreen
import com.app.ocrscanner.ui.screens.home.HomeScreen
import com.app.ocrscanner.ui.screens.ocr.OcrResultScreen
import com.app.ocrscanner.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Crop : Screen("crop")
    object OcrResult : Screen("ocr_result")
    object Settings : Screen("settings")
    object DocumentDetail : Screen("document/{documentId}") {
        fun createRoute(documentId: Long) = "document/$documentId"
    }
}

object BitmapHolder {
    var capturedBitmap: Bitmap? = null
    var processedBitmap: Bitmap? = null
}

@Composable
fun ScanlyNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onScan = { navController.navigate(Screen.Camera.route) },
                onOpenDocument = { docId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(docId))
                },
                onImportReady = { bitmap ->
                    BitmapHolder.capturedBitmap = bitmap
                    navController.navigate(Screen.Crop.route)
                },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onClose = { navController.popBackStack() },
                onCapture = { bitmap ->
                    BitmapHolder.capturedBitmap = bitmap
                    navController.navigate(Screen.Crop.route)
                },
                onGalleryImport = { bitmap ->
                    BitmapHolder.capturedBitmap = bitmap
                    navController.navigate(Screen.Crop.route)
                },
            )
        }

        composable(Screen.Crop.route) {
            val bitmap = BitmapHolder.capturedBitmap
            if (bitmap != null) {
                CropScreen(
                    originalBitmap = bitmap,
                    onRetake = { navController.popBackStack() },
                    onConfirm = { processedBitmap ->
                        BitmapHolder.processedBitmap = processedBitmap
                        navController.navigate(Screen.OcrResult.route)
                    },
                    onAddPage = { navController.navigate(Screen.Camera.route) },
                )
            } else {
                // Use LaunchedEffect to avoid calling navController during composition
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.OcrResult.route) {
            val bitmap = BitmapHolder.processedBitmap
            if (bitmap != null) {
                OcrResultScreen(
                    bitmap = bitmap,
                    onBack = { navController.popBackStack() },
                    onSave = { docId ->
                        // Clear bitmaps AFTER navigating to avoid the composable
                        // re-reading null and calling popBackStack() during transition.
                        navController.navigate(Screen.DocumentDetail.createRoute(docId)) {
                            popUpTo(Screen.Home.route)
                        }
                        BitmapHolder.capturedBitmap = null
                        BitmapHolder.processedBitmap = null
                    },
                )
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.DocumentDetail.route,
            arguments = listOf(navArgument("documentId") { type = NavType.LongType }),
        ) { backStack ->
            val documentId = backStack.arguments?.getLong("documentId") ?: return@composable
            DocumentDetailScreen(
                documentId = documentId,
                onBack = { navController.popBackStack() },
                onAddPage = { navController.navigate(Screen.Camera.route) },
            )
        }
    }
}
