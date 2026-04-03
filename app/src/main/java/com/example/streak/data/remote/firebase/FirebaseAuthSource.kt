package com.example.streak.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class FirebaseAuthSource {

    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(idToken: String): FirebaseUser? {

        val credential =
            GoogleAuthProvider.getCredential(idToken, null)

        val result =
            auth.signInWithCredential(credential).await()

        return result.user
    }

    fun currentUser(): FirebaseUser? = auth.currentUser
}