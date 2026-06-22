package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.TransportRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class BusLiveViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransportRepository(application)

    // --- Core Setting States ---
    private val _isEnglish = MutableStateFlow(true)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    private val _isSeniorMode = MutableStateFlow(false)
    val isSeniorMode: StateFlow<Boolean> = _isSeniorMode.asStateFlow()

    private val _isAutoSpeak = MutableStateFlow(true)
    val isAutoSpeak: StateFlow<Boolean> = _isAutoSpeak.asStateFlow()

    // --- Authentication States ---
    private val _userProfile = MutableStateFlow<String?>(null) // null means logged out, otherwise displays email/name
    val userProfile: StateFlow<String?> = _userProfile.asStateFlow()

    // --- Bus Selection states ---
    private val _buses = MutableStateFlow<List<BusLiveStatus>>(repository.mockBuses)
    val buses: StateFlow<List<BusLiveStatus>> = _buses.asStateFlow()

    private val _selectedBus = MutableStateFlow<BusLiveStatus?>(repository.mockBuses.firstOrNull())
    val selectedBus: StateFlow<BusLiveStatus?> = _selectedBus.asStateFlow()

    // --- Live tracking & Query variables ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Route Planner Output states ---
    private val _plannerSource = MutableStateFlow("Salem")
    val plannerSource: StateFlow<String> = _plannerSource.asStateFlow()

    private val _plannerDestination = MutableStateFlow("Coimbatore")
    val plannerDestination: StateFlow<String> = _plannerDestination.asStateFlow()

    private val _plannerResults = MutableStateFlow<List<RouteOption>>(emptyList())
    val plannerResults: StateFlow<List<RouteOption>> = _plannerResults.asStateFlow()

    private val _isPlanning = MutableStateFlow(false)
    val isPlanning: StateFlow<Boolean> = _isPlanning.asStateFlow()

    // --- Chat Room States ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // --- SOS simulation ---
    private val _sosCounter = MutableStateFlow<Int?>(null)
    val sosCounter: StateFlow<Int?> = _sosCounter.asStateFlow()

    // --- Text-to-Speech Communication Channel (Shared Flow) ---
    private val _textToSpeak = MutableSharedFlow<String>(replay = 0)
    val textToSpeak: SharedFlow<String> = _textToSpeak.asSharedFlow()

    // --- Room Database Streams mapped ---
    val favorites = repository.allFavorites
    val complaints = repository.allComplaints
    val notifications = repository.allNotifications
    val mockBusStops = repository.mockBusStops

    init {
        // Prepare initial greeting and alert notifications
        viewModelScope.launch {
            seedInitialAlerts()
            resetChat()
        }
    }

    // --- Actions ---

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
        val announcement = if (_isEnglish.value) {
            "Language changed to English."
        } else {
            "மொழி தமிழுக்கு மாற்றப்பட்டுள்ளது."
        }
        speakText(announcement)
        resetChat()
    }

    fun toggleSeniorMode() {
        _isSeniorMode.value = !_isSeniorMode.value
        val announcement = if (_isEnglish.value) {
            if (_isSeniorMode.value) "Senior Citizen Mode activated. Larger text and voice assist on." else "Normal Mode activated."
        } else {
            if (_isSeniorMode.value) "முதியோர் முறைமை இயக்கப்பட்டது. கூடுதல் குரல் வழிகாட்டி தயார்." else "சாதாரண முறைமை மாற்றப்பட்டது."
        }
        speakText(announcement)
    }

    fun toggleAutoSpeak() {
        _isAutoSpeak.value = !_isAutoSpeak.value
    }

    fun loginSimulated(email: String) {
        _userProfile.value = email
        val userPrefix = email.substringBefore("@")
        val greeting = if (_isEnglish.value) "Success, welcome back $userPrefix!" else "வெற்றி, வருக $userPrefix!"
        speakText(greeting)
    }

    fun logout() {
        _userProfile.value = null
        val msg = if (_isEnglish.value) "Logged out successfully" else "வெற்றிகரமாக வெளியேற்றப்பட்டீர்கள்"
        speakText(msg)
    }

    fun selectBus(bus: BusLiveStatus) {
        _selectedBus.value = bus
        val engText = "Tracking bus ${bus.busNumber} to ${bus.destEn}. Expected ETA is ${bus.etaMinutes} minutes with ${bus.confidencePercent}% confidence."
        val tamText = "${bus.busNumber} பேருந்து கண்காணிப்பில் உள்ளது. இது ${bus.destTa}-க்கு இன்னும் ${bus.etaMinutes} நிமிடங்களில் வரும்."
        
        speakText(if (_isEnglish.value) engText else tamText)

        // Crowd check and TTS announcement
        if (bus.crowdLevel == CrowdLevel.HEAVY) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                val crowdWarning = if (_isEnglish.value) {
                    "This bus is currently heavy crowded."
                } else {
                    "இந்த பேருந்தில் தற்போது அதிக கூட்டம் உள்ளது."
                }
                speakText(crowdWarning)
            }
        }
    }

    fun setSource(value: String) {
        _plannerSource.value = value
    }

    fun setDestination(value: String) {
        _plannerDestination.value = value
    }

    fun runRoutePlanner() {
        _isPlanning.value = true
        viewModelScope.launch {
            val results = repository.planRoute(_plannerSource.value, _plannerDestination.value)
            _plannerResults.value = results
            _isPlanning.value = false

            // Automatically read first results aloud in selected language
            if (results.isNotEmpty() && _isAutoSpeak.value) {
                val bestOption = results.first()
                val summary = if (_isEnglish.value) {
                    "Found optimal routes. The best option is ${bestOption.typeEn}, takes ${bestOption.durationMinutes} minutes. ${bestOption.summaryEn}"
                } else {
                    "சிறந்த வழித்தடங்கள் கண்டறியப்பட்டுள்ளன. ${bestOption.typeTa}, பயண நேரம் ${bestOption.durationMinutes} நிமிடங்கள். ${bestOption.summaryTa}"
                }
                speakText(summary)
            }
        }
    }

    fun speakText(text: String) {
        if (_isAutoSpeak.value || _isSeniorMode.value) {
            viewModelScope.launch {
                _textToSpeak.emit(text)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Filter live buses based on input
        if (query.isBlank()) {
            _buses.value = repository.mockBuses
        } else {
            val lc = query.lowercase()
            _buses.value = repository.mockBuses.filter {
                it.busNumber.contains(lc, ignoreCase = true) ||
                it.sourceEn.contains(lc, ignoreCase = true) ||
                it.destEn.contains(lc, ignoreCase = true) ||
                it.sourceTa.contains(lc, ignoreCase = true) ||
                it.destTa.contains(lc, ignoreCase = true)
            }
        }
    }

    // --- Database Operations ---

    fun toggleSaveFavorite(busNo: String, src: String, dest: String) {
        viewModelScope.launch {
            val title = "$src to $dest"
            val newFav = Favorite(type = "route", title = title, source = src, destination = dest, busNumber = busNo)
            repository.insertFavorite(newFav)
            val ack = if (_isEnglish.value) "$title added to Favorites" else "$title பிடித்தவைகளில் சேர்க்கப்பட்டது"
            speakText(ack)
        }
    }

    fun deleteFav(favorite: Favorite) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
            val ack = if (_isEnglish.value) "Deleted from favorites" else "பிடித்தவைகளிலிருந்து நீக்கப்பட்டது"
            speakText(ack)
        }
    }

    fun fileComplaint(category: String, description: String, isVoice: Boolean, voiceSec: Int = 0) {
        viewModelScope.launch {
            val c = Complaint(category = category, description = description, isVoice = isVoice, voiceDurationSec = voiceSec)
            repository.insertComplaint(c)
            val ack = if (_isEnglish.value) {
                "Your complaint about $category has been lodged. Status: Pending Review."
            } else {
                "உங்கள் $category பற்றிய புகார் பதிவு செய்யப்பட்டது. நிலை: சரிபார்க்கப்படுகிறது."
            }
            speakText(ack)
        }
    }

    fun deleteComplaintById(id: Int) {
        viewModelScope.launch {
            repository.deleteComplaintById(id)
            val ack = if (_isEnglish.value) "Complaint log removed" else "புகார் பதிவு நீக்கப்பட்டது"
            speakText(ack)
        }
    }

    // --- Chat Assistant Operations ---

    fun resetChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = Trans.get("chatbot_welcome", _isEnglish.value),
                sender = ChatMessage.Sender.ASSISTANT
            )
        )
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), text = text, sender = ChatMessage.Sender.USER)
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userMsg)
        _chatMessages.value = currentList

        _isChatLoading.value = true
        viewModelScope.launch {
            val aiResponse = repository.getGeminiChatResponse(currentList, _isEnglish.value)
            val assistantMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = aiResponse,
                sender = ChatMessage.Sender.ASSISTANT
            )
            _chatMessages.value = _chatMessages.value + assistantMsg
            _isChatLoading.value = false

            // Automatically read AI response out loud
            speakText(aiResponse)
        }
    }

    // --- Simulated Continuous Audio Transcription for Search or Assistant ---
    fun toggleVoiceListening(onResult: (String) -> Unit) {
        if (_isListening.value) {
            _isListening.value = false
        } else {
            _isListening.value = true
            viewModelScope.launch {
                speakText(if (_isEnglish.value) "Listening..." else "கேட்கிறது...")
                kotlinx.coroutines.delay(2500) // Simulated delay of speech input
                if (_isListening.value) {
                    _isListening.value = false
                    val spokenOutput = if (_isEnglish.value) {
                        "Nearest bus stop"
                    } else {
                        "அருகிலுள்ள பேருந்து நிறுத்தம்"
                    }
                    onResult(spokenOutput)
                    speakText(if (_isEnglish.value) "Registered voice input: $spokenOutput" else "பதிவுசெய்யப்பட்டது: $spokenOutput")
                }
            }
        }
    }

    // --- SOS Operation ---
    fun triggerSOS() {
        if (_sosCounter.value != null) return // Already running
        _sosCounter.value = 3
        viewModelScope.launch {
            while (_sosCounter.value != null && _sosCounter.value!! > 0) {
                val t = if (_isEnglish.value) "SOS Dispatches in " else "தொடங்குகிறது "
                speakText(t + _sosCounter.value)
                kotlinx.coroutines.delay(1000)
                _sosCounter.update { if (it != null) it - 1 else null }
            }
            if (_sosCounter.value == 0) {
                _sosCounter.value = null
                val message = if (_isEnglish.value) "Emergency SOS Dispatched!" else "அவசரக்கால SOS அனுப்பப்பட்டது!"
                speakText(message)
                
                // Add emergency alert in live notifications database
                repository.insertNotification(
                    AlertNotification(
                        titleEn = "Emergency Alert Dispatched!",
                        titleTa = "அவசரக்கால SOS அனுப்பப்பட்டது!",
                        messageEn = "Live location and Bus details sent to emergency contacts successfully.",
                        messageTa = "உங்களது நேரலை இருப்பிடம் மற்றும் பேருந்து விவரங்கள் அவசரகால தொடர்புகளுக்கு அனுப்பப்பட்டது.",
                        type = "Delay"
                    )
                )
            }
        }
    }

    fun cancelSOS() {
        _sosCounter.value = null
        val cancellation = if (_isEnglish.value) "SOS Alert Cancelled" else "SOS அலர்ட் ரத்து செய்யப்பட்டது"
        speakText(cancellation)
    }

    private suspend fun seedInitialAlerts() {
        repository.insertNotification(
            AlertNotification(
                titleEn = "Heavy Traffic Congestion on NH-544",
                titleTa = "NH-544 இல் அதிக போக்குவரத்து நெரிசல்",
                messageEn = "Traffic congestion between Salem and Coimbatore. ETA might increase by 10-15 minutes.",
                messageTa = "சேலம் மற்றும் கோவை இடையே போக்குவரத்து நெரிசல். வருகை நேரம் 10-15 நிமிடங்கள் தாமதம் ஆகலாம்.",
                type = "Delay"
            )
        )
        repository.insertNotification(
            AlertNotification(
                titleEn = "Rain Alert in Chennai Koyambedu",
                titleTa = "சென்னை கோயம்பேட்டில் மழை எச்சரிக்கை",
                messageEn = "Heavy downpour near CMBT. Bus schedules might alter slightly.",
                messageTa = "கோயம்பேடு CMBT பகுதியில் பலத்த மழை. பேருந்து அட்டவணை சற்று மாறக்கூடும்.",
                type = "Weather"
            )
        )
    }
}

// Multilingual Localizer Inline Object
object Trans {
    fun get(key: String, isEnglish: Boolean): String {
        return if (isEnglish) {
            when (key) {
                "app_title" -> "BusLive TN"
                "search_placeholder" -> "Search route, stop, or bus..."
                "home_tab" -> "Track Live"
                "planner_tab" -> "Route AI"
                "chat_tab" -> "Voice AI"
                "services_tab" -> "Services"
                "senior_mode" -> "Senior Mode"
                "normal_mode" -> "Normal Mode"
                "nearby_stops" -> "Nearby Bus Stops"
                "favorites" -> "Saved Favorites"
                "save_favorite" -> "Favorite"
                "route_planner" -> "AI Route Planner"
                "source" -> "Source"
                "destination" -> "Destination"
                "plan_button" -> "Formulate Optimal Routes"
                "fastest" -> "Fastest Route"
                "cheapest" -> "Cheapest Option"
                "least_crowded" -> "Low Crowd Route"
                "emergency_sos" -> "EMERGENCY SOS"
                "sos_sub" -> "Dispatches coordinates and bus data to family"
                "complaints" -> "Complaints & Feedback"
                "complaint_success" -> "Lodged successfully!"
                "live_map" -> "Srinagar-Kanyakumari High-Fidelity Transit Route"
                "eta" -> "Estimated arrival"
                "confidence" -> "Confidence level"
                "crowd" -> "Crowding"
                "low_crowd" -> "Low Crowd"
                "med_crowd" -> "Medium Crowd"
                "heavy_crowd" -> "Heavy Crowd"
                "driver" -> "Pilot / Host"
                "speed" -> "Velocity"
                "delay" -> "Status delay"
                "chatbot_welcome" -> "Hello! I am BusLive Assistant, your real-time Transit AI. Ask me about Tamil Nadu bus routes, ETA, crowds, and stops."
                "listen" -> "Voice Assist"
                "stop_listen" -> "Listening..."
                "auth_login" -> "Access BusLive TN Console"
                "google_login" -> "Sign in with Google API"
                "phone_login" -> "Passwordless Phone OTP"
                "email_login" -> "Secure Email Session"
                "logged_in_as" -> "Operator profile"
                "sign_out" -> "Terminate Session"
                "report_issue" -> "Post Transit Complaint / Breakdown"
                "write_desc" -> "Detail delay, breakdown, or conduct..."
                "voice_desc" -> "Continuous spoken reporting..."
                "submit" -> "File Official Complaint"
                "crowd_warn" -> "Visual Warning: Heavy congestion / crowd density"
                "sos_sent_msg" -> "Emergency dispatch verified with high precision!"
                else -> key
            }
        } else {
            when (key) {
                "app_title" -> "பஸ்லைவ் TN"
                "search_placeholder" -> "பஸ், தடம் அல்லது நிறுத்தம்..."
                "home_tab" -> "நேரலை"
                "planner_tab" -> "AI வழித்தடம்"
                "chat_tab" -> "குரல் AI"
                "services_tab" -> "புகார் / SOS"
                "senior_mode" -> "முதியோர் முறை"
                "normal_mode" -> "இயல்பு முறை"
                "nearby_stops" -> "அருகிலுள்ள நிறுத்தங்கள்"
                "favorites" -> "பிடித்தமானவை"
                "save_favorite" -> "சேமி"
                "route_planner" -> "AI வழித்தடத் திட்டம்"
                "source" -> "புறப்படும் இடம்"
                "destination" -> "சேருமிடம்"
                "plan_button" -> "சிறந்த வழித்தட வழிகள்"
                "fastest" -> "வேகமான வழி"
                "cheapest" -> "மலிவான கட்டணம்"
                "least_crowded" -> "குறைவான நெரிசல்"
                "emergency_sos" -> "அவசரக் குரல் SOS"
                "sos_sub" -> "வண்டியின் விவரங்களையும் நேரலை வரைபடத்தையும் பகிரும்"
                "complaints" -> "புகார்கள் & பதிவுகள்"
                "complaint_success" -> "வெற்றிகரமாக பதிவு செய்யப்பட்டது!"
                "live_map" -> "நேரலை கண்காணிப்பு மற்றும் இருப்பிட தகவமைப்பு"
                "eta" -> "வருகை நேரம்"
                "confidence" -> "நம்பகத்தன்மை"
                "crowd" -> "நெரிசல்"
                "low_crowd" -> "குறைந்த கூட்டம்"
                "med_crowd" -> "மிதமான கூட்டம்"
                "heavy_crowd" -> "அதிக கூட்டம்"
                "driver" -> "ஓட்டுநர்"
                "speed" -> "வேகம்"
                "delay" -> "தாமதம்"
                "chatbot_welcome" -> "வணக்கம்! நான் பஸ்லைவ் குரல் அசிஸ்டண்ட். தமிழகத்தின் பஸ் வழித்தடங்கள், நேரலை இருப்பிடம், வருகை பற்றி கேட்டு தெரிந்து கொள்ளுங்கள்."
                "listen" -> "வாய்ஸ் சப்போர்ட்"
                "stop_listen" -> "கேட்கிறது..."
                "auth_login" -> "பஸ்லைவ் உள்நுழைவு"
                "google_login" -> "கூகுள் கணக்கு உள்நுழைவு"
                "phone_login" -> "OTP மூலம் உள்நுழைக"
                "email_login" -> "மின்னஞ்சல் உள்நுழைவு"
                "logged_in_as" -> "தற்போதைய பயனர்"
                "sign_out" -> "வெளியேறு"
                "report_issue" -> "புகார் அல்லது பழுது பதிவு செய்ய"
                "write_desc" -> "தாமதம், ஓட்டுனர் நடத்தை போன்றவற்றை விவரிக்கவும்..."
                "voice_desc" -> "குரல்வழியாகப் பேச மைக்கை அழுத்தவும்..."
                "submit" -> "புகாரைச் சமர்ப்பி"
                "crowd_warn" -> "பெரிய எச்சரிக்கை: அதிக நெரிசல் நிறைந்த பேருந்து!"
                "sos_sent_msg" -> "இருப்பிடம் மற்றும் பேருந்து எண்களுடன் அவசர செய்தி பகிரப்பட்டது!"
                else -> key
            }
        }
    }
}
