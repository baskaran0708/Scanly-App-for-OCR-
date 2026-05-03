package com.app.ocrscanner.ui.screens.document

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.Danger
import com.app.ocrscanner.ui.theme.Primary
import com.app.ocrscanner.ui.theme.PrimaryContainer
import com.app.ocrscanner.ui.theme.Success
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: Long,
    onBack: () -> Unit,
    onAddPage: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    val exportSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(documentId) { viewModel.loadDocument(documentId) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete document?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteDocument { onBack() }
                }) { Text("Delete", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = exportSheetState,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    "Export as",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                listOf(
                    Triple(Icons.Outlined.PictureAsPdf, "PDF Document (.pdf)", Color(0xFFDC2626)),
                    Triple(Icons.Outlined.Image, "JPEG Image (.jpg)", Color(0xFF059669)),
                    Triple(Icons.Outlined.Image, "PNG Image (.png)", Color(0xFF0891B2)),
                    Triple(Icons.Outlined.TextSnippet, "Plain Text (.txt)", Color(0xFF2563EB)),
                    Triple(Icons.Outlined.Description, "Word Document (.rtf)", Color(0xFF7C3AED)),
                    Triple(Icons.Outlined.Share, "Share Text", Accent),
                ).forEach { (icon, label, tint) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch { exportSheetState.hide() }.invokeOnCompletion { showExportSheet = false }
                                when (label) {
                                    "PDF Document (.pdf)" -> viewModel.exportPdf(context)
                                    "JPEG Image (.jpg)" -> viewModel.exportImage(context)
                                    "PNG Image (.png)" -> viewModel.exportPng(context)
                                    "Plain Text (.txt)" -> viewModel.exportTxt(context)
                                    "Word Document (.rtf)" -> viewModel.exportDoc(context)
                                    "Share Text" -> viewModel.shareText(context)
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                        }
                        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Primary) }
            return@Scaffold
        }

        val doc = uiState.document ?: run {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { Text("Document not found") }
            return@Scaffold
        }

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
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    if (uiState.isEditingTitle) {
                        OutlinedTextField(
                            value = uiState.editTitle,
                            onValueChange = viewModel::updateTitle,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                IconButton(onClick = viewModel::commitTitleEdit) {
                                    Icon(Icons.Outlined.Check, null, tint = Primary)
                                }
                            },
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.startEditTitle() },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.Edit, null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "${doc.pageCount} page${if (doc.pageCount != 1) "s" else ""} · ${formatFileSize(doc.fileSizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Outlined.CloudDone, null, tint = Success, modifier = Modifier.size(12.dp))
                            Text("Saved", color = Success, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                IconButton(onClick = { viewModel.toggleStar() }) {
                    Icon(
                        if (doc.isStarred) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Star",
                        tint = if (doc.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showExportSheet = true }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {

                // Page preview — shows REAL scanned image
                item {
                    val pageBitmap = uiState.scannedBitmap
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .aspectRatio(
                                    if (pageBitmap != null)
                                        pageBitmap.width.toFloat() / pageBitmap.height.toFloat()
                                    else 0.72f
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (pageBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = pageBitmap.asImageBitmap(),
                                        contentDescription = "Scanned document",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                } else {
                                    // Fallback placeholder
                                    Column(
                                        modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(16.dp),
                                    ) {
                                        Box(Modifier.fillMaxWidth(0.5f).height(3.dp).background(Color(0xFF1E3A8A)))
                                        Spacer(Modifier.height(4.dp))
                                        repeat(8) {
                                            Spacer(Modifier.height(3.dp))
                                            Box(Modifier.fillMaxWidth(0.5f + (it % 3) * 0.15f).height(1.5.dp).background(Color(0xFF1E293B).copy(0.4f)))
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(0.6f), RoundedCornerShape(999.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        "${uiState.currentPage + 1} / ${doc.pageCount}",
                                        color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Page ${uiState.currentPage + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Page thumbnails strip
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Pages", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items((1..doc.pageCount).toList()) { pageNum ->
                                    val isActive = pageNum - 1 == uiState.currentPage
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { viewModel.setCurrentPage(pageNum - 1) },
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 78.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(2.dp, if (isActive) Primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                        ) {
                                            val thumbBitmap = uiState.scannedBitmap
                                            if (thumbBitmap != null && isActive) {
                                                androidx.compose.foundation.Image(
                                                    bitmap = thumbBitmap.asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop,
                                                )
                                            } else {
                                                Column(modifier = Modifier.padding(6.dp)) {
                                                    Box(Modifier.fillMaxWidth(0.7f).height(2.dp).background(Color(0xFF1E3A8A)))
                                                    Spacer(Modifier.height(2.dp))
                                                    repeat(4) {
                                                        Box(Modifier.fillMaxWidth(0.5f + (it % 3) * 0.15f).height(1.dp).background(Color(0xFF1E293B).copy(0.5f)))
                                                        Spacer(Modifier.height(2.dp))
                                                    }
                                                }
                                            }
                                        }
                                        Text(
                                            "$pageNum",
                                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                            color = if (isActive) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                }
                                item {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 78.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { onAddPage() },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                        }
                                        Text(
                                            "Add", fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Extracted text
                if (doc.extractedText.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Extracted Text", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = doc.extractedText.take(400) + if (doc.extractedText.length > 400) "…" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Metadata
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            MetaRow("Created", SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(doc.createdAt)))
                            MetaRow("Pages", doc.pageCount.toString())
                            MetaRow("Size", formatFileSize(doc.fileSizeBytes))
                            MetaRow("Type", doc.kind.replaceFirstChar { it.uppercase() })
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(
                    Triple(Icons.Outlined.TextSnippet, "OCR") { viewModel.shareText(context) },
                    Triple(Icons.Outlined.Edit, "Edit") { viewModel.startEditTitle() },
                    Triple(Icons.Outlined.RotateRight, "Rotate") {},
                    Triple(Icons.Outlined.Delete, "Delete") { showDeleteDialog = true },
                ).forEach { (icon, label, action) ->
                    BottomActionButton(
                        icon = icon,
                        label = label,
                        tint = if (label == "Delete") Danger else null,
                        modifier = Modifier.weight(1f),
                        onClick = action,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary)
                        .clickable { showExportSheet = true },
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Download, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Export", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionButton(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = tint ?: MaterialTheme.colorScheme.onSurface,
        )
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = tint ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1_048_576 -> "${bytes / 1024}KB"
    else -> String.format("%.1fMB", bytes / 1_048_576.0)
}
