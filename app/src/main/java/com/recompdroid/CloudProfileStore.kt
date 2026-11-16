package com.recompdroid

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class CloudGameProgress(
    val title: String,
    val playSeconds: Long,
    val launches: Long,
)

class CloudProfileStore {
    private val db = FirebaseFirestore.getInstance()

    fun saveProfile(
        uid: String,
        displayName: String,
        username: String,
        photoBase64: String?,
        onComplete: (Exception?) -> Unit,
    ) {
        val values = mutableMapOf<String, Any>(
            "displayName" to displayName.take(60),
            "username" to username.take(24),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (photoBase64 != null) values["photoBase64"] = photoBase64.take(700_000)
        db.collection("users").document(uid).set(values, com.google.firebase.firestore.SetOptions.merge())
            .addOnCompleteListener { onComplete(if (it.isSuccessful) null else it.exception) }
    }

    fun loadProfile(uid: String, onComplete: (Map<String, Any>?, Exception?) -> Unit) {
        db.collection("users").document(uid).get().addOnCompleteListener { task ->
            onComplete(task.result?.data, if (task.isSuccessful) null else task.exception)
        }
    }

    fun loadProgress(uid: String, onComplete: (List<CloudGameProgress>, Exception?) -> Unit) {
        db.collection("users").document(uid).collection("games").get().addOnCompleteListener { task ->
            val rows = if (task.isSuccessful) task.result?.documents.orEmpty().mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                CloudGameProgress(
                    title = title,
                    playSeconds = doc.getLong("playSeconds") ?: 0L,
                    launches = doc.getLong("launches") ?: 0L,
                )
            } else emptyList()
            onComplete(rows, if (task.isSuccessful) null else task.exception)
        }
    }

    fun recordPlay(uid: String, slug: String, title: String, seconds: Long) {
        if (seconds <= 0) return
        db.collection("users").document(uid).collection("games").document(slug.take(120))
            .set(
                mapOf(
                    "title" to title.take(160),
                    "playSeconds" to FieldValue.increment(seconds),
                    "launches" to FieldValue.increment(1),
                    "lastPlayedAt" to FieldValue.serverTimestamp(),
                ),
                com.google.firebase.firestore.SetOptions.merge(),
            )
    }
}
