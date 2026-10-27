package com.example.appcontactos

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Correo Electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Contraseña (mínimo 6 caracteres)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmar Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()
                    val cleanConfirm = confirmPassword.trim()

                    if (cleanEmail.isBlank() || cleanPassword.isBlank() || cleanConfirm.isBlank()) {
                        errorMessage = "Por favor, completa todos los campos."
                        return@Button
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                        errorMessage = "Ingresa un correo electrónico válido."
                        return@Button
                    }

                    if (cleanPassword.length < 6) {
                        errorMessage = "La contraseña debe tener al menos 6 caracteres."
                        return@Button
                    }

                    if (cleanPassword != cleanConfirm) {
                        errorMessage = "Las contraseñas no coinciden."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onRegisterSuccess()
                            } else {
                                val exception = task.exception
                                errorMessage = when (exception) {
                                    is FirebaseAuthWeakPasswordException ->
                                        "La contraseña es demasiado débil. Debe tener al menos 6 caracteres."
                                    is FirebaseAuthUserCollisionException ->
                                        "Este correo electrónico ya está registrado. Intenta iniciar sesión."
                                    else -> {
                                        val msg = exception?.localizedMessage ?: ""
                                        if (msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)) {
                                            "Firebase Authentication no está activado en la consola de Firebase.\n\nSolución: En Firebase Console -> Authentication -> haz clic en 'Comenzar' (Get started) y habilita el método 'Correo electrónico/Contraseña'."
                                        } else if (msg.contains("disabled", ignoreCase = true) || msg.contains("provider", ignoreCase = true)) {
                                            "El inicio de sesión con Correo/Contraseña está deshabilitado en Firebase Console. Actívalo en Authentication -> Sign-in method."
                                        } else if (msg.contains("network", ignoreCase = true)) {
                                            "Error de red. Verifica tu conexión a Internet."
                                        } else {
                                            msg.ifBlank { "Error al registrar el usuario." }
                                        }
                                    }
                                }
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateBack,
                enabled = !isLoading
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
