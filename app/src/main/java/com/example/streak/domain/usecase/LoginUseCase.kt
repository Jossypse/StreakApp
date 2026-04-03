package com.example.streak.domain.usecase

import com.example.streak.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String) =
        repository.signInWithGoogle(token)
}