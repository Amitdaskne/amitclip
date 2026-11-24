package com.amit.amitclip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amit.amitclip.ui.screens.HomeScreen
import com.amit.amitclip.ui.theme.AmitClipTheme
import com.amit.amitclip.viewmodel.ClipViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmitClipTheme {
                val vm: ClipViewModel = viewModel(factory = ClipViewModel.Factory)
                HomeScreen(vm)
            }
        }
    }
}
