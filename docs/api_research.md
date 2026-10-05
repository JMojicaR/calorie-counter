# API Research — Caloric Content of Foods & Supermarket Products

Research for the **Contador de Calorías** Android app. Focus: APIs that return the
caloric (kcal) content of **dishes** (especially Mexican) and **supermarket
products** (barcode). Verified Sept 2026.

## Summary

| API | Auth | Free tier | Best for | Mexican dishes |
|-----|------|-----------|----------|----------------|
| **Open Food Facts** | ❌ none (read) | unlimited | Supermarket products + barcode + name search | ⚠️ products only |
| **CalorieNinjas** | 🔑 free key | yes | Natural-language dishes ("tacos al pastor") | ✅ good |
| **USDA FoodData Central** | 🔑 free key | yes | Comprehensive nutrient DB (generic + branded) | ✅ some |
| **Nutritionix** | 🔑 free key | ~200 req/day | Natural language + autocomplete | ✅ good |
| Edamam | 🔑 free key | ~1000/mo | Recipes + nutrition | ⚠️ some |
| Spoonacular | 🔑 free key | 150/day | Meal planning + recipes | ⚠️ some |
| FatSecret | 🔑 free key | 500/day | Food + recipes | ✅ good |

**Chosen strategy (per user):** keyless now (Open Food Facts) + key-based later
(CalorieNinjas + USDA). Keys are entered in the app's Ajustes screen.

---

## 1. Open Food Facts — KEYLESS ⭐

The workhorse for **supermarket products** (requirement #4) and a fallback for
general food search. No signup or key needed for *reading* data.

- **Base URL:** `https://world.openfoodfacts.org/api/v2`
- **Barcode lookup:** `GET /product/{barcode}.json?fields=product_name,brands,nutriments`
  - Response: `{ "status": 1, "product": { "product_name": "...", "nutriments": { "energy-kcal_100g": 123 } } }`
  - `status: 0` → product not found.
- **Text search:** `GET /search?search_terms={query}&page_size=20&fields=product_name,brands,nutriments`
  - Response: `{ "products": [ { "product_name": ..., "nutriments": { "energy-kcal_100g": ... } } ] }`
- **Calories field:** `nutriments.energy-kcal_100g` (kcal/100 g). Fallback:
  `nutriments.energy_100g` is kJ → divide by 4.184.
- **Pros:** no key, global (incl. Mexican supermarket products), barcode + search.
- **Cons:** product-focused — weak for *cooked dishes* (tacos, pozole) which is
  why the key-based APIs below are used for requirement #2.
- **Docs:** https://openfoodfacts.github.io/openfoodfacts-server/api/

## 2. CalorieNinjas — free key ⭐

Best free option for **natural-language Mexican dishes** (requirement #2).

- **Endpoint:** `GET https://api.calorieninjas.com/v1/nutrition?query={text}`
- **Auth:** header `X-Api-Key: {key}` (free key from calorieninjas.com/api)
- **Response:** `{ "items": [ { "name": "taco al pastor", "calories": 150, "serving_size_g": 100, "protein_g": ..., "carbohydrates_total_g": ..., "fat_total_g": ... } ] }`
- **Pros:** ~100k foods, natural-language queries ("enchiladas verdes", "pozole
  rojo"), returns kcal + macros. Great Mexican coverage.
- **Cons:** needs a key; "calories" is per `serving_size_g` (convert to /100 g).

## 3. USDA FoodData Central — free key

Deep, authoritative nutrient database for **generic foods** (rice, beans, tortilla,
meat) and many branded foods.

- **Endpoint:** `GET https://api.nal.usda.gov/fdc/v1/foods/search?query={text}&api_key={key}&pageSize=20`
- **Auth:** query param `api_key` (free key from fdc.nal.usda.gov/api-key-signup)
- **Response:** `{ "foods": [ { "description": "Tortilla, corn", "foodNutrients": [ { "nutrientName": "Energy", "unitName": "KCAL", "value": 218 } ] } ] }`
- **Calories:** find the `foodNutrients` entry with `unitName == "KCAL"` (value is
  kcal/100 g for standard reference foods).
- **Pros:** authoritative, huge, free. **Cons:** English descriptions, no natural
  language (raw search), key required.

## 4. Nutritionix — free key

Strong natural-language + autocomplete; used by many calorie apps.

- **Endpoint:** `POST https://trackapi.nutritionix.com/v2/natural/nutrients`
  body `{ "query": "tacos al pastor" }`
- **Auth:** headers `x-app-id` + `x-app-key` (free at nutritionix.com/api)
- **Pros:** excellent natural language, fast autocomplete (`/v2/search/instant`).
- **Cons:** key required, rate-limited free tier. *Not wired in v1 (add later).*

## 5. Others considered

- **Edamam Food Database** — free tier, recipes + nutrition, key required.
- **Spoonacular** — free 150/day, meal planning + recipes, key required.
- **FatSecret Platform** — free 500/day, good Mexican food coverage, OAuth + key.

---

## Which requirement each API serves

| Requirement | Primary API | Fallback |
|-------------|-------------|----------|
| #2 Dishes (Mexican) — search + list | CalorieNinjas (key) | USDA, Open Food Facts; seed list of ~44 Mexican dishes (offline) |
| #3 Dish not found → save manually | *(local Room DB)* + "buscar en internet" (Google) | — |
| #4 Supermarket products | Open Food Facts (barcode + name) | — |

---

## Implementation notes

- The app ships **keyless**: Open Food Facts works immediately for products and
  as a general search. The ~44-dish Mexican seed list (`MexicanDishes.kt`) works
  fully offline.
- Entering a CalorieNinjas/USDA key in Ajustes automatically upgrades search
  results with better Mexican-dish coverage — no code change.
- All results are normalized to a common shape: `name`, `kcalPer100g` (nullable),
  `kcalPerUnit` (nullable), `source`.
