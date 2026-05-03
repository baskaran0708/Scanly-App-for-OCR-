package com.app.ocrscanner.ui.screens.home

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.ocrscanner.data.local.DocumentEntity
import com.app.ocrscanner.ui.components.ScanlyChip
import com.app.ocrscanner.ui.components.ScanlyFab
import com.app.ocrscanner.ui.theme.Accent
import com.app.ocrscanner.ui.theme.AccentSoft
import com.app.ocrscanner.ui.theme.Primary
import com.app.ocrscanner.ui.theme.PrimaryContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    onImportReady: (Bitmap) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedNavItem by remember { mutableStateOf(0) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.processImportedFile(context, it, onImportReady) }
    }

    Scaffold(
        bottomBar = {
            ScanlyBottomNav(
                selectedIndex = selectedNavItem,
                onItemSelected = { selectedNavItem = it },
            )
        },
        floatingActionButton = {
            if (selectedNavItem == 0) {
                ScanlyFab(
                    onClick = onScan,
                    icon = Icons.Outlined.CameraAlt,
                    size = 64,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when (selectedNavItem) {
            1 -> DocsContent(
                documents = uiState.documents,
                selectedFilter = uiState.selectedFilter,
                onFilterSelect = viewModel::onFilterChange,
                onOpenDocument = onOpenDocument,
                onStar = { id, starred -> viewModel.onToggleStar(id, starred) },
                onScan = onScan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            2 -> SearchContent(
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::onSearchQueryChange,
                documents = uiState.documents,
                onOpenDocument = onOpenDocument,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> HomeContent(
                uiState = uiState,
                onScan = onScan,
                onOpenDocument = onOpenDocument,
                onFilterSelect = viewModel::onFilterChange,
                onToggleStar = { id, starred -> viewModel.onToggleStar(id, starred) },
                onImportPdf = { importLauncher.launch("*/*") },
                onOpenSettings = onOpenSettings,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onScan: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    onFilterSelect: (DocumentFilter) -> Unit,
    onToggleStar: (Long, Boolean) -> Unit,
    onImportPdf: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            HomeHeader(onSettingsClick = onOpenSettings)
        }

        item {
            HeroScanCard(onClick = onScan)
            Spacer(Modifier.height(24.dp))
        }

        item {
            QuickActions(onScan = onScan, onImportPdf = onImportPdf)
            Spacer(Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent Scans",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "See all",
                    color = Primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { },
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            FilterChips(
                selected = uiState.selectedFilter,
                onSelect = onFilterSelect,
            )
            Spacer(Modifier.height(16.dp))
        }

        if (uiState.documents.isEmpty() && !uiState.isLoading) {
            item { EmptyState(onScan = onScan) }
        } else {
            item {
                DocumentGrid(
                    documents = uiState.documents,
                    onDocumentClick = onOpenDocument,
                    onStar = onToggleStar,
                )
            }
        }
    }
}

@Composable
private fun DocsContent(
    documents: List<DocumentEntity>,
    selectedFilter: DocumentFilter,
    onFilterSelect: (DocumentFilter) -> Unit,
    onOpenDocument: (Long) -> Unit,
    onStar: (Long, Boolean) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "My Documents",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${documents.size} docs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FilterChips(selected = selectedFilter, onSelect = onFilterSelect)
        Spacer(Modifier.height(8.dp))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(onScan = onScan)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp)) {
                items(documents) { doc ->
                    DocumentListItem(
                        document = doc,
                        onClick = { onOpenDocument(doc.id) },
                        onStar = { onStar(doc.id, doc.isStarred) },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun DocumentListItem(
    document: DocumentEntity,
    onClick: () -> Unit,
    onStar: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(brush = documentThumbnailColor(document.kind)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Description, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${document.pageCount} page${if (document.pageCount != 1) "s" else ""} · ${formatDate(document.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (document.extractedText.isNotBlank()) {
                    Text(
                        text = document.extractedText.take(60),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onStar) {
                Icon(
                    imageVector = if (document.isStarred) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Star",
                    tint = if (document.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchContent(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    documents: List<DocumentEntity>,
    onOpenDocument: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                "Search",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search documents, text content…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = Primary,
                ),
            )
        }

        if (searchQuery.isNotBlank()) {
            Text(
                "${documents.size} results for \"$searchQuery\"",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (documents.isEmpty() && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No results found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Try different keywords",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Outlined.Search, null, tint = Primary.copy(0.3f), modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Search your documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Search by title or extracted text content",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 80.dp)) {
                items(documents) { doc ->
                    DocumentListItem(
                        document = doc,
                        onClick = { onOpenDocument(doc.id) },
                        onStar = {},
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Primary, Color(0xFF0E7490))),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "LM",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "LMI Scanly",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary,
            )
        }

        // Notification bell with badge
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp),
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, end = 10.dp)
                    .size(8.dp)
                    .background(Color(0xFF06B6D4), CircleShape),
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun HeroScanCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Primary, Color(0xFF0E7490)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan a document",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Capture, crop, extract text with AI — in seconds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Tap to scan",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@Composable
private fun QuickActions(onScan: () -> Unit, onImportPdf: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                icon = Icons.Outlined.CameraAlt,
                label = "Camera Scan",
                onClick = onScan,
                modifier = Modifier.weight(1f),
                tint = Primary,
                bg = PrimaryContainer,
            )
            QuickActionCard(
                icon = Icons.Outlined.FolderOpen,
                label = "Import PDF",
                onClick = onImportPdf,
                modifier = Modifier.weight(1f),
                tint = Color(0xFF0E7490),
                bg = AccentSoft,
            )
            QuickActionCard(
                icon = Icons.Outlined.TextSnippet,
                label = "OCR Text",
                onClick = onScan,
                modifier = Modifier.weight(1f),
                tint = Color(0xFF7C3AED),
                bg = Color(0xFFEDE9FE),
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color,
    bg: Color,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun FilterChips(selected: DocumentFilter, onSelect: (DocumentFilter) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DocumentFilter.values()) { filter ->
            ScanlyChip(
                text = filter.label,
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun DocumentGrid(
    documents: List<DocumentEntity>,
    onDocumentClick: (Long) -> Unit,
    onStar: (Long, Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val rows = documents.chunked(2)
        rows.forEach { rowDocs ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowDocs.forEach { doc ->
                    DocumentCard(
                        document = doc,
                        modifier = Modifier.weight(1f),
                        onClick = { onDocumentClick(doc.id) },
                        onStar = { onStar(doc.id, doc.isStarred) },
                    )
                }
                if (rowDocs.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DocumentCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onStar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.78f)
                    .background(brush = documentThumbnailColor(document.kind)),
                contentAlignment = Alignment.Center,
            ) {
                DocumentThumbnailIcon(kind = document.kind)

                IconButton(
                    onClick = onStar,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp),
                ) {
                    Icon(
                        imageVector = if (document.isStarred) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Star",
                        tint = if (document.isStarred) Color(0xFFF59E0B) else Color.White.copy(0.7f),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(0.65f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "${document.pageCount}p",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = formatDate(document.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DocumentThumbnailIcon(kind: String) {
    val icon = when (kind) {
        "lab" -> Icons.Outlined.Description
        "rx" -> Icons.Outlined.TextSnippet
        "imaging" -> Icons.Outlined.GridView
        "form" -> Icons.Outlined.Description
        else -> Icons.Outlined.Description
    }
    Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(0.5f), modifier = Modifier.size(40.dp))
}

private fun documentThumbnailColor(kind: String) = when (kind) {
    "lab" -> Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF)))
    "rx" -> Brush.linearGradient(listOf(Color(0xFF0E7490), Color(0xFF0369A1)))
    "imaging" -> Brush.linearGradient(listOf(Color(0xFF0B1428), Color(0xFF1E293B)))
    "form" -> Brush.linearGradient(listOf(Color(0xFF374151), Color(0xFF4B5563)))
    else -> Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF64748B)))
}

@Composable
private fun EmptyState(onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CameraAlt, null, tint = Primary, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No documents yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tap + to scan your first document",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScanlyBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        listOf(
            Pair(Icons.Outlined.Home, "Home"),
            Pair(Icons.Outlined.Description, "Docs"),
            Pair(Icons.Outlined.Search, "Search"),
        ).forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryContainer,
                ),
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "Today, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))}"
        diff < 172_800_000 -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
