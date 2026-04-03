package com.example.streak.data.repository

import com.example.streak.data.mapper.toDomain
import com.example.streak.data.remote.firebase.FirebaseAuthSource
import com.example.streak.domain.model.User
import com.example.streak.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val source: FirebaseAuthSource
) : AuthRepository {

    override suspend fun signInWithGoogle(
        idToken: String
    ): User? {
        return source.signIn(idToken)?.toDomain()
    }

    override fun getCurrentUser(): User? {
        return source.currentUser()?.toDomain()
    }
}