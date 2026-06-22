package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.CrowdLevel
import com.example.data.model.RouteOption
import com.example.ui.viewmodel.BusLiveViewModel
import com.example.ui.viewmodel.Trans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: BusLiveViewModel,
    modifier: Modifier = Modifier
) {
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val isSenior by viewModel.isSeniorMode.collectAsStateWithLifecycle()
    val source by viewModel.plannerSource.collectAsStateWithLifecycle()
    val destination by viewModel.plannerDestination.collectAsStateWithLifecycle()
    val results by viewModel.plannerResults.collectAsStateWithLifecycle()
    val isPlanning by viewModel.isPlanning.collectAsStateWithLifecycle()

    // Dynamic Fonts
    val headerSize = if (isSenior) 26.sp else 22.sp
    val labelSize = if (isSenior) 18.sp else 14.sp
    val detailSize = if (isSenior) 16.sp else 12.sp

    val tnPlaces = listOf("Salem", "Erode", "Coimbatore", "Madurai", "Trichy", "Chennai")

    var showSourceDropdown by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Route planner title
        Text(
            text = Trans.get("route_planner", isEnglish),
            fontSize = headerSize,
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
            border = BorderStroke(
                width = 1.dp,
                color = if (isSenior) Color(0xFFFF8C00) else Color(0xFFE2E8F0)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Source Field
                Box {
                    OutlinedTextField(
                        value = source,
                        onValueChange = {
                            viewModel.setSource(it)
                            showSourceDropdown = true
                        },
                        label = { Text(Trans.get("source", isEnglish), fontSize = labelSize) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_source_input")
                            .clickable { showSourceDropdown = true },
                        textStyle = LocalTextStyle.current.copy(fontSize = labelSize),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    DropdownMenu(
                        expanded = showSourceDropdown,
                        onDismissRequest = { showSourceDropdown = false }
                    ) {
                        tnPlaces.filter { it.contains(source, ignoreCase = true) }.forEach { place ->
                            DropdownMenuItem(
                                text = { Text(place, fontSize = labelSize) },
                                onClick = {
                                    viewModel.setSource(place)
                                    showSourceDropdown = false
                                }
                            )
                        }
                    }
                }

                // Swap Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            val temp = source
                            viewModel.setSource(destination)
                            viewModel.setDestination(temp)
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .shadow(2.dp, RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50.dp))
                            .testTag("swap_cities_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Locations",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Destination Field
                Box {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = {
                            viewModel.setDestination(it)
                            showDestDropdown = true
                        },
                        label = { Text(Trans.get("destination", isEnglish), fontSize = labelSize) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_destination_input")
                            .clickable { showDestDropdown = true },
                        textStyle = LocalTextStyle.current.copy(fontSize = labelSize),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    DropdownMenu(
                        expanded = showDestDropdown,
                        onDismissRequest = { showDestDropdown = false }
                    ) {
                        tnPlaces.filter { it.contains(destination, ignoreCase = true) }.forEach { place ->
                            DropdownMenuItem(
                                text = { Text(place, fontSize = labelSize) },
                                onClick = {
                                    viewModel.setDestination(place)
                                    showDestDropdown = false
                                }
                            )
                        }
                    }
                }

                // Run Plan Button
                Button(
                    onClick = { viewModel.runRoutePlanner() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("run_planner_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSenior) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                        contentColor = if (isSenior) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isPlanning) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = Trans.get("plan_button", isEnglish),
                            fontSize = if (isSenior) 20.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Planning Outputs UI
        AnimatedVisibility(visible = results.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isEnglish) "AI Smart Recommendations" else "AI சிறந்த வழித்தட பரிந்துரைகள்",
                    fontSize = if (isSenior) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                results.forEach { option ->
                    RouteOptionCard(
                        option = option,
                        isEnglish = isEnglish,
                        isSenior = isSenior,
                        onSpeakSummary = {
                            val info = if (isEnglish) {
                                "The recommended route of type: ${option.typeEn} will take ${option.durationMinutes} minutes with an estimated fare of ${option.costInr} Rupees. Path includes: ${option.pathLines.joinToString(" then ")}. ${option.summaryEn}"
                            } else {
                                "${option.typeTa} வகையைச் சார்ந்த பரிந்துரைக்கப்பட்ட வழித்தடம் ${option.durationMinutes} நிமிடங்கள் ஆகும். இதன் பயணக் கட்டணம் ${option.costInr} ரூபாய். கடந்து செல்லும் நிலையங்கள்: ${option.pathLines.joinToString(" பிறகு ")}. ${option.summaryTa}"
                            }
                            viewModel.speakText(info)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RouteOptionCard(
    option: RouteOption,
    isEnglish: Boolean,
    isSenior: Boolean,
    onSpeakSummary: () -> Unit
) {
    val crowdColor = when (option.crowdLevel) {
        CrowdLevel.LOW -> Color(0xFF10B981)
        CrowdLevel.MEDIUM -> Color(0xFFF59E0B)
        CrowdLevel.HEAVY -> Color(0xFFEF4444)
    }

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
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = "Bus Recommendation",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isEnglish) option.typeEn else option.typeTa,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 20.sp else 16.sp,
                        color = if (isSenior) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                // Volume announcement button
                IconButton(
                    onClick = onSpeakSummary,
                    modifier = Modifier.testTag("speak_route_${option.typeEn.replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Sub metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${option.durationMinutes} mins • ₹${option.costInr}",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSenior) 18.sp else 14.sp
                )

                Text(
                    text = if (isEnglish) "${option.changesCount} Changes" else "${option.changesCount} பஸ் மாற்றம்",
                    fontSize = if (isSenior) 16.sp else 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Path Nodes visual chain
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                option.pathLines.forEachIndexed { index, stop ->
                    Text(
                        text = stop,
                        fontSize = if (isSenior) 14.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (index < option.pathLines.size - 1) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "then",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(12.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            // Text description summary
            Text(
                text = if (isEnglish) option.summaryEn else option.summaryTa,
                fontSize = if (isSenior) 16.sp else 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = if (isSenior) 22.sp else 16.sp
            )

            // Warning indicators (crowd check)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(crowdColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isEnglish) "Forecast Crowd: ${option.crowdLevel.name}" else "எதிர்பார்க்கப்படும் கூட்டம்: ${option.crowdLevel.name}",
                    fontSize = if (isSenior) 14.sp else 11.sp,
                    color = crowdColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
