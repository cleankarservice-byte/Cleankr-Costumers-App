package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.di.AppContainer
import com.example.ui.MainAppScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.theme.CleankrTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as CleankrApp).container

        setContent {
            CleankrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLoggedIn by appContainer.sessionManager.isLoggedIn.collectAsState()

                    if (isLoggedIn) {
                        MainAppScreen(
                            container = appContainer,
                            onSignOut = {
                                appContainer.sessionManager.clearSession()
                            }
                        )
                    } else {
                        val authViewModel: AuthViewModel = viewModel {
                            AuthViewModel(appContainer.sessionManager)
                        }
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // Handled via session state
                            }
                        )
                    }
                }
            }
        }
    }
}
