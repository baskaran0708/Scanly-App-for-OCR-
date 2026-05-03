package com.app.ocrscanner.ui.screens.ocr

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.ocrscanner.ui.components.ConfidenceBadge
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.AccentSoft
import com.app.ocrscanner.ui.theme.Danger
import com.app.ocrscanner.ui.theme.DangerSoft
import com.app.ocrscanner.ui.theme.Primary
import com.app.ocrscanner.ui.theme.PrimaryContainer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OcrResultScreen(
    bitmap: Bitmap,
    onBack: () -> Unit,
    onSave: (Long) -> Unit,
    viewModel: OcrViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var documentTitle by remember { mutableStateOf("Scanned Document") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Request WRITE_EXTERNAL_STORAGE on Android 9 and below for gallery save
    val writePermission = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    LaunchedEffect(bitmap) { viewModel.processOcr(bitmap) }

    // Show save error if it occurs
    LaunchedEffect(uiState.error) {
        val err = uiState.error
        if (!err.isNullOrBlank()) {
            snackbarHostState.showSnackbar(err)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    Text(
                        text = "OCR Result",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!uiState.isProcessing) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${uiState.blocks.size} blocks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (uiState.confidence > 0f) {
                                ConfidenceBadge(confidence = uiState.confidence)
                            }
                        }
                    }
                }
            }

            // View mode toggle
            ViewModeToggle(
                selectedMode = uiState.viewMode,
                onModeChange = viewModel::setViewMode,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            // Loading state
            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Extracting text…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // Content area
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (uiState.viewMode == OcrViewMode.IMAGE || uiState.viewMode == OcrViewMode.SPLIT) {
                        item {
                            ImageWithBlocks(
                                bitmap = bitmap,
                                blocks = uiState.blocks,
                                selectedBlockIndex = uiState.selectedBlockId,
                                onBlockSelect = viewModel::selectBlock,
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    if (uiState.viewMode == OcrViewMode.TEXT || uiState.viewMode == OcrViewMode.SPLIT) {
                        if (uiState.fullText.isBlank()) {
                            item {
                                NoTextDetected()
                            }
                        } else {
                            item {
                                EditableTextArea(
                                    text = uiState.editedText,
                                    onTextChange = viewModel::updateText,
                                )
                            }
                        }
                    }

                    if (uiState.viewMode == OcrViewMode.IMAGE && uiState.selectedBlockId >= 0) {
                        item {
                            SelectedBlockSheet(
                                block = uiState.blocks.getOrNull(uiState.selectedBlockId),
                                onCopy = { text ->
                                    clipboard.setText(AnnotatedString(text))
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                },
                            )
                        }
                    }
                }
            }

            // Bottom actions
            BottomActionBar(
                onCopyAll = {
                    clipboard.setText(AnnotatedString(uiState.editedText))
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, uiState.editedText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                },
                onExportPdf = { viewModel.exportPdf(context, documentTitle) },
                onExportTxt = { viewModel.exportTxt(context, documentTitle) },
                onExportImage = { viewModel.exportImage(context, bitmap, documentTitle) },
                onExportDoc = { viewModel.exportDoc(context, documentTitle) },
                onSave = {
                    // On Android 9 and below, request WRITE_EXTERNAL_STORAGE for gallery save.
                    // On Android 10+, MediaStore handles gallery without extra permissions.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !writePermission.status.isGranted) {
                        writePermission.launchPermissionRequest()
                    }
                    viewModel.saveDocument(context, bitmap, documentTitle) { docId ->
                        onSave(docId)
                    }
                },
                isSaving = uiState.isSaving,
            )
        }
    }
}

@Composable
private fun ViewModeToggle(
    selectedMode: OcrViewMode,
    onModeChange: (OcrViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
            .padding(3.dp),
    ) {
        OcrViewMode.values().forEach { mode ->
            val selected = mode == selectedMode
            val icon = when (mode) {
                OcrViewMode.IMAGE -> Icons.Outlined.Image
                OcrViewMode.TEXT -> Icons.Outlined.FormatAlignLeft
                OcrViewMode.SPLIT -> Icons.Outlined.FormatListBulleted
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onModeChange(mode) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.label,
                        modifier = Modifier.size(14.dp),
                        tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = mode.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageWithBlocks(
    bitmap: Bitmap,
    blocks: List<com.app.ocrscanner.ocr.OcrTextBlock>,
    selectedBlockIndex: Int,
    onBlockSelect: (Int) -> Unit,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
    ) {
        val displayW = constraints.maxWidth.toFloat()
        val displayH = constraints.maxHeight.toFloat()
        val scaleX = displayW / bitmap.width
        val scaleY = displayH / bitmap.height

        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Scanned document",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        // Highlight overlays positioned over detected text blocks
        blocks.forEachIndexed { index, block ->
            val bb = block.boundingBox
            if (bb != null) {
                val isSelected = index == selectedBlockIndex
                val isFlag = block.kind == "value" && block.text.contains(Regex("[LH]$"))
                val bg = when {
                    isSelected -> Accent.copy(alpha = 0.28f)
                    isFlag -> Danger.copy(alpha = 0.10f)
                    else -> Primary.copy(alpha = 0.08f)
                }
                Box(
                    modifier = Modifier
                        .absoluteOffset(
                            x = with(androidx.compose.ui.platform.LocalDensity.current) { (bb.left * scaleX).toDp() },
                            y = with(androidx.compose.ui.platform.LocalDensity.current) { (bb.top * scaleY).toDp() },
                        )
                        .size(
                            width = with(androidx.compose.ui.platform.LocalDensity.current) { (bb.width() * scaleX).toDp() },
                            height = with(androidx.compose.ui.platform.LocalDensity.current) { (bb.height() * scaleY).toDp() },
                        )
                        .background(bg, RoundedCornerShape(3.dp))
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) Accent else Color.Transparent, RoundedCornerShape(3.dp))
                        .clickable { onBlockSelect(index) },
                )
            }
        }

        // Confidence ribbon
        Row(
            modifier = Modifier
                .padding(10.dp)
                .background(Color.White.copy(0.95f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = Accent, modifier = Modifier.size(11.dp))
            Text(
                text = "${blocks.size} text blocks · Tap to extract",
                color = Primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SelectedBlockSheet(
    block: com.app.ocrscanner.ocr.OcrTextBlock?,
    onCopy: (String) -> Unit,
) {
    if (block == null) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val isFlagged = block.text.contains(Regex("[LH]$"))
                Box(
                    modifier = Modifier
                        .background(
                            if (isFlagged) DangerSoft else PrimaryContainer,
                            RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = block.kind.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFlagged) Danger else Primary,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (block.confidence > 0.95f) Color(0xFF10B981) else Color(0xFFF59E0B),
                                CircleShape,
                            ),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${(block.confidence * 100).toInt()}% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChipButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Copy",
                    modifier = Modifier.weight(1f),
                    onClick = { onCopy(block.text) },
                )
                ActionChipButton(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    modifier = Modifier.weight(1f),
                    onClick = {},
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary)
                        .clickable {}
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.Download, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Add to note", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableTextArea(text: String, onTextChange: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun NoTextDetected() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No text detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Try re-scanning with better lighting or a higher-quality image.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionChipButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BottomActionBar(
    onCopyAll: () -> Unit,
    onShare: () -> Unit,
    onExportPdf: () -> Unit,
    onExportTxt: () -> Unit,
    onExportImage: () -> Unit,
    onExportDoc: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
            .navigationBarsPadding(),
    ) {
        // Export row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                Triple(Icons.Outlined.ContentCopy, "Copy", onCopyAll),
                Triple(Icons.Outlined.Share, "Share", onShare),
                Triple(Icons.Outlined.PictureAsPdf, "PDF", onExportPdf),
                Triple(Icons.Outlined.Image, "Image", onExportImage),
                Triple(Icons.Outlined.Download, "TXT", onExportTxt),
                Triple(Icons.Outlined.Description, "DOC", onExportDoc),
            ).forEach { (icon, label, action) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = action)
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(icon, null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Save button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary)
                .clickable(onClick = onSave),
            contentAlignment = Alignment.Center,
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Save to Documents & Gallery", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
