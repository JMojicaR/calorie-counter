package com.jmojicar.calorias.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** A normalized food hit from any provider. */
data class FoodResult(
    val name: String,
    val kcalPer100g: Int?, // null if unknown
    val kcalPerUnit: Int?, // null if unknown
    val source: String
)

/**
 * Talks to the food/nutrition APIs.
 *
 * Keyless (always available):
 *  - Open Food Facts  https://world.openfoodfacts.org/api/v2
 * Key-based (optional, improves Mexican-dish coverage):
 *  - CalorieNinjas    https://api.calorieninjas.com/v1/nutrition   (X-Api-Key header)
 *  - USDA FoodData    https://api.nal.usda.gov/fdc/v1/foods/search (api_key param)
 */
class FoodApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String =
        withContext(Dispatchers.IO) {
            val b = Request.Builder().url(url)
            headers.forEach { (k, v) -> b.header(k, v) }
            client.newCall(b.build()).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                resp.body?.string() ?: ""
            }
        }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    // ---- Open Food Facts (keyless) ----
    suspend fun searchOpenFoodFacts(query: String): List<FoodResult> {
        val url = "https://world.openfoodfacts.org/api/v2/search?search_terms=${enc(query)}" +
                "&page_size=20&fields=product_name,brands,nutriments"
        val obj = JSONObject(get(url))
        val products = obj.optJSONArray("products") ?: return emptyList()
        val out = mutableListOf<FoodResult>()
        for (i in 0 until products.length()) {
            val p = products.optJSONObject(i) ?: continue
            val name = p.optString("product_name", "").trim().takeIf { it.isNotBlank() } ?: continue
            out.add(FoodResult(name, kcalPer100g(p.optJSONObject("nutriments")), null, "Open Food Facts"))
        }
        return out
    }

    suspend fun lookupBarcode(barcode: String): FoodResult? {
        val url = "https://world.openfoodfacts.org/api/v2/product/${enc(barcode)}.json" +
                "?fields=product_name,brands,nutriments"
        val obj = JSONObject(get(url))
        if (obj.optInt("status", 0) != 1) return null
        val p = obj.optJSONObject("product") ?: return null
        val name = p.optString("product_name", "").trim().takeIf { it.isNotBlank() } ?: return null
        return FoodResult(name, kcalPer100g(p.optJSONObject("nutriments")), null, "Open Food Facts")
    }

    // ---- CalorieNinjas (key) ----
    suspend fun searchCalorieNinjas(query: String, apiKey: String): List<FoodResult> {
        val url = "https://api.calorieninjas.com/v1/nutrition?query=${enc(query)}"
        val obj = JSONObject(get(url, mapOf("X-Api-Key" to apiKey)))
        val items = obj.optJSONArray("items") ?: return emptyList()
        val out = mutableListOf<FoodResult>()
        for (i in 0 until items.length()) {
            val it = items.optJSONObject(i) ?: continue
            val name = it.optString("name", "").trim().takeIf { s -> s.isNotBlank() } ?: continue
            val kcal = it.optDouble("calories", -1.0).let { if (it >= 0) it.toInt() else null }
            val grams = it.optDouble("serving_size_g", -1.0).let { if (it > 0) it.toInt() else null }
            val kcalPer100 = if (kcal != null && grams != null && grams > 0)
                ((kcal * 100.0) / grams).toInt() else kcal
            out.add(FoodResult(name, kcalPer100, kcal, "CalorieNinjas"))
        }
        return out
    }

    // ---- USDA FoodData Central (key) ----
    suspend fun searchUsda(query: String, apiKey: String): List<FoodResult> {
        val url = "https://api.nal.usda.gov/fdc/v1/foods/search?query=${enc(query)}" +
                "&api_key=${enc(apiKey)}&pageSize=20"
        val obj = JSONObject(get(url))
        val foods = obj.optJSONArray("foods") ?: return emptyList()
        val out = mutableListOf<FoodResult>()
        for (i in 0 until foods.length()) {
            val f = foods.optJSONObject(i) ?: continue
            val name = f.optString("description", "").trim().takeIf { s -> s.isNotBlank() } ?: continue
            var kcal: Int? = null
            val nf = f.optJSONArray("foodNutrients")
            if (nf != null) {
                for (j in 0 until nf.length()) {
                    val n = nf.optJSONObject(j) ?: continue
                    if (n.optString("unitName", "").contains("KCAL")) {
                        val v = n.optDouble("value", -1.0)
                        if (v >= 0) { kcal = v.toInt(); break }
                    }
                }
            }
            out.add(FoodResult(name, kcal, null, "USDA"))
        }
        return out
    }

    // kcal per 100g from an Open Food Facts "nutriments" object (falls back to kJ).
    private fun kcalPer100g(nut: JSONObject?): Int? {
        if (nut == null) return null
        val kcal = nut.optDouble("energy-kcal_100g", -1.0)
        if (kcal >= 0) return kcal.toInt()
        val kj = nut.optDouble("energy_100g", -1.0)
        if (kj >= 0) return (kj / 4.184).toInt()
        return null
    }
}
