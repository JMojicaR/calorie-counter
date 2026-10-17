package com.jmojicar.calorias.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(vm: CalorieViewModel) {
    var goalText by remember { mutableStateOf(vm.goal.toString()) }
    var proteinText by remember { mutableStateOf(if (vm.proteinGoal > 0) vm.proteinGoal.toString() else "") }
    var fatText by remember { mutableStateOf(if (vm.fatGoal > 0) vm.fatGoal.toString() else "") }
    var carbsText by remember { mutableStateOf(if (vm.carbsGoal > 0) vm.carbsGoal.toString() else "") }
    var usdaKey by remember { mutableStateOf(vm.getUsdaKey()) }
    var ninjasKey by remember { mutableStateOf(vm.getNinjasKey()) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Ajustes", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        Text("🎯 Objetivo diario", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(goalText, { goalText = it.filter { c -> c.isDigit() } },
            label = { Text("Calorías (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.updateGoal(goalText.toIntOrNull() ?: 2000) }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar objetivo")
        }

        Spacer(Modifier.height(24.dp))
        Text("🥩 Metas de macronutrientes (g/día)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Deja en 0 para no fijar meta.", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(proteinText, { proteinText = it.filter { c -> c.isDigit() } },
                label = { Text("Proteínas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(fatText, { fatText = it.filter { c -> c.isDigit() } },
                label = { Text("Grasas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(carbsText, { carbsText = it.filter { c -> c.isDigit() } },
            label = { Text("Carbohidratos") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            vm.updateMacroGoals(
                proteinText.toIntOrNull() ?: 0,
                fatText.toIntOrNull() ?: 0,
                carbsText.toIntOrNull() ?: 0
            )
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar metas") }

        Spacer(Modifier.height(24.dp))
        Text("🔑 Claves de API (opcional)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Sin claves, la app usa Open Food Facts (gratis). Con claves mejora la búsqueda de platillos mexicanos (CalorieNinjas) y alimentos genéricos (USDA).",
            fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(usdaKey, { usdaKey = it }, label = { Text("USDA FoodData Central (clave)") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(ninjasKey, { ninjasKey = it }, label = { Text("CalorieNinjas (clave)") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.setApiKeys(usdaKey, ninjasKey) }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar claves")
        }

        Spacer(Modifier.height(24.dp))
        Text("💡 Cómo obtener claves gratis:\n• USDA: fdc.nal.usda.gov/api-key-signup\n• CalorieNinjas: calorieninjas.com/api",
            fontSize = 12.sp, color = Color.Gray)
    }
}
