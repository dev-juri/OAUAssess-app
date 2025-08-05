package com.oau.assess.screens.admin.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit = {},
    viewModel: AdminLoginViewModel = koinInject<AdminLoginViewModel>()
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Collect state from ViewModel
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val primaryBlue = Color(0xFF2196F3)
    val lightGray = Color(0xFFF5F5F5)
    val errorRed = Color(0xFFD32F2F)

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            else -> { /* No action needed */ }
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailRegex.matches(email)
    }

    // Validation functions
    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Please enter a valid email address"
            else -> null
        }
        return emailError == null
    }

    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        return passwordError == null
    }

    fun validateForm(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        return isEmailValid && isPasswordValid
    }

    fun handleLogin() {
        if (validateForm()) {
            viewModel.login()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "OAU Assess - Admin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Admin Login",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Access administrative dashboard",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Show network error if exists
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        color = errorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Email Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { newEmail ->
                        viewModel.onEmailChanged(newEmail)
                        if (emailError != null) emailError = null
                    },
                    placeholder = {
                        Text("Enter your email address", color = Color.Gray)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError != null) errorRed else primaryBlue,
                        unfocusedBorderColor = if (emailError != null) errorRed else Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    isError = emailError != null,
                    enabled = uiState !is LoginUiState.Loading
                )

                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = errorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { newPassword ->
                        viewModel.onPasswordChanged(newPassword)
                        if (passwordError != null) passwordError = null
                    },
                    placeholder = {
                        Text("Enter your password", color = Color.Gray)
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = Color.Gray
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError != null) errorRed else primaryBlue,
                        unfocusedBorderColor = if (passwordError != null) errorRed else Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    isError = passwordError != null,
                    enabled = uiState !is LoginUiState.Loading
                )

                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = errorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { handleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    contentColor = Color.White
                ),
                enabled = uiState !is LoginUiState.Loading && email.isNotBlank() && password.isNotBlank()
            ) {
                when (uiState) {
                    is LoginUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Secure admin access only",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}