package com.example.data.model

data class BotConfig(
    val restaurantName: String = "SpiceCraft Kitchen",
    val phoneFormatted: String = "+92 300 1234567",
    val metaPhoneNumberId: String = "109823485729103",
    val metaWabaId: String = "982736152849102",
    val n8nWebhookUrl: String = "https://n8n.cloud.instance/webhook/whatsapp-restaurant",
    val verifyToken: String = "spicecraft_meta_verify_2026",
    val currency: String = "Rs.",
    val deliveryFee: Double = 50.0,
    val minOrderAmount: Double = 200.0,
    val memoryWindowSize: Int = 10,
    val languageMode: String = "Multilingual (English/Urdu/Roman Urdu)",
    val systemPrompt: String = """You are the official WhatsApp AI Automation Bot for SpiceCraft Kitchen.
Your main job is to help customers order food, check menu stock, and answer queries.

CRITICAL RULES:
1. Always be polite, warm, and concise. You are chatting on WhatsApp.
2. Check real-time stock using the Google Sheets Inventory tool before taking or confirming any order.
3. NEVER confirm an order for an item that is Out of Stock or if requested quantity exceeds available stock.
4. To place an order, you MUST collect:
   - Customer's full name
   - Contact phone number
   - Delivery address
   - Exact dish names and quantities
5. When all details are gathered and stock is confirmed:
   - Summarize items with their prices.
   - Add delivery fee ($50 / Rs. 50).
   - Generate and confirm a unique Order ID (e.g. ORD-XXXX).
   - Tell the customer estimated delivery time (35-45 minutes).
6. Support English, Urdu, and Roman Urdu seamlessly."""
)
