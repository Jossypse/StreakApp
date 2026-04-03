package com.example.streak.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.example.streak.domain.model.User

fun FirebaseUser.toDomain(): User {
    return User(
        uid = uid,
        name = displayName ?: "",
        email = email ?: "",
        photoUrl = photoUrl?.toString()
    )
}