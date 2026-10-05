package com.jmojicar.calorias.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jmojicar.calorias.data.AppDatabase
import com.jmojicar.calorias.data.CustomFood
import com.jmojicar.calorias.data.FoodApiClient
import com.jmojicar.calorias.data.FoodRepository
import com.jmojicar.calorias.data.FoodResult
import com.jmojicar.calorias.data.LogEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalorieViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("calorias_prefs", Context.MODE_PRIVATE)
    private val repo = FoodRepository(AppDatabase.get(app).dao(), FoodApiClient())

    // ---- settings ----
    var goal by mutableStateOf(prefs.getInt("goal", 2000))
        private set

    // ---- today's data ----
    var todayLog by mutableStateOf<List<LogEntry>>(emptyList())
        private set
    var todayTotal by mutableStateOf(0)
        private set
    var customFoods by mutableStateOf<List<CustomFood>>(emptyList())
        private set

    // ---- search state ----
    var searchResults by mutableStateOf<List<FoodResult>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set
    var searchMessage by mutableStateOf("")
        private set

    // ---- barcode state ----
    var barcodeResult by mutableStateOf<FoodResult?>(null)
        private set
    var barcodeMessage by mutableStateOf("")
        private set

    val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            todayLog = repo.getLog(today)
            todayTotal = repo.totalFor(today)
            customFoods = repo.getCustomFoods()
        }
    }

    // ---- goal ----
    fun updateGoal(cal: Int) {
        if (cal <= 0) return
        goal = cal
        prefs.edit().putInt("goal", cal).apply()
    }

    // ---- log ----
    fun addEntry(name: String, calories: Int) {
        if (calories <= 0) return
        viewModelScope.launch {
            repo.addLog(name, calories, today)
            refresh()
        }
    }

    fun deleteEntry(entry: LogEntry) {
        viewModelScope.launch {
            repo.deleteLog(entry)
            refresh()
        }
    }

    // ---- custom foods ----
    fun saveCustomFood(name: String, kcalPer100g: Int, kcalPerUnit: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.saveCustomFood(name.trim(), kcalPer100g, kcalPerUnit)
            refresh()
        }
    }

    fun deleteCustomFood(food: CustomFood) {
        viewModelScope.launch {
            repo.deleteCustomFood(food)
            refresh()
        }
    }

    // ---- search ----
    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            isSearching = true
            searchMessage = ""
            searchResults = emptyList()
            val r = repo.searchFoods(query.trim(), getUsdaKey(), getNinjasKey())
            searchResults = r
            isSearching = false
            if (r.isEmpty()) {
                searchMessage = "No se encontró. Toca \"Buscar en internet\" para consultarlo y guárdalo manualmente."
            }
        }
    }

    fun clearSearch() {
        searchResults = emptyList()
        searchMessage = ""
    }

    // ---- barcode ----
    fun lookupBarcode(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            barcodeMessage = ""
            barcodeResult = repo.lookupBarcode(code.trim())
            if (barcodeResult == null) barcodeMessage = "Producto no encontrado en Open Food Facts."
        }
    }

    fun clearBarcode() {
        barcodeResult = null
        barcodeMessage = ""
    }

    // ---- api keys ----
    fun setApiKeys(usda: String, ninjas: String) {
        prefs.edit().putString("usda_key", usda.trim()).putString("ninjas_key", ninjas.trim()).apply()
    }

    fun getUsdaKey(): String = prefs.getString("usda_key", "") ?: ""
    fun getNinjasKey(): String = prefs.getString("ninjas_key", "") ?: ""
}
