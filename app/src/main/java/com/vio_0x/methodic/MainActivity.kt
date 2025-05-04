package com.vio_0x.methodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vio_0x.methodic.ui.screens.TodoListScreen // Import the main screen
import com.vio_0x.methodic.ui.theme.MethodicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MethodicTheme {
                // Host the main screen composable
                TodoListScreen()
            }
        }
    }
}