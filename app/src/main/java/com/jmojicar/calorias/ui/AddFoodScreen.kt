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
import androidx.compose.foundation.text.KeyboardOptions
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
import com.jmojicar.calorias.data.CustomFood
import com.jmojicar.calorias.data.FoodResult
import com.jmojicar.calorias.data.MEXICAN_DISHES
import com.jmojicar.calorias.data.MexicanDish
import java.net.URLEncoder

private data class FoodToAdd(
    val name: String,
    val kcalPer100g: Int?,
    val kcalPerUnit: Int?
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
            onDismiss = { pending = null },
            onConfirm = { kcal ->
                vm.addEntry(p.name, kcal)
                pending = null
            }
        )
    }
}

// ---- Tab 0: text search across APIs ----
@Composable
private fun SearchTab(vm: CalorieViewModel, onAdd: (FoodToAdd) -> Unit) {
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar platillo o alimento") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.search(query) }) { Text("🔍") }
        }

        Spacer(Modifier.height(8.dp))

        if (vm.isSearching) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text("Buscando…", color = Color.Gray)
            }
        } else if (vm.searchResults.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.searchResults) { r ->
                    FoodRow(r.name, kcalLabel(r.kcalPer100g, r.kcalPerUnit), r.source) {
                        onAdd(FoodToAdd(r.name, r.kcalPer100g, r.kcalPerUnit))
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

// ---- Tab 1: barcode lookup (supermarket products) ----
@Composable
private fun BarcodeTab(vm: CalorieViewModel, onAdd: (FoodToAdd) -> Unit) {
    var code by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Escanea o escribe el código de barras del producto de supermercado (Open Food Facts).",
            fontSize = 12.sp, color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { c -> c.isDigit() } },
                label = { Text("Código de barras") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.lookupBarcode(code) }) { Text("Buscar") }
        }

        Spacer(Modifier.height(12.dp))

        val r = vm.barcodeResult
        if (r != null) {
            FoodRow(r.name, kcalLabel(r.kcalPer100g, r.kcalPerUnit), r.source) {
                onAdd(FoodToAdd(r.name, r.kcalPer100g, r.kcalPerUnit))
            }
        } else if (vm.barcodeMessage.isNotBlank()) {
            Text(vm.barcodeMessage, color = Color.Gray)
        }
    }
}

// ---- Tab 2: saved foods + Mexican dishes ----
@Composable
private fun DishesTab(vm: CalorieViewModel, onAdd: (FoodToAdd) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (vm.customFoods.isNotEmpty()) {
            item { Text("Mis platillos guardados", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            items(vm.customFoods, key = { it.id }) { f ->
                FoodRow(
                    f.name,
                    kcalLabel(if (f.kcalPer100g > 0) f.kcalPer100g else null,
                              if (f.kcalPerUnit > 0) f.kcalPerUnit else null),
                    "Guardado",
                    onDelete = { vm.deleteCustomFood(f) }
                ) {
                    onAdd(FoodToAdd(f.name, if (f.kcalPer100g > 0) f.kcalPer100g else null,
                                     if (f.kcalPerUnit > 0) f.kcalPerUnit else null))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        item { Text("Platillos mexicanos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        items(MEXICAN_DISHES) { d ->
            FoodRow(d.name, "${d.kcal} kcal · ${d.portion}", "Estimado") {
                onAdd(FoodToAdd(d.name, null, d.kcal))
            }
        }
    }
}

// ---- Tab 3: manual entry + save locally ----
@Composable
private fun ManualTab(vm: CalorieViewModel) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var per100g by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Si el platillo no aparece, búscalo en internet y guárdalo aquí con su contenido calórico.",
            fontSize = 12.sp, color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del platillo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = kcal,
            onValueChange = { kcal = it.filter { c -> c.isDigit() } },
            label = { Text("Calorías (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !per100g, onClick = { per100g = false })
            Text("Por porción")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = per100g, onClick = { per100g = true })
            Text("Por 100 g")
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val k = kcal.toIntOrNull() ?: 0
                    if (name.isNotBlank() && k > 0) {
                        vm.saveCustomFood(name, if (per100g) k else 0, if (per100g) 0 else k)
                        name = ""; kcal = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("💾 Guardar") }
            WebSearchButton(name)
        }
    }
}

// ---- shared bits ----

@Composable
private fun FoodRow(
    name: String,
    kcalLabel: String,
    source: String,
    onDelete: (() -> Unit)? = null,
    onAdd: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium)
                Text("$kcalLabel · $source", color = Color.Gray, fontSize = 12.sp)
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete) { Text("🗑️") }
            }
            Button(onClick = onAdd, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text("Añadir")
            }
        }
    }
}

private fun kcalLabel(kcalPer100g: Int?, kcalPerUnit: Int?): String = when {
    kcalPer100g != null && kcalPer100g > 0 -> "$kcalPer100g kcal / 100 g"
    kcalPerUnit != null && kcalPerUnit > 0 -> "$kcalPerUnit kcal / porción"
    else -> "calorías no disponibles"
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
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val per100 = (kcalPer100g ?: 0) > 0
    val perUnit = (kcalPerUnit ?: 0) > 0
    val mode = when {
        per100 -> "grams"
        perUnit -> "units"
        else -> "total"
    }

    var amountText by remember(name) {
        mutableStateOf(if (per100) "100" else if (perUnit) "1" else "")
    }

    val amount = amountText.toDoubleOrNull()
    val total = when (mode) {
        "grams" -> if (amount != null) ((kcalPer100g!! * amount) / 100.0).toInt() else 0
        "units" -> if (amount != null) (kcalPerUnit!! * amount).toInt() else 0
        else -> amountText.toIntOrNull() ?: 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name) },
        text = {
            Column {
                when (mode) {
                    "grams" -> OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Gramos") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    "units" -> OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Porciones") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    else -> OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Calorías totales") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("Total: $total kcal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (total > 0) onConfirm(total) }, enabled = total > 0) {
                Text("Añadir")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
