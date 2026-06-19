package com.kevinnesbitt.simple_ist

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "grocery_lists")
data class GroceryListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String
)

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val listId: Int,
    val itemName: String,
    val strike: Boolean
)

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_lists")
    fun getAllLists(): Flow<List<GroceryListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: GroceryListEntity)

    @Query("DELETE FROM grocery_items WHERE id = :listId")
    suspend fun deleteItemsByListId(listId: Int)

    @Query("DELETE FROM grocery_lists WHERE id = :listId")
    suspend fun deleteList(listId: Int)

    @Query("UPDATE grocery_lists SET name = :newName WHERE id = :listId")
    suspend fun updateListName(listId: Int, newName: String)

    @Query("SELECT * FROM grocery_items")
    fun getAllItems(): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)

    @Query("UPDATE grocery_items SET strike = :strike WHERE id = :itemId")
    suspend fun updateItemStrike(itemId: Int, strike: Boolean)
}

@Database(entities = [GroceryListEntity::class, GroceryItemEntity::class], version = 2)
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
                ).build().also { INSTANCE = it }
            }
        }
    }
}