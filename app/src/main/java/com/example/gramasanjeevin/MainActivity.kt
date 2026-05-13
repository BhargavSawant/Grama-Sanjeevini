package com.example.gramasanjeevin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gramasanjeevin.ui.MainScreen
import com.example.gramasanjeevin.ui.theme.GramaSanjeevinTheme
import com.example.gramasanjeevin.utils.DatabaseSeeder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seed database with mock user and inventory data on startup
        DatabaseSeeder.seedDatabase()

        enableEdgeToEdge()
        setContent {
            GramaSanjeevinTheme {
                MainScreen()
            }
        }
    }
}