package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NodeStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    WARNING,
    ERROR
}

data class N8nNodeConfig(
    val id: String,
    val name: String,
    val type: String,
    val iconName: String,
    val description: String,
    val status: NodeStatus = NodeStatus.IDLE,
    val parameterSummary: String = "",
    val lastInputSnippet: String = "",
    val lastOutputSnippet: String = "",
    val executionCount: Int = 0
)

@Entity(tableName = "n8n_execution_traces")
data class N8nExecutionTrace(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val traceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val incomingMessage: String,
    val nodesTriggered: String, // comma-separated list of nodes
    val toolsExecuted: String,
    val memoryContextUsed: String,
    val rawGeminiPromptOrReasoning: String,
    val botResponse: String,
    val durationMs: Long,
    val success: Boolean = true
)
