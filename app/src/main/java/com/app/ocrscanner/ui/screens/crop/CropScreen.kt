package com.app.ocrscanner.ui.screens.crop

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.FilterNone
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RotateLeft
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.CameraBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private enum class CropHandle { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }

@Composable
fun CropScreen(
    originalBitmap: Bitmap,
    onRetake: () -> Unit,
    onConfirm: (Bitmap) -> Unit,
    onAddPage: () -> Unit = {},
    viewModel: CropViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmScope = rememberCoroutineScope()
    var isConfirming by remember { mutableStateOf(false) }

    var displayBitmap by remember { mutableStateOf(originalBitmap) }

    // Only reprocess on filter/rotation change — brightness is applied via ColorFilter instantly
    LaunchedEffect(uiState.selectedFilter, uiState.rotation) {
        val result = withContext(Dispatchers.Default) {
            val filtered = viewModel.applyFilter(originalBitmap, uiState.selectedFilter, 0f)
            viewModel.applyRotation(filtered, uiState.rotation)
        }
        displayBitmap = result
    }

    // Brightness ColorFilter applied directly to Image for instant real-time preview
    val brightnessColorFilter = remember(uiState.brightness) {
        if (uiState.brightness == 0f) null
        else ColorFilter.colorMatrix(
            ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, uiState.brightness,
                0f, 1f, 0f, 0f, uiState.brightness,
                0f, 0f, 1f, 0f, uiState.brightness,
                0f, 0f, 0f, 1f, 0f,
            ))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CameraBackground),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRetake) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retake", tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Retake", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color.White.copy(0.12f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("1 of 1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.MoreVert, null, tint = Color.White)
            }
        }

        // Image with interactive crop overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val ratio = displayBitmap.width.toFloat() / displayBitmap.height.toFloat().coerceAtLeast(0.01f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(4.dp)),
            ) {
                androidx.compose.foundation.Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = "Document preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = brightnessColorFilter,
                )

                if (uiState.selectedTab == CropTab.CROP) {
                    InteractiveCropOverlay(
                        cropLeft = uiState.cropLeft,
                        cropTop = uiState.cropTop,
                        cropRight = uiState.cropRight,
                        cropBottom = uiState.cropBottom,
                        onCropChange = { l, t, r, b -> viewModel.updateCropRect(l, t, r, b) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Enhance slider (visible only on Enhance tab)
        if (uiState.selectedTab == CropTab.ENHANCE) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.3f))
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Brightness",
                        color = Color.White.copy(0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${uiState.brightness.toInt()}",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(
                    value = uiState.brightness,
                    onValueChange = viewModel::setBrightness,
                    valueRange = -100f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = Color.White.copy(0.3f),
                    ),
                )
            }
        }

        // Rotate controls (visible only on Rotate tab)
        if (uiState.selectedTab == CropTab.ROTATE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.3f))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(0.1f))
                        .clickable { viewModel.rotateLeft() }
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Outlined.RotateLeft, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Rotate Left", color = Color.White.copy(0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(24.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(0.1f))
                        .clickable { viewModel.rotate90() }
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Outlined.RotateRight, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Rotate Right", color = Color.White.copy(0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf(
                Triple(CropTab.CROP, Icons.Outlined.Crop, "Crop"),
                Triple(CropTab.ENHANCE, Icons.Outlined.Tune, "Enhance"),
                Triple(CropTab.FILTER, Icons.Outlined.FilterNone, "Filter"),
                Triple(CropTab.ROTATE, Icons.Outlined.RotateRight, "Rotate"),
            ).forEach { (tab, icon, label) ->
                CropTabButton(
                    icon = icon,
                    label = label,
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.setTab(tab) },
                )
            }
        }

        // Filter strip (visible on Filter or Enhance tabs)
        if (uiState.selectedTab == CropTab.FILTER || uiState.selectedTab == CropTab.ENHANCE) {
            FilterStrip(
                filters = ImageFilter.values().toList(),
                selectedFilter = uiState.selectedFilter,
                onFilterSelect = viewModel::setFilter,
            )
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1020).copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .border(1.dp, Color.White.copy(0.25f), RoundedCornerShape(999.dp))
                    .clickable { onRetake() }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text("Retake", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(0.12f))
                    .clickable { onAddPage() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add page", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Accent)
                    .clickable(enabled = !isConfirming) {
                        // Run bitmap ops on background thread to avoid freezing the UI
                        isConfirming = true
                        confirmScope.launch {
                            val finalBitmap = withContext(Dispatchers.Default) {
                                val withBrightness = viewModel.applyBrightness(displayBitmap, uiState.brightness)
                                viewModel.cropBitmap(withBrightness)
                            }
                            isConfirming = false
                            onConfirm(finalBitmap)
                        }
                    }
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isConfirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF04181C),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Check, null, tint = Color(0xFF04181C), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Confirm", color = Color(0xFF04181C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveCropOverlay(
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    onCropChange: (Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberUpdatedState ensures the drag lambda always reads the LATEST crop values,
    // not stale captured values from the first composition.
    val cropLState = rememberUpdatedState(cropLeft)
    val cropTState = rememberUpdatedState(cropTop)
    val cropRState = rememberUpdatedState(cropRight)
    val cropBState = rememberUpdatedState(cropBottom)
    val onChangedState = rememberUpdatedState(onCropChange)

    BoxWithConstraints(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // dragHandle persists across the entire drag gesture (start → drag → end)
                    // because it's a local variable inside the coroutine block, not a Compose state.
                    var dragHandle: CropHandle? = null

                    detectDragGestures(
                        onDragStart = { pos ->
                            val cl = cropLState.value
                            val ct = cropTState.value
                            val cr = cropRState.value
                            val cb = cropBState.value
                            val zone = 56.dp.roundToPx().toFloat()

                            // Manhattan distance from each corner — pick the nearest within zone
                            val tlDist = abs(pos.x - cl * size.width) + abs(pos.y - ct * size.height)
                            val trDist = abs(pos.x - cr * size.width) + abs(pos.y - ct * size.height)
                            val brDist = abs(pos.x - cr * size.width) + abs(pos.y - cb * size.height)
                            val blDist = abs(pos.x - cl * size.width) + abs(pos.y - cb * size.height)
                            val minDist = minOf(tlDist, trDist, brDist, blDist)

                            dragHandle = if (minDist > zone) null else when (minDist) {
                                tlDist -> CropHandle.TOP_LEFT
                                trDist -> CropHandle.TOP_RIGHT
                                brDist -> CropHandle.BOTTOM_RIGHT
                                else   -> CropHandle.BOTTOM_LEFT
                            }
                        },
                        onDragEnd = { dragHandle = null },
                        onDragCancel = { dragHandle = null },
                    ) { change, drag ->
                        change.consume()
                        val h = dragHandle ?: return@detectDragGestures

                        // Always read the current (fresh) crop values via rememberUpdatedState
                        val cl = cropLState.value
                        val ct = cropTState.value
                        val cr = cropRState.value
                        val cb = cropBState.value

                        val dx = drag.x / size.width
                        val dy = drag.y / size.height
                        val min = 0.08f

                        when (h) {
                            CropHandle.TOP_LEFT -> onChangedState.value(
                                (cl + dx).coerceIn(0f, cr - min),
                                (ct + dy).coerceIn(0f, cb - min),
                                cr, cb,
                            )
                            CropHandle.TOP_RIGHT -> onChangedState.value(
                                cl,
                                (ct + dy).coerceIn(0f, cb - min),
                                (cr + dx).coerceIn(cl + min, 1f),
                                cb,
                            )
                            CropHandle.BOTTOM_RIGHT -> onChangedState.value(
                                cl, ct,
                                (cr + dx).coerceIn(cl + min, 1f),
                                (cb + dy).coerceIn(ct + min, 1f),
                            )
                            CropHandle.BOTTOM_LEFT -> onChangedState.value(
                                (cl + dx).coerceIn(0f, cr - min),
                                ct, cr,
                                (cb + dy).coerceIn(ct + min, 1f),
                            )
                        }
                    }
                },
        ) {
            val l = cropLeft * size.width
            val t = cropTop * size.height
            val r = cropRight * size.width
            val b = cropBottom * size.height
            val dim = Color.Black.copy(alpha = 0.45f)

            // Dim outside the crop rect
            drawRect(dim, topLeft = Offset.Zero, size = Size(size.width, t))
            drawRect(dim, topLeft = Offset(0f, b), size = Size(size.width, size.height - b))
            drawRect(dim, topLeft = Offset(0f, t), size = Size(l, b - t))
            drawRect(dim, topLeft = Offset(r, t), size = Size(size.width - r, b - t))

            // Crop border
            drawRect(
                color = Color(0xFF06B6D4),
                topLeft = Offset(l, t),
                size = Size(r - l, b - t),
                style = Stroke(width = 2.dp.toPx()),
            )

            // Grid lines (rule of thirds)
            val cw = (r - l) / 3f
            val ch = (b - t) / 3f
            repeat(2) { i ->
                drawLine(
                    Color.White.copy(0.3f),
                    Offset(l + cw * (i + 1), t), Offset(l + cw * (i + 1), b),
                    strokeWidth = 1.dp.toPx(),
                )
                drawLine(
                    Color.White.copy(0.3f),
                    Offset(l, t + ch * (i + 1)), Offset(r, t + ch * (i + 1)),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // Corner bracket handles
            val sw = 4.dp.toPx()
            val len = 22.dp.toPx()
            val cornerDefs = listOf(
                floatArrayOf(l, t,  1f,  1f),
                floatArrayOf(r, t, -1f,  1f),
                floatArrayOf(r, b, -1f, -1f),
                floatArrayOf(l, b,  1f, -1f),
            )
            cornerDefs.forEach { (cx, cy, dx, dy) ->
                drawLine(Color(0xFF06B6D4), Offset(cx, cy), Offset(cx + dx * len, cy), sw, cap = StrokeCap.Round)
                drawLine(Color(0xFF06B6D4), Offset(cx, cy), Offset(cx, cy + dy * len), sw, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun CropTabButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White.copy(0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(3.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterStrip(
    filters: List<ImageFilter>,
    selectedFilter: ImageFilter,
    onFilterSelect: (ImageFilter) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(0.2f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(filters) { filter ->
            val active = filter == selectedFilter
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onFilterSelect(filter) },
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            2.dp,
                            if (active) Accent else Color.Transparent,
                            RoundedCornerShape(8.dp),
                        )
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = filter.label.take(3),
                        color = Color(0xFF1E3A8A),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = filter.label,
                    color = if (active) Accent else Color.White.copy(0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
