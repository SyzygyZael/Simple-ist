package com.kevinnesbitt.simple_ist

import android.app.Application
import androidx.glance.appwidget.updateAll
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
                        .map { ItemList(id = it.id, itemName = it.itemName, strike = it.strike) },
                    type = list.listType
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentList: StateFlow<List<ContentList>> = dao.getAllContent()
        .combine(dao.getAllTransformationRanges()) { contentLists, transformationRanges ->
            contentLists.map { content ->
                ContentList(
                    listId = content.listId,
                    content = content.content,
                    transformationRanges = transformationRanges
                        .filter { it.listId == content.listId }
                        .map { TransformationRanges(id = it.id, listId = it.listId, type = it.type, start = it.start, end = it.endIndex) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listNames: StateFlow<List<String>> = dao.getListNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listIds: StateFlow<List<Int>> = dao.getListIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings = dao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    fun addList(name: String, type: String, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val newId = dao.insertList(GroceryListEntity(id = 0, name = name, listType = type))
            onComplete(newId.toInt())
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun addItem(listId: Int, itemName: String) {
        viewModelScope.launch {
            dao.insertItem(GroceryItemEntity(id = 0, listId = listId, itemName = itemName, strike = false))
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun updateContent(listId: Int, newContent: String) {
        viewModelScope.launch {
            dao.upsertContent(GenericContentEntity(listId = listId, content = newContent))
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun addTransformationRange(listId: Int, type: String, start: Int, end: Int, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val newId = dao.insertTransformationRange(TransformationRangesEntity(type = type, listId = listId, start = start, endIndex = end))
            onComplete(newId.toInt())
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun deleteTransformationRange(id: Int) {
        viewModelScope.launch {
            dao.deleteTransformationRange(id)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun updateRange(id: Int, start: Int, end: Int) {
        viewModelScope.launch {
            dao.updateRange(id =  id, start = start, end = end)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun updateListName(listId: Int, newName: String) {
        viewModelScope.launch {
            dao.updateListName(listId, newName)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            dao.deleteItemsByListId(listId)
            dao.deleteList(listId)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun deleteItem(listId: Int, itemId: Int) {
        viewModelScope.launch {
            dao.deleteItem(itemId)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun strikeItem(listId: Int, itemId: Int) {
        viewModelScope.launch {
            val currentStrike = lists.value
                .find { it.id == listId }
                ?.items?.find { it.id == itemId }
                ?.strike ?: false
            dao.updateItemStrike(itemId, !currentStrike)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun updateSetting(darkMode: Boolean, barColor: Long, widgetDisplayListId: Int, barTextColor: Long) {
        viewModelScope.launch {
            dao.updateSetting(switch = darkMode, color = barColor, widgetDisplayListId = widgetDisplayListId, barTextColor = barTextColor)
            GlanceWidget().updateAll(getApplication())
        }
    }

    data class GroceryList(
        val id: Int,
        val name: String,
        val items: List<ItemList>,
        val type: String
    )

    data class ItemList(
        val id: Int,
        val itemName: String,
        val strike: Boolean
    )

    data class TransformationRanges(
        val id: Int,
        val listId: Int,
        val type: String,
        val start: Int,
        val end: Int
    )

    data class ContentList(
        val listId: Int,
        val content: String,
        val transformationRanges: List<TransformationRanges>
    )
}