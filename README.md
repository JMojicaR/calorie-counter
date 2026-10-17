# Contador de Calorías (Calorie Counter)

A native Android app (Kotlin + Jetpack Compose) to track daily calories **and
macronutrients** (protein, fat, carbs, sugar), with Mexican-dish coverage,
supermarket-product lookup, and a **calendar/history register**. UI in **Spanish**.

## Features (→ requirements)

1. **Calorie objective** — set a daily kcal goal; the home screen shows consumed
   vs. goal with a progress bar and remaining kcal. *(Ajustes tab)*
2. **Dish search** — search dishes across food APIs. A built-in list of ~44 common
   Mexican dishes works offline; searching queries Open Food Facts (keyless) and,
   when a key is set, CalorieNinjas + USDA for better Mexican coverage. *(Añadir → Buscar / Platillos)*
3. **Not found?** — if a dish isn't found, tap **"🌐 Buscar en internet"** to open
   a web search, then save the dish + calories **and macros** locally (Room) via the
   *Manual* tab. Saved dishes appear under *Platillos*.
4. **Supermarket products** — look up products by **barcode** or name via
   Open Food Facts (no key needed). *(Añadir → Código)*
5. **Macronutrient counters** — track daily totals for **proteínas, grasas,
   carbohidratos y azúcares** (grams), with optional daily goals (protein/fat/carbs)
   shown as progress bars on the dashboard. *(Hoy + Ajustes)*
6. **Calendar & register** — a month calendar showing per-day calorie totals; tap a
   day to see that day's full register (calories + macros). *(Calendario tab)*

## Screens

- **Hoy** — goal progress + macronutrient dashboard + today's logged foods.
- **Añadir** — 4 tabs: Buscar (text search), Código (barcode), Platillos (saved +
  Mexican dishes), Manual (save custom dish with macros + web search).
- **Calendario** — month calendar + per-day history register.
- **Ajustes** — calorie + macro goals, optional API keys (USDA / CalorieNinjas).

## Data persistence

All data is stored in the device's **internal memory** via **Room** (SQLite at
`/data/data/com.jmojicar.calorias/databases/calorias.db`) — dishes, daily log,
and every day's macro totals survive app restarts.

## Tech stack

- Kotlin 2.0.21, Jetpack Compose (Material 3), Compose BOM 2024.12.01
- Room 2.6.1 (local DB), OkHttp 4.12.0 (networking), coroutines
- AGP 8.12.2 · Gradle 8.13 · JDK 21 · compileSdk 35 · minSdk 26 (Android 8.0+)

## APIs used

| API | Auth | Used for |
|-----|------|----------|
| Open Food Facts | none | supermarket products (barcode) + general search |
| CalorieNinjas | free key (optional) | natural-language Mexican dishes |
| USDA FoodData Central | free key (optional) | generic/branded foods |

Full research: [`docs/api_research.md`](docs/api_research.md)

## Build (command line, no Android Studio)

```bash
export ANDROID_HOME="$HOME/tools/android-sdk"
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"   # JDK 17+
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

Install on a device (USB debugging on):
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## API keys (optional)

1. USDA — https://fdc.nal.usda.gov/api-key-signup
2. CalorieNinjas — https://calorieninjas.com/api

Enter them in **Ajustes → Claves de API**. Without keys the app still works
(Open Food Facts + offline Mexican-dish list).

## Notes

- Calories/macros for the Mexican-dish seed list are ballpark values per typical
  portion; macro values are approximate (grams).
- The debug APK is unsigned-release-signable; for Play Store you'd add a release
  signing config + `minifyEnabled` + an app icon.
