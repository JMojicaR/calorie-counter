package com.jmojicar.calorias.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(vm: CalorieViewModel) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    LaunchedEffect(currentMonth) { vm.loadMonth(currentMonth) }

    val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))
    val dateFmt = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es"))

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calendario", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Month navigation
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Text("◀", fontSize = 18.sp) }
            Text(
                currentMonth.format(monthFmt).replaceFirstChar { it.uppercase() },
                Modifier.weight(1f), textAlign = TextAlign.Center,
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Text("▶", fontSize = 18.sp) }
        }

        Spacer(Modifier.height(4.dp))

        // Weekday header (Monday-first)
        Row(Modifier.fillMaxWidth()) {
            for (d in listOf("L", "M", "X", "J", "V", "S", "D")) {
                Text(d, Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Day grid
        val first = currentMonth.atDay(1)
        val offset = first.dayOfWeek.value - 1            // Monday=1 → 0
        val cells: List<LocalDate?> = List(offset) { null } + (1..currentMonth.lengthOfMonth()).map { currentMonth.atDay(it) }
        Column {
            for (row in cells.chunked(7)) {
                Row(Modifier.fillMaxWidth()) {
                    for (cell in row) {
                        DayCell(cell, vm)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Register for the selected day
        val selTotals = totalsOf(vm.selectedLog)
        Text(
            "Registro · ${try { LocalDate.parse(vm.selectedDate).format(dateFmt) } catch (e: Exception) { vm.selectedDate }}",
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${selTotals.calories} kcal · P ${fmt(selTotals.protein)} g · G ${fmt(selTotals.fat)} g · C ${fmt(selTotals.carbs)} g · Az ${fmt(selTotals.sugar)} g",
            color = Color.Gray, fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))

        if (vm.selectedLog.isEmpty()) {
            Text("Sin registros este día.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                items(vm.selectedLog, key = { it.id }) { e ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(e.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${e.calories} kcal · P ${fmt(e.protein)} g · G ${fmt(e.fat)} g · C ${fmt(e.carbs)} g · Az ${fmt(e.sugar)} g",
                                color = Color.Gray, fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(date: LocalDate?, vm: CalorieViewModel) {
    val selected = date != null && date.toString() == vm.selectedDate
    val total = date?.let { vm.monthTotals[it.toString()] }
    Box(
        Modifier.weight(1f).height(52.dp)
            .padding(2.dp)
            .background(if (selected) Color(0xFF2E7D32) else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(enabled = date != null) { if (date != null) vm.selectDate(date.toString()) },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${date.dayOfMonth}",
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) Color.White else Color(0xFF222222)
                )
                if (total != null && total.calories > 0) {
                    Text(
                        "${total.calories}",
                        fontSize = 9.sp,
                        color = if (selected) Color.White else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

private fun totalsOf(entries: List<com.jmojicar.calorias.data.LogEntry>): DayTotals {
    var c = 0; var p = 0f; var f = 0f; var cb = 0f; var s = 0f
    for (e in entries) { c += e.calories; p += e.protein; f += e.fat; cb += e.carbs; s += e.sugar }
    return DayTotals(c, p, f, cb, s)
}

private fun fmt(v: Float): String = String.format(Locale.US, "%.0f", v)
