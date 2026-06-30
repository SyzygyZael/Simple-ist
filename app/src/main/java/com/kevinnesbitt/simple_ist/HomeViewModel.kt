package com.kevinnesbitt.simple_ist

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val billingManager = BillingManager(application)
    val isPremiumUser: StateFlow<Boolean> = billingManager.isPremium

    fun launchBillingFlow(activity: Activity) {
        billingManager.launchPurchaseFlow(activity, "premium")
    }

    private val dao = GroceryDao.AppDatabase.getDatabase(application).groceryDao()

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
                        .map { ItemList(id = it.id, listId = it.listId, itemName = it.itemName, strike = it.strike, order = it.itemOrder) },
                    type = list.listType,
                    order = list.listOrder
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

    fun addList(name: String, type: String, navController: NavController) {
        viewModelScope.launch(Dispatchers.IO) {
            val maxOrder = dao.getMaxListOrder()
            val newList = GroceryListEntity(
                id = 0,
                name = name,
                listType = type,
                listOrder = maxOrder + 1 // Starts at 0 if empty (-1 + 1)
            )
            val newId = dao.insertList(newList)

            // onComplete(newId.toInt())
            GlanceWidget().updateAll(getApplication())

            withContext(Dispatchers.Main) {
                navController.navigate("list/$newId/$type")
            }
        }
    }

    fun addItem(listId: Int, itemName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val maxOrder = dao.getMaxItemOrder()
            val newItem = GroceryItemEntity(
                id = 0,
                listId = listId,
                itemName = itemName,
                strike = false,
                itemOrder = maxOrder + 1
            )

            dao.insertItem(newItem)

            GlanceWidget().updateAll(getApplication())
        }
    }

    fun addImage(listId: Int, imagePath: String) {
        viewModelScope.launch {
            dao.insertImagePath(ImageEntity(listId = listId, imagePath = imagePath))
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

    fun updateSetting(darkMode: Boolean, barColor: Long, widgetDisplayListId: Int, barTextColor: Long, backgroundColor: Long, mainTextColor: Long, theme: String, barColorString: String, barTextColorString: String) {
        viewModelScope.launch {
            dao.updateSetting(
                switch = darkMode,
                color = barColor,
                widgetDisplayListId = widgetDisplayListId,
                barTextColor = barTextColor,
                theme = theme,
                backgroundColor = backgroundColor,
                mainTextColor = mainTextColor,
                barColorString = barColorString,
                barTextColorString = barTextColorString
            )
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun updateListOrder(reorderedList: List<GroceryList>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Map the UI models back to Entities with their new index sequence
            val updatedEntities = reorderedList.mapIndexed { index, groceryList ->
                GroceryListEntity(
                    id = groceryList.id,
                    name = groceryList.name,
                    listType = groceryList.type,
                    listOrder = index // 👈 The new index becomes the database position
                )
            }
            dao.updateLists(updatedEntities)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun updateItemOrder(reorderedItems: List<ItemList>, listId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedEntities = reorderedItems.mapIndexed { index, item ->
                GroceryItemEntity(
                    id = item.id,
                    itemName = item.itemName,
                    strike = item.strike,
                    listId = listId,
                    itemOrder = index
                )
            }
            dao.updateItems(updatedEntities)
            GlanceWidget().updateAll(getApplication())
        }
    }

    fun handleSelectedImage(uri: Uri, currentText: String, onUpdated: (String) -> Unit) {
        viewModelScope.launch {
            val permanentPath = saveImageToInternalStorage(uri)

            if (permanentPath != null) {
                // Construct your custom image token string
                val updatedText = currentText + "\n[[image:$permanentPath]]\n"

                // Pass the updated string back to your UI to update the state / save to Room
                onUpdated(updatedText)
            }
        }
    }

    fun getImagePathsForList(listId: Int): StateFlow<List<String>> {
        return dao.getAllImagePaths(listId)
            .map { entityList ->
                // Extract just the string path from each entity
                entityList.map { it.imagePath }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>().applicationContext

            // 1. Create a dedicated folder inside your app's private internal storage
            val directory = File(context.filesDir, "note_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // 2. Create a unique filename so photos never overwrite each other
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            // 3. Open streams to copy the bytes from the temporary URI to your permanent file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // 4. Return the absolute permanent file path string!
            return@withContext file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    data class GroceryList(
        val id: Int,
        val name: String,
        val items: List<ItemList>,
        val type: String,
        val order: Int
    )

    data class ItemList(
        val id: Int,
        val listId: Int,
        val itemName: String,
        val strike: Boolean,
        val order: Int
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

    data class ImagePaths(
        val listId: Int,
        val imagePath: String
    )
}