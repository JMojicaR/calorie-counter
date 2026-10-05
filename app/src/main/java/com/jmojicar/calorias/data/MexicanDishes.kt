package com.jmojicar.calorias.data

/**
 * Seed list of common Mexican dishes with approximate calories per typical
 * portion. These are ballpark values (standard nutrition references) so the app
 * works offline/keyless; users can adjust amounts when logging.
 */
data class MexicanDish(
    val name: String,
    val kcal: Int,        // approx kcal per portion
    val portion: String   // e.g. "1 taco"
)

val MEXICAN_DISHES: List<MexicanDish> = listOf(
    MexicanDish("Taco al pastor", 150, "1 taco"),
    MexicanDish("Taco de bistec", 180, "1 taco"),
    MexicanDish("Taco de carnitas", 200, "1 taco"),
    MexicanDish("Taco dorado", 130, "1 pieza"),
    MexicanDish("Enchiladas verdes", 350, "3 piezas"),
    MexicanDish("Enchiladas rojas", 380, "3 piezas"),
    MexicanDish("Chilaquiles verdes", 420, "1 plato"),
    MexicanDish("Chilaquiles rojos", 450, "1 plato"),
    MexicanDish("Pozole rojo", 350, "1 plato"),
    MexicanDish("Pozole blanco", 320, "1 plato"),
    MexicanDish("Tamal de rajas", 250, "1 pieza"),
    MexicanDish("Tamal de pollo", 230, "1 pieza"),
    MexicanDish("Mole poblano con pollo", 480, "1 plato"),
    MexicanDish("Quesadilla de queso", 300, "1 pieza"),
    MexicanDish("Sope", 180, "1 pieza"),
    MexicanDish("Tostada", 220, "1 pieza"),
    MexicanDish("Flautas de pollo", 280, "3 piezas"),
    MexicanDish("Burrito de carne", 500, "1 pieza"),
    MexicanDish("Burrito de frijol", 400, "1 pieza"),
    MexicanDish("Huevos rancheros", 350, "1 plato"),
    MexicanDish("Huevos a la mexicana", 300, "1 plato"),
    MexicanDish("Chile relleno", 350, "1 pieza"),
    MexicanDish("Arroz a la mexicana", 200, "1 taza"),
    MexicanDish("Frijoles refritos", 220, "1 taza"),
    MexicanDish("Frijoles de la olla", 150, "1 taza"),
    MexicanDish("Guacamole", 150, "100 g"),
    MexicanDish("Elote con mayonesa", 200, "1 pieza"),
    MexicanDish("Esquites", 180, "1 taza"),
    MexicanDish("Torta de milanesa", 450, "1 pieza"),
    MexicanDish("Torta de jamón", 380, "1 pieza"),
    MexicanDish("Ceviche", 200, "1 taza"),
    MexicanDish("Cóctel de camarón", 250, "1 vaso"),
    MexicanDish("Caldo de res", 350, "1 plato"),
    MexicanDish("Caldo de pollo", 250, "1 plato"),
    MexicanDish("Menudo", 300, "1 plato"),
    MexicanDish("Barbacoa", 350, "200 g"),
    MexicanDish("Carnitas", 400, "200 g"),
    MexicanDish("Birria", 380, "1 plato"),
    MexicanDish("Pambazo", 500, "1 pieza"),
    MexicanDish("Gordita", 250, "1 pieza"),
    MexicanDish("Memela", 230, "1 pieza"),
    MexicanDish("Churro", 200, "1 pieza"),
    MexicanDish("Flan", 250, "1 porción"),
    MexicanDish("Arroz con leche", 220, "1 taza")
)
