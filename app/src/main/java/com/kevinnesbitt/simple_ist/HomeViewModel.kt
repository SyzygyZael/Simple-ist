package com.kevinnesbitt.simple_ist

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
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
                dao.insertSettings(SettingsEntity(acceptedPrivacyPolicy = 0))
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
            dao.insertImagePath(ImageEntity(id = 0, listId = listId, imagePath = imagePath))
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

    fun deleteImage(id: Int) {
        viewModelScope.launch {
            dao.deleteImage(id)
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

    fun getImagesForList(listId: Int): StateFlow<List<ImageData>> {
        return dao.getAllImagePaths(listId)
            .map { entityList ->
                entityList.mapNotNull { entity ->
                    try {
                        val bitmap = BitmapFactory.decodeFile(entity.imagePath)?.asImageBitmap()
                        if (bitmap != null) {
                            // Keep the DB primary key tied to the graphic asset!
                            ImageData(id = entity.id, bitmap = bitmap)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
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

    // Make sure your wrapper class structure matches this
    data class PdfExportData(
        val listName: String,
        val content: String,
        val ranges: List<TransformationRanges>,
        val imagePaths: List<String>
    )

    fun exportRichPdf(context: Context, data: PdfExportData) {
        viewModelScope.launch {
            val success = generateRichPdf(context, data)
            if (success) {
                Toast.makeText(context, "${data.listName}_Export.pdf saved to Downloads!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun generateRichPdf(context: Context, data: PdfExportData): Boolean = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        var yPosition = 60f
        val marginX = 40f

        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }

        // Flexible single-pass painter brush
        val textPaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
        }

        canvas.drawText(data.listName.ifBlank { "Untitled List" }, marginX, yPosition, titlePaint)
        yPosition += 50f

        // Draw associated list images
        if (data.imagePaths.isNotEmpty()) {
            var xPosition = marginX
            val imageSize = 100f // The maximum bounding slot width/height
            val spacing = 10f

            data.imagePaths.forEach { path ->
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    if (bitmap != null) {
                        // Check if the bounding slot layout box will exceed the edge of the PDF page
                        if (xPosition + imageSize > 555f) {
                            xPosition = marginX
                            yPosition += imageSize + spacing
                        }

                        // 1. Calculate the proportional scale ratio required for a uniform "Fit"
                        val originalWidth = bitmap.width.toFloat()
                        val originalHeight = bitmap.height.toFloat()
                        val scaleFactor = Math.min(imageSize / originalWidth, imageSize / originalHeight)

                        // 2. Compute the exact dimensions keeping the aspect ratio locked
                        val finalWidth = (originalWidth * scaleFactor).coerceAtLeast(1f).toInt()
                        val finalHeight = (originalHeight * scaleFactor).coerceAtLeast(1f).toInt()

                        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                            bitmap, finalWidth, finalHeight, true
                        )

                        // 3. Center the fit image cleanly inside its 100x100 layout square slot
                        val offsetX = (imageSize - finalWidth) / 2f
                        val offsetY = (imageSize - finalHeight) / 2f

                        canvas.drawBitmap(scaledBitmap, xPosition + offsetX, yPosition + offsetY, null)

                        // Recycle the temporary scaled instance to keep memory footprints low
                        if (scaledBitmap != bitmap) {
                            scaledBitmap.recycle()
                        }

                        // Advance the cursor by the standard grid slot size to keep alignment straight
                        xPosition += imageSize + spacing
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            yPosition += imageSize + 30f
        }

        // Fix: Normalize all variations of line breaks to prevent character index drift
        val normalizedContent = data.content.replace("\r\n", "\n").replace("\r", "\n")
        val lines = normalizedContent.split("\n")
        var globalIndexOffset = 0

        lines.forEach { line ->
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            var currentX = marginX
            var chunkStart = 0
            var maxLineTextSize = 14f // Tracks line height dynamically based on largest text element

            // Process line by breaking it into chunks sharing identical formatting styles
            while (chunkStart < line.length) {
                val firstPos = globalIndexOffset + chunkStart

                // Map ALL formatting options—including headers—directly to character indices
                val isBiggerHead = data.ranges.any { it.type.contains("biggerHeader") && firstPos >= it.start && firstPos < it.end }
                val isBigHead = data.ranges.any { it.type.contains("bigHeader") && firstPos >= it.start && firstPos < it.end }
                val isBold = data.ranges.any { it.type.contains("bold") && firstPos >= it.start && firstPos < it.end }
                val isItalic = data.ranges.any { it.type.contains("italic") && firstPos >= it.start && firstPos < it.end }
                val isUnderline = data.ranges.any { it.type.contains("underline") && firstPos >= it.start && firstPos < it.end }

                // Find how far this precise combination of styles extends down the string segment
                var chunkEnd = chunkStart + 1
                while (chunkEnd < line.length) {
                    val nextPos = globalIndexOffset + chunkEnd
                    val nextBiggerHead = data.ranges.any { it.type.contains("biggerHeader") && nextPos >= it.start && nextPos < it.end }
                    val nextBigHead = data.ranges.any { it.type.contains("bigHeader") && nextPos >= it.start && nextPos < it.end }
                    val nextBold = data.ranges.any { it.type.contains("bold") && nextPos >= it.start && nextPos < it.end }
                    val nextItalic = data.ranges.any { it.type.contains("italic") && nextPos >= it.start && nextPos < it.end }
                    val nextUnderline = data.ranges.any { it.type.contains("underline") && nextPos >= it.start && nextPos < it.end }

                    if (nextBiggerHead == isBiggerHead && nextBigHead == isBigHead &&
                        nextBold == isBold && nextItalic == isItalic && nextUnderline == isUnderline) {
                        chunkEnd++
                    } else {
                        break
                    }
                }

                val chunkText = line.substring(chunkStart, chunkEnd)

                // Dynamic layout resizing applied cleanly per-chunk
                val currentChunkSize = when {
                    isBiggerHead -> 24f
                    isBigHead -> 20f
                    else -> 14f
                }
                textPaint.textSize = currentChunkSize
                if (currentChunkSize > maxLineTextSize) {
                    maxLineTextSize = currentChunkSize
                }

                // Headers are implicitly bolded, but can also carry italic styling modifiers
                val finalBold = isBold || isBigHead || isBiggerHead
                val styleFlag = when {
                    finalBold && isItalic -> Typeface.BOLD_ITALIC
                    finalBold -> Typeface.BOLD
                    isItalic -> Typeface.ITALIC
                    else -> Typeface.NORMAL
                }

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, styleFlag)
                textPaint.isUnderlineText = isUnderline

                // Render text with correct font kerning pairs
                canvas.drawText(chunkText, currentX, yPosition, textPaint)
                currentX += textPaint.measureText(chunkText)

                chunkStart = chunkEnd
            }

            // Advance line drop spacing safely depending on the largest text component found on this line
            yPosition += maxLineTextSize * 1.4f

            // Move structural tracker offset forward (+1 accounts for the stripped line break character)
            globalIndexOffset += line.length + 1
        }

        pdfDocument.finishPage(page)

        val displayName = "${data.listName.replace(" ", "_")}_Export.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        return@withContext if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
                pdfDocument.close()
                true
            } catch (e: Exception) {
                pdfDocument.close()
                false
            }
        } else {
            pdfDocument.close()
            false
        }
    }

    fun acceptPrivacyPolicy() {
        viewModelScope.launch {
            dao.acceptPrivacyPolicy()
            GlanceWidget().updateAll(getApplication())
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

    data class ImageData(
        val id: Int,
        val bitmap: ImageBitmap
    )
}