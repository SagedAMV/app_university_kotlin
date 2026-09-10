package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val type: String, // نصفي, نهائي, فجائي, عملي
    val examDate: String,
    val time: String = "",
    val room: String = "",
    val notes: String = ""
)
