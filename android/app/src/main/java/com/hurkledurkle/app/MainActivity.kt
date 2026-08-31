package com.hurkledurkle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hurkledurkle.app.ui.navigation.NavGraph
import com.hurkledurkle.app.ui.theme.HurkledurkleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HurkledurkleTheme {
                NavGraph()
            }
        }
    }
}
