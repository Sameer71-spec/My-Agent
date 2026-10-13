package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.N8nExecutionTrace
import com.example.data.remote.GeminiClient
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.N8nOrange
import com.example.ui.theme.N8nPurple
import com.example.ui.theme.SheetsGreen
import com.example.ui.theme.WhatsAppPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkflowNodeDef(
    val id: String,
    val name: String,
    val type: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val summary: String,
    val configDetails: String
)

@Composable
fun N8nWorkflowScreen(
    traces: List<N8nExecutionTrace>,
    onTestWebhook: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Visual Workflow Nodes, 1: Live Execution Logs
    var inspectedNode by remember { mutableStateOf<WorkflowNodeDef?>(null) }
    var inspectedTrace by remember { mutableStateOf<N8nExecutionTrace?>(null) }

    val workflowNodes = listOf(
        WorkflowNodeDef(
            id = "node_webhook",
            name = "1. WhatsApp Trigger (Webhook)",
            type = "n8n-nodes-base.webhook",
            icon = Icons.Default.Webhook,
            badgeColor = WhatsAppPrimary,
            summary = "Listens to incoming messages from Meta WhatsApp Cloud API via HTTP POST",
            configDetails = """
                Path: /webhook/whatsapp-restaurant
                HTTP Method: POST
                Authentication: Header (Verify-Token)
                Event Filter: messages[0].text.body
            """.trimIndent()
        ),
        WorkflowNodeDef(
            id = "node_agent",
            name = "2. AI Agent (Gemini Flash)",
            type = "@n8n/n8n-nodes-langchain.agent",
            icon = Icons.Default.Psychology,
            badgeColor = GeminiBlue,
            summary = "Core reasoning engine powered by Google Gemini 3.5 Flash",
            configDetails = """
                Model: gemini-3.5-flash
                System Prompt: Order & Stock Manager Persona
                Max Iterations: 5
                API Key: ${if (GeminiClient.isApiKeyConfigured()) "Configured in AI Studio Secrets" else "Using Local AI Simulator"}
            """.trimIndent()
        ),
        WorkflowNodeDef(
            id = "node_memory",
            name = "3. Simple Memory Node",
            type = "@n8n/n8n-nodes-langchain.memoryBufferWindow",
            icon = Icons.Default.Memory,
            badgeColor = N8nPurple,
            summary = "Retains 10 previous conversational turns to remember customer name & pending order",
            configDetails = """
                Context Window Size: 10 messages
                Key: customer_phone_session
                Format: (HumanMessage, AIMessage)
            """.trimIndent()
        ),
        WorkflowNodeDef(
            id = "node_sheet_read",
            name = "4. Tool: Google Sheets (Read Stock)",
            type = "n8n-nodes-base.googleSheets",
            icon = Icons.Default.TableChart,
            badgeColor = SheetsGreen,
            summary = "Queries the live 'Inventory' sheet before confirming food availability",
            configDetails = """
                Operation: Read Sheet Rows
                Sheet: Inventory
                Fields: [Name, Price, StockQuantity, IsAvailable]
                Execution: Called whenever customer asks menu or items
            """.trimIndent()
        ),
        WorkflowNodeDef(
            id = "node_sheet_append",
            name = "5. Tool: Google Sheets (Append Order)",
            type = "n8n-nodes-base.googleSheets",
            icon = Icons.Default.DataObject,
            badgeColor = SheetsGreen,
            summary = "Appends confirmed customer order row to 'Orders' sheet & updates inventory",
            configDetails = """
                Operation: Append Row
                Sheet: Orders
                Columns: [OrderID, CustomerName, Phone, Items, Total, Timestamp, Status]
                Stock Sync: Deducts purchased quantities
            """.trimIndent()
        ),
        WorkflowNodeDef(
            id = "node_send_message",
            name = "6. WhatsApp Send Message",
            type = "n8n-nodes-base.whatsapp",
            icon = Icons.AutoMirrored.Filled.Send,
            badgeColor = WhatsAppPrimary,
            summary = "Dispatches formatted WhatsApp response back to recipient's phone number",
            configDetails = """
                Recipient: {{ ${'$'}json.body.from }}
                Template / Body: Markdown formatted with order summary
                Preview Urls: Enabled
            """.trimIndent()
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        // N8N Header
        Surface(
            color = N8nOrange,
            contentColor = Color.White,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "N8N Logo",
                            tint = N8nOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "N8N Workflow Automation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "WhatsApp Bot Pipeline • Active & Executing",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00E676), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = N8nOrange,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pipeline Nodes (${workflowNodes.size})")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execution Traces (${traces.size})")
                    }
                }
            )
        }

        // Content
        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = N8nOrange)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Tap any node below to inspect its LangChain agent parameters, Google Sheets tool bindings, and payload schema.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6D4C41)
                                )
                            }
                        }
                    }

                    items(workflowNodes) { node ->
                        WorkflowNodeCard(
                            node = node,
                            onClick = { inspectedNode = node }
                        )

                        // Flow arrow except last item
                        if (node.id != "node_send_message") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Flow direction",
                                    tint = N8nOrange.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            1 -> {
                if (traces.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No executions recorded yet", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Send a message in the WhatsApp Chat tab to trigger the N8N workflow!",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    val timeFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(traces) { trace ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inspectedTrace = trace }
                                    .testTag("trace_${trace.traceId}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = trace.traceId,
                                            fontWeight = FontWeight.Bold,
                                            color = N8nOrange,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${trace.durationMs}ms • ${timeFormat.format(Date(trace.timestamp))}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "📥 Incoming: \"${trace.incomingMessage}\"",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1F2937),
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "⚡ Nodes: ${trace.nodesTriggered}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF4B5563),
                                        maxLines = 2
                                    )

                                    if (trace.toolsExecuted.isNotBlank() && trace.toolsExecuted != "None") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = SheetsGreen.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "🛠️ Tools: ${trace.toolsExecuted}",
                                                fontSize = 11.sp,
                                                color = SheetsGreen,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

    // Node Inspector Dialog
    inspectedNode?.let { node ->
        AlertDialog(
            onDismissRequest = { inspectedNode = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(node.badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(node.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(node.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(node.summary, fontSize = 13.sp, color = Color(0xFF374151))
                    HorizontalDivider()
                    Text("Configuration & Bindings:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = node.configDetails,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { inspectedNode = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Trace Inspector Dialog
    inspectedTrace?.let { trace ->
        AlertDialog(
            onDismissRequest = { inspectedTrace = null },
            title = { Text("Execution Log: ${trace.traceId}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User Message:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(trace.incomingMessage, fontSize = 13.sp)

                    Text("Nodes Executed:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(trace.nodesTriggered, fontSize = 12.sp, color = N8nOrange)

                    Text("Tools Fired:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(trace.toolsExecuted, fontSize = 12.sp, color = SheetsGreen)

                    Text("Bot Response:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(trace.botResponse, fontSize = 13.sp, maxLines = 5)
                }
            },
            confirmButton = {
                Button(onClick = { inspectedTrace = null }) { Text("Done") }
            }
        )
    }
}

@Composable
fun WorkflowNodeCard(
    node: WorkflowNodeDef,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(node.id),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(node.badgeColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = node.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = node.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Active",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
