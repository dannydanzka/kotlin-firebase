package com.roberto.firebase.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roberto.firebase.ui.screens.*

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object RegisterEdit : Routes("register_edit/{uid}") {
        fun createRoute(uid: String) = "register_edit/$uid"
    }
    object Empleados : Routes("empleados")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Register.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                uidEmpleadoEditando = null,
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

        composable(Routes.RegisterEdit.route) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            RegisterScreen(
                uidEmpleadoEditando = uid,
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

        composable(Routes.Empleados.route) {
            EmployeesListScreen(
                onBack = { navController.popBackStack() },
                onEditEmpleado = { uid ->
                    navController.navigate(Routes.RegisterEdit.createRoute(uid))
                }
            )
        }
    }
}
