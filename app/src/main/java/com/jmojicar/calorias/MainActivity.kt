package com.jmojicar.calorias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jmojicar.calorias.ui.AddFoodScreen
import com.jmojicar.calorias.ui.CaloriasTheme
import com.jmojicar.calorias.ui.CalorieViewModel
import com.jmojicar.calorias.ui.HomeScreen
import com.jmojicar.calorias.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private val vm: CalorieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaloriasTheme {
                var tab by remember { mutableIntStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0, onClick = { tab = 0 },
                                icon = { Text("📊") }, label = { Text("Hoy") }
                            )
                            NavigationBarItem(
                                selected = tab == 1, onClick = { tab = 1 },
                                icon = { Text("➕") }, label = { Text("Añadir") }
                            )
                            NavigationBarItem(
                                selected = tab == 2, onClick = { tab = 2 },
                                icon = { Text("⚙️") }, label = { Text("Ajustes") }
                            )
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (tab) {
                            0 -> HomeScreen(vm)
                            1 -> AddFoodScreen(vm)
                            2 -> SettingsScreen(vm)
                        }
                    }
                }
            }
        }
    }
}
