package com.example.data.automation

import com.example.data.local.AppDatabase
import com.example.data.model.BotConfig
import com.example.data.model.ChatMessage
import com.example.data.model.CustomerOrder
import com.example.data.model.FaqItem
import com.example.data.model.InventoryItem
import com.example.data.model.MessageStatus
import com.example.data.model.N8nExecutionTrace
import com.example.data.model.OrderStatus
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

data class AutomationResult(
    val replyText: String,
    val toolUsed: String?,
    val intent: String,
    val orderCreated: CustomerOrder? = null,
    val executionTrace: N8nExecutionTrace
)

class N8nAutomationEngine(
    private val database: AppDatabase
) {

    suspend fun processIncomingWhatsAppMessage(
        incomingText: String,
        config: BotConfig
    ): AutomationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val traceId = "EXEC-" + UUID.randomUUID().toString().substring(0, 8).uppercase(Locale.ROOT)

        val triggeredNodes = mutableListOf<String>()
        val toolsExecuted = mutableListOf<String>()

        // 1. WhatsApp Webhook Trigger Node
        triggeredNodes.add("WhatsApp Trigger Webhook")

        // 2. Simple Memory Node (fetch last 10 messages)
        triggeredNodes.add("Simple Memory (Window: ${config.memoryWindowSize})")
        val recentMessages = database.chatMessageDao().getRecentMessages(config.memoryWindowSize).reversed()
        val historyTurns = recentMessages.map {
            (if (it.isFromUser) "user" else "model") to it.text
        }

        // 3. Read Google Sheets (Inventory + FAQs)
        triggeredNodes.add("Google Sheets: Read Inventory")
        val inventory = database.inventoryDao().getAllItemsList()
        val faqs = database.faqDao().getAllFaqsList()
        toolsExecuted.add("read_inventory_sheet (${inventory.size} items)")

        // Build Live Sheet context for AI
        val inventoryContext = buildString {
            appendLine("=== CURRENT LIVE GOOGLE SHEET: INVENTORY ===")
            inventory.forEach { item ->
                val status = if (!item.isAvailable || item.stockQuantity <= 0) "OUT OF STOCK" else "IN STOCK (${item.stockQuantity} left)"
                appendLine("- [ID: ${item.id}] ${item.name} | Category: ${item.category} | Price: Rs. ${item.price.toInt()} | Status: $status")
            }
            appendLine("\n=== LIVE GOOGLE SHEET: FAQS & POLICIES ===")
            faqs.forEach { faq ->
                appendLine("Q: ${faq.question} -> A: ${faq.answer}")
            }
            appendLine("Delivery Fee: Rs. ${config.deliveryFee.toInt()} | Min Order: Rs. ${config.minOrderAmount.toInt()}")
        }

        // 4. AI Agent Node (Gemini 3.5 Flash or Fallback Brain)
        triggeredNodes.add("Gemini AI Agent")

        var replyText = ""
        var intent = "General Inquiry"
        var createdOrder: CustomerOrder? = null
        var toolUsedName: String? = null
        var reasoningText = ""

        if (GeminiClient.isApiKeyConfigured()) {
            val fullSystemPrompt = """
${config.systemPrompt}

CURRENT DATABASE STATE:
$inventoryContext

INSTRUCTIONS FOR DETECTING ORDERS:
If the user is trying to place an order, make sure you collect:
1. Customer Name
2. Phone Number
3. Delivery Address
4. Food items and quantities

If all 4 are present and all items are in stock, append this exact JSON tag at the very end of your response:
<<<ORDER_JSON>>>
{
  "customerName": "...",
  "phone": "...",
  "address": "...",
  "items": [{"name": "exact dish name", "qty": 2}],
  "subtotal": 760,
  "deliveryFee": 50,
  "total": 810
}
<<<END_ORDER_JSON>>>

If any requested item is OUT OF STOCK, clearly state it and do NOT output the ORDER_JSON.
Never order items that are out of stock.
            """.trimIndent()

            val geminiResult = GeminiClient.generateContent(
                systemInstruction = fullSystemPrompt,
                conversationHistory = historyTurns,
                userMessage = incomingText
            )

            if (geminiResult.isSuccess) {
                val rawResponse = geminiResult.getOrNull() ?: ""
                reasoningText = "Gemini Flash reasoning completed."

                // Check for order JSON block
                if (rawResponse.contains("<<<ORDER_JSON>>>")) {
                    val parts = rawResponse.split("<<<ORDER_JSON>>>")
                    val userFacingText = parts[0].trim()
                    val jsonChunk = parts[1].substringBefore("<<<END_ORDER_JSON>>>").trim()

                    try {
                        val parsedOrder = parseAndExecuteOrder(jsonChunk, inventory, config)
                        if (parsedOrder != null) {
                            createdOrder = parsedOrder
                            replyText = userFacingText
                            intent = "Order Placed"
                            toolUsedName = "Google Sheets: Append Row (Orders)"
                            toolsExecuted.add("append_order_sheet (${parsedOrder.orderId})")
                            triggeredNodes.add("Google Sheets: Append Row")
                        } else {
                            replyText = userFacingText
                            intent = "Order Attempted"
                        }
                    } catch (e: Exception) {
                        replyText = userFacingText
                    }
                } else {
                    replyText = rawResponse
                    intent = detectIntentFromText(incomingText)
                }
            } else {
                // Fallback to local intelligent rule engine
                val fallback = executeLocalRuleEngine(incomingText, inventory, faqs, config, historyTurns)
                replyText = fallback.reply
                intent = fallback.intent
                createdOrder = fallback.order
                toolUsedName = fallback.toolUsed
                reasoningText = "Local Automation Rule Engine evaluated prompt."
                if (createdOrder != null) {
                    toolsExecuted.add("append_order_sheet (${createdOrder.orderId})")
                    triggeredNodes.add("Google Sheets: Append Row")
                }
            }
        } else {
            // Local intelligent rule engine (guarantees offline functionality & seamless testing)
            val fallback = executeLocalRuleEngine(incomingText, inventory, faqs, config, historyTurns)
            replyText = fallback.reply
            intent = fallback.intent
            createdOrder = fallback.order
            toolUsedName = fallback.toolUsed
            reasoningText = "Executed local N8N simulation agent with live inventory matching."
            if (createdOrder != null) {
                toolsExecuted.add("append_order_sheet (${createdOrder.orderId})")
                triggeredNodes.add("Google Sheets: Append Row")
            }
        }

        // 5. WhatsApp Send Message Node
        triggeredNodes.add("WhatsApp Send Message")

        val duration = System.currentTimeMillis() - startTime

        val trace = N8nExecutionTrace(
            traceId = traceId,
            timestamp = System.currentTimeMillis(),
            incomingMessage = incomingText,
            nodesTriggered = triggeredNodes.joinToString(" ➔ "),
            toolsExecuted = if (toolsExecuted.isEmpty()) "None" else toolsExecuted.joinToString(", "),
            memoryContextUsed = "Retrieved ${historyTurns.size} prior messages",
            rawGeminiPromptOrReasoning = reasoningText,
            botResponse = replyText,
            durationMs = duration,
            success = true
        )

        // Save execution trace to DB
        database.executionTraceDao().insertTrace(trace)

        AutomationResult(
            replyText = replyText,
            toolUsed = toolUsedName ?: if (toolsExecuted.isNotEmpty()) toolsExecuted.first() else null,
            intent = intent,
            orderCreated = createdOrder,
            executionTrace = trace
        )
    }

    private suspend fun parseAndExecuteOrder(
        jsonString: String,
        inventory: List<InventoryItem>,
        config: BotConfig
    ): CustomerOrder? {
        val json = JSONObject(jsonString)
        val customerName = json.optString("customerName", "Customer")
        val phone = json.optString("phone", "N/A")
        val address = json.optString("address", "N/A")
        val itemsArray = json.optJSONArray("items") ?: return null

        val summaryList = mutableListOf<String>()
        var subtotal = 0.0

        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.getJSONObject(i)
            val dishName = itemObj.optString("name")
            val qty = itemObj.optInt("qty", 1)

            val matchedItem = inventory.firstOrNull {
                it.name.contains(dishName, ignoreCase = true) || dishName.contains(it.name, ignoreCase = true)
            }

            if (matchedItem != null) {
                val newStock = (matchedItem.stockQuantity - qty).coerceAtLeast(0)
                database.inventoryDao().updateStock(matchedItem.id, newStock)
                val itemCost = matchedItem.price * qty
                subtotal += itemCost
                summaryList.add("${qty}x ${matchedItem.name} (Rs. ${itemCost.toInt()})")
            } else {
                summaryList.add("${qty}x $dishName")
            }
        }

        val deliveryFee = config.deliveryFee
        val total = subtotal + deliveryFee
        val orderId = "ORD-${(1000..9999).random()}"

        val order = CustomerOrder(
            orderId = orderId,
            customerName = customerName,
            customerPhone = phone,
            deliveryAddress = address,
            itemsSummary = summaryList.joinToString(", "),
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            totalPrice = total,
            status = OrderStatus.CONFIRMED,
            timestamp = System.currentTimeMillis()
        )

        database.customerOrderDao().insertOrder(order)
        return order
    }

    private data class FallbackResult(
        val reply: String,
        val intent: String,
        val order: CustomerOrder? = null,
        val toolUsed: String? = null
    )

    private suspend fun executeLocalRuleEngine(
        input: String,
        inventory: List<InventoryItem>,
        faqs: List<FaqItem>,
        config: BotConfig,
        history: List<Pair<String, String>>
    ): FallbackResult {
        val lower = input.lowercase(Locale.ROOT)

        // 1. Check for Menu / List query
        if (lower.contains("menu") || lower.contains("kya hai") || lower.contains("items") || lower.contains("list") || lower.contains("dishes")) {
            val menuText = buildString {
                append("📋 *SpiceCraft Kitchen Official Menu*\n")
                append("━━━━━━━━━━━━━━━━━━━\n\n")
                val grouped = inventory.groupBy { it.category }
                grouped.forEach { (category, items) ->
                    append("🔸 *${category.uppercase()}*\n")
                    items.forEach { item ->
                        val stockNotice = if (item.stockQuantity <= 0 || !item.isAvailable) " _(Out of stock)_" else ""
                        append("• *${item.name}* — Rs. ${item.price.toInt()}$stockNotice\n")
                    }
                    append("\n")
                }
                append("🛵 Delivery Fee: Rs. ${config.deliveryFee.toInt()} | Min Order: Rs. ${config.minOrderAmount.toInt()}\n")
                append("👉 _To order, simply tell me what you'd like along with your name, phone number, and delivery address!_")
            }
            return FallbackResult(
                reply = menuText,
                intent = "Menu Query",
                toolUsed = "Google Sheets: Read Inventory"
            )
        }

        // 2. Check for Specific Stock query
        for (item in inventory) {
            val itemNameLower = item.name.lowercase(Locale.ROOT)
            val keywords = itemNameLower.split(" ")
            val matched = keywords.any { it.length > 3 && lower.contains(it) } || lower.contains(itemNameLower)
            if (matched && (lower.contains("available") || lower.contains("stock") || lower.contains("hai") || lower.contains("price") || lower.contains("kitna"))) {
                return if (item.stockQuantity > 0 && item.isAvailable) {
                    FallbackResult(
                        reply = "✅ Yes! *${item.name}* is available in stock (${item.stockQuantity} ${item.unit}s remaining).\n💰 Price: *Rs. ${item.price.toInt()}*\n\nWould you like to place an order for this?",
                        intent = "Stock Check",
                        toolUsed = "Google Sheets: Query Stock (${item.name})"
                    )
                } else {
                    FallbackResult(
                        reply = "❌ Sorry, *${item.name}* is currently *Out of Stock*. Would you like to check other items from our menu?",
                        intent = "Stock Check (Out of Stock)",
                        toolUsed = "Google Sheets: Query Stock (${item.name})"
                    )
                }
            }
        }

        // 3. Check for FAQs (timings, delivery fee, payment, time)
        if (lower.contains("timing") || lower.contains("time") || lower.contains("open") || lower.contains("hours") || lower.contains("kab tak")) {
            val faq = faqs.find { it.category == "Timings" }
            return FallbackResult(
                reply = "⏰ ${faq?.answer ?: "We are open daily from 12:00 PM to 12:00 AM (Midnight)!"}",
                intent = "FAQ (Timings)",
                toolUsed = "Google Sheets: Read FAQs"
            )
        }

        if (lower.contains("delivery fee") || lower.contains("delivery charges") || lower.contains("charges") || lower.contains("delivery kitni")) {
            return FallbackResult(
                reply = "🛵 *Delivery Details:*\n• Delivery Fee: *Rs. ${config.deliveryFee.toInt()}* flat\n• Minimum Order: *Rs. ${config.minOrderAmount.toInt()}*\n• Estimated Delivery Time: *35-45 minutes*",
                intent = "FAQ (Delivery)",
                toolUsed = "Google Sheets: Read FAQs"
            )
        }

        if (lower.contains("payment") || lower.contains("cod") || lower.contains("jazzcash") || lower.contains("easypaisa") || lower.contains("card")) {
            return FallbackResult(
                reply = "💳 *Payment Methods:*\nWe accept:\n• Cash on Delivery (COD)\n• JazzCash & EasyPaisa\n• Online Bank Transfer",
                intent = "FAQ (Payment)",
                toolUsed = "Google Sheets: Read FAQs"
            )
        }

        // 4. Check for Order Placement intent
        val isOrderIntent = lower.contains("order") || lower.contains("chahiye") || lower.contains("bhej do") ||
                lower.contains("pack") || lower.contains("deliver") || lower.contains("send") || lower.contains("mangwana")

        if (isOrderIntent) {
            // Find matched dishes
            val orderedDishes = mutableListOf<Pair<InventoryItem, Int>>()
            for (item in inventory) {
                val itemNameLower = item.name.lowercase(Locale.ROOT)
                val keyTokens = itemNameLower.split(" ", "(", ")").filter { it.length > 3 }
                val matched = keyTokens.any { lower.contains(it) } || lower.contains(itemNameLower)
                if (matched) {
                    // Extract quantity if mentioned (e.g. "2 biryani", "2x chicken", "3 burgers")
                    var qty = 1
                    val dishWords = itemNameLower.split(" ", "(", ")").filter { it.length > 2 }
                    for (w in dishWords) {
                        val qRegex = Regex("(\\d+)\\s*(x|piece|pcs|plate|plates)?\\s*$w")
                        val qMatch = qRegex.find(lower)
                        if (qMatch != null) {
                            qty = qMatch.groupValues[1].toIntOrNull() ?: 1
                            break
                        }
                    }
                    if (qty == 1) {
                        val orderNumMatch = Regex("order\\s+(\\d+)", RegexOption.IGNORE_CASE).find(lower)
                        if (orderNumMatch != null) {
                            qty = orderNumMatch.groupValues[1].toIntOrNull() ?: 1
                        }
                    }
                    orderedDishes.add(item to qty)
                }
            }

            // Check if user gave phone number (e.g. 0300... or 10-11 digits)
            val phoneRegex = Regex("(03\\d{9}|\\+92\\d{10}|\\b\\d{10,11}\\b)")
            val phoneMatch = phoneRegex.find(input)

            // Check address keywords
            val hasAddress = lower.contains("street") || lower.contains("house") || lower.contains("sector") ||
                    lower.contains("block") || lower.contains("gali") || lower.contains("makan") || lower.contains("road") || lower.contains("phase")

            // Check name
            val nameMatch = Regex("\\b(?:name|naam|for)\\s*[:is]*\\s*([A-Za-z]+)", RegexOption.IGNORE_CASE).find(input)
            val customerName = nameMatch?.groupValues?.getOrNull(1) ?: "Valued Customer"

            if (orderedDishes.isEmpty()) {
                return FallbackResult(
                    reply = "I'd love to take your order! 🍔🍛 What items would you like from our menu, and what quantity?",
                    intent = "Order Incomplete (No Items)"
                )
            }

            // Check stock of ordered dishes
            for ((dish, requestedQty) in orderedDishes) {
                if (!dish.isAvailable || dish.stockQuantity <= 0) {
                    return FallbackResult(
                        reply = "⚠️ Unfortunately, *${dish.name}* is currently Out of Stock! Would you like to pick another dish from our menu instead?",
                        intent = "Order Rejected (Stock Unavailable)",
                        toolUsed = "Google Sheets: Check Stock"
                    )
                }
                if (dish.stockQuantity < requestedQty) {
                    return FallbackResult(
                        reply = "⚠️ We only have *${dish.stockQuantity}* of *${dish.name}* left in stock, but you requested *${requestedQty}*. Would you like to take ${dish.stockQuantity} instead?",
                        intent = "Order Rejected (Insufficient Stock)",
                        toolUsed = "Google Sheets: Check Stock"
                    )
                }
            }

            // If missing address or phone, ask for them politely
            if (phoneMatch == null || !hasAddress) {
                val itemsSummary = orderedDishes.joinToString(", ") { "${it.second}x ${it.first.name}" }
                val subtotal = orderedDishes.sumOf { it.first.price * it.second }
                val total = subtotal + config.deliveryFee

                val missingPrompt = buildString {
                    append("Great choice! I have reserved your items:\n")
                    append("🛒 *Items:* $itemsSummary\n")
                    append("💰 *Estimated Total:* Rs. ${total.toInt()} (including Rs. ${config.deliveryFee.toInt()} delivery)\n\n")
                    append("To confirm and dispatch this order, please provide:\n")
                    if (phoneMatch == null) append("📱 *Contact Phone Number*\n")
                    if (!hasAddress) append("📍 *Complete Delivery Address*\n")
                    if (customerName == "Valued Customer") append("👤 *Your Name*\n")
                }
                return FallbackResult(
                    reply = missingPrompt,
                    intent = "Order Pending Info",
                    toolUsed = "Google Sheets: Verify Stock"
                )
            }

            // All details present! Create order and deduct stock!
            val phone = phoneMatch.value
            val address = input.substring(input.indexOfAny(listOf("house", "street", "gali", "block", "sector", "road", "phase"), ignoreCase = true).coerceAtLeast(0))
                .take(60)

            val summaryList = mutableListOf<String>()
            var subtotal = 0.0

            for ((dish, qty) in orderedDishes) {
                val newQty = (dish.stockQuantity - qty).coerceAtLeast(0)
                database.inventoryDao().updateStock(dish.id, newQty)
                val cost = dish.price * qty
                subtotal += cost
                summaryList.add("${qty}x ${dish.name} (Rs. ${cost.toInt()})")
            }

            val deliveryFee = config.deliveryFee
            val total = subtotal + deliveryFee
            val orderId = "ORD-${(1000..9999).random()}"

            val newOrder = CustomerOrder(
                orderId = orderId,
                customerName = customerName,
                customerPhone = phone,
                deliveryAddress = address,
                itemsSummary = summaryList.joinToString(", "),
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                totalPrice = total,
                status = OrderStatus.CONFIRMED,
                timestamp = System.currentTimeMillis()
            )
            database.customerOrderDao().insertOrder(newOrder)

            val confirmationReply = buildString {
                append("🎉 *ORDER CONFIRMED!*\n")
                append("━━━━━━━━━━━━━━━━━━━\n")
                append("🆔 *Order ID:* `$orderId`\n")
                append("👤 *Customer:* $customerName\n")
                append("📱 *Phone:* $phone\n")
                append("📍 *Address:* $address\n\n")
                append("📦 *Items Ordered:*\n")
                orderedDishes.forEach { (d, q) ->
                    append("• ${q}x ${d.name} — Rs. ${(d.price * q).toInt()}\n")
                }
                append("\n💵 Subtotal: Rs. ${subtotal.toInt()}\n")
                append("🛵 Delivery Fee: Rs. ${deliveryFee.toInt()}\n")
                append("💰 *Total Payable:* *Rs. ${total.toInt()}*\n")
                append("💳 Payment: Cash on Delivery\n")
                append("⏱️ *Estimated Delivery:* 35-45 mins\n\n")
                append("✅ _Your order has been logged into our Google Sheets and sent to the kitchen for preparation!_")
            }

            return FallbackResult(
                reply = confirmationReply,
                intent = "Order Confirmed & Stock Deducted",
                order = newOrder,
                toolUsed = "Google Sheets: Append Row (Orders) & Deduct Stock"
            )
        }

        // 5. Default Friendly Fallback
        return FallbackResult(
            reply = "Hello! 👋 I am the AI Automation Assistant for *${config.restaurantName}*.\n\nYou can ask me:\n• To see our *Menu & Prices*\n• To check *Stock of any dish*\n• Delivery timings & FAQs\n• Or directly place an order with your details!\n\nHow can I help you?",
            intent = "General Greeting"
        )
    }

    private fun detectIntentFromText(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("menu") || lower.contains("dishes") -> "Menu Request"
            lower.contains("stock") || lower.contains("available") -> "Stock Query"
            lower.contains("order") -> "Order Handling"
            lower.contains("price") -> "Pricing Inquiry"
            else -> "Customer Query"
        }
    }
}
