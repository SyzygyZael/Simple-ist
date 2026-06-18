package com.kevinnesbitt.simple_ist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var lists by mutableStateOf(listOf<GroceryList>())
        private set

    private var nextId = 1

    fun addList(name: String, itemList: List<String> = emptyList()) {
        lists = lists + GroceryList(
            id = nextId++,
            name = name,
            items = itemList
        )
    }

    data class GroceryList(
        val id: Int,
        val name: String,
        val items: List<String>
    )

    // data class ItemList(
    //     val listName: String,
    //     val item: String
    // )
}