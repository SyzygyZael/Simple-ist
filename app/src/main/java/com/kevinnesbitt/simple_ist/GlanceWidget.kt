package com.kevinnesbitt.simple_ist

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.Box
import androidx.glance.text.TextAlign
import kotlin.collections.sortedBy

class GlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // This is a suspend block, so you can fetch Room data or DataStore prefs right here!
        val application = context.applicationContext as Application
        val viewModel = HomeViewModel(application)
        // Inside here, you invoke provideContent to render your composable layout
        provideContent {
            WidgetContent(viewModel)
        }
    }
}

@Composable
fun WidgetContent(viewModel: HomeViewModel) {
    val settings by viewModel.settings.collectAsState()
    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == settings.widgetDisplayListId }
    val itemLst = groceryListObj?.items?: emptyList()
    val listId = groceryListObj?.id?: 0

    val contentListObj = viewModel.contentList.collectAsState().value.find { it.listId == listId }
    val content = contentListObj?.content?: ""
    val ranges = contentListObj?.transformationRanges?: emptyList()

    val annotatedString = AnnotatedString.Builder(content).apply {
        ranges.forEach { range ->
            // Ensure indices stay safely within bounds
            if (range.start <= content.length && range.end <= content.length) {
                when (range.type) {
                    "bold" -> addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                        start = range.start,
                        end = range.end
                    )
                    "underline" -> addStyle(
                        style = SpanStyle(textDecoration = TextDecoration.Underline),
                        start = range.start,
                        end = range.end
                    )
                }
            }
        }
    }.toAnnotatedString()

    var barTextColor by remember(settings) {
        if (settings.barColor == 0xFF111111L || settings.barColor == 0xFF000000L || settings.barColor == 0xFFFF0000L || settings.barColor == 0xFF0000FFL || settings.barColor == 0xFF808080L || settings.barColor == 0xFFFF69B4L || settings.barColor == 0xFF7851A9L) {
            mutableStateOf(Color.White)
        } else {
            mutableStateOf(Color.Black)
        }
    }

    var mainTextColor by remember(settings) {
        if (settings.darkMode) {
            mutableStateOf(Color.White)
        } else {
            mutableStateOf(Color.Black)
        }
    }

    var backgroundColor by remember(settings) {
        if (settings.darkMode) {
            mutableLongStateOf(0xFF111111L)
        } else {
            mutableLongStateOf(0xFFFFFFFFL)
        }
    }

    val localRanges = remember {
        mutableStateListOf<HomeViewModel.TransformationRanges>()
    }

    LaunchedEffect(ranges) {
        if (localRanges.isEmpty() && ranges.isNotEmpty()) {
            localRanges.addAll(ranges)
        }
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(color = Color(backgroundColor))
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(color = Color(settings.barColor)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = groceryListObj?.name ?: "",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = ColorProvider(day = barTextColor, night = barTextColor),
                    fontWeight = androidx.glance.text.FontWeight.Bold
                )
            )

            Text(
                text = "     ➜]",
                modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                style = TextStyle(
                    fontSize = 20.sp,
                    color = ColorProvider(day = barTextColor, night = barTextColor),
                    fontWeight = androidx.glance.text.FontWeight.Bold

                )
            )
        }
        // display grocery list items if the type is a Grocery List
        if (itemLst.isNotEmpty() && groceryListObj?.type == "grocery") {
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
        } else if (content.isNotEmpty() && groceryListObj?.type == "generic"){
            // GlanceRichText(
            //     rawText = content,
            //     ranges = localRanges,
            //     settings = settings
            // )
            LazyColumn {
                item {
                    Text(
                        text = content,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(day = mainTextColor, night = mainTextColor)
                        )
                    )
                }
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .background(color = Color(backgroundColor))
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add a list in the app settings to display a list here!",
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

@Composable
fun GlanceRichText(rawText: String, ranges: List<HomeViewModel.TransformationRanges>, settings: SettingsEntity) {
    // Sort your formatting blocks chronologically by where they start
    val sortedRanges = ranges.sortedBy { it.start }

    Row {
        var lastIndex = 0

        sortedRanges.forEach { range ->
            // 1. Render any plain text that lives BEFORE this styled block
            if (range.start > lastIndex && range.start <= rawText.length) {
                val plainText = rawText.substring(lastIndex, range.start)
                Text(text = plainText)
            }

            // 2. Render the styled block itself
            if (range.start < rawText.length) {
                val endBound = minOf(range.end, rawText.length)
                val styledText = rawText.substring(range.start, endBound)

                // Determine style configurations
                val style = when (range.type) {
                    "bold" -> TextStyle(fontWeight = androidx.glance.text.FontWeight.Bold)
                    "underline" -> TextStyle(textDecoration = androidx.glance.text.TextDecoration.Underline)
                    else -> TextStyle()
                }

                Text(text = styledText, style = style)
                lastIndex = endBound
            }
        }

        // 3. Render any remaining unstyled plain text at the very end
        if (lastIndex < rawText.length) {
            Text(text = rawText.substring(lastIndex))
        }
    }
}