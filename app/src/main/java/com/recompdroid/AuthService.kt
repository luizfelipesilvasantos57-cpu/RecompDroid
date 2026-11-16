package com.recompdroid

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

/**
 * Thin Firebase Authentication boundary for account/session operations.
 * Firebase SDK persists the session and refreshes its ID token automatically.
 */
class AuthService private constructor(private val auth: FirebaseAuth) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit): FirebaseAuth.AuthStateListener {
        val firebaseListener = FirebaseAuth.AuthStateListener { listener(it.currentUser) }
        auth.addAuthStateListener(firebaseListener)
        return firebaseListener
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    fun signIn(email: String, password: String): Task<AuthResult> =
        auth.signInWithEmailAndPassword(email, password)

    fun createAccount(email: String, password: String): Task<AuthResult> =
        auth.createUserWithEmailAndPassword(email, password)

    fun updateDisplayName(user: FirebaseUser, displayName: String): Task<Void> =
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build(),
        )

    fun sendVerificationEmail(user: FirebaseUser): Task<Void> = user.sendEmailVerification()

    fun sendPasswordReset(email: String): Task<Void> = auth.sendPasswordResetEmail(email)

    fun signOut() = auth.signOut()

    fun reload(user: FirebaseUser): Task<Void> = user.reload()

    companion object {
        fun create(context: Context): AuthService? {
            val firebaseApp = runCatching {
                FirebaseApp.initializeApp(context.applicationContext)
            }.getOrNull() ?: return null

            return runCatching {
                AuthService(FirebaseAuth.getInstance(firebaseApp))
            }.getOrNull()
        }
    }
}
