package com.app.ocrscanner.data.repository

import com.app.ocrscanner.data.local.DocumentDao
import com.app.ocrscanner.data.local.DocumentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
) {
    fun getAllDocuments(): Flow<List<DocumentEntity>> =
        documentDao.getAllDocuments()

    fun searchDocuments(query: String): Flow<List<DocumentEntity>> =
        documentDao.searchDocuments(query)

    suspend fun getDocumentById(id: Long): DocumentEntity? =
        documentDao.getDocumentById(id)

    suspend fun saveDocument(document: DocumentEntity): Long =
        documentDao.insertDocument(document)

    suspend fun updateDocument(document: DocumentEntity) =
        documentDao.updateDocument(document)

    suspend fun deleteDocument(id: Long) =
        documentDao.deleteDocumentById(id)

    suspend fun setStarred(id: Long, starred: Boolean) =
        documentDao.setStarred(id, starred)
}
