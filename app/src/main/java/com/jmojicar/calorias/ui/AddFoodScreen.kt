package com.jmojicar.calorias.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmojicar.calorias.data.MEXICAN_DISHES
import java.net.URLEncoder
import java.util.Locale

private data class FoodToAdd(
    val name: String,
    val kcalPer100g: Int?,
    val kcalPerUnit: Int?,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val sugar: Float
)

@Composable
fun AddFoodScreen(vm: CalorieViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<FoodToAdd?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Añadir alimento", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Buscar") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Código") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Platillos") })
            Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Manual") })
        }
        Spacer(Modifier.height(12.dp))

        when (tab) {
            0 -> SearchTab(vm) { pending = it }
            1 -> BarcodeTab(vm) { pending = it }
            2 -> DishesTab(vm) { pending = it }
            3 -> ManualTab(vm)
        }
    }

    pending?.let { p ->
        FoodAddDialog(
            name = p.name,
            kcalPer100g = p.kcalPer100g,
            kcalPerUnit = p.kcalPerUnit,
            protein = p.protein, fat = p.fat, carbs = p.carbs, sugar = p.sugar,
            onDismiss = { pending = null },
            onConfirm = { kcal, prot, fat, carbs, sug ->
                vm.addEntry(p.name, kcal, prot, fat, carbs, sug)
                pending = null
            }
        )
    }
}

// ---- Tab 0: search ----
@Composable
private fun SearchTab(vm: CalorieViewModel, onAdd: (FoodToAdd) -> Unit) {
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(query, { query = it }, label = { Text("Buscar platillo o alimento") },
                singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.search(query) }) { Text("🔍") }
        }
        Spacer(Modifier.height(8.dp))
        if (vm.isSearching) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(); Text("Buscando…", color = Color.Gray)
            }
        } else if (vm.searchResults.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.searchResults) { r ->
                    FoodRow(r.name, kcalLabel(r.kcalPer100g, r.kcalPerUnit),
                        macroLine(r.protein, r.fat, r.carbs, r.sugar), r.source) {
                        onAdd(FoodToAdd(r.name, r.kcalPer100g, r.kcalPerUnit, r.protein, r.fat, r.carbs, r.sugar))
                    }
                }
            }
        } else if (vm.searchMessage.isNotBlank()) {
            Column {
                Text(vm.searchMessage, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                WebSearchButton(query)
            }
        }
    }
}

// ---- Tab 1: barcode ----
@Composable
private fun BarcodeTab(vm: CalorieViewModel, onAdd: (FoodToAdd) -> Unit) {
    var code by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Text("Escribe el código de barras del producto de supermercado (Open Food Facts).",
            fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(code, { code = it.filter { c -> c.isDigit() } },
                label = { Text("Código de barras") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.lookupBarcode(code) }) { Text("Buscar") }
        }
        Spacer(Modifier.height(12.dp))
        val r = vm.barcodeResult
        if (r != null) {
            FoodRow(r.name, kcalLabel(r.kcalPer100g, r.kcalPerUnit),
                macroLine(r.protein, r.fat, r.carbs, r.sugar), r.source) {
                onAdd(FoodToAdd(r.name, r.kcalPer100g, r.kcalPerUnit, r.protein, r.fat, r.carbs, r.sugar))
            }
        } else if (vm.barcodeMessage.isNotBlank()) {
            Text(vm.barcodeMessage, color = Color.Gray)
        }
    }
}

// ---- Tab 2: saved foods + mexican dishes ----
@Composable
private fun DishesTab(vm: CalorieViewModel, onAdd: (FoodToAdd) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (vm.customFoods.isNotEmpty()) {
            item { Text("Mis platillos guardados", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            items(vm.customFoods, key = { it.id }) { f ->
                FoodRow(f.name,
                    if (f.per100g) "${f.kcal} kcal / 100 g" else "${f.kcal} kcal / porción",
                    macroLine(f.protein, f.fat, f.carbs, f.sugar), "Guardado",
                    onDelete = { vm.deleteCustomFood(f) }) {
                    onAdd(FoodToAdd(f.name,
                        if (f.per100g) f.kcal else null,
                        if (f.per100g) null else f.kcal,
                        f.protein, f.fat, f.carbs, f.sugar))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        item { Text("Platillos mexicanos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        items(MEXICAN_DISHES) { d ->
            FoodRow(d.name, "${d.kcal} kcal · ${d.portion}",
                macroLine(d.protein, d.fat, d.carbs, d.sugar), "Estimado") {
                onAdd(FoodToAdd(d.name, null, d.kcal, d.protein, d.fat, d.carbs, d.sugar))
            }
        }
    }
}

// ---- Tab 3: manual entry ----
@Composable
private fun ManualTab(vm: CalorieViewModel) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var per100g by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Si el platillo no aparece, búscalo en internet y guárdalo con su contenido calórico y macronutrientes.",
            fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Nombre del platillo") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(kcal, { kcal = it.filter { c -> c.isDigit() } }, label = { Text("Calorías (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !per100g, onClick = { per100g = false }); Text("Por porción")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = per100g, onClick = { per100g = true }); Text("Por 100 g")
        }
        Spacer(Modifier.height(8.dp))
        Text("Macronutrientes (opcional, en gramos)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroField(protein, { protein = it }, "Proteínas", Modifier.weight(1f))
            MacroField(fat, { fat = it }, "Grasas", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroField(carbs, { carbs = it }, "Carbohidratos", Modifier.weight(1f))
            MacroField(sugar, { sugar = it }, "Azúcares", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val k = kcal.toIntOrNull() ?: 0
                if (name.isNotBlank() && k > 0) {
                    vm.saveCustomFood(name, per100g, k,
                        protein.toFloatOrNull() ?: 0f, fat.toFloatOrNull() ?: 0f,
                        carbs.toFloatOrNull() ?: 0f, sugar.toFloatOrNull() ?: 0f)
                    name = ""; kcal = ""; protein = ""; fat = ""; carbs = ""; sugar = ""
                }
            }, modifier = Modifier.weight(1f)) { Text("💾 Guardar") }
            WebSearchButton(name)
        }
    }
}

@Composable
private fun MacroField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
    )
}

// ---- shared bits ----

@Composable
private fun FoodRow(name: String, kcalLabel: String, macroLine: String, source: String,
                    onDelete: (() -> Unit)? = null, onAdd: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium)
                Text("$kcalLabel · $source", color = Color.Gray, fontSize = 12.sp)
                if (macroLine.isNotBlank()) Text(macroLine, color = Color(0xFF7A7A7A), fontSize = 11.sp)
            }
            if (onDelete != null) TextButton(onClick = onDelete) { Text("🗑️") }
            Button(onClick = onAdd, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) { Text("Añadir") }
        }
    }
}

private fun kcalLabel(kcalPer100g: Int?, kcalPerUnit: Int?): String = when {
    kcalPer100g != null && kcalPer100g > 0 -> "$kcalPer100g kcal / 100 g"
    kcalPerUnit != null && kcalPerUnit > 0 -> "$kcalPerUnit kcal / porción"
    else -> "calorías no disponibles"
}

private fun macroLine(p: Float, f: Float, c: Float, s: Float): String {
    if (p + f + c + s <= 0f) return ""
    fun g(v: Float) = String.format(Locale.US, "%.0f", v)
    return "P ${g(p)} g · G ${g(f)} g · C ${g(c)} g · Az ${g(s)} g"
}

@Composable
private fun WebSearchButton(query: String) {
    val context = LocalContext.current
    val q = query.ifBlank { "platillo mexicano" }
    Button(onClick = {
        val encoded = URLEncoder.encode("calorías $q", "UTF-8")
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")))
    }) { Text("🌐 Buscar en internet") }
}

@Composable
private fun FoodAddDialog(
    name: String,
    kcalPer100g: Int?,
    kcalPerUnit: Int?,
    protein: Float, fat: Float, carbs: Float, sugar: Float,
    onDismiss: () -> Unit,
    onConfirm: (Int, Float, Float, Float, Float) -> Unit
) {
    val per100 = (kcalPer100g ?: 0) > 0
    val perUnit = (kcalPerUnit ?: 0) > 0
    val mode = when { per100 -> "grams"; perUnit -> "units"; else -> "total" }

    var amountText by remember(name) {
        mutableStateOf(if (per100) "100" else if (perUnit) "1" else "")
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val totalKcal: Int
    val tp: Float; val tf: Float; val tc: Float; val ts: Float
    when (mode) {
        "grams" -> {
            val factor = amount / 100.0
            totalKcal = ((kcalPer100g ?: 0) * factor).toInt()
            tp = (protein * factor).toFloat(); tf = (fat * factor).toFloat()
            tc = (carbs * factor).toFloat(); ts = (sugar * factor).toFloat()
        }
        "units" -> {
            totalKcal = ((kcalPerUnit ?: 0) * amount).toInt()
            tp = (protein * amount).toFloat(); tf = (fat * amount).toFloat()
            tc = (carbs * amount).toFloat(); ts = (sugar * amount).toFloat()
        }
        else -> {
            totalKcal = amountText.toIntOrNull() ?: 0
            tp = 0f; tf = 0f; tc = 0f; ts = 0f
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name) },
        text = {
            Column {
                when (mode) {
                    "grams" -> OutlinedTextField(amountText, { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Gramos") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    "units" -> OutlinedTextField(amountText, { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Porciones") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    else -> OutlinedTextField(amountText, { amountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Calorías totales") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                }
                Spacer(Modifier.height(10.dp))
                Text("Total: $totalKcal kcal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(macroLine(tp, tf, tc, ts), color = Color.Gray, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (totalKcal > 0) onConfirm(totalKcal, tp, tf, tc, ts) }, enabled = totalKcal > 0) {
                Text("Añadir")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
