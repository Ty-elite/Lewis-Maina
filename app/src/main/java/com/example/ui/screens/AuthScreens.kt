package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.MathCaptchaWidget
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun LoginScreen(
    viewModel: KenyaRentViewModel,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var captchaAnswer by remember { mutableStateOf("") }

    val authError by viewModel.authError.collectAsState()
    val otpRequiredUser by viewModel.otpVerificationRequired.collectAsState()
    val captchaVal1 by viewModel.captchaValue1.collectAsState()
    val captchaVal2 by viewModel.captchaValue2.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Identity
            Text(
                text = "KenyaRent",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen
            )
            Text(
                text = "Find your perfect home in Kenya.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card inputs Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Access Your Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = "Toggle Visibility")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CAPTCHA Puzzle Widget
                    MathCaptchaWidget(
                        val1 = captchaVal1,
                        val2 = captchaVal2,
                        answer = captchaAnswer,
                        onAnswerChanged = { captchaAnswer = it },
                        onRegenerate = { viewModel.regenerateCaptcha() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error presentation
                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.login(email, password, captchaAnswer)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Secure Login")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form switcher link text
            Row(
                modifier = Modifier.clickable { onNavigateToRegister() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New occupant or landlord? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Create Registered Account",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Standard 2FA Landlord OTP Verification Screen Overlay
        AnimatedVisibility(
            visible = otpRequiredUser != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            val focusManager = LocalFocusManager.current
            var otpEntered by remember { mutableStateOf("") }
            val otpError by viewModel.otpError.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.mdfix()),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "2FA Verification Required",
                            tint = KenyaGreen,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "2-Factor Authentication",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Strict landlord verification required. We have sent a simulated M-PESA registered OTP code to your number to authorize dashboard updates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = otpEntered,
                            onValueChange = { otpEntered = it },
                            label = { Text("Secure 4-Digit OTP") },
                            placeholder = { Text("Enter 2541 or 1234 to bypass") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (otpError != null) {
                            Text(
                                text = otpError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.verifyOTP(otpEntered)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Validate landlord access")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { viewModel.cancelOTP() }
                        ) {
                            Text("Cancel Verification", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: KenyaRentViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLandlord by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val scrollState = rememberScrollState()

    // Calculate strong password features criteria (Security guideline!)
    val isMinLength = password.length >= 8
    val hasCapital = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    val strengthProgress = (if (isMinLength) 0.25f else 0f) +
            (if (hasCapital) 0.25f else 0f) +
            (if (hasDigit) 0.25f else 0f) +
            (if (hasSpecial) 0.25f else 0f)

    val strengthColor = if (strengthProgress <= 0.25f) Color.Red
    else if (strengthProgress <= 0.75f) Color(0xFFE5A93B)
    else KenyaGreen

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "KenyaRent Hub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen
            )
            Text(
                text = "Connect with thousands of vacant listings today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Register Account profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mobile active line (For 2FA SMS OTP delivery confirmation)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Kenyan Mobile Line") },
                        placeholder = { Text("e.g. 0712345678") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = "Toggle Visibility")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Strong Password rating indicator
                    LinearProgressIndicator(
                        progress = { strengthProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = strengthColor,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (strengthProgress <= 0.25f) "Weak password (add capital, digit, & complex symbol)"
                        else if (strengthProgress <= 0.75f) "Fairly secure password"
                        else "Strong secure password protected!",
                        fontSize = 10.sp,
                        color = strengthColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role Picker Swit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Landlord Partner Mode?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Allows you to post and manage vacant property units.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isLandlord,
                            onCheckedChange = { isLandlord = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = KenyaGreen, checkedTrackColor = KenyaGreen.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error presentation
                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.register(
                                email = email,
                                password = password,
                                fullName = fullName,
                                phone = phone,
                                role = if (isLandlord) UserRole.LANDLORD else UserRole.SEEKER
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Account Profile")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.clickable { onNavigateToLogin() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already registered? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Log In Here",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun Int.mdfix(): androidx.compose.ui.unit.Dp {
    return this.dp
}
