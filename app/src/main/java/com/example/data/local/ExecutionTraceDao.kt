package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.N8nExecutionTrace
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionTraceDao {
    @Query("SELECT * FROM n8n_execution_traces ORDER BY timestamp DESC")
    fun getAllTraces(): Flow<List<N8nExecutionTrace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrace(trace: N8nExecutionTrace): Long

    @Query("DELETE FROM n8n_execution_traces")
    suspend fun clearAll()
}
