package com.example.streak.domain.repository

import com.example.streak.domain.model.User

interface AuthRepository {

    suspend fun signInWithGoogle(idToken: String): User?

    fun getCurrentUser(): User?
}