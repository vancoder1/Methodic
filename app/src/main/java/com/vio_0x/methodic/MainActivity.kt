package com.vio_0x.methodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vio_0x.methodic.ui.screens.TodoListScreen
import com.vio_0x.methodic.ui.theme.MethodicTheme
import com.vio_0x.methodic.viewmodel.TaskListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()
        setContent {
            MethodicTheme {
                // Create the ViewModel using the AndroidViewModelFactory
                val viewModel: TaskListViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )
                // Pass the created ViewModel to the screen
                TodoListScreen(viewModel = viewModel)
            }
        }
    }
}