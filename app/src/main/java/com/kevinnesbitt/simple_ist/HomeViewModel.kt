package com.kevinnesbitt.simple_ist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var lists by mutableStateOf(listOf<GroceryList>())
        private set

    private var nextId = 1
    private var nextItemId = 1

    fun addList(name: String, itemList: List<ItemList> = emptyList()) {
        lists = lists + GroceryList(
            id = nextId++,
            name = name,
            items = itemList
        )
    }

    fun addItem(listId: Int, itemName: String) {
        lists = lists.map{ groceryList ->
            if (groceryList.id == listId) {
                groceryList.copy(items = groceryList.items + ItemList(itemName = itemName, strike = false, id = nextItemId++))
            } else {
                groceryList
            }
        }
    }

    fun updateListName(listId: Int, newName: String) {
        lists = lists.map{ groceryList ->
            if (groceryList.id == listId) {
                groceryList.copy(name = newName)
            } else {
                groceryList
            }
        }
    }

    fun deleteList(listId: Int) {
        lists = lists.filter { groceryList ->
            groceryList.id != listId
        }
    }

    fun deleteItem(listId: Int, itemId: Int) {
        lists = lists.map { groceryList ->
            if (groceryList.id == listId) {
                groceryList.copy(items = groceryList.items.filter { item ->
                    item.id != itemId
                })
            } else {
                groceryList
            }
        }
    }

    fun strikeItem(listId: Int, itemId: Int) {
        lists = lists.map { groceryList ->
            if (groceryList.id == listId) {
                groceryList.copy(items = groceryList.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(strike = !item.strike)
                    } else {
                        item
                    }
                })
            } else {
                groceryList
            }
        }
    }

    data class GroceryList(
        val id: Int,
        val name: String,
        var items: List<ItemList>
    )

    data class ItemList(
        val id: Int,
        val itemName: String,
        val strike: Boolean
    )
}