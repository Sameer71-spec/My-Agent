package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.READ,
    val senderName: String = if (isFromUser) "Customer" else "SpiceCraft AI",
    val intentDetected: String? = null,
    val toolUsed: String? = null, // e.g. "Read Google Sheet (Inventory)", "Append Row (Orders)"
    val reasoningLog: String? = null
)
