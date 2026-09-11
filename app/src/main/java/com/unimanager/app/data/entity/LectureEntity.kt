package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lectures",
    indices = [
        Index(value = ["day"]),
        Index(value = ["timeFrom"])
    ]
)
data class LectureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val doctor: String = "",
    val day: String,
    val timeFrom: String,
    val timeTo: String = "",
    val room: String = ""
)
