package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.BusLiveViewModel
import com.example.ui.viewmodel.Trans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: BusLiveViewModel,
    isEnglish: Boolean,
    isSenior: Boolean
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    var loginTabState by remember { mutableIntStateOf(0) } // 0 = Google, 1 = Email, 2 = Phone

    val headerFontSize = if (isSenior) 28.sp else 22.sp
    val labelFontSize = if (isSenior) 18.sp else 14.sp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate Dark
                        Color(0xFF1E3A8A)  // Indigo Deep Blue representing Tamil Nadu express lines grid
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f)) // Gold asset trim
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bus logo icon
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "BusLive TN logo",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(60.dp)
                )

                Text(
                    text = Trans.get("auth_login", isEnglish),
                    fontSize = headerFontSize,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isEnglish) "Sign in to backup favorites & file voice complaints" else "பிடித்தவைகளைச் சேமிக்கவும் புகாரைப் பதிவு செய்யவும் உள்நுழையவும்",
                    fontSize = if (isSenior) 16.sp else 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Login Mode Selection Tabs
                TabRow(
                    selectedTabIndex = loginTabState,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = loginTabState == 0,
                        onClick = { loginTabState = 0 },
                        text = { Text("Google", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = loginTabState == 1,
                        onClick = { loginTabState = 1 },
                        text = { Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = loginTabState == 2,
                        onClick = { loginTabState = 2 },
                        text = { Text("Phone", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Conditionally render options
                when (loginTabState) {
                    0 -> {
                        // Google Sign In Simple action trigger
                        Button(
                            onClick = { viewModel.loginSimulated("naveenkumaranbu13@gmail.com") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_login_btn")
                        ) {
                            Text(
                                text = Trans.get("google_login", isEnglish),
                                fontSize = labelFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    1 -> {
                        // Email Sign In Inputs
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("Email address", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = labelFontSize),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("Password", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = labelFontSize),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        Button(
                            onClick = {
                                if (emailInput.isNotBlank()) {
                                    viewModel.loginSimulated(emailInput)
                                } else {
                                    viewModel.loginSimulated("user_tn@buslive.com")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("email_login_btn")
                        ) {
                            Text(
                                text = Trans.get("email_login", isEnglish),
                                fontSize = labelFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    2 -> {
                        // Phone OTP Sign In
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            placeholder = { Text("+91 Mobile Number", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White) },
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = labelFontSize),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            placeholder = { Text("Enter 4-digit OTP", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input"),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = labelFontSize),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        Button(
                            onClick = {
                                if (phoneInput.isNotBlank()) {
                                    viewModel.loginSimulated("+91-$phoneInput")
                                } else {
                                    viewModel.loginSimulated("+91-9443212345")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("phone_login_btn")
                        ) {
                            Text(
                                text = Trans.get("phone_login", isEnglish),
                                fontSize = labelFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Guest Session Access
                TextButton(
                    onClick = { viewModel.loginSimulated("guest_session@buslive.com") },
                    modifier = Modifier.testTag("guest_login_btn")
                ) {
                    Text(
                        text = if (isEnglish) "Operate as Commuter Guest" else "பயணியாகத் தொடரவும்",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = if (isSenior) 18.sp else 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
