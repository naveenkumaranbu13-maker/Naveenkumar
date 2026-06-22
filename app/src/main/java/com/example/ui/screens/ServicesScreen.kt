package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Complaint
import com.example.data.model.Favorite
import com.example.ui.viewmodel.BusLiveViewModel
import com.example.ui.viewmodel.Trans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: BusLiveViewModel,
    modifier: Modifier = Modifier
) {
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val isSenior by viewModel.isSeniorMode.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val complaints by viewModel.complaints.collectAsStateWithLifecycle(initialValue = emptyList())
    val sosCounter by viewModel.sosCounter.collectAsStateWithLifecycle()

    var complaintCategory by remember { mutableStateOf("Bus Delay") }
    var complaintDesc by remember { mutableStateOf("") }
    var voiceModeActive by remember { mutableStateOf(false) }
    var mockWaveDuration by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

    // Fonts scale
    val titleFontSize = if (isSenior) 26.sp else 20.sp
    val labelFontSize = if (isSenior) 18.sp else 14.sp
    val bodyFontSize = if (isSenior) 16.sp else 12.sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. EMERGENCY SOS SHIELD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (sosCounter != null) Color(0xFF7F1D1D) else Color(0xFF450A0A)
            ),
            border = BorderStroke(2.dp, Color(0xFFEF4444))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Trans.get("emergency_sos", isEnglish),
                    fontSize = if (isSenior) 30.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = Trans.get("sos_sub", isEnglish),
                    fontSize = bodyFontSize,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = if (isSenior) 22.sp else 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Huge Panic Trigger Button
                Box(contentAlignment = Alignment.Center) {
                    Button(
                        onClick = { viewModel.triggerSOS() },
                        modifier = Modifier
                            .size(if (isSenior) 130.dp else 100.dp)
                            .shadow(8.dp, CircleShape)
                            .testTag("sos_trigger_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "SOS",
                                tint = Color.White,
                                modifier = Modifier.size(if (isSenior) 40.dp else 32.dp)
                            )
                            if (sosCounter != null) {
                                Text(
                                    text = "${sosCounter}s",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Disarming Overlay
                AnimatedVisibility(visible = sosCounter != null) {
                    OutlinedButton(
                        onClick = { viewModel.cancelSOS() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier.testTag("cancel_sos_btn")
                    ) {
                        Text(
                            text = if (isEnglish) "CANCEL PANIC ALERT" else "அலர்ட்டை ரத்து செய்",
                            fontSize = bodyFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 2. MULTILINGUAL COMPLAINT / BREAKDOWN REPORTING ---
        Text(
            text = Trans.get("report_issue", isEnglish),
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSenior) Color(0xFF0F172A) else Color.White
            ),
            border = BorderStroke(1.dp, if (isSenior) Color(0xFFFF8C00) else Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Selector chips
                Text(
                    text = if (isEnglish) "Select Category" else "வகையைத் தேர்ந்தெடுக்கவும்",
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.SemiBold
                )

                val categoriesList = listOf("Rash Driving", "Bus Delay", "Overcrowding", "Breakdown")
                val categoriesListTa = listOf("வேகமான ஓட்டுதல்", "பஸ் தாமதம்", "அதிக நெரிசல்", "வண்டி பழுது")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEachIndexed { idx, cat ->
                        val selected = complaintCategory == cat
                        Button(
                            onClick = { complaintCategory = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Color(0xFF006A4D) else Color.White,
                                contentColor = if (selected) Color.White else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(1.dp, if (selected) Color(0xFF006A4D) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("complaint_chip_${cat.replace(" ", "_")}")
                        ) {
                            Text(
                                text = if (isEnglish) cat else categoriesListTa[idx],
                                fontSize = bodyFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Text Description mode vs simulated Audio recording Complaint Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Complaint Entry Mode" else "புகார் பதிவு முறை",
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { voiceModeActive = false },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (!voiceModeActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Text complaint")
                        }
                        IconButton(
                            onClick = { voiceModeActive = true },
                            modifier = Modifier.testTag("complaint_mode_voice_toggle"),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (voiceModeActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice complaint")
                        }
                    }
                }

                if (!voiceModeActive) {
                    OutlinedTextField(
                        value = complaintDesc,
                        onValueChange = { complaintDesc = it },
                        placeholder = { Text(Trans.get("write_desc", isEnglish), fontSize = bodyFontSize) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("complaint_text_input"),
                        textStyle = LocalTextStyle.current.copy(fontSize = bodyFontSize),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                } else {
                    // Beautiful simulated interactive continuous audio complaint wave visual
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (mockWaveDuration == 0) Trans.get("voice_desc", isEnglish) else "Recording... $mockWaveDuration secs",
                            fontSize = bodyFontSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wave visuals
                            for (i in 0..10) {
                                val stateValue = if (mockWaveDuration == 0) 5.dp else (10 + (1..30).random()).dp
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(stateValue)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Mic speaking controller
                        Button(
                            onClick = {
                                if (mockWaveDuration == 0) {
                                    mockWaveDuration = 5
                                    complaintDesc = if (isEnglish) "Recorded audio statement regarding $complaintCategory." else "குரல் பதிவுசெய்யப்பட்டது."
                                } else {
                                    mockWaveDuration = 0
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mockWaveDuration > 0) Color.Red else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.height(32.dp).testTag("complaint_record_btn")
                        ) {
                            Text(
                                text = if (mockWaveDuration == 0) "START" else "STOP REC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // File Submit Button
                Button(
                    onClick = {
                        if (complaintDesc.isNotBlank() || mockWaveDuration > 0) {
                            viewModel.fileComplaint(
                                category = complaintCategory,
                                description = complaintDesc.ifBlank { "Voice statement ($complaintCategory)" },
                                isVoice = voiceModeActive,
                                voiceSec = if (voiceModeActive) 5 else 0
                            )
                            complaintDesc = ""
                            mockWaveDuration = 0
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_complaint_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSenior) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                        contentColor = if (isSenior) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = Trans.get("submit", isEnglish),
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 3. COMMUTER COMPLAINT LOGS (ROOM DATA STREAM) ---
        if (complaints.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnglish) "Lodge Status History" else "முந்தைய புகாரின் நிலவரங்கள்",
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                complaints.forEach { cmp ->
                    ComplaintReportRow(
                        cmp = cmp,
                        isEnglish = isEnglish,
                        isSenior = isSenior,
                        onDelete = { viewModel.deleteComplaintById(cmp.id) }
                    )
                }
            }
        }

        // --- 4. FAVORITES SHORTCUTS (ROOM PERSISTENCE) ---
        Text(
            text = Trans.get("favorites", isEnglish),
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) "No favorite places saved yet. Save from home screen!" else "பிடித்த வழித்தடங்கள் இல்லை. முகப்புத் திரையில் சேமிக்கவும்!",
                    textAlign = TextAlign.Center,
                    fontSize = bodyFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                favorites.forEach { fav ->
                    FavoriteShortcutRow(
                        fav = fav,
                        isEnglish = isEnglish,
                        isSenior = isSenior,
                        onDelete = { viewModel.deleteFav(fav) }
                    )
                }
            }
        }
    }
}

@Composable
fun ComplaintReportRow(
    cmp: Complaint,
    isEnglish: Boolean,
    isSenior: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isSenior) Color(0xFF1E293B) else Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSenior) Color(0xFFFF8C00) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (cmp.isVoice) Icons.Default.VolumeUp else Icons.Default.Assignment,
                        contentDescription = "Status Category",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isSenior) 22.dp else 16.dp)
                    )
                    Text(
                        text = cmp.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 18.sp else 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cmp.description,
                    fontSize = if (isSenior) 16.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = cmp.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_complaint_${cmp.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete complaint log",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(if (isSenior) 26.dp else 20.dp)
                )
            }
        }
    }
}

@Composable
fun FavoriteShortcutRow(
    fav: Favorite,
    isEnglish: Boolean,
    isSenior: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .testTag("fav_${fav.busNumber}"),
        colors = CardDefaults.cardColors(containerColor = if (isSenior) Color(0xFF1E293B) else Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSenior) Color(0xFFFF8C00) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Saved location icon",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(if (isSenior) 28.dp else 22.dp)
                )

                Column {
                    Text(
                        text = fav.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 18.sp else 14.sp
                    )
                    Text(
                        text = "Bus ${fav.busNumber}",
                        fontSize = if (isSenior) 16.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_fav_${fav.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Favorite",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(if (isSenior) 26.dp else 20.dp)
                )
            }
        }
    }
}
