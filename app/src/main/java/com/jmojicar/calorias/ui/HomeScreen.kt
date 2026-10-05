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

@Composable
fun HomeScreen(vm: CalorieViewModel) {
    val goal = vm.goal
    val total = vm.todayTotal
    val log = vm.todayLog
    val progress = if (goal > 0) (total.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val remaining = goal - total

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hoy", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$total / $goal kcal", fontSize = 28.sp, fontWeight = FontWeight.Bold)
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

        Spacer(Modifier.height(20.dp))
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
                                Text("${entry.calories} kcal", color = Color.Gray, fontSize = 13.sp)
                            }
                            IconButton(onClick = { vm.deleteEntry(entry) }) { Text("🗑️") }
                        }
                    }
                }
            }
        }
    }
}
