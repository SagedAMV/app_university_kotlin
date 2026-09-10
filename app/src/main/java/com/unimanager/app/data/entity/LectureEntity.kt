package com.unimanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lectures")
data class LectureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val doctor: String = "",
    val day: String,
    val timeFrom: String,
    val timeTo: String = "",
    val room: String = ""
)
