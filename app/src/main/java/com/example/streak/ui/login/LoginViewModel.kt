package com.example.streak.ui.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streak.di.AppModule
import com.example.streak.domain.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LoginViewModel : ViewModel() {

    private val loginUseCase = AppModule.loginUseCase

    sealed interface LoginEvent {
        data class Success(val user: User) : LoginEvent
        data class Failure(val message: String) : LoginEvent
    }

    private val _loginEvents = MutableSharedFlow<LoginEvent>(extraBufferCapacity = 1)
    val loginEvents = _loginEvents.asSharedFlow()

    var user by mutableStateOf<User?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    fun login(idToken: String) {
        loading = true
        viewModelScope.launch {
            try {
                user = loginUseCase(idToken)
                val currentUser = user
                if (currentUser != null) {
                    Log.d("LoginVM", "Login success: ${currentUser.email}")
                    _loginEvents.tryEmit(LoginEvent.Success(currentUser))
                } else {
                    val msg = "Firebase login failed (user is null)"
                    Log.e("LoginVM", msg)
                    _loginEvents.tryEmit(LoginEvent.Failure(msg))
                }
            } catch (e: Exception) {
                user = null
                val msg = e.localizedMessage ?: "Firebase login failed"
                Log.e("LoginVM", "Login failed", e)
                _loginEvents.tryEmit(LoginEvent.Failure(msg))
            } finally {
                loading = false
            }
        }
    }
}