package com.app.ocrscanner.domain.usecases

import com.app.ocrscanner.data.local.DocumentEntity
import com.app.ocrscanner.data.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    operator fun invoke(): Flow<List<DocumentEntity>> = repository.getAllDocuments()

    fun search(query: String): Flow<List<DocumentEntity>> = repository.searchDocuments(query)
}
