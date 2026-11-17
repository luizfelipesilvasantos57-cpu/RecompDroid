package com.recompdroid

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.security.MessageDigest

data class GameReview(
    val rating: Int,
    val comment: String,
)

data class GameReviewWriteResult(
    val success: Boolean,
    val error: Exception? = null,
)

class GameReviewsStore {
    private val db = FirebaseFirestore.getInstance()

    fun loadReviews(gameSlug: String, onComplete: (List<GameReview>, Exception?) -> Unit) {
        val gameId = gameDocumentId(gameSlug)
        db.collection("games").document(gameId).collection("reviews")
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnCompleteListener { task ->
                val reviews = if (task.isSuccessful) {
                    task.result?.documents.orEmpty().mapNotNull { document ->
                        val rating = document.getLong("rating")?.toInt() ?: return@mapNotNull null
                        val comment = document.getString("comment") ?: return@mapNotNull null
                        GameReview(rating, comment)
                    }
                } else emptyList()
                onComplete(reviews, if (task.isSuccessful) null else task.exception)
            }
    }

    fun loadMyReview(uid: String, gameSlug: String, onComplete: (GameReview?, Exception?) -> Unit) {
        val gameId = gameDocumentId(gameSlug)
        db.collection("users").document(uid).collection("reviews").document(gameId).get()
            .addOnCompleteListener { task ->
                val review = if (task.isSuccessful && task.result?.exists() == true) {
                    val data = task.result!!
                    val rating = data.getLong("rating")?.toInt()
                    val comment = data.getString("comment")
                    if (rating != null && comment != null) GameReview(rating, comment) else null
                } else null
                onComplete(review, if (task.isSuccessful) null else task.exception)
            }
    }

    fun saveReview(
        uid: String,
        gameSlug: String,
        rating: Int,
        comment: String,
        onComplete: (GameReviewWriteResult) -> Unit,
    ) {
        val gameId = gameDocumentId(gameSlug)
        val ownerRef = db.collection("users").document(uid).collection("reviews").document(gameId)
        val publicReviews = db.collection("games").document(gameId).collection("reviews")
        ownerRef.get().addOnSuccessListener { existing ->
            val existingId = existing.getString("reviewId")
            val reviewId = if (!existingId.isNullOrBlank()) existingId else publicReviews.document().id
            val publicRef = publicReviews.document(reviewId)
            val timestamp = FieldValue.serverTimestamp()
            val ownerData = mapOf(
                "reviewId" to reviewId,
                "rating" to rating,
                "comment" to comment.trim().take(500),
                "updatedAt" to timestamp,
            )
            val publicData = mapOf(
                "rating" to rating,
                "comment" to comment.trim().take(500),
                "updatedAt" to timestamp,
            )
            db.batch()
                .set(ownerRef, ownerData, SetOptions.merge())
                .set(publicRef, publicData, SetOptions.merge())
                .commit()
                .addOnCompleteListener { task ->
                    onComplete(GameReviewWriteResult(task.isSuccessful, task.exception))
                }
        }.addOnFailureListener { error ->
            onComplete(GameReviewWriteResult(false, error))
        }
    }

    private fun gameDocumentId(slug: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(slug.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
