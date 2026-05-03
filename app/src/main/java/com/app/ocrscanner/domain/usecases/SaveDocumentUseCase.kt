package com.app.ocrscanner.domain.usecases

import com.app.ocrscanner.data.local.DocumentEntity
import com.app.ocrscanner.data.repository.DocumentRepository
import javax.inject.Inject

class SaveDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(
        title: String,
        filePath: String,
        extractedText: String,
        pageCount: Int,
        fileSizeBytes: Long,
        kind: String = "doc",
    ): Long {
        val now = System.currentTimeMillis()
        val entity = DocumentEntity(
            title = title,
            filePath = filePath,
            extractedText = extractedText,
            pageCount = pageCount,
            fileSizeBytes = fileSizeBytes,
            kind = kind,
            createdAt = now,
            updatedAt = now,
        )
        return repository.saveDocument(entity)
    }
}
