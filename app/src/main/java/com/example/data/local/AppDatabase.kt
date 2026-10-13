package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.CustomerOrder
import com.example.data.model.FaqItem
import com.example.data.model.InventoryItem
import com.example.data.model.MessageStatus
import com.example.data.model.N8nExecutionTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InventoryItem::class,
        CustomerOrder::class,
        FaqItem::class,
        ChatMessage::class,
        N8nExecutionTrace::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun customerOrderDao(): CustomerOrderDao
    abstract fun faqDao(): FaqDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun executionTraceDao(): ExecutionTraceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whatsapp_bot_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed Inventory (Google Sheet: Inventory tab)
            val initialInventory = listOf(
                InventoryItem(
                    name = "Chicken Biryani (Special)",
                    category = "Main Course",
                    price = 380.0,
                    stockQuantity = 24,
                    unit = "single plate",
                    isAvailable = true,
                    description = "Aromatic basmati rice cooked with tender marinated chicken & potatoes"
                ),
                InventoryItem(
                    name = "Mutton Dum Biryani",
                    category = "Main Course",
                    price = 650.0,
                    stockQuantity = 12,
                    unit = "plate",
                    isAvailable = true,
                    description = "Slow cooked royal mutton biryani with caramelized onions & saffron"
                ),
                InventoryItem(
                    name = "Zinger Crunchy Burger",
                    category = "Fast Food",
                    price = 420.0,
                    stockQuantity = 18,
                    unit = "burger",
                    isAvailable = true,
                    description = "Crispy spicy chicken fillet with lettuce & secret mayo in sesame bun"
                ),
                InventoryItem(
                    name = "BBQ Chicken Pizza (Medium)",
                    category = "Fast Food",
                    price = 950.0,
                    stockQuantity = 8,
                    unit = "pizza",
                    isAvailable = true,
                    description = "Smoked BBQ chicken chunks, mozzarella cheese, bell peppers & onions"
                ),
                InventoryItem(
                    name = "Beef Seekh Kabab Roll",
                    category = "Fast Food",
                    price = 280.0,
                    stockQuantity = 15,
                    unit = "roll",
                    isAvailable = true,
                    description = "Charcoal grilled juicy beef seekh kabab wrapped in crispy paratha"
                ),
                InventoryItem(
                    name = "Mint Raita & Salad Cup",
                    category = "Sides",
                    price = 60.0,
                    stockQuantity = 40,
                    unit = "cup",
                    isAvailable = true,
                    description = "Fresh coriander mint yogurt raita with crunchy onion cucumber salad"
                ),
                InventoryItem(
                    name = "Cold Drink (Coke / Sprite 500ml)",
                    category = "Drinks",
                    price = 110.0,
                    stockQuantity = 35,
                    unit = "bottle",
                    isAvailable = true,
                    description = "Chilled 500ml Coca-Cola or Sprite"
                ),
                InventoryItem(
                    name = "Gulab Jamun (2 Pcs)",
                    category = "Desserts",
                    price = 160.0,
                    stockQuantity = 10,
                    unit = "box",
                    isAvailable = true,
                    description = "Warm soft milk dumplings soaked in cardamom sugar syrup"
                )
            )
            db.inventoryDao().insertAll(initialInventory)

            // Seed FAQs (Google Sheet: FAQs tab)
            val initialFaqs = listOf(
                FaqItem(
                    question = "What are your business and delivery hours?",
                    answer = "We are open daily from 12:00 PM to 12:00 AM (Midnight).",
                    category = "Timings"
                ),
                FaqItem(
                    question = "What is the delivery fee and minimum order?",
                    answer = "Standard delivery fee is Rs. 50 flat across the city. Minimum order value is Rs. 200.",
                    category = "Delivery"
                ),
                FaqItem(
                    question = "What payment methods do you accept?",
                    answer = "We accept Cash on Delivery (COD), JazzCash, EasyPaisa, and online Bank Transfer.",
                    category = "Payment"
                ),
                FaqItem(
                    question = "How long does delivery usually take?",
                    answer = "Normal delivery time is 35 to 45 minutes depending on distance and rush hours.",
                    category = "Delivery"
                )
            )
            db.faqDao().insertAll(initialFaqs)

            // Initial Welcome Message from the WhatsApp Bot
            val welcomeMessage = ChatMessage(
                text = "Assalam-o-Alaikum! 🌟 Welcome to *SpiceCraft Kitchen* WhatsApp Order Assistant.\n\nI can help you check our live menu, confirm food availability, and place your order directly. How may I help you today?",
                isFromUser = false,
                status = MessageStatus.READ,
                senderName = "SpiceCraft AI"
            )
            db.chatMessageDao().insertMessage(welcomeMessage)
        }
    }
}
