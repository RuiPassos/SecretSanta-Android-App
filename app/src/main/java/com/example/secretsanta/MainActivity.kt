package com.example.secretsanta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.secretsanta.ui.navigation.SecretSantaNavHost
import com.example.secretsanta.ui.theme.SecretSantaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecretSantaTheme {
                SecretSantaNavHost()
            }
        }
    }
}
