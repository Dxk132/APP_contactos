package com.example.appcontactos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appcontactos.model.Note
import com.example.appcontactos.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: String? = null,
    onNavigateBack: () -> Unit,
    repository: NoteRepository = remember { NoteRepository() }
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoadingNote by remember { mutableStateOf(noteId != null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = noteId != null

    LaunchedEffect(noteId) {
        if (noteId != null && userId.isNotEmpty()) {
            repository.getNoteById(userId, noteId) { note ->
                isLoadingNote = false
                if (note != null) {
                    title = note.title
                    content = note.content
                } else {
                    errorMessage = "No se pudo cargar la nota."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Nota" else "Nueva Nota") },
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
            if (isLoadingNote) {
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Contenido de la nota") },
                        minLines = 6,
                        maxLines = 12,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (title.isBlank() && content.isBlank()) {
                                errorMessage = "Ingresa al menos un título o contenido."
                                return@Button
                            }

                            isSaving = true
                            errorMessage = null

                            if (isEditing && noteId != null) {
                                val updatedNote = Note(
                                    id = noteId,
                                    title = title.trim(),
                                    content = content.trim(),
                                    userId = userId
                                )
                                repository.updateNote(updatedNote) { success, error ->
                                    isSaving = false
                                    if (success) {
                                        onNavigateBack()
                                    } else {
                                        errorMessage = error ?: "Error al actualizar la nota."
                                    }
                                }
                            } else {
                                val newNote = Note(
                                    title = title.trim(),
                                    content = content.trim(),
                                    userId = userId
                                )
                                repository.addNote(newNote) { success, error ->
                                    isSaving = false
                                    if (success) {
                                        onNavigateBack()
                                    } else {
                                        errorMessage = error ?: "Error al guardar la nota."
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
                            Text(if (isEditing) "Actualizar Nota" else "Guardar Nota")
                        }
                    }
                }
            }
        }
    }
}
