package com.unimanager.app.data.dao

import androidx.room.*
import com.unimanager.app.data.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDate ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY examDate ASC")
    suspend fun getAllExamsSync(): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE examDate >= date('now') ORDER BY examDate ASC")
    fun getUpcomingExams(): Flow<List<ExamEntity>>

    @Insert
    suspend fun insert(exam: ExamEntity): Long

    @Insert
    suspend fun insertSync(exam: ExamEntity): Long

    @Update
    suspend fun update(exam: ExamEntity)

    @Delete
    suspend fun delete(exam: ExamEntity)

    @Query("DELETE FROM exams")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM exams WHERE examDate >= date('now')")
    fun getUpcomingExamCount(): Flow<Int>
}
