package com.kevinnesbitt.simple_ist

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.TextAlign
import kotlinx.coroutines.flow.first

class GlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 1. Get the database instance using your correct method name [cite: 12]
        val db = GroceryDao.AppDatabase.getDatabase(context)
        val dao = db.groceryDao()

        // 2. Fetch the one-shot settings snapshot using .first()
        val settingsSnapshot = dao.getSettings().first()
        val targetListId = settingsSnapshot.widgetDisplayListId

        // 3. Collect snapshots of all lists, items, and content
        val allLists = dao.getAllLists().first()
        val allItems = dao.getAllItems().first()
        val allContent = dao.getAllContent().first()

        // 4. Filter the data dynamically based on the active widget display selection
        val groceryListObj = allLists.find { it.id == targetListId }

        // Correctly filter out list items by comparing their listId
        val itemLst = allItems.filter { it.listId == targetListId }

        val contentListObj = allContent.find { it.listId == targetListId }
        val contentText = contentListObj?.content ?: ""

        // 5. Send clean static elements into the composable layout
        provideContent {
            WidgetContent(
                settings = settingsSnapshot,
                groceryListObj = groceryListObj,
                itemLst = itemLst,
                content = contentText
            )
        }
    }
}

@Composable
fun WidgetContent(
    settings: SettingsEntity,
    groceryListObj: GroceryListEntity?,
    itemLst: List<GroceryItemEntity>,
    content: String
) {
    val barTextColor = Color(settings.barTextColor)
    val mainTextColor = Color(settings.mainTextColor)
    val backgroundColor = settings.backgroundColor

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(color = Color(backgroundColor))
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(color = Color(settings.barColor)),
            horizontalAlignment = Alignment.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "     ➜]",
                modifier = GlanceModifier
                    .padding(4.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                style = TextStyle(
                    fontSize = 20.sp,
                    color = ColorProvider(day = barTextColor, night = barTextColor),
                    fontWeight = androidx.glance.text.FontWeight.Bold
                )
            )
        }

        // Check using your exact field name 'listType' from GroceryListEntity
        if (itemLst.isNotEmpty() && groceryListObj?.listType == "grocery") {
            LazyColumn {
                items(itemLst) { groceryItem ->
                    if (groceryItem.strike) {
                        Text(
                            text = "• " + groceryItem.itemName,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                textDecoration = androidx.glance.text.TextDecoration.LineThrough,
                                color = ColorProvider(day = Color.LightGray, night = Color.LightGray)
                            )
                        )
                    } else {
                        Text(
                            text = "• " + groceryItem.itemName,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(day = mainTextColor, night = mainTextColor)
                            )
                        )
                    }
                }
            }
        } else if (content.isNotEmpty() && groceryListObj?.listType == "generic") {
            LazyColumn {
                item {
                    Text(
                        text = content,
                        modifier = GlanceModifier.padding(8.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(day = mainTextColor, night = mainTextColor)
                        )
                    )
                }
            }
        } else {
            // Safe fallback layout if no list matches or is selected
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add a list in the app settings to display a list here!",
                    modifier = GlanceModifier.padding(16.dp),
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ColorProvider(day = Color.Gray, night = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}