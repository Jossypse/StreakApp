package com.example.streak.ui.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.streak.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController) {

    // Obtain ViewModel
    val viewModel: LoginViewModel = viewModel()
    val context = LocalContext.current
    val webClientId = context.getString(R.string.default_web_client_id)

    LaunchedEffect(viewModel) {
        viewModel.loginEvents.collect { event ->
            when (event) {
                is LoginViewModel.LoginEvent.Success -> {
                    Toast.makeText(
                        context,
                        "Logged in: ${event.user.email}",
                        Toast.LENGTH_LONG
                    ).show()
                    // After successful Firebase auth, go to the home screen.
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }

                is LoginViewModel.LoginEvent.Failure -> {
                    Toast.makeText(
                        context,
                        "Login failed: ${event.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Prepare Google Sign-In client
    val googleSignInClient = remember {
        if (webClientId.startsWith("AIza")) {
            val msg =
                "default_web_client_id looks like an API key. It must be the OAuth Web Client ID from Firebase."
            Log.e(
                "LoginScreen",
                msg
            )
        }

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, options)
    }

    // Launcher to handle Google Sign-In result
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            val data = result.data
            if (data == null) {
                val msg = "Google sign-in finished but data is null (resultCode=${result.resultCode})"
                Log.e("LoginScreen", msg)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }

            // Even when resultCode != RESULT_OK, Google sometimes still puts error details in the intent.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val token = account?.idToken
                if (token != null) {
                    // Pass ID token to ViewModel for Firebase login
                    viewModel.login(token)
                } else {
                    val msg =
                        "Google sign-in returned a null idToken. Replace default_web_client_id with the OAuth Web Client ID from Firebase."
                    Log.e("LoginScreen", msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                val msg = when (e.statusCode) {
                    10 -> "Google sign-in developer error (status=10). Check Firebase/Google OAuth setup: SHA-1/SHA-256 fingerprints and that google-services.json matches package `com.example.streak`."
                    else -> "Google sign-in error (status=${e.statusCode}): ${e.localizedMessage ?: "unknown"}"
                }
                Log.e("LoginScreen", "Google sign-in failed", e)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                if (webClientId.startsWith("AIza")) {
                    val msg =
                        "Fix Firebase config: default_web_client_id is an API key. It must be the OAuth Web Client ID from Firebase."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    return@Button
                }
                launcher.launch(googleSignInClient.signInIntent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.josephpic), // your Google icon
                contentDescription = "Google logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Login with Google",
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}