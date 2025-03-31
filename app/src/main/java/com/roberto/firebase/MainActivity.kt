package com.roberto.firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.roberto.firebase.ui.navigation.AppNavigation
import com.roberto.firebase.ui.theme.FirebaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            FirebaseTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
