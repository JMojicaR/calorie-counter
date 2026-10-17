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
import java.time.LocalDate
import java.time.YearMonth

/** Per-day nutrition totals. */
data class DayTotals(
    val calories: Int = 0,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carbs: Float = 0f,
    val sugar: Float = 0f
)

class CalorieViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("calorias_prefs", Context.MODE_PRIVATE)
    private val repo = FoodRepository(AppDatabase.get(app).dao(), FoodApiClient())

    // ---- settings ----
    var goal by mutableStateOf(prefs.getInt("goal", 2000)); private set
    var proteinGoal by mutableStateOf(prefs.getInt("protein_goal", 0)); private set
    var fatGoal by mutableStateOf(prefs.getInt("fat_goal", 0)); private set
    var carbsGoal by mutableStateOf(prefs.getInt("carbs_goal", 0)); private set

    // ---- today ----
    var todayLog by mutableStateOf<List<LogEntry>>(emptyList()); private set
    var todayTotals by mutableStateOf(DayTotals()); private set
    var customFoods by mutableStateOf<List<CustomFood>>(emptyList()); private set

    // ---- search ----
    var searchResults by mutableStateOf<List<FoodResult>>(emptyList()); private set
    var isSearching by mutableStateOf(false); private set
    var searchMessage by mutableStateOf(""); private set

    // ---- barcode ----
    var barcodeResult by mutableStateOf<FoodResult?>(null); private set
    var barcodeMessage by mutableStateOf(""); private set

    // ---- calendar ----
    var monthTotals by mutableStateOf<Map<String, DayTotals>>(emptyMap()); private set
    var selectedDate by mutableStateOf(LocalDate.now().toString()); private set
    var selectedLog by mutableStateOf<List<LogEntry>>(emptyList()); private set

    val today: String get() = LocalDate.now().toString()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            todayLog = repo.getLog(today)
            todayTotals = totalsOf(todayLog)
            customFoods = repo.getCustomFoods()
        }
    }

    private fun totalsOf(entries: List<LogEntry>): DayTotals {
        var c = 0; var p = 0f; var f = 0f; var cb = 0f; var s = 0f
        for (e in entries) { c += e.calories; p += e.protein; f += e.fat; cb += e.carbs; s += e.sugar }
        return DayTotals(c, p, f, cb, s)
    }

    // ---- goals ----
    fun updateGoal(cal: Int) {
        if (cal <= 0) return
        goal = cal; prefs.edit().putInt("goal", cal).apply()
    }

    fun updateMacroGoals(protein: Int, fat: Int, carbs: Int) {
        proteinGoal = protein; fatGoal = fat; carbsGoal = carbs
        prefs.edit().putInt("protein_goal", protein).putInt("fat_goal", fat).putInt("carbs_goal", carbs).apply()
    }

    // ---- log ----
    fun addEntry(name: String, calories: Int, protein: Float, fat: Float, carbs: Float, sugar: Float) {
        if (calories <= 0) return
        viewModelScope.launch {
            repo.addLog(LogEntry(date = today, name = name, calories = calories,
                protein = protein, fat = fat, carbs = carbs, sugar = sugar))
            refresh()
        }
    }

    fun deleteEntry(entry: LogEntry) {
        viewModelScope.launch { repo.deleteLog(entry); refresh() }
    }

    // ---- custom foods ----
    fun saveCustomFood(name: String, per100g: Boolean, kcal: Int, protein: Float, fat: Float, carbs: Float, sugar: Float) {
        if (name.isBlank() || kcal <= 0) return
        viewModelScope.launch {
            repo.saveCustomFood(CustomFood(name = name.trim(), per100g = per100g, kcal = kcal,
                protein = protein, fat = fat, carbs = carbs, sugar = sugar))
            refresh()
        }
    }

    fun deleteCustomFood(food: CustomFood) {
        viewModelScope.launch { repo.deleteCustomFood(food); refresh() }
    }

    // ---- search ----
    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            isSearching = true; searchMessage = ""; searchResults = emptyList()
            val r = repo.searchFoods(query.trim(), getUsdaKey(), getNinjasKey())
            searchResults = r; isSearching = false
            if (r.isEmpty()) searchMessage = "No se encontró. Toca \"Buscar en internet\" para consultarlo y guárdalo manualmente."
        }
    }

    // ---- barcode ----
    fun lookupBarcode(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            barcodeMessage = ""; barcodeResult = repo.lookupBarcode(code.trim())
            if (barcodeResult == null) barcodeMessage = "Producto no encontrado en Open Food Facts."
        }
    }

    // ---- calendar ----
    fun loadMonth(ym: YearMonth) {
        viewModelScope.launch {
            val entries = repo.getLogBetween(ym.atDay(1).toString(), ym.atEndOfMonth().toString())
            val map = mutableMapOf<String, DayTotals>()
            for (e in entries) {
                val t = map[e.date] ?: DayTotals()
                map[e.date] = DayTotals(t.calories + e.calories, t.protein + e.protein,
                    t.fat + e.fat, t.carbs + e.carbs, t.sugar + e.sugar)
            }
            monthTotals = map
        }
    }

    fun selectDate(date: String) {
        selectedDate = date
        viewModelScope.launch { selectedLog = repo.getLog(date) }
    }

    // ---- api keys ----
    fun setApiKeys(usda: String, ninjas: String) {
        prefs.edit().putString("usda_key", usda.trim()).putString("ninjas_key", ninjas.trim()).apply()
    }

    fun getUsdaKey(): String = prefs.getString("usda_key", "") ?: ""
    fun getNinjasKey(): String = prefs.getString("ninjas_key", "") ?: ""
}
