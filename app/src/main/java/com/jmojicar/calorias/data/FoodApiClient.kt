package com.jmojicar.calorias.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** A normalized food hit from any provider (macros default to 0 when unknown). */
data class FoodResult(
    val name: String,
    val kcalPer100g: Int?, // set for per-100g foods (OFF/USDA)
    val kcalPerUnit: Int?, // set for per-serving foods (CalorieNinjas/dishes)
    val protein: Float,    // grams, same basis as the kcal field
    val fat: Float,
    val carbs: Float,
    val sugar: Float,
    val source: String
)

/**
 * Food/nutrition APIs:
 *  - Open Food Facts (keyless): per-100g + barcode.
 *  - CalorieNinjas (free key): per-serving natural language.
 *  - USDA FoodData Central (free key): per-100g generic foods.
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

    // kcal per 100g from an OFF "nutriments" object (kJ fallback).
    private fun kcalPer100g(nut: JSONObject?): Int? {
        if (nut == null) return null
        val kcal = nut.optDouble("energy-kcal_100g", -1.0)
        if (kcal >= 0) return kcal.toInt()
        val kj = nut.optDouble("energy_100g", -1.0)
        if (kj >= 0) return (kj / 4.184).toInt()
        return null
    }

    private fun offMacros(nut: JSONObject?): FloatArray {
        if (nut == null) return FloatArray(4)
        fun g(key: String): Float = nut.optDouble(key, -1.0).let { if (it >= 0) it.toFloat() else 0f }
        return floatArrayOf(
            g("proteins_100g"),
            g("fat_100g"),
            g("carbohydrates_100g"),
            g("sugars_100g")
        )
    }

    // ---- Open Food Facts (keyless, per 100 g) ----
    suspend fun searchOpenFoodFacts(query: String): List<FoodResult> {
        val url = "https://world.openfoodfacts.org/api/v2/search?search_terms=${enc(query)}" +
                "&page_size=20&fields=product_name,brands,nutriments"
        val obj = JSONObject(get(url))
        val products = obj.optJSONArray("products") ?: return emptyList()
        val out = mutableListOf<FoodResult>()
        for (i in 0 until products.length()) {
            val p = products.optJSONObject(i) ?: continue
            val name = p.optString("product_name", "").trim().takeIf { it.isNotBlank() } ?: continue
            val m = offMacros(p.optJSONObject("nutriments"))
            out.add(FoodResult(name, kcalPer100g(p.optJSONObject("nutriments")), null,
                m[0], m[1], m[2], m[3], "Open Food Facts"))
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
        val nut = p.optJSONObject("nutriments")
        val m = offMacros(nut)
        return FoodResult(name, kcalPer100g(nut), null, m[0], m[1], m[2], m[3], "Open Food Facts")
    }

    // ---- CalorieNinjas (free key, per serving) ----
    suspend fun searchCalorieNinjas(query: String, apiKey: String): List<FoodResult> {
        val url = "https://api.calorieninjas.com/v1/nutrition?query=${enc(query)}"
        val obj = JSONObject(get(url, mapOf("X-Api-Key" to apiKey)))
        val items = obj.optJSONArray("items") ?: return emptyList()
        val out = mutableListOf<FoodResult>()
        for (i in 0 until items.length()) {
            val it = items.optJSONObject(i) ?: continue
            val name = it.optString("name", "").trim().takeIf { s -> s.isNotBlank() } ?: continue
            val kcal = it.optDouble("calories", -1.0).let { v -> if (v >= 0) v.toInt() else null }
            fun g(key: String): Float = it.optDouble(key, -1.0).let { v -> if (v >= 0) v.toFloat() else 0f }
            out.add(FoodResult(name, null, kcal,
                g("protein_g"), g("fat_total_g"), g("carbohydrates_total_g"), g("sugar_g"),
                "CalorieNinjas"))
        }
        return out
    }

    // ---- USDA FoodData Central (free key, per 100 g) ----
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
            var protein = 0f; var fat = 0f; var carbs = 0f; var sugar = 0f
            val nf = f.optJSONArray("foodNutrients")
            if (nf != null) {
                for (j in 0 until nf.length()) {
                    val n = nf.optJSONObject(j) ?: continue
                    val nm = n.optString("nutrientName", "")
                    val unit = n.optString("unitName", "")
                    val v = n.optDouble("value", -1.0)
                    if (v < 0) continue
                    when {
                        nm.contains("Energy") && unit.contains("KCAL") -> kcal = v.toInt()
                        nm == "Protein" -> protein = v.toFloat()
                        nm.contains("Total lipid") -> fat = v.toFloat()
                        nm.contains("Carbohydrate, by difference") -> carbs = v.toFloat()
                        nm.contains("Total Sugars") || nm.contains("Sugars, total") -> sugar = v.toFloat()
                    }
                }
            }
            out.add(FoodResult(name, kcal, null, protein, fat, carbs, sugar, "USDA"))
        }
        return out
    }
}
