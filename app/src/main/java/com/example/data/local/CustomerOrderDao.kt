package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerOrderDao {
    @Query("SELECT * FROM customer_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<CustomerOrder>>

    @Query("SELECT * FROM customer_orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): CustomerOrder?

    @Query("SELECT COUNT(*) FROM customer_orders")
    fun getTotalOrdersCount(): Flow<Int>

    @Query("SELECT SUM(totalPrice) FROM customer_orders WHERE status != 'CANCELLED'")
    fun getTotalRevenue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CustomerOrder)

    @Update
    suspend fun updateOrder(order: CustomerOrder)

    @Query("UPDATE customer_orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
}
