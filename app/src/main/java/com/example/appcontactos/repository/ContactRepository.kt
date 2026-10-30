package com.example.appcontactos.repository

import com.example.appcontactos.model.Contact
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ContactRepository {
    private val firestore = FirebaseFirestore.getInstance()

    private fun getContactsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("contacts")

    fun getContactsFlow(userId: String): Flow<List<Contact>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = getContactsCollection(userId)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val contacts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Contact::class.java)?.copy(id = doc.id)
                    }
                    trySend(contacts)
                }
            }

        awaitClose { listener.remove() }
    }

    fun getContactById(userId: String, contactId: String, onResult: (Contact?) -> Unit) {
        if (userId.isEmpty() || contactId.isEmpty()) {
            onResult(null)
            return
        }

        getContactsCollection(userId).document(contactId)
            .get()
            .addOnSuccessListener { doc ->
                if ((doc != null) && doc.exists()) {
                    val contact = doc.toObject(Contact::class.java)?.copy(id = doc.id)
                    onResult(contact)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun addContact(contact: Contact, onResult: (Boolean, String?) -> Unit) {
        if (contact.userId.isEmpty()) {
            onResult(false, "Usuario no autenticado")
            return
        }

        val docRef = getContactsCollection(contact.userId).document()
        val contactToSave = contact.copy(id = docRef.id, timestamp = System.currentTimeMillis())

        docRef.set(contactToSave)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Error al guardar el contacto")
            }
    }

    fun updateContact(contact: Contact, onResult: (Boolean, String?) -> Unit) {
        if (contact.userId.isEmpty() || contact.id.isEmpty()) {
            onResult(false, "Información de contacto inválida")
            return
        }

        val contactToUpdate = mapOf(
            "name" to contact.name,
            "phone" to contact.phone,
            "email" to contact.email,
            "address" to contact.address,
            "category" to contact.category,
            "timestamp" to System.currentTimeMillis()
        )

        getContactsCollection(contact.userId).document(contact.id)
            .update(contactToUpdate)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Error al actualizar el contacto")
            }
    }

    fun deleteContact(userId: String, contactId: String, onResult: (Boolean, String?) -> Unit) {
        if (userId.isEmpty() || contactId.isEmpty()) {
            onResult(false, "Parámetros inválidos")
            return
        }

        getContactsCollection(userId).document(contactId)
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Error al eliminar el contacto")
            }
    }
}
