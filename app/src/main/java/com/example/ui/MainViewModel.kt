package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.automation.N8nAutomationEngine
import com.example.data.local.AppDatabase
import com.example.data.model.BotConfig
import com.example.data.model.ChatMessage
import com.example.data.model.CustomerOrder
import com.example.data.model.FaqItem
import com.example.data.model.InventoryItem
import com.example.data.model.MessageStatus
import com.example.data.model.N8nExecutionTrace
import com.example.data.model.OrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val engine = N8nAutomationEngine(database)

    val chatMessages: StateFlow<List<ChatMessage>> = database.chatMessageDao()
        .getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItem>> = database.inventoryDao()
        .getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<CustomerOrder>> = database.customerOrderDao()
        .getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faqs: StateFlow<List<FaqItem>> = database.faqDao()
        .getAllFaqs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val traces: StateFlow<List<N8nExecutionTrace>> = database.executionTraceDao()
        .getAllTraces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _botConfig = MutableStateFlow(BotConfig())
    val botConfig: StateFlow<BotConfig> = _botConfig.asStateFlow()

    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping.asStateFlow()

    private val _selectedTrace = MutableStateFlow<N8nExecutionTrace?>(null)
    val selectedTrace: StateFlow<N8nExecutionTrace?> = _selectedTrace.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun selectTraceForInspection(trace: N8nExecutionTrace?) {
        _selectedTrace.value = trace
    }

    fun sendCustomerMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val userMsg = ChatMessage(
                text = text.trim(),
                isFromUser = true,
                status = MessageStatus.READ,
                senderName = "Customer"
            )
            database.chatMessageDao().insertMessage(userMsg)

            _isBotTyping.value = true

            // Simulate slight WhatsApp network latency for authenticity
            kotlinx.coroutines.delay(650)

            val result = engine.processIncomingWhatsAppMessage(text.trim(), _botConfig.value)

            val botMsg = ChatMessage(
                text = result.replyText,
                isFromUser = false,
                status = MessageStatus.READ,
                senderName = _botConfig.value.restaurantName,
                intentDetected = result.intent,
                toolUsed = result.toolUsed,
                reasoningLog = result.executionTrace.nodesTriggered
            )
            database.chatMessageDao().insertMessage(botMsg)

            _isBotTyping.value = false
        }
    }

    fun simulateMetaWebhook(senderPhone: String, messageText: String) {
        sendCustomerMessage(messageText)
    }

    fun updateStock(id: Long, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            database.inventoryDao().updateStock(id, newQuantity.coerceAtLeast(0))
        }
    }

    fun toggleAvailability(id: Long, isAvailable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.inventoryDao().updateAvailability(id, isAvailable)
        }
    }

    fun addInventoryItem(name: String, category: String, price: Double, stock: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = InventoryItem(
                name = name,
                category = category,
                price = price,
                stockQuantity = stock,
                unit = unit,
                isAvailable = true
            )
            database.inventoryDao().insertItem(item)
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            database.customerOrderDao().updateOrderStatus(orderId, status)
        }
    }

    fun updateBotConfig(newConfig: BotConfig) {
        _botConfig.value = newConfig
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            database.chatMessageDao().clearHistory()
            val welcomeMessage = ChatMessage(
                text = "Chat cleared. Hello! 🌟 I'm *${_botConfig.value.restaurantName}* AI Bot. What would you like to order today?",
                isFromUser = false,
                status = MessageStatus.READ,
                senderName = _botConfig.value.restaurantName
            )
            database.chatMessageDao().insertMessage(welcomeMessage)
        }
    }
}
