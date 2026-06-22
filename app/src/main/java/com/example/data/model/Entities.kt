package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "favorites")
@JsonClass(generateAdapter = true)
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "route" or "stop"
    val title: String, // e.g. "Home to College" or "Salem Bus Stand"
    val source: String = "",
    val destination: String = "",
    val busNumber: String = ""
)

@Entity(tableName = "complaints")
@JsonClass(generateAdapter = true)
data class Complaint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Rash Driving", "Bus Delay", "Overcrowding", "Breakdown"
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val status: String = "Pending Review" // "Pending Review", "Under Investigation", "Resolved"
)

@Entity(tableName = "notifications")
@JsonClass(generateAdapter = true)
data class AlertNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleEn: String,
    val titleTa: String,
    val messageEn: String,
    val messageTa: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "Delay", "Weather", "Congestion", "Arrival"
    var isRead: Boolean = false
)

enum class CrowdLevel {
    LOW, MEDIUM, HEAVY
}

data class BusStop(
    val id: String,
    val nameEn: String,
    val nameTa: String,
    val distanceMeters: Int,
    val availableBuses: List<String>,
    val etaMinutes: Int
)

data class BusLiveStatus(
    val busNumber: String,
    val driverNameEn: String,
    val driverNameTa: String,
    val sourceEn: String,
    val sourceTa: String,
    val destEn: String,
    val destTa: String,
    val currentStopEn: String,
    val currentStopTa: String,
    val speedKmh: Int,
    val etaMinutes: Int,
    val confidencePercent: Int,
    val crowdLevel: CrowdLevel,
    val coordinates: Pair<Float, Float>, // Mock coordinates for custom tracking view
    val delayMinutes: Int = 0
)

data class RouteOption(
    val typeEn: String, // "Fastest", "Cheapest", "Least Crowded", "Minimum Bus Changes"
    val typeTa: String,
    val durationMinutes: Int,
    val costInr: Int,
    val changesCount: Int,
    val crowdLevel: CrowdLevel,
    val summaryEn: String,
    val summaryTa: String,
    val pathLines: List<String>
)

data class ChatMessage(
    val id: String,
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false
) {
    enum class Sender {
        USER, ASSISTANT, SYSTEM
    }
}
