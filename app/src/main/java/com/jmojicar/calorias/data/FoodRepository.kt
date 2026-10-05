package com.jmojicar.calorias.data

/** Combines the Room DAO and the network client into one access point. */
class FoodRepository(
    private val dao: AppDao,
    private val api: FoodApiClient
) {
    val mexicanDishes: List<MexicanDish> = MEXICAN_DISHES

    // ---- daily log ----
    suspend fun getLog(date: String) = dao.getLogForDate(date)
    suspend fun totalFor(date: String) = dao.totalForDate(date)
    suspend fun addLog(name: String, calories: Int, date: String) =
        dao.insertLog(LogEntry(date = date, name = name, calories = calories))
    suspend fun deleteLog(entry: LogEntry) = dao.deleteLog(entry)

    // ---- saved custom foods ----
    suspend fun getCustomFoods() = dao.getAllFoods()
    suspend fun saveCustomFood(name: String, kcalPer100g: Int, kcalPerUnit: Int) =
        dao.insertFood(CustomFood(name = name, kcalPer100g = kcalPer100g, kcalPerUnit = kcalPerUnit))
    suspend fun deleteCustomFood(food: CustomFood) = dao.deleteFood(food)

    // ---- search across providers ----
    suspend fun searchFoods(query: String, usdaKey: String, ninjasKey: String): List<FoodResult> {
        val out = mutableListOf<FoodResult>()
        try { out.addAll(api.searchOpenFoodFacts(query)) } catch (_: Exception) {}
        if (ninjasKey.isNotBlank()) try { out.addAll(api.searchCalorieNinjas(query, ninjasKey)) } catch (_: Exception) {}
        if (usdaKey.isNotBlank()) try { out.addAll(api.searchUsda(query, usdaKey)) } catch (_: Exception) {}
        return out.distinctBy { it.name.lowercase() }
    }

    suspend fun lookupBarcode(code: String): FoodResult? =
        try { api.lookupBarcode(code) } catch (_: Exception) { null }
}
