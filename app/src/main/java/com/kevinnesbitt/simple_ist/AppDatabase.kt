package com.kevinnesbitt.simple_ist

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "grocery_lists")
data class GroceryListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val listType: String
)

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val listId: Int,
    val itemName: String,
    val strike: Boolean
)

@Entity(tableName = "generic_list_content")
data class GenericContentEntity(
    @PrimaryKey(autoGenerate = false) val listId: Int,
    val content: String
)

@Entity(tableName = "transformation_ranges")
data class TransformationRangesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int,
    val type: String,
    val start: Int,
    val endIndex: Int
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val settingId: Int = 1,
    val darkMode: Boolean = false,
    val barColor: Long = 0xFFFFFF00L
)

@Dao
interface GroceryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: GroceryListEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContent(content: GenericContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransformationRange(transformationRange: TransformationRangesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Query("SELECT * FROM grocery_lists")
    fun getAllLists(): Flow<List<GroceryListEntity>>

    @Query("SELECT * FROM transformation_ranges")
    fun getAllTransformationRanges(): Flow<List<TransformationRangesEntity>>

    @Query("SELECT * FROM generic_list_content")
    fun getAllContent(): Flow<List<GenericContentEntity>>

    @Query("SELECT * FROM grocery_items")
    fun getAllItems(): Flow<List<GroceryItemEntity>>

    @Query("DELETE FROM grocery_items WHERE id = :listId")
    suspend fun deleteItemsByListId(listId: Int)

    @Query("DELETE FROM grocery_lists WHERE id = :listId")
    suspend fun deleteList(listId: Int)

    @Query("DELETE FROM grocery_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)

    @Query("DELETE FROM transformation_ranges WHERE id = :id")
    suspend fun deleteTransformationRange(id: Int)

    @Query("UPDATE grocery_lists SET name = :newName WHERE id = :listId")
    suspend fun updateListName(listId: Int, newName: String)

    @Query("UPDATE grocery_items SET strike = :strike WHERE id = :itemId")
    suspend fun updateItemStrike(itemId: Int, strike: Boolean)

    @Query("UPDATE settings SET darkMode = :switch, barColor = :color WHERE settingId = 1")
    suspend fun updateSetting(switch: Boolean, color: Long)

    @Query("UPDATE transformation_ranges SET start = :start, endIndex = :end WHERE id = :id")
    suspend fun updateRange(id: Int, start: Int, end: Int)

    @Query("SELECT * FROM settings WHERE settingId = 1")
    fun getSettings(): Flow<SettingsEntity>

    @Query("SELECT * FROM settings WHERE settingId = 1")
    suspend fun getSettingsOnce(): SettingsEntity?
}

@Database(entities = [GroceryListEntity::class, GroceryItemEntity::class, GenericContentEntity::class, SettingsEntity::class, TransformationRangesEntity::class], version = 14)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "grocery_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}