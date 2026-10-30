package com.example.appcontactos.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appcontactos.model.Contact
import com.example.appcontactos.repository.ContactRepository
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contactId: String? = null,
    onNavigateBack: () -> Unit,
    repository: ContactRepository = remember { ContactRepository() }
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val presetCategories = remember {
        listOf("Familia", "Amigos", "Compañeros", "Clientes", "Profesores", "Trabajo")
    }
    var selectedCategory by remember { mutableStateOf("Familia") }
    var customCategory by remember { mutableStateOf("") }
    var isCustomCategory by remember { mutableStateOf(false) }

    var isLoadingContact by remember { mutableStateOf(contactId != null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = contactId != null

    LaunchedEffect(contactId) {
        if (contactId != null && userId.isNotEmpty()) {
            repository.getContactById(userId, contactId) { contact ->
                isLoadingContact = false
                if (contact != null) {
                    name = contact.name
                    phone = contact.phone
                    email = contact.email
                    address = contact.address

                    if (presetCategories.contains(contact.category)) {
                        selectedCategory = contact.category
                        isCustomCategory = false
                    } else if (contact.category.isNotBlank()) {
                        isCustomCategory = true
                        customCategory = contact.category
                    }
                } else {
                    errorMessage = "No se pudo cargar el contacto."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Contacto" else "Nuevo Contacto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoadingContact) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        label = { Text("Nombre Completo *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            errorMessage = null
                        },
                        label = { Text("Teléfono *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Correo Electrónico (Opcional)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            errorMessage = null
                        },
                        label = { Text("Dirección (Opcional)") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Selección de Etiquetas Inteligentes
                    Text(
                        text = "Etiquetas inteligentes / Categoría",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetCategories.forEach { category ->
                            FilterChip(
                                selected = !isCustomCategory && selectedCategory == category,
                                onClick = {
                                    isCustomCategory = false
                                    selectedCategory = category
                                },
                                label = { Text(category) }
                            )
                        }
                        FilterChip(
                            selected = isCustomCategory,
                            onClick = { isCustomCategory = true },
                            label = { Text("Personalizado") }
                        )
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customCategory,
                            onValueChange = { customCategory = it },
                            label = { Text("Escribe tu etiqueta personalizada") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            val cleanName = name.trim()
                            val cleanPhone = phone.trim()
                            val cleanEmail = email.trim()
                            val cleanAddress = address.trim()
                            val finalCategory = if (isCustomCategory) {
                                customCategory.trim().ifBlank { "General" }
                            } else {
                                selectedCategory
                            }

                            if (cleanName.isBlank() || cleanPhone.isBlank()) {
                                errorMessage = "El Nombre y el Teléfono son campos obligatorios."
                                return@Button
                            }

                            isSaving = true
                            errorMessage = null

                            if (isEditing && contactId != null) {
                                val updatedContact = Contact(
                                    id = contactId,
                                    name = cleanName,
                                    phone = cleanPhone,
                                    email = cleanEmail,
                                    address = cleanAddress,
                                    category = finalCategory,
                                    userId = userId
                                )
                                repository.updateContact(updatedContact) { success, error ->
                                    isSaving = false
                                    if (success) {
                                        onNavigateBack()
                                    } else {
                                        errorMessage = error ?: "Error al actualizar el contacto."
                                    }
                                }
                            } else {
                                val newContact = Contact(
                                    name = cleanName,
                                    phone = cleanPhone,
                                    email = cleanEmail,
                                    address = cleanAddress,
                                    category = finalCategory,
                                    userId = userId
                                )
                                repository.addContact(newContact) { success, error ->
                                    isSaving = false
                                    if (success) {
                                        onNavigateBack()
                                    } else {
                                        errorMessage = error ?: "Error al guardar el contacto."
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isEditing) "Actualizar Contacto" else "Guardar Contacto")
                        }
                    }
                }
            }
        }
    }
}
