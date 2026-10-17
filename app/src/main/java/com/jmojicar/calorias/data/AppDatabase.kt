package com.jmojicar.calorias.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/** A dish/food saved locally (manual entry or from a search result). */
@Entity(tableName = "custom_foods")
data class CustomFood(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val per100g: Boolean,   // true = values per 100 g, false = per serving
    val kcal: Int,
    val protein: Float,     // grams
    val fat: Float,         // grams
    val carbs: Float,       // grams
    val sugar: Float        // grams
)

/** One food logged on a given day (macro totals already scaled). */
@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,       // "yyyy-MM-dd"
    val name: String,
    val calories: Int,      // kcal
    val protein: Float,     // grams
    val fat: Float,         // grams
    val carbs: Float,       // grams
    val sugar: Float        // grams
)

@Dao
interface AppDao {

    // Custom foods
    @Insert suspend fun insertFood(food: CustomFood): Long
    @Query("SELECT * FROM custom_foods ORDER BY name COLLATE NOCASE")
    suspend fun getAllFoods(): List<CustomFood>
    @Delete suspend fun deleteFood(food: CustomFood)

    // Log entries
    @Insert suspend fun insertLog(entry: LogEntry): Long
    @Query("SELECT * FROM log_entries WHERE date = :date ORDER BY id DESC")
    suspend fun getLogForDate(date: String): List<LogEntry>
    @Query("SELECT * FROM log_entries WHERE date BETWEEN :start AND :end ORDER BY date DESC, id DESC")
    suspend fun getLogBetween(start: String, end: String): List<LogEntry>
    @Delete suspend fun deleteLog(entry: LogEntry)
}

@Database(entities = [CustomFood::class, LogEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calorias.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
