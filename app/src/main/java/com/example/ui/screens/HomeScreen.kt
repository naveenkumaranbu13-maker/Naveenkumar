package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.BusLiveViewModel
import com.example.ui.viewmodel.Trans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BusLiveViewModel,
    modifier: Modifier = Modifier
) {
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val isSenior by viewModel.isSeniorMode.collectAsStateWithLifecycle()
    val buses by viewModel.buses.collectAsStateWithLifecycle()
    val selectedBus by viewModel.selectedBus.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())

    // Font Sizes scaling with Senior Mode
    val titleFontSize = if (isSenior) 26.sp else 20.sp
    val bodyFontSize = if (isSenior) 18.sp else 14.sp
    val labelFontSize = if (isSenior) 16.sp else 12.sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = {
                Text(
                    text = Trans.get("search_placeholder", isEnglish),
                    fontSize = bodyFontSize
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        viewModel.toggleVoiceListening { spokenQuery ->
                            viewModel.updateSearchQuery(spokenQuery)
                        }
                    },
                    modifier = Modifier.testTag("mic_speak_btn")
                ) {
                    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = if (isListening) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_bar")
                .shadow(1.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = bodyFontSize),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        // Maps/Live tracking viewport Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .shadow(4.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSenior) Color(0xFF0F172A) else Color(0xFF006A4D)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive dynamic map simulator
                BusLiveMapCanvas(selectedBus = selectedBus)

                // Bento Grid live tracking pill overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "LIVE NOW" else "நேரலை",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4ADE80), CircleShape)
                        )
                        Text(
                            text = if (isEnglish) "GPS: High" else "GPS: உயர்தர",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // High Contrast Map Overlays
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.70f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = Trans.get("live_map", isEnglish),
                        color = Color.White,
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Bold
                    )
                    selectedBus?.let {
                        Text(
                            text = "${it.busNumber}: ${if (isEnglish) it.sourceEn else it.sourceTa} ➔ ${if (isEnglish) it.destEn else it.destTa}",
                            color = Color(0xFFFFD700),
                            fontSize = if (isSenior) 16.sp else 12.sp
                        )
                    }
                }
            }
        }

        // Selected Bus Insights with ETA and Confidence
        selectedBus?.let { bus ->
            BusDetailsBadge(
                bus = bus,
                isEnglish = isEnglish,
                isSenior = isSenior,
                onSaveFavorite = {
                    viewModel.toggleSaveFavorite(
                        bus.busNumber,
                        if (isEnglish) bus.sourceEn else bus.sourceTa,
                        if (isEnglish) bus.destEn else bus.destTa
                    )
                },
                isSaved = favorites.any { it.busNumber == bus.busNumber }
            )
        }

        // Live Bus list selector
        Text(
            text = if (isEnglish) "Buses in Service" else "சேவையில் உள்ள பேருந்துகள்",
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            buses.forEach { bus ->
                val isSelected = selectedBus?.busNumber == bus.busNumber
                BusRowCard(
                    bus = bus,
                    isSelected = isSelected,
                    isEnglish = isEnglish,
                    isSenior = isSenior,
                    onClick = { viewModel.selectBus(bus) }
                )
            }
        }

        // Nearby Stops
        Text(
            text = Trans.get("nearby_stops", isEnglish),
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (stop in viewModel.mockBusStops) {
                NearbyStopRowCard(
                    stop = stop,
                    isEnglish = isEnglish,
                    isSenior = isSenior,
                    onVoiceNav = {
                        val directions = if (isEnglish) {
                            "Navigating to ${stop.nameEn}. It is ${stop.distanceMeters} meters away. Available buses: ${stop.availableBuses.joinToString(", ")}."
                        } else {
                            "வழி காட்டப்படுகிறது: ${stop.nameTa}. இது ${stop.distanceMeters} மீட்டர் தொலைவில் உங்களது இடத்திலிருந்து உள்ளது. பேருந்துகள்: ${stop.availableBuses.joinToString(", ")}."
                        }
                        viewModel.speakText(directions)
                    }
                )
            }
        }
    }
}

@Composable
fun BusLiveMapCanvas(selectedBus: BusLiveStatus?) {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar pulse")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulseFraction"
    )

    val busAnimOffset = remember { Animatable(0f) }
    LaunchedEffect(selectedBus) {
        busAnimOffset.snapTo(0f)
        busAnimOffset.animateTo(
            targetValue = 1f,
            animationSpec = tween(4000, easing = LinearEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background GRID lines (Aesthetic dark modern design)
        val gridLinesCount = 8
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        for (i in 1..gridLinesCount) {
            val ratio = i.toFloat() / (gridLinesCount + 1)
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, height * ratio),
                end = Offset(width, height * ratio),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(width * ratio, 0f),
                end = Offset(width * ratio, height),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
        }

        // Draw NH-544/Highway Polyline (S-Curve path)
        val routePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.1f, height * 0.8f) // Salem
            cubicTo(
                width * 0.3f, height * 0.4f,
                width * 0.6f, height * 0.9f,
                width * 0.9f, height * 0.2f // Coimbatore
            )
        }

        drawPath(
            routePath,
            color = Color(0xFF475569),
            style = Stroke(width = 6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
        )

        drawPath(
            routePath,
            color = Color(0xFF10B981).copy(alpha = 0.5f),
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw Nodes
        val salemCoord = Offset(width * 0.1f, height * 0.8f)
        val erodeCoord = Offset(width * 0.45f, height * 0.63f)
        val coimbatoreCoord = Offset(width * 0.9f, height * 0.2f)

        // Terminal labels
        drawCircle(Color(0xFFEF4444), radius = 8.dp.toPx(), center = salemCoord)
        drawCircle(Color(0xFF3B82F6), radius = 6.dp.toPx(), center = erodeCoord)
        drawCircle(Color(0xFFEF4444), radius = 8.dp.toPx(), center = coimbatoreCoord)

        // User Live GPS Coordinates Beacon (Pulse radar)
        val userCoord = Offset(width * 0.3f, height * 0.5f)
        drawCircle(
            color = Color(0xFF60A5FA).copy(alpha = 1f - pulseRatio),
            radius = 24.dp.toPx() * pulseRatio,
            center = userCoord,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(Color(0xFF3B82F6), radius = 5.dp.toPx(), center = userCoord)

        // Map live moving buses
        selectedBus?.let {
            // Find coordinates along path
            // Simplistic linear approximation along bezier
            val t = busAnimOffset.value
            val busX = (1 - t) * (1 - t) * salemCoord.x + 2 * (1 - t) * t * erodeCoord.x + t * t * coimbatoreCoord.x
            val busY = (1 - t) * (1 - t) * salemCoord.y + 2 * (1 - t) * t * erodeCoord.y + t * t * coimbatoreCoord.y

            val vehicleCoord = Offset(busX, busY)

            // Neon glowing bus beacon
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                radius = 16.dp.toPx(),
                center = vehicleCoord
            )
            drawCircle(
                color = Color(0xFFFFD700),
                radius = 8.dp.toPx(),
                center = vehicleCoord
            )
        }
    }
}

@Composable
fun BusDetailsBadge(
    bus: BusLiveStatus,
    isEnglish: Boolean,
    isSenior: Boolean,
    onSaveFavorite: () -> Unit,
    isSaved: Boolean
) {
    val crowdColor = when (bus.crowdLevel) {
        CrowdLevel.LOW -> Color(0xFF10B981)
        CrowdLevel.MEDIUM -> Color(0xFFF59E0B)
        CrowdLevel.HEAVY -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSenior) Color(0xFF0F172A) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSenior) Color(0xFFFFE082) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = "Bus Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Bus ${bus.busNumber}",
                        fontSize = if (isSenior) 26.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSenior) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Favorite Toggle
                IconButton(
                    onClick = onSaveFavorite,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("favorite_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Save Favorite",
                        tint = if (isSaved) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Multi-column metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = Trans.get("eta", isEnglish),
                        fontSize = if (isSenior) 16.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${bus.etaMinutes} mins",
                        fontSize = if (isSenior) 22.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSenior) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = Trans.get("confidence", isEnglish),
                        fontSize = if (isSenior) 16.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${bus.confidencePercent}%",
                            fontSize = if (isSenior) 22.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Column {
                    Text(
                        text = Trans.get("crowd", isEnglish),
                        fontSize = if (isSenior) 16.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(crowdColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isEnglish) bus.crowdLevel.name else when (bus.crowdLevel) {
                                CrowdLevel.LOW -> "குறைவான கூட்டம்"
                                CrowdLevel.MEDIUM -> "மிதமான கூட்டம்"
                                CrowdLevel.HEAVY -> "அதிக கூட்டம்"
                            },
                            color = crowdColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 16.sp else 12.sp
                        )
                    }
                }
            }

            // Route & Pilots info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${if (isEnglish) bus.sourceEn else bus.sourceTa} ➔ ${if (isEnglish) bus.destEn else bus.destTa}",
                    fontWeight = FontWeight.Medium,
                    fontSize = if (isSenior) 18.sp else 14.sp
                )
                Text(
                    text = "${Trans.get("driver", isEnglish)}: ${if (isEnglish) bus.driverNameEn else bus.driverNameTa} | ${Trans.get("speed", isEnglish)}: ${bus.speedKmh} km/h",
                    fontSize = if (isSenior) 16.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BusRowCard(
    bus: BusLiveStatus,
    isSelected: Boolean,
    isEnglish: Boolean,
    isSenior: Boolean,
    onClick: () -> Unit
) {
    val crowdColor = when (bus.crowdLevel) {
        CrowdLevel.LOW -> Color(0xFF10B981)
        CrowdLevel.MEDIUM -> Color(0xFFF59E0B)
        CrowdLevel.HEAVY -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .testTag("bus_${bus.busNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (isSenior) Color(0xFF1E3A8A) else MaterialTheme.colorScheme.primaryContainer
            } else {
                if (isSenior) Color(0xFF172554) else Color.White
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFFF8C00) else Color(0xFFE2E8F0)
        )
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Bus indicator
                Box(
                    modifier = Modifier
                        .size(if (isSenior) 54.dp else 44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = "Bus",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isSenior) 30.dp else 24.dp)
                    )
                }

                // Core labels
                Column {
                    Text(
                        text = "Bus ${bus.busNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 22.sp else 16.sp
                    )
                    Text(
                        text = "${if (isEnglish) bus.destEn else bus.destTa} (eta ${bus.etaMinutes}m)",
                        fontSize = if (isSenior) 16.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Crowd Level Indicator Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(crowdColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEnglish) bus.crowdLevel.name else when (bus.crowdLevel) {
                        CrowdLevel.LOW -> "குறைவான "
                        CrowdLevel.MEDIUM -> "மிதமான "
                        CrowdLevel.HEAVY -> "அதிகம்"
                    },
                    color = crowdColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSenior) 14.sp else 11.sp
                )
            }
        }
    }
}

@Composable
fun NearbyStopRowCard(
    stop: BusStop,
    isEnglish: Boolean,
    isSenior: Boolean,
    onVoiceNav: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSenior) Color(0xFF0F172A) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSenior) Color(0xFFFF8C00) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = if (isEnglish) stop.nameEn else stop.nameTa,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSenior) 18.sp else 14.sp
                )
                Text(
                    text = "${stop.distanceMeters}m away • ${Trans.get("eta", isEnglish)}: ${stop.etaMinutes} mins",
                    fontSize = if (isSenior) 15.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Buses: ${stop.availableBuses.joinToString(", ")}",
                    fontSize = if (isSenior) 14.sp else 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Compass Navigation audio beacon
            IconButton(
                onClick = onVoiceNav,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("voice_nav_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = "Navigate speaking directions",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isSenior) 30.dp else 24.dp)
                )
            }
        }
    }
}
