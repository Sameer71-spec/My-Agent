package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.automation.N8nAutomationEngine
import com.example.data.local.AppDatabase
import com.example.data.model.BotConfig
import com.example.data.model.InventoryItem
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var engine: N8nAutomationEngine

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        engine = N8nAutomationEngine(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WhatsApp AI Bot", appName)
    }

    @Test
    fun `test menu inquiry triggers inventory read tool`() = runTest {
        // Seed an inventory item
        db.inventoryDao().insertItem(
            InventoryItem(
                name = "Chicken Biryani",
                category = "Main Course",
                price = 380.0,
                stockQuantity = 20
            )
        )

        val config = BotConfig()
        val result = engine.processIncomingWhatsAppMessage("Can you show me the menu?", config)

        assertTrue(result.replyText.contains("Chicken Biryani"))
        assertEquals("Menu Query", result.intent)
        assertEquals("Google Sheets: Read Inventory", result.toolUsed)
    }

    @Test
    fun `test stock check for available item`() = runTest {
        db.inventoryDao().insertItem(
            InventoryItem(
                name = "Zinger Crunchy Burger",
                category = "Fast Food",
                price = 420.0,
                stockQuantity = 15
            )
        )

        val config = BotConfig()
        val result = engine.processIncomingWhatsAppMessage("Is Zinger Burger available in stock?", config)

        assertTrue(result.replyText.contains("available in stock"))
        assertTrue(result.replyText.contains("15"))
    }

    @Test
    fun `test complete order logs order and deducts stock`() = runTest {
        val initialStock = 10
        val itemId = db.inventoryDao().insertItem(
            InventoryItem(
                name = "Chicken Biryani",
                category = "Main Course",
                price = 380.0,
                stockQuantity = initialStock
            )
        )

        val config = BotConfig()
        val orderPrompt = "Order 2 Biryani for Ali, phone 03001234567, address House 12 Street 4"
        val result = engine.processIncomingWhatsAppMessage(orderPrompt, config)

        assertNotNull(result.orderCreated)
        assertEquals("Ali", result.orderCreated?.customerName)
        assertEquals("03001234567", result.orderCreated?.customerPhone)

        // Verify stock was deducted
        val updatedItem = db.inventoryDao().getItemById(itemId)
        assertEquals(8, updatedItem?.stockQuantity)

        // Verify order saved in database
        val allOrders = db.customerOrderDao().getOrderById(result.orderCreated!!.orderId)
        assertNotNull(allOrders)
    }
}
