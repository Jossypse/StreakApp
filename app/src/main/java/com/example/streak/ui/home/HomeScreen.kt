package com.example.streak.ui.home

import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.streak.R
import coil.compose.AsyncImage
import com.example.streak.domain.model.User

@Composable
fun HomeScreen(navController: NavController) {
    val vm: HomeViewModel = viewModel()
    val context = LocalContext.current
    val user = vm.currentUser
    val partners = vm.partners
    val incomingRequests = vm.incomingRequests
    val searchQuery = vm.searchQuery
    val searchResults = vm.searchResults

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Logout button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleClient = GoogleSignIn.getClient(context, gso)
                googleClient.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Logout", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current user
        user?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfilePictureAndName(user = it)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notifications
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Streak notifications", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (incomingRequests.isEmpty()) {
                            Text("No incoming partner requests.")
                        } else {
                            incomingRequests.forEach { requester ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    ProfilePictureAndName(user = requester)
                                    Button(onClick = { vm.acceptPartnerRequest(requester) }) {
                                        Text("Accept")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Streak partners
            item {
                Text("Streak partners", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (partners.isEmpty()) {
                    Text("No partners yet. Search and add one below.")
                } else {
                    partners.forEach { partner ->
                        ProfilePictureAndName(user = partner)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Search + add partner
            item {
                Text("Search users by email", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search email") }
                )
            }

            items(searchResults) { candidate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfilePictureAndName(user = candidate)
                    Button(onClick = { vm.addPartnerFromSearch(candidate) }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePictureAndName(user: User) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = user.photoUrl,
            contentDescription = "Profile picture",
            modifier = Modifier.size(48.dp)
        )
        Column {
            Text(text = user.name.ifBlank { user.email }, fontWeight = FontWeight.Bold)
            if (user.name.isNotBlank()) {
                Text(text = user.email)
            }
        }
    }
}
