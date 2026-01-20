package com.focusapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    
    @Insert
    suspend fun insertSession(session: SessionEntity): Long
    
    @Update
    suspend fun updateSession(session: SessionEntity)
    
    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?
    
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>
    
    @Query("""
        SELECT * FROM sessions 
        WHERE startTime >= :startDate AND startTime < :endDate 
        ORDER BY startTime DESC
    """)
    suspend fun getSessionsInRange(startDate: Long, endDate: Long): List<SessionEntity>
    
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
}
