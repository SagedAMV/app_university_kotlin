package com.unimanager.app.data.dao

import androidx.room.*
import com.unimanager.app.data.entity.LectureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureDao {
    @Query("SELECT * FROM lectures ORDER BY day, timeFrom")
    fun getAllLectures(): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE day = :day ORDER BY timeFrom")
    fun getLecturesByDay(day: String): Flow<List<LectureEntity>>

    @Query("SELECT * FROM lectures WHERE id = :id")
    fun getLectureById(id: Long): Flow<LectureEntity?>

    @Insert
    suspend fun insert(lecture: LectureEntity): Long

    @Update
    suspend fun update(lecture: LectureEntity)

    @Delete
    suspend fun delete(lecture: LectureEntity)

    @Query("DELETE FROM lectures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM lectures")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lectures")
    fun getLectureCount(): Flow<Int>
}
