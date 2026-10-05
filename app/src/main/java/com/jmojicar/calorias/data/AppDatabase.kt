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
    val kcalPer100g: Int, // 0 = not per-gram
    val kcalPerUnit: Int  // 0 = not per-unit (e.g. "1 taco")
)

/** One food logged on a given day. */
@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,   // "yyyy-MM-dd"
    val name: String,
    val calories: Int   // total kcal
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
    @Delete suspend fun deleteLog(entry: LogEntry)
    @Query("SELECT COALESCE(SUM(calories), 0) FROM log_entries WHERE date = :date")
    suspend fun totalForDate(date: String): Int
}

@Database(entities = [CustomFood::class, LogEntry::class], version = 1, exportSchema = false)
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
                ).build().also { INSTANCE = it }
            }
    }
}
