package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerOrder
import com.example.data.model.FaqItem
import com.example.data.model.InventoryItem
import com.example.data.model.OrderStatus
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SheetsGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WhatsAppPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSheetsScreen(
    inventory: List<InventoryItem>,
    orders: List<CustomerOrder>,
    faqs: List<FaqItem>,
    onUpdateStock: (Long, Int) -> Unit,
    onToggleAvailability: (Long, Boolean) -> Unit,
    onAddItem: (String, String, Double, Int, String) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSheetTab by remember { mutableIntStateOf(0) } // 0: Inventory, 1: Orders, 2: FAQs
    var searchQuery by remember { mutableStateOf("") }
    var showAddItemDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Header imitating Google Sheets App
        Surface(
            color = SheetsGreen,
            contentColor = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Sheets Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Sheets Database",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Restaurant Live Backend • Synced with N8N Agent",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Sheet Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSheetTab,
            containerColor = Color.White,
            contentColor = SheetsGreen,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedSheetTab == 0,
                onClick = { selectedSheetTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inventory (${inventory.size})")
                    }
                }
            )
            Tab(
                selected = selectedSheetTab == 1,
                onClick = { selectedSheetTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Orders (${orders.size})")
                    }
                }
            )
            Tab(
                selected = selectedSheetTab == 2,
                onClick = { selectedSheetTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FAQs & Policy (${faqs.size})")
                    }
                }
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSheetTab) {
                0 -> InventorySheetTab(
                    inventory = inventory,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onUpdateStock = onUpdateStock,
                    onToggleAvailability = onToggleAvailability
                )
                1 -> OrdersSheetTab(
                    orders = orders,
                    onUpdateOrderStatus = onUpdateOrderStatus
                )
                2 -> FaqsSheetTab(faqs = faqs)
            }

            // FAB for adding new item when on Inventory tab
            if (selectedSheetTab == 0) {
                FloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("fab_add_inventory_item"),
                    containerColor = SheetsGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Dish")
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, cat, price, stock, unit ->
                onAddItem(name, cat, price, stock, unit)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun InventorySheetTab(
    inventory: List<InventoryItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onUpdateStock: (Long, Int) -> Unit,
    onToggleAvailability: (Long, Boolean) -> Unit
) {
    val filtered = inventory.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val lowStockCount = inventory.count { it.stockQuantity in 1..5 }
    val outOfStockCount = inventory.count { it.stockQuantity <= 0 || !it.isAvailable }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary metrics bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard("Total Items", "${inventory.size}", SheetsGreen, Modifier.weight(1f))
                MetricCard("Low Stock (<=5)", "$lowStockCount", WarningAmber, Modifier.weight(1f))
                MetricCard("Out of Stock", "$outOfStockCount", DangerRed, Modifier.weight(1f))
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_inventory"),
                placeholder = { Text("Search dishes in Google Sheet...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Inventory Items Table Cards
        items(filtered) { item ->
            InventoryItemRowCard(
                item = item,
                onUpdateStock = { newQty -> onUpdateStock(item.id, newQty) },
                onToggleAvailability = { onToggleAvailability(item.id, !item.isAvailable) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun InventoryItemRowCard(
    item: InventoryItem,
    onUpdateStock: (Int) -> Unit,
    onToggleAvailability: () -> Unit
) {
    val isOut = item.stockQuantity <= 0 || !item.isAvailable
    val isLow = item.stockQuantity in 1..5

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when {
                            isOut -> DangerRed.copy(alpha = 0.12f)
                            isLow -> WarningAmber.copy(alpha = 0.15f)
                            else -> SheetsGreen.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when {
                                isOut -> "OUT OF STOCK"
                                isLow -> "LOW STOCK"
                                else -> "IN STOCK"
                            },
                            color = when {
                                isOut -> DangerRed
                                isLow -> WarningAmber
                                else -> SheetsGreen
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(text = " • ", color = Color.Gray)
                    Text(
                        text = "Rs. ${item.price.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = WhatsAppPrimary,
                        fontSize = 13.sp
                    )
                }

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Stock Modifier (+ / -)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onUpdateStock(item.stockQuantity - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Stock",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "${item.stockQuantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isOut) DangerRed else Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = { onUpdateStock(item.stockQuantity + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Stock",
                        tint = WhatsAppPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrdersSheetTab(
    orders: List<CustomerOrder>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit
) {
    val totalRevenue = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalPrice }
    val timeFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No orders logged in Sheets yet",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Text(
                    text = "Chat with the WhatsApp Bot to place test orders!",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard("Total Orders", "${orders.size}", SheetsGreen, Modifier.weight(1f))
                    MetricCard("Total Revenue", "Rs. ${totalRevenue.toInt()}", WhatsAppPrimary, Modifier.weight(1f))
                }
            }

            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_${order.orderId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.orderId,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppPrimary
                            )
                            Text(
                                text = timeFormat.format(Date(order.timestamp)),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "👤 ${order.customerName} • 📱 ${order.customerPhone}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "📍 ${order.deliveryAddress}",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🛒 Items: ${order.itemsSummary}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp),
                                color = Color(0xFF374151)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total: Rs. ${order.totalPrice.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = WhatsAppPrimary
                            )

                            // Status selection buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (order.status == OrderStatus.CONFIRMED) {
                                    Button(
                                        onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.PREPARING) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Prepare", fontSize = 11.sp)
                                    }
                                }
                                if (order.status == OrderStatus.PREPARING) {
                                    Button(
                                        onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.OUT_FOR_DELIVERY) },
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Dispatch", fontSize = 11.sp)
                                    }
                                }
                                if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                                    Button(
                                        onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.DELIVERED) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SheetsGreen),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Delivered", fontSize = 11.sp)
                                    }
                                }
                                if (order.status == OrderStatus.DELIVERED) {
                                    Surface(
                                        color = SheetsGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Completed",
                                            color = SheetsGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqsSheetTab(faqs: List<FaqItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(faqs) { faq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = faq.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = faq.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, cat: String, price: Double, stock: Int, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Main Course") }
    var priceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("plate") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Dish to Google Sheet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Main Course, Fast Food)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price (Rs.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 100.0
                    val stock = stockStr.toIntOrNull() ?: 10
                    if (name.isNotBlank()) {
                        onConfirm(name, category, price, stock, unit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SheetsGreen)
            ) {
                Text("Append to Sheet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
