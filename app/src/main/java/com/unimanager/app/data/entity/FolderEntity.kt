package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: String = "#6366f1",
    val parentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
