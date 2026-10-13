package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Main Course", "Fast Food", "Drinks", "Desserts", "Sides"
    val price: Double,
    val stockQuantity: Int,
    val unit: String = "serving", // "serving", "box", "can", "plate"
    val isAvailable: Boolean = true,
    val description: String = ""
)
