package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY category ASC, name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items")
    suspend fun getAllItemsList(): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE isAvailable = 1 AND stockQuantity > 0")
    suspend fun getAvailableItems(): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findItemByName(query: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET stockQuantity = :newQuantity WHERE id = :id")
    suspend fun updateStock(id: Long, newQuantity: Int)

    @Query("UPDATE inventory_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: Long, isAvailable: Boolean)
}
