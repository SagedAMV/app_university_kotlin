package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "files",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["name"]),
        Index(value = ["folderId"]),
        Index(value = ["createdAt"]),
        Index(value = ["isFavorite"])
    ]
)
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
