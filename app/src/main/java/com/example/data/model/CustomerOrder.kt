package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

@Entity(tableName = "customer_orders")
data class CustomerOrder(
    @PrimaryKey
    val orderId: String, // e.g. "ORD-9482"
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val itemsSummary: String, // e.g. "2x Chicken Biryani, 1x Coke 500ml"
    val subtotal: Double,
    val deliveryFee: Double = 50.0,
    val totalPrice: Double,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val paymentMethod: String = "Cash on Delivery"
)
