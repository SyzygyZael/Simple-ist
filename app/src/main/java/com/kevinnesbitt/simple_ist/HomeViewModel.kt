package com.kevinnesbitt.simple_ist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).groceryDao()

    init {
        viewModelScope.launch {
            val existing = dao.getSettingsOnce()
            if (existing == null) {
                dao.insertSettings(SettingsEntity())
            }
        }
    }

    // private var nextId = 1
    // private var nextItemId = 1

    val lists: StateFlow<List<GroceryList>> = dao.getAllLists()
        .combine(dao.getAllItems()) { lists, items ->
            lists.map { list ->
                GroceryList(
                    id = list.id,
                    name = list.name,
                    items = items
                        .filter { it.listId == list.id }
                        .map { ItemList(id = it.id, itemName = it.itemName, strike = it.strike) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings = dao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())



    fun addList(name: String, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val newId = dao.insertList(GroceryListEntity(id = 0, name = name))
            onComplete(newId.toInt())
        }
    }

    fun addItem(listId: Int, itemName: String) {
        viewModelScope.launch {
            dao.insertItem(GroceryItemEntity(id = 0, listId = listId, itemName = itemName, strike = false))
        }
    }

    fun updateListName(listId: Int, newName: String) {
        viewModelScope.launch {
            dao.updateListName(listId, newName)
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            dao.deleteItemsByListId(listId)
            dao.deleteList(listId)
        }
    }

    fun deleteItem(listId: Int, itemId: Int) {
        viewModelScope.launch {
            dao.deleteItem(itemId)
        }
    }

    fun strikeItem(listId: Int, itemId: Int) {
        viewModelScope.launch {
            val currentStrike = lists.value
                .find { it.id == listId }
                ?.items?.find { it.id == itemId }
                ?.strike ?: false
            dao.updateItemStrike(itemId, !currentStrike)
        }
    }

    fun updateSetting(darkMode: Boolean, barColor: Long) {
        viewModelScope.launch {
            dao.updateSetting(switch = darkMode, color = barColor)
        }
    }

    data class GroceryList(
        val id: Int,
        val name: String,
        val items: List<ItemList>
    )

    data class ItemList(
        val id: Int,
        val itemName: String,
        val strike: Boolean
    )
}