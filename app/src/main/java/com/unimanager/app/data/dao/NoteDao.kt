package com.unimanager.app.data.dao

import androidx.room.*
import com.unimanager.app.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllNotesSync(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Insert
    suspend fun insertSync(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM notes")
    fun getNoteCount(): Flow<Int>
}
