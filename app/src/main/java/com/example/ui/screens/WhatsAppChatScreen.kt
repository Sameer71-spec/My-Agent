package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotConfig
import com.example.data.model.ChatMessage
import com.example.ui.theme.N8nOrange
import com.example.ui.theme.SheetsGreen
import com.example.ui.theme.WhatsAppAccent
import com.example.ui.theme.WhatsAppBubbleIncoming
import com.example.ui.theme.WhatsAppBubbleOutgoing
import com.example.ui.theme.WhatsAppCheckmarkBlue
import com.example.ui.theme.WhatsAppPrimary
import com.example.ui.theme.WhatsAppPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WhatsAppChatScreen(
    messages: List<ChatMessage>,
    isBotTyping: Boolean,
    botConfig: BotConfig,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onViewWorkflowTrace: () -> Unit,
    onViewSheets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size, isBotTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "📋 Show Menu",
        "🍗 Is Chicken Biryani available?",
        "Order 2 Biryani for Ali, phone 03001234567, address House 14, Street 2",
        "⏰ What are delivery timings?",
        "🛵 What is the delivery fee?",
        "Zinger Burger stock check"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFEAE2)) // Classic WhatsApp light chat wallpaper
    ) {
        // WhatsApp Top Bar
        Surface(
            color = WhatsAppPrimary,
            contentColor = Color.White,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with AI badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(WhatsAppPrimaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Bot Avatar",
                        tint = WhatsAppAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = botConfig.restaurantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified Business",
                            tint = Color(0xFF64D2FF),
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D47A1).copy(alpha = 0.4f))
                                .padding(2.dp)
                        )
                    }
                    Text(
                        text = if (isBotTyping) "typing..." else "online • automated via N8N & Gemini",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isBotTyping) WhatsAppAccent else Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                // Quick buttons to Jump to Sheets or N8N Inspector
                IconButton(
                    onClick = onViewSheets,
                    modifier = Modifier.testTag("btn_quick_sheets")
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Open Google Sheets",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onViewWorkflowTrace,
                    modifier = Modifier.testTag("btn_quick_workflow")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Open N8N Workflow",
                        tint = N8nOrange
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Chat") },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onClearChat()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Inventory Sheet") },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onViewSheets()
                            }
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips (WhatsApp test scenarios)
        Surface(
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 1.dp
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(quickPrompts) { prompt ->
                    AssistChip(
                        onClick = { onSendMessage(prompt) },
                        label = {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFF0F2F5),
                            labelColor = WhatsAppPrimaryDark
                        ),
                        modifier = Modifier.testTag("chip_prompt")
                    )
                }
            }
        }

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notice banner: 24/7 AI Automation powered by Gemini + Google Sheets
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECCB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🔒 Messages are processed in real-time by Gemini AI and synced with Google Sheets Inventory.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5D4037),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            items(messages) { message ->
                MessageBubbleItem(
                    message = message,
                    onInspectTrace = onViewWorkflowTrace
                )
            }

            if (isBotTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WhatsAppBubbleIncoming),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = WhatsAppPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI is querying Google Sheets & typing...",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Message Input Field
        Surface(
            color = Color(0xFFF0F2F5),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Message WhatsApp bot...", fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_button"),
                    containerColor = WhatsAppPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: ChatMessage,
    onInspectTrace: () -> Unit
) {
    val isUser = message.isFromUser
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) WhatsAppBubbleOutgoing else WhatsAppBubbleIncoming
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                // Sender label if bot
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppPrimaryDark,
                            fontSize = 11.sp
                        )
                        if (message.toolUsed != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SheetsGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "📊 ${message.toolUsed}",
                                    fontSize = 9.sp,
                                    color = SheetsGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Message Text
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF111B21),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp and Read Receipts
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color(0xFF667781)
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = WhatsAppCheckmarkBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
