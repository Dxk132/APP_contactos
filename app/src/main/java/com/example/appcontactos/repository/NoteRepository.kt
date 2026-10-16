package com.example.appcontactos.repository

import com.example.appcontactos.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NoteRepository {
    private val firestore = FirebaseFirestore.getInstance()

    private fun getNotesCollection(userId: String) =
        firestore.collection("users").document(userId).collection("notes")

    fun getNotesFlow(userId: String): Flow<List<Note>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = getNotesCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Note::class.java)?.copy(id = doc.id)
                    }
                    trySend(notes)
                }
            }

        awaitClose { listener.remove() }
    }

    fun getNoteById(userId: String, noteId: String, onResult: (Note?) -> Unit) {
        if (userId.isEmpty() || noteId.isEmpty()) {
            onResult(null)
            return
        }

        getNotesCollection(userId).document(noteId)
            .get()
            .addOnSuccessListener { doc ->
                if ((doc != null) && doc.exists()) {
                    val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
                    onResult(note)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun addNote(note: Note, onResult: (Boolean, String?) -> Unit) {
        if (note.userId.isEmpty()) {
            onResult(false, "Usuario no autenticado")
            return
        }

        val docRef = getNotesCollection(note.userId).document()
        val noteToSave = note.copy(id = docRef.id, timestamp = System.currentTimeMillis())

        docRef.set(noteToSave)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Error al guardar la nota")
            }
    }

    fun updateNote(note: Note, onResult: (Boolean, String?) -> Unit) {
        if (note.userId.isEmpty() || note.id.isEmpty()) {
            onResult(false, "Información de nota inválida")
            return
        }

        val noteToUpdate = mapOf(
            "title" to note.title,
            "content" to note.content,
            "timestamp" to System.currentTimeMillis()
        )

        getNotesCollection(note.userId).document(note.id)
            .update(noteToUpdate)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Error al actualizar la nota")
            }
    }

    fun deleteNote(userId: String, noteId: String, onResult: (Boolean, String?) -> Unit) {
        if (userId.isEmpty() || noteId.isEmpty()) {
            onResult(false, "Parámetros inválidos")
            return
        }

        getNotesCollection(userId).document(noteId)
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Error al eliminar la nota")
            }
    }
}
