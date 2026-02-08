package com.boltmind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.boltmind.app.ui.navigation.BoltMindNavHost
import com.boltmind.app.ui.theme.BoltMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoltMindTheme {
                BoltMindNavHost()
            }
        }
    }
}
