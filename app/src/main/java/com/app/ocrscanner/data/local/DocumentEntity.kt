package com.app.ocrscanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val extractedText: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isStarred: Boolean = false,
    val kind: String = "doc", // lab | rx | imaging | form | doc
)
