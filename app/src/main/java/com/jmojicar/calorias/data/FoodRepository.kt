package com.jmojicar.calorias.data

/** Combines the Room DAO and the network client into one access point. */
class FoodRepository(
    private val dao: AppDao,
    private val api: FoodApiClient
) {
    val mexicanDishes: List<MexicanDish> = MEXICAN_DISHES

    // ---- daily log ----
    suspend fun getLog(date: String) = dao.getLogForDate(date)
    suspend fun getLogBetween(start: String, end: String) = dao.getLogBetween(start, end)
    suspend fun addLog(entry: LogEntry) = dao.insertLog(entry)
    suspend fun deleteLog(entry: LogEntry) = dao.deleteLog(entry)

    // ---- saved custom foods ----
    suspend fun getCustomFoods() = dao.getAllFoods()
    suspend fun saveCustomFood(food: CustomFood) = dao.insertFood(food)
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
