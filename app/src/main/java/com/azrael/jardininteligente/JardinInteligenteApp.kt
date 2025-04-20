package com.azrael.jardininteligente.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azrael.jardininteligente.HomeScreen
import com.azrael.jardininteligente.LoginScreen
import com.azrael.jardininteligente.RegisterScreen

@Composable
fun JardinInteligenteApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("main") { HomeScreen() } // ya no se le pasa navController
    }
}

