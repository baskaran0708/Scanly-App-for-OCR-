package com.app.ocrscanner.ui.screens.camera

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.CameraBackground
import com.app.ocrscanner.ui.theme.EdgeDetectionCyan

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onCapture: (Bitmap) -> Unit,
    onGalleryImport: (Bitmap) -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val previewView = remember { PreviewView(context) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.importPdf(context, it, onGalleryImport) }
    }

    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted) {
            viewModel.bindCamera(context, lifecycleOwner, previewView)
        } else {
            cameraPermission.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.unbindCamera() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraBackground),
    ) {
        // Camera preview fills the entire screen
        if (cameraPermission.status.isGranted) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CameraPermissionDenied(onRequestPermission = { cameraPermission.launchPermissionRequest() })
        }

        // Edge detection overlay
        EdgeDetectionOverlay(modifier = Modifier.fillMaxSize())

        // Status bar area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
            }

            Spacer(Modifier.weight(1f))

            // Auto / Manual segmented control
            AutoManualToggle(
                autoMode = uiState.autoMode,
                onToggle = viewModel::setAutoMode,
            )

            Spacer(Modifier.width(8.dp))

            GlassIconButton(
                onClick = viewModel::toggleFlash,
                tint = if (uiState.flashEnabled) Color(0xFFFBBF24) else null,
            ) {
                Icon(
                    imageVector = if (uiState.flashEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                    contentDescription = "Flash",
                    tint = if (uiState.flashEnabled) Color(0xFF0F172A) else Color.White,
                )
            }
        }

        // Document detected chip
        DocumentDetectedChip(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
        )

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            // Mode chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            ) {
                items(ScanMode.values()) { mode ->
                    CameraModeChip(
                        text = mode.label,
                        selected = mode == uiState.selectedMode,
                        onClick = { viewModel.setScanMode(mode) },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Shutter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Gallery thumbnail
                GlassIconButton(
                    onClick = { galleryLauncher.launch("*/*") },
                    size = 56,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(24.dp))
                }

                // Shutter button
                ShutterButton(
                    isCapturing = uiState.isCapturing,
                    onClick = {
                        viewModel.capturePhoto(context) { bitmap -> onCapture(bitmap) }
                    },
                )

                // Multi-page counter
                GlassIconButton(
                    onClick = {},
                    size = 56,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("1/∞", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (uiState.autoMode) "Capturing automatically when stable…" else "Tap shutter to capture",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        // Error snackbar
        uiState.error?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp, start = 16.dp, end = 16.dp)
                    .background(Color(0xFFDC2626), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(error, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun EdgeDetectionOverlay(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 32.dp.toPx()
        val margin = 48.dp.toPx()
        val left = margin
        val top = size.height * 0.18f
        val right = size.width - margin
        val bottom = size.height * 0.78f

        val paint = androidx.compose.ui.graphics.Paint().apply {
            color = EdgeDetectionCyan
            this.strokeWidth = strokeWidth
            style = androidx.compose.ui.graphics.PaintingStyle.Stroke
        }

        // Filled polygon overlay
        drawContext.canvas.drawPath(
            androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right, bottom)
                lineTo(left, bottom)
                close()
            },
            androidx.compose.ui.graphics.Paint().apply {
                color = EdgeDetectionCyan.copy(alpha = 0.08f)
                style = androidx.compose.ui.graphics.PaintingStyle.Fill
            },
        )

        // Polygon outline
        drawContext.canvas.drawPath(
            androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right, bottom)
                lineTo(left, bottom)
                close()
            },
            paint,
        )

        // Corner brackets
        val corners = listOf(
            Pair(left, top) to Pair(1f, 1f),
            Pair(right, top) to Pair(-1f, 1f),
            Pair(right, bottom) to Pair(-1f, -1f),
            Pair(left, bottom) to Pair(1f, -1f),
        )
        val cornerPaint = androidx.compose.ui.graphics.Paint().apply {
            color = Accent
            this.strokeWidth = strokeWidth * 1.5f
            style = androidx.compose.ui.graphics.PaintingStyle.Stroke
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        }
        corners.forEach { (pos, dir) ->
            val (x, y) = pos
            val (dx, dy) = dir
            drawContext.canvas.drawLine(
                androidx.compose.ui.geometry.Offset(x, y),
                androidx.compose.ui.geometry.Offset(x + dx * cornerLength, y),
                cornerPaint,
            )
            drawContext.canvas.drawLine(
                androidx.compose.ui.geometry.Offset(x, y),
                androidx.compose.ui.geometry.Offset(x, y + dy * cornerLength),
                cornerPaint,
            )
        }
    }
}

@Composable
private fun DocumentDetectedChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Accent.copy(alpha = 0.95f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF04181C), CircleShape),
        )
        Text(
            text = "Document detected · Hold steady",
            color = Color(0xFF04181C),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AutoManualToggle(autoMode: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFF0F172A).copy(alpha = 0.55f), RoundedCornerShape(999.dp))
            .padding(3.dp),
    ) {
        listOf(true to "Auto", false to "Manual").forEach { (isAuto, label) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (autoMode == isAuto) Color.White else Color.Transparent)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = { onToggle(isAuto) },
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (autoMode == isAuto) Color(0xFF0F172A) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun CameraModeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Color.White else Color(0xFF0F172A).copy(alpha = 0.45f))
            .border(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF0F172A) else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    size: Int = 40,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    tint: Color? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(shape)
            .background(if (tint != null) tint else Color(0xFF0F172A).copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ShutterButton(isCapturing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .border(4.dp, Color.White, CircleShape)
            .background(Color.White.copy(alpha = 0.15f), CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Color.White,
                strokeWidth = 3.dp,
            )
        } else {
            Button(
                onClick = onClick,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {}
        }
    }
}

@Composable
private fun CameraPermissionDenied(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
            tint = Color.White.copy(0.5f),
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Camera permission required", color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) {
            Text("Grant permission", color = Color(0xFF04181C))
        }
    }
}
