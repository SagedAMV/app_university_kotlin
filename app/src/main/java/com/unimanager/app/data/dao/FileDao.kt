package com.unimanager.app.data.dao

import androidx.room.*
import com.unimanager.app.data.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files ORDER BY createdAt DESC")
    suspend fun getAllFilesSync(): List<FileEntity>

    @Query("SELECT * FROM files WHERE folderId = :folderId ORDER BY name")
    fun getFilesInFolder(folderId: Long?): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :id")
    fun getFileById(id: Long): Flow<FileEntity?>

    @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%' OR extension LIKE '%' || :query || '%'")
    fun searchFiles(query: String): Flow<List<FileEntity>>

    @Insert
    suspend fun insert(file: FileEntity): Long

    @Insert
    suspend fun insertSync(file: FileEntity): Long

    @Update
    suspend fun update(file: FileEntity)

    @Delete
    suspend fun delete(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM files")
    suspend fun deleteAll()

    @Query("UPDATE files SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM files")
    fun getFileCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(size), 0) FROM files")
    fun getTotalSize(): Flow<Long>
}
