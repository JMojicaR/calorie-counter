package com.jmojicar.calorias.data

/**
 * Seed list of common Mexican dishes with approximate calories and macros per
 * typical portion. Values are ballpark (standard nutrition references) so the
 * app works offline/keyless; users can adjust amounts when logging.
 */
data class MexicanDish(
    val name: String,
    val kcal: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val sugar: Float,
    val portion: String
)

val MEXICAN_DISHES: List<MexicanDish> = listOf(
    MexicanDish("Taco al pastor", 150, 10f, 7f, 12f, 1f, "1 taco"),
    MexicanDish("Taco de bistec", 180, 14f, 9f, 12f, 1f, "1 taco"),
    MexicanDish("Taco de carnitas", 200, 12f, 13f, 12f, 1f, "1 taco"),
    MexicanDish("Taco dorado", 130, 5f, 7f, 13f, 1f, "1 pieza"),
    MexicanDish("Enchiladas verdes", 350, 15f, 18f, 32f, 4f, "3 piezas"),
    MexicanDish("Enchiladas rojas", 380, 16f, 19f, 34f, 5f, "3 piezas"),
    MexicanDish("Chilaquiles verdes", 420, 12f, 20f, 45f, 3f, "1 plato"),
    MexicanDish("Chilaquiles rojos", 450, 13f, 22f, 46f, 4f, "1 plato"),
    MexicanDish("Pozole rojo", 350, 25f, 10f, 38f, 3f, "1 plato"),
    MexicanDish("Pozole blanco", 320, 26f, 9f, 34f, 2f, "1 plato"),
    MexicanDish("Tamal de rajas", 250, 6f, 12f, 30f, 3f, "1 pieza"),
    MexicanDish("Tamal de pollo", 230, 9f, 9f, 28f, 3f, "1 pieza"),
    MexicanDish("Mole poblano con pollo", 480, 30f, 22f, 40f, 8f, "1 plato"),
    MexicanDish("Quesadilla de queso", 300, 12f, 16f, 28f, 2f, "1 pieza"),
    MexicanDish("Sope", 180, 6f, 9f, 20f, 2f, "1 pieza"),
    MexicanDish("Tostada", 220, 7f, 11f, 24f, 2f, "1 pieza"),
    MexicanDish("Flautas de pollo", 280, 14f, 15f, 22f, 2f, "3 piezas"),
    MexicanDish("Burrito de carne", 500, 22f, 18f, 55f, 3f, "1 pieza"),
    MexicanDish("Burrito de frijol", 400, 15f, 12f, 55f, 3f, "1 pieza"),
    MexicanDish("Huevos rancheros", 350, 16f, 22f, 20f, 4f, "1 plato"),
    MexicanDish("Huevos a la mexicana", 300, 14f, 20f, 14f, 3f, "1 plato"),
    MexicanDish("Chile relleno", 350, 15f, 22f, 22f, 6f, "1 pieza"),
    MexicanDish("Arroz a la mexicana", 200, 4f, 5f, 34f, 2f, "1 taza"),
    MexicanDish("Frijoles refritos", 220, 12f, 9f, 25f, 1f, "1 taza"),
    MexicanDish("Frijoles de la olla", 150, 9f, 1f, 27f, 1f, "1 taza"),
    MexicanDish("Guacamole", 150, 2f, 13f, 8f, 1f, "100 g"),
    MexicanDish("Elote con mayonesa", 200, 5f, 9f, 25f, 6f, "1 pieza"),
    MexicanDish("Esquites", 180, 4f, 7f, 27f, 4f, "1 taza"),
    MexicanDish("Torta de milanesa", 450, 20f, 18f, 50f, 4f, "1 pieza"),
    MexicanDish("Torta de jamón", 380, 18f, 12f, 48f, 4f, "1 pieza"),
    MexicanDish("Ceviche", 200, 18f, 4f, 15f, 3f, "1 taza"),
    MexicanDish("Cóctel de camarón", 250, 20f, 4f, 18f, 6f, "1 vaso"),
    MexicanDish("Caldo de res", 350, 30f, 12f, 25f, 4f, "1 plato"),
    MexicanDish("Caldo de pollo", 250, 25f, 8f, 15f, 2f, "1 plato"),
    MexicanDish("Menudo", 300, 22f, 12f, 25f, 2f, "1 plato"),
    MexicanDish("Barbacoa", 350, 30f, 18f, 3f, 1f, "200 g"),
    MexicanDish("Carnitas", 400, 28f, 28f, 2f, 0f, "200 g"),
    MexicanDish("Birria", 380, 28f, 18f, 15f, 2f, "1 plato"),
    MexicanDish("Pambazo", 500, 18f, 22f, 55f, 4f, "1 pieza"),
    MexicanDish("Gordita", 250, 8f, 12f, 28f, 2f, "1 pieza"),
    MexicanDish("Memela", 230, 7f, 10f, 26f, 2f, "1 pieza"),
    MexicanDish("Churro", 200, 3f, 11f, 23f, 7f, "1 pieza"),
    MexicanDish("Flan", 250, 7f, 10f, 34f, 30f, "1 porción"),
    MexicanDish("Arroz con leche", 220, 5f, 5f, 38f, 20f, "1 taza")
)
