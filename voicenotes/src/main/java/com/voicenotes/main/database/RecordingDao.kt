package com.voicenotes.main.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>
    
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    suspend fun getAllRecordingsList(): List<Recording>
    
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordingsLiveData(): LiveData<List<Recording>>
    
    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): Recording?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordings(recordings: List<Recording>)
    
    @Update
    suspend fun updateRecording(recording: Recording)
    
    @Delete
    suspend fun deleteRecording(recording: Recording)
    
    @Query("SELECT COUNT(*) FROM recordings")
    suspend fun getRecordingCount(): Int
}
