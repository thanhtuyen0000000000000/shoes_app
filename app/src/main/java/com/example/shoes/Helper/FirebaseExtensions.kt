
package com.example.shoes.Helper

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> DatabaseReference.setValueAwait(value: T): Unit =
    suspendCancellableCoroutine { continuation ->
        this.setValue(value)
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
