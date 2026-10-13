package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotConfig
import com.example.data.remote.GeminiClient
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.N8nOrange
import com.example.ui.theme.WhatsAppPrimary
import com.example.ui.theme.WhatsAppPrimaryDark

@Composable
fun SettingsScreen(
    botConfig: BotConfig,
    onSaveConfig: (BotConfig) -> Unit,
    onSimulateWebhook: (phone: String, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var restaurantName by remember { mutableStateOf(botConfig.restaurantName) }
    var deliveryFeeStr by remember { mutableStateOf(botConfig.deliveryFee.toInt().toString()) }
    var minOrderStr by remember { mutableStateOf(botConfig.minOrderAmount.toInt().toString()) }
    var systemPrompt by remember { mutableStateOf(botConfig.systemPrompt) }
    var phoneNumberId by remember { mutableStateOf(botConfig.metaPhoneNumberId) }
    var wabaId by remember { mutableStateOf(botConfig.metaWabaId) }
    var webhookUrl by remember { mutableStateOf(botConfig.n8nWebhookUrl) }
    var verifyToken by remember { mutableStateOf(botConfig.verifyToken) }

    // Webhook simulation inputs
    var testSenderPhone by remember { mutableStateOf("+923001234567") }
    var testIncomingText by remember { mutableStateOf("Hi, order 1 Chicken Biryani and 1 Coke, address Street 12, Gulberg, for Usman, phone 03001234567") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .verticalScroll(rememberScrollState())
    ) {
        // Settings Header
        Surface(
            color = WhatsAppPrimaryDark,
            contentColor = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WhatsApp AI Bot Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Meta Cloud API • Gemini AI Prompt • N8N Webhooks",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Gemini AI Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = GeminiBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI Model (gemini-3.5-flash)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val isConfigured = GeminiClient.isApiKeyConfigured()
                    Surface(
                        color = if (isConfigured) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isConfigured) Color(0xFF16A34A) else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isConfigured) "Gemini API Key active via AI Studio Secrets" else "Running built-in AI Engine & N8N logic (add key in Secrets panel for live cloud model)",
                                fontSize = 12.sp,
                                color = if (isConfigured) Color(0xFF15803D) else Color(0xFFB45309),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. Business Profile & Delivery Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🏪 Restaurant Profile & Pricing",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    OutlinedTextField(
                        value = restaurantName,
                        onValueChange = { restaurantName = it },
                        label = { Text("Restaurant Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_restaurant_name"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = deliveryFeeStr,
                            onValueChange = { deliveryFeeStr = it },
                            label = { Text("Delivery Fee (Rs.)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = minOrderStr,
                            onValueChange = { minOrderStr = it },
                            label = { Text("Min Order (Rs.)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // 3. System Instructions & Persona Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🧠 Bot System Prompt & Guardrails",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Defines how the AI checks stock, interacts with customers, and enforces rules.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("input_system_prompt"),
                        maxLines = 10
                    )
                }
            }

            // 4. Meta WhatsApp Cloud API Setup (Video Chapter 13:15)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📱 Meta WhatsApp Business API Link",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Connects this N8N workflow to Meta Developer Portal (developers.facebook.com).",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = phoneNumberId,
                        onValueChange = { phoneNumberId = it },
                        label = { Text("Meta Phone Number ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = webhookUrl,
                        onValueChange = { webhookUrl = it },
                        label = { Text("N8N Webhook Callback URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = verifyToken,
                        onValueChange = { verifyToken = it },
                        label = { Text("Webhook Verify Token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val url = "https://developers.facebook.com/apps/"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppPrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Meta Portal", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(webhookUrl))
                                Toast.makeText(context, "Webhook URL copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Webhook", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 5. Test Webhook Trigger Simulator (Video Chapter 16:45)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Webhook, contentDescription = null, tint = WhatsAppPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ Webhook Payload Simulator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppPrimary
                        )
                    }
                    Text(
                        text = "Simulates incoming Meta WhatsApp JSON payload sent to the N8N webhook trigger.",
                        fontSize = 12.sp,
                        color = Color(0xFF374151)
                    )

                    OutlinedTextField(
                        value = testSenderPhone,
                        onValueChange = { testSenderPhone = it },
                        label = { Text("Sender Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = testIncomingText,
                        onValueChange = { testIncomingText = it },
                        label = { Text("Message Body") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (testIncomingText.isNotBlank()) {
                                onSimulateWebhook(testSenderPhone, testIncomingText)
                                Toast.makeText(context, "Webhook fired! Switched to Chat tab.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_trigger_test_webhook")
                    ) {
                        Text("Trigger Simulated WhatsApp Message")
                    }
                }
            }

            // Save Configuration Button
            Button(
                onClick = {
                    val updated = botConfig.copy(
                        restaurantName = restaurantName.trim(),
                        deliveryFee = deliveryFeeStr.toDoubleOrNull() ?: 50.0,
                        minOrderAmount = minOrderStr.toDoubleOrNull() ?: 200.0,
                        systemPrompt = systemPrompt.trim(),
                        metaPhoneNumberId = phoneNumberId.trim(),
                        metaWabaId = wabaId.trim(),
                        n8nWebhookUrl = webhookUrl.trim(),
                        verifyToken = verifyToken.trim()
                    )
                    onSaveConfig(updated)
                    Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_settings"),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppPrimaryDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Configuration")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
