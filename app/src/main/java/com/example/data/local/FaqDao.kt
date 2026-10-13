package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FaqItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FaqDao {
    @Query("SELECT * FROM faq_items ORDER BY id ASC")
    fun getAllFaqs(): Flow<List<FaqItem>>

    @Query("SELECT * FROM faq_items")
    suspend fun getAllFaqsList(): List<FaqItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: FaqItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faqs: List<FaqItem>)

    @Update
    suspend fun updateFaq(faq: FaqItem)

    @Delete
    suspend fun deleteFaq(faq: FaqItem)
}
