package com.example.streak.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.streak.ui.login.LoginScreen
import com.example.streak.ui.home.HomeScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }
    }
}