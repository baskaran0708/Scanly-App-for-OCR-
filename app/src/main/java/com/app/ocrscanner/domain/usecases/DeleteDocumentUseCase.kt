package com.app.ocrscanner.domain.usecases

import com.app.ocrscanner.data.repository.DocumentRepository
import java.io.File
import javax.inject.Inject

class DeleteDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(id: Long, filePath: String) {
        repository.deleteDocument(id)
        val file = File(filePath)
        if (file.exists()) file.delete()
    }
}
