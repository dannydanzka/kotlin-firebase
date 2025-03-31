package com.roberto.firebase.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roberto.firebase.ui.screens.*

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Empleados : Routes("empleados")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.Login.route) {

        // Login
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Registro principal
        composable(Routes.Register.route) {
            RegisterScreen(
                onGoToLista = {
                    navController.navigate(Routes.Empleados.route)
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Lista de empleados
        composable(Routes.Empleados.route) {
            EmployeesListScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
