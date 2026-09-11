package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isDone"]),
        Index(value = ["priority"]),
        Index(value = ["dueDate"]),
        Index(value = ["createdAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "medium", // high, medium, low
    val dueDate: String? = null,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
