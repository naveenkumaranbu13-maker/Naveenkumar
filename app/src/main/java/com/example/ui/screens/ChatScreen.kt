package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.ui.viewmodel.BusLiveViewModel
import com.example.ui.viewmodel.Trans
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: BusLiveViewModel,
    modifier: Modifier = Modifier
) {
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val isSenior by viewModel.isSeniorMode.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var userTextEntry by remember { mutableStateOf("") }

    // Font heights of elements
    val messageFontSize = if (isSenior) 18.sp else 14.sp
    val buttonTextSize = if (isSenior) 16.sp else 12.sp

    // Auto-scroll on new message added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Suggested Templates
    val enSuggestions = listOf(
        "When will bus 17A arrive?",
        "Which bus goes to Chennai?",
        "Where is the nearest bus stop?",
        "Salem to Coimbatore fare?"
    )
    val taSuggestions = listOf(
        "17A பேருந்து எப்போது வரும்?",
        "சென்னைக்கு எந்த பேருந்து செல்கிறது?",
        "அருகிலுள்ள பேருந்து நிறுத்தம் எது?",
        "சேலம் டு கோவை கட்டணம் எவ்வளவு?"
    )
    val suggestions = if (isEnglish) enSuggestions else taSuggestions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chat Header with Clear Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "BusLive Assistant",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (isEnglish) "BusLive Assistant" else "பஸ்லைவ் குரல் உதவியாளர்",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSenior) 22.sp else 18.sp
                )
            }

            IconButton(
                onClick = { viewModel.resetChat() },
                modifier = Modifier.testTag("reset_chat_history_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Conversation",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Chat Bubble area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(chatMessages) { message ->
                    val isUser = message.sender == ChatMessage.Sender.USER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (isUser) 20.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 20.dp
                            ),
                            color = if (isUser) {
                                Color(0xFF006A4D)
                            } else {
                                if (isSenior) Color(0xFF1E293B) else Color.White
                            },
                            contentColor = if (isUser) {
                                Color.White
                            } else {
                                if (isSenior) Color.White else Color(0xFF1E293B)
                            },
                            border = if (isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .shadow(1.dp, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = message.text,
                                    fontSize = messageFontSize,
                                    lineHeight = if (isSenior) 24.sp else 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val formattedTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(message.timestamp)
                                    Text(
                                        text = formattedTime,
                                        fontSize = 10.sp,
                                        color = (if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Text(
                                        text = if (isEnglish) "Assistant is translating..." else "கணிக்கப்படுகிறது...",
                                        fontSize = if (isSenior) 16.sp else 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Suggested Voice Templates (Horizontal Row Scroll)
        Text(
            text = if (isEnglish) "Suggested Questions:" else "பரிந்துரைக்கப்பட்டவை:",
            fontSize = buttonTextSize,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            suggestions.forEach { entry ->
                Button(
                    onClick = { viewModel.sendChatMessage(entry) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF006A4D)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(24.dp), // Pill shape
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("chip_${entry.take(10).replace(" ", "_")}")
                ) {
                    Text(entry, fontSize = buttonTextSize, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Input Tray Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated / Real Microphone Speaking Button
            IconButton(
                onClick = {
                    viewModel.toggleVoiceListening { voiceText ->
                        userTextEntry = voiceText
                        // Auto dispatch on spoken trigger completed
                        viewModel.sendChatMessage(voiceText)
                        userTextEntry = ""
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isListening) Color.Red else MaterialTheme.colorScheme.secondaryContainer)
                    .testTag("chat_mic_voice_trigger_btn")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Continuous Listening Mode",
                    tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Keyboard Text Box
            OutlinedTextField(
                value = userTextEntry,
                onValueChange = { userTextEntry = it },
                placeholder = { Text(text = Trans.get("search_placeholder", isEnglish), fontSize = messageFontSize) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input")
                    .shadow(1.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = messageFontSize),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF006A4D),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // Submit Button - styled elegantly
            IconButton(
                onClick = {
                    if (userTextEntry.isNotBlank()) {
                        viewModel.sendChatMessage(userTextEntry)
                        userTextEntry = ""
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF006A4D))
                    .testTag("chat_submit_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
