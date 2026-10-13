package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.GoogleSheetsScreen
import com.example.ui.screens.N8nWorkflowScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WhatsAppChatScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.N8nOrange
import com.example.ui.theme.SheetsGreen
import com.example.ui.theme.WhatsAppPrimary
import com.example.ui.theme.WhatsAppPrimaryDark

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isBotTyping by viewModel.isBotTyping.collectAsStateWithLifecycle()
    val inventory by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val faqs by viewModel.faqs.collectAsStateWithLifecycle()
    val traces by viewModel.traces.collectAsStateWithLifecycle()
    val botConfig by viewModel.botConfig.collectAsStateWithLifecycle()

    // Handle system back navigation to return to WhatsApp tab
    BackHandler(enabled = currentTab != 0) {
        viewModel.setTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp Bot") },
                    label = { Text("WhatsApp", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppPrimary,
                        selectedTextColor = WhatsAppPrimary,
                        indicatorColor = WhatsAppPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_whatsapp")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Google Sheets") },
                    label = { Text("Sheets DB", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SheetsGreen,
                        selectedTextColor = SheetsGreen,
                        indicatorColor = SheetsGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_sheets")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = "N8N Workflow") },
                    label = { Text("N8N Pipeline", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = N8nOrange,
                        selectedTextColor = N8nOrange,
                        indicatorColor = N8nOrange.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_n8n")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Meta & AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppPrimaryDark,
                        selectedTextColor = WhatsAppPrimaryDark,
                        indicatorColor = WhatsAppPrimaryDark.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            0 -> WhatsAppChatScreen(
                messages = messages,
                isBotTyping = isBotTyping,
                botConfig = botConfig,
                onSendMessage = { text -> viewModel.sendCustomerMessage(text) },
                onClearChat = { viewModel.clearChat() },
                onViewWorkflowTrace = { viewModel.setTab(2) },
                onViewSheets = { viewModel.setTab(1) },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> GoogleSheetsScreen(
                inventory = inventory,
                orders = orders,
                faqs = faqs,
                onUpdateStock = { id, qty -> viewModel.updateStock(id, qty) },
                onToggleAvailability = { id, avail -> viewModel.toggleAvailability(id, avail) },
                onAddItem = { name, cat, price, stock, unit ->
                    viewModel.addInventoryItem(name, cat, price, stock, unit)
                },
                onUpdateOrderStatus = { id, status -> viewModel.updateOrderStatus(id, status) },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> N8nWorkflowScreen(
                traces = traces,
                onTestWebhook = { viewModel.setTab(3) },
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SettingsScreen(
                botConfig = botConfig,
                onSaveConfig = { updated -> viewModel.updateBotConfig(updated) },
                onSimulateWebhook = { phone, msg ->
                    viewModel.simulateMetaWebhook(phone, msg)
                    viewModel.setTab(0)
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
