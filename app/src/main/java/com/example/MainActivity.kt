package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BusLiveViewModel
import com.example.ui.viewmodel.Trans
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private val viewModel: BusLiveViewModel by viewModels()
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Explicitly initialize Android TextToSpeech engine
        tts = TextToSpeech(this, this)

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
            val isSenior by viewModel.isSeniorMode.collectAsStateWithLifecycle()
            val isAutoSpeak by viewModel.isAutoSpeak.collectAsStateWithLifecycle()

            // Handle switching TTS languages reactively on change
            LaunchedEffect(isEnglish, isTtsReady) {
                if (isTtsReady) {
                    val targetLocale = if (isEnglish) Locale.US else Locale("ta", "IN")
                    val result = tts?.setLanguage(targetLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("MainActivity", "Locale $targetLocale data missing or not supported. Falling back.")
                        tts?.language = Locale.US
                    }
                }
            }

            // Real voice assistant speaking loop hook
            LaunchedEffect(isTtsReady) {
                viewModel.textToSpeak.collect { speechText ->
                    if (isTtsReady && speechText.isNotBlank()) {
                        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "BusLiveSpeechId")
                    }
                }
            }

            MyApplicationTheme {
                if (userProfile == null) {
                    // Fully descriptive immersion gate
                    AuthScreen(
                        viewModel = viewModel,
                        isEnglish = isEnglish,
                        isSenior = isSenior
                    )
                } else {
                    AppMainScaffoldContent(
                        viewModel = viewModel,
                        isEnglish = isEnglish,
                        isSenior = isSenior,
                        isAutoSpeak = isAutoSpeak
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Defaults to US english initially, updates to Tamil reactively when user changes language switcher
            tts?.language = Locale.US
            Log.d("MainActivity", "TextToSpeech system successfully synthesized!")
        } else {
            Log.e("MainActivity", "TextToSpeech engine failed to boot.")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScaffoldContent(
    viewModel: BusLiveViewModel,
    isEnglish: Boolean,
    isSenior: Boolean,
    isAutoSpeak: Boolean
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Navigation sizes scaling
    val itemLabelSize = if (isSenior) 15.sp else 11.sp
    val iconSize = if (isSenior) 32.dp else 24.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_title_header")
                    ) {
                        // Bento Logo Box
                        Box(
                            modifier = Modifier
                                .size(if (isSenior) 44.dp else 38.dp)
                                .background(Color(0xFF006A4D), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(if (isSenior) 26.dp else 22.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            Text(
                                text = "BusLive TN",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSenior) 21.sp else 17.sp,
                                color = if (isSenior) Color(0xFFFFD700) else MaterialTheme.colorScheme.onBackground,
                                lineHeight = if (isSenior) 24.sp else 18.sp
                            )
                            Text(
                                text = if (isEnglish) "Salem Division" else "சேலம் பிரிவு",
                                fontSize = if (isSenior) 11.sp else 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A4D).copy(alpha = 0.85f),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Language Switcher Action Button - Bento Pill styled
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(20.dp), // Pill Shape
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(if (isSenior) 48.dp else 34.dp)
                            .testTag("language_toggle_btn")
                    ) {
                        Text(
                            text = if (isEnglish) "தமிழ்" else "EN",
                            fontSize = if (isSenior) 15.sp else 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Accessibility Senior Citizen mode switch icon
                    IconButton(
                        onClick = { viewModel.toggleSeniorMode() },
                        modifier = Modifier
                            .size(if (isSenior) 48.dp else 40.dp)
                            .testTag("senior_citizen_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isSenior) Icons.Default.AccessibleForward else Icons.Default.AccessibilityNew,
                            contentDescription = "Toggle Senior citizen Accessibility Mode",
                            tint = if (isSenior) Color(0xFFFF8C00) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(if (isSenior) 32.dp else 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Auto speak mode switch icon
                    IconButton(
                        onClick = { viewModel.toggleAutoSpeak() },
                        modifier = Modifier
                            .size(if (isSenior) 48.dp else 40.dp)
                            .testTag("auto_speak_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isAutoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle auto narration",
                            tint = if (isAutoSpeak) Color(0xFF10B981) else Color.Gray,
                            modifier = Modifier.size(if (isSenior) 32.dp else 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isSenior) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isSenior) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = Trans.get("home_tab", isEnglish),
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    label = {
                        Text(
                            text = Trans.get("home_tab", isEnglish),
                            fontSize = itemLabelSize,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("navbar_home_tab")
                )

                // Route Planner Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.Navigation else Icons.Outlined.Navigation,
                            contentDescription = Trans.get("planner_tab", isEnglish),
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    label = {
                        Text(
                            text = Trans.get("planner_tab", isEnglish),
                            fontSize = itemLabelSize,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("navbar_planner_tab")
                )

                // AI Assist Chat Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.SupportAgent else Icons.Outlined.SupportAgent,
                            contentDescription = Trans.get("chat_tab", isEnglish),
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    label = {
                        Text(
                            text = Trans.get("chat_tab", isEnglish),
                            fontSize = itemLabelSize,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("navbar_chat_tab")
                )

                // Services & Emergency SOS Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.SettingsApplications else Icons.Outlined.SettingsApplications,
                            contentDescription = Trans.get("services_tab", isEnglish),
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    label = {
                        Text(
                            text = Trans.get("services_tab", isEnglish),
                            fontSize = itemLabelSize,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("navbar_services_tab")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        // Render panels based on selection with gorgeous transitions
        Crossfade(
            targetState = selectedTab,
            label = "ScreenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> PlannerScreen(viewModel = viewModel)
                2 -> ChatScreen(viewModel = viewModel)
                3 -> ServicesScreen(viewModel = viewModel)
            }
        }
    }
}
