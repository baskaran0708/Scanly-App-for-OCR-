package com.app.ocrscanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR extractedText LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("UPDATE documents SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int
}
