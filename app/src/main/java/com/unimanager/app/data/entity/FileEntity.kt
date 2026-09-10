package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val extension: String,
    val type: String, // pdf, doc, img, video, audio, archive, code, data, other
    val mimeType: String,
    val size: Long,
    val folderId: Long?,
    val filePath: String, // actual file path on device
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
