package com.app.ocrscanner.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.ocrscanner.data.local.DocumentEntity
import com.app.ocrscanner.data.repository.DocumentRepository
import com.app.ocrscanner.domain.usecases.DeleteDocumentUseCase
import com.app.ocrscanner.domain.usecases.GetDocumentsUseCase
import com.app.ocrscanner.pdf.PdfProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val documents: List<DocumentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: DocumentFilter = DocumentFilter.ALL,
)

enum class DocumentFilter(val label: String) {
    ALL("All"),
    STARRED("Starred"),
    RECENT("Recent"),
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val repository: DocumentRepository,
    private val pdfProcessor: PdfProcessor,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(DocumentFilter.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) getDocumentsUseCase()
                else getDocumentsUseCase.search(query)
            },
        _searchQuery,
        _filter,
    ) { docs, query, filter ->
        val filtered = when (filter) {
            DocumentFilter.ALL -> docs
            DocumentFilter.STARRED -> docs.filter { it.isStarred }
            DocumentFilter.RECENT -> docs.take(10)
        }
        HomeUiState(documents = filtered, searchQuery = query, selectedFilter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: DocumentFilter) {
        _filter.value = filter
    }

    fun onDeleteDocument(id: Long, filePath: String) {
        viewModelScope.launch {
            deleteDocumentUseCase(id, filePath)
        }
    }

    fun onToggleStar(id: Long, currentlyStarred: Boolean) {
        viewModelScope.launch {
            repository.setStarred(id, !currentlyStarred)
        }
    }

    fun processImportedFile(context: Context, uri: Uri, onReady: (Bitmap) -> Unit) {
        viewModelScope.launch {
            runCatching {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType == "application/pdf") {
                    pdfProcessor.pdfToBitmaps(context, uri).firstOrNull()
                        ?: throw IllegalStateException("Empty or unreadable PDF")
                } else {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        } ?: throw IllegalStateException("Cannot open image file")
                    }
                }
            }.onSuccess { bitmap -> onReady(bitmap) }
        }
    }
}
