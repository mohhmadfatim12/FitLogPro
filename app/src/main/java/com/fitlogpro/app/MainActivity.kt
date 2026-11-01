package com.fitlogpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fitlogpro.app.ui.navigation.FitLogNavGraph
import com.fitlogpro.app.ui.theme.FitLogProTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as FitLogApplication
        val container = app.container

        setContent {
            val isDarkTheme by container.userPreferencesManager.darkThemeFlow
                .collectAsState(initial = false)

            FitLogProTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    FitLogNavGraph(
                        navController = navController,
                        container = container
                    )
                }
            }
        }
    }
}
