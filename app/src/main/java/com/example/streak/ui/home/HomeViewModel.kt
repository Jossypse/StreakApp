package com.example.streak.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streak.data.repository.AuthRepositoryImpl
import com.example.streak.data.remote.firebase.FirebaseAuthSource
import com.example.streak.data.repository.StreakRepositoryImpl
import com.example.streak.domain.model.User
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepositoryImpl = AuthRepositoryImpl(FirebaseAuthSource()),
    private val streakRepository: StreakRepositoryImpl = StreakRepositoryImpl()
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    var partners by mutableStateOf<List<User>>(emptyList())
        private set

    // "Notifications" = incoming partner requests waiting for acceptance
    var incomingRequests by mutableStateOf<List<User>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<User>>(emptyList())
        private set

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            currentUser = user
            user?.let {
                // Ensure profile exists in Firestore
                streakRepository.upsertCurrentUser(it)
                // Load existing partners
                partners = streakRepository.getStreakPartners(it.uid)
                // Load incoming requests
                incomingRequests = streakRepository.getIncomingPartnerRequests(it.uid)
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        searchQuery = value
        performSearch()
    }

    private fun performSearch() {
        val me = currentUser ?: return
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            searchResults = emptyList()
            return
        }
        viewModelScope.launch {
            searchResults = streakRepository.searchUsers(query, excludeUid = me.uid)
        }
    }

    fun addPartnerFromSearch(partner: User) {
        val me = currentUser ?: return
        viewModelScope.launch {
            // Directly add as streak partners
            streakRepository.addStreakPartner(me.uid, partner)
            partners = streakRepository.getStreakPartners(me.uid)
        }
    }

    fun acceptPartnerRequest(requester: User) {
        val me = currentUser ?: return
        viewModelScope.launch {
            streakRepository.acceptPartnerRequest(currentUser = me, requesterUid = requester.uid)
            partners = streakRepository.getStreakPartners(me.uid)
            incomingRequests = streakRepository.getIncomingPartnerRequests(me.uid)
        }
    }
}

