package com.islamparty.karyakarta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.islamparty.karyakarta.navigation.AppNavGraph
import com.islamparty.karyakarta.navigation.Routes
import com.islamparty.karyakarta.ui.theme.IslamPartyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as IslamPartyApp).container

        setContent {
            IslamPartyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        startDestination = if (container.authRepository.isLoggedIn()) {
                            Routes.WORKER_LIST
                        } else {
                            Routes.LOGIN
                        }
                    }

                    val destination = startDestination
                    if (destination == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        AppNavGraph(container = container, startDestination = destination)
                    }
                }
            }
        }
    }
}
