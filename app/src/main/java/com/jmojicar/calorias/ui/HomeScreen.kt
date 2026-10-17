package com.jmojicar.calorias.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HomeScreen(vm: CalorieViewModel) {
    val goal = vm.goal
    val t = vm.todayTotals
    val log = vm.todayLog
    val progress = if (goal > 0) (t.calories.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val remaining = goal - t.calories

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hoy", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // Calories card
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${t.calories} / $goal kcal", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Text(
                    if (remaining >= 0) "Te quedan $remaining kcal" else "Excediste por ${-remaining} kcal",
                    fontSize = 15.sp,
                    color = if (remaining >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Macros card
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Macronutrientes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                MacroRow("Proteínas", t.protein, vm.proteinGoal, Color(0xFF1E88E5))
                MacroRow("Grasas", t.fat, vm.fatGoal, Color(0xFFF9A825))
                MacroRow("Carbohidratos", t.carbs, vm.carbsGoal, Color(0xFF43A047))
                MacroRow("Azúcares", t.sugar, 0, Color(0xFFE53935))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Alimentos registrados", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (log.isEmpty()) {
            Text("Aún no registras alimentos hoy. Toca ➕ Añadir.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(log, key = { it.id }) { entry ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(entry.name, fontWeight = FontWeight.Medium)
                                Text(entrySummary(entry), color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = { vm.deleteEntry(entry) }) { Text("🗑️") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroRow(name: String, value: Float, goal: Int, color: Color) {
    val hasGoal = goal > 0
    val progress = if (hasGoal) (value / goal).coerceIn(0f, 1f) else 0f
    val valueText = String.format(Locale.US, "%.0f g", value)
    val goalText = if (hasGoal) "de $goal g" else ""

    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(name, Modifier.weight(1f), color = Color(0xFF333333), fontSize = 14.sp)
            Text("$valueText $goalText", color = Color.Gray, fontSize = 13.sp)
        }
        if (hasGoal) {
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                Modifier.fillMaxWidth().height(6.dp),
                color = color
            )
        }
    }
}

private fun entrySummary(e: com.jmojicar.calorias.data.LogEntry): String {
    fun g(v: Float) = String.format(Locale.US, "%.0f", v)
    return "${e.calories} kcal · P ${g(e.protein)} · G ${g(e.fat)} · C ${g(e.carbs)} · Az ${g(e.sugar)}"
}
