package com.focusapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthenticationScreen(
    onSignIn: (String, String) -> Boolean,
    onSignUp: (String, String) -> Boolean,
    theme: String
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val backgroundColor = if (theme == "dark") Color(0xFF181C14) else Color(0xFFFBFBFB)
    val textColor = if (theme == "dark") Color(0xFFECDFCC) else Color.Black
    val buttonColor = Color(0xFF4CAF50)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "📊 Focus App",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Title
            Text(
                text = if (isSignUpMode) "Create Account" else "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = ""
                },
                label = { Text("Email", color = textColor.copy(alpha = 0.7f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = buttonColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = ""
                },
                label = { Text("Password", color = textColor.copy(alpha = 0.7f)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = buttonColor,
                    unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = buttonColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isSignUpMode) 16.dp else 24.dp)
            )

            // Confirm Password Field (Sign Up only)
            if (isSignUpMode) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        errorMessage = ""
                    },
                    label = { Text("Confirm Password", color = textColor.copy(alpha = 0.7f)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = buttonColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Action Button
            Button(
                onClick = {
                    // Validation
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    
                    if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "Invalid email format"
                        return@Button
                    }
                    
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    
                    if (isSignUpMode) {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }
                        
                        val success = onSignUp(email, password)
                        if (!success) {
                            errorMessage = "Email already exists"
                        }
                    } else {
                        val success = onSignIn(email, password)
                        if (!success) {
                            errorMessage = "Invalid email or password"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isSignUpMode) "Create Account" else "Sign In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        errorMessage = ""
                        password = ""
                        confirmPassword = ""
                    }
                ) {
                    Text(
                        text = if (isSignUpMode) "Sign In" else "Sign Up",
                        color = buttonColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
