package com.example.streak.di

import com.example.streak.data.remote.firebase.FirebaseAuthSource
import com.example.streak.data.repository.AuthRepositoryImpl
import com.example.streak.domain.usecase.LoginUseCase

object AppModule {

    private val firebaseSource = FirebaseAuthSource()

    private val authRepository =
        AuthRepositoryImpl(firebaseSource)

    val loginUseCase =
        LoginUseCase(authRepository)
}