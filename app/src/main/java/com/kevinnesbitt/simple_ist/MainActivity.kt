package com.kevinnesbitt.simple_ist

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.textclassifier.TextSelection
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TransitEnterexit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.kevinnesbitt.simple_ist.ui.theme.SimpleistTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kevinnesbitt.simple_ist.ui.TextVisualTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlin.collections.emptyList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        var keepSplashScreenOn = true

        splashScreen.setKeepOnScreenCondition {
            keepSplashScreenOn
        }

        lifecycleScope.launch {
            delay(200.milliseconds)

            keepSplashScreenOn = false
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            SimpleistTheme {
                val navController = rememberNavController()
                val viewModel: HomeViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )

                val isPremium by viewModel.isPremiumUser.collectAsState()
                // val isPremium = false

                val settings by viewModel.settings.collectAsState()

                LaunchedEffect(settings.barColor, settings) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(
                            scrim = settings.barColor.toInt(),
                            darkScrim = settings.barColor.toInt()
                        )
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(navController, viewModel, isPremium)
                    }

                    composable("settings") {
                        SettingsScreen(navController, viewModel, isPremium)
                    }

                    composable(
                        route = "list/{listId}/{listType}",
                        arguments = listOf(
                            navArgument("listId") { type = NavType.IntType },
                            navArgument("listType") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val listId = backStackEntry.arguments?.getInt("listId")?: 0
                        val listType = backStackEntry.arguments?.getString("listType")?:"grocery"

                        when (listType) {
                            "grocery" -> GroceryListScreen(listId = listId, navController = navController, viewModel)
                            "generic" -> GenericListScreen(listId = listId, navController = navController, viewModel, isPremium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    val activity = context as Activity

    val windowInfo = LocalWindowInfo.current
    val screenHeight = windowInfo.containerDpSize.height

    val settings by viewModel.settings.collectAsState()
    // 1. Get the source of truth stream from ViewModel
    val databaseLists by viewModel.lists.collectAsStateWithLifecycle()

    // 2. Create a local mutable state buffer initialized with the database state
    var localLists by remember { mutableStateOf(emptyList<HomeViewModel.GroceryList>()) }

    // 3. Keep local state synced whenever the database emits a fresh list from disk
    LaunchedEffect(databaseLists) {
        localLists = databaseLists
    }

    val lazyListState = rememberLazyListState()

    // 4. Update local state immediately so it doesn't snap back, then tell VM
    val reorderableState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        localLists = localLists.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        // Tell the viewmodel to write this new arrangement to disk in the background
        viewModel.updateListOrder(localLists)
    }

    var barTextColor by remember(settings) {
        mutableStateOf(Color(settings.barTextColor))
    }

    var mainTextColor by remember(settings) {
        // if (settings.darkMode) {
        //     mutableStateOf(Color.White)
        // } else {
        //     mutableStateOf(Color.Black)
        // }

        mutableStateOf(Color(settings.mainTextColor))
    }

    var backgroundColor by remember(settings) {
        // if (settings.darkMode) {
        //     mutableLongStateOf(0xFF111111L)
        // } else {
        //     mutableLongStateOf(0xFFFFFFFFL)
        // }

        mutableLongStateOf(settings.backgroundColor)
    }

    var dropdownColor by remember(settings) {
        if (settings.darkMode) {
            mutableStateOf(Color.Gray)
        } else {
            mutableStateOf(Color.White)
        }
    }

    var lstName by remember {
        mutableStateOf("")
    }

    var isAddingGroceryLst by remember {
        mutableStateOf(false)
    }

    var isAddingGenericLst by remember {
        mutableStateOf(false)
    }

    var isChoosingListType by remember {
        mutableStateOf(false)
    }

    var expandedListId by remember {
        mutableStateOf<Int?>(null)
    }

    var isChangingListName by remember {
        mutableStateOf(false)
    }

    var newName by remember {
        mutableStateOf(TextFieldValue(text = ""))
    }

    var tempGroceryListId by remember {
        mutableIntStateOf(0)
    }

    var showDuplicateListNameDialog by remember {
        mutableStateOf(false)
    }

    var promptPremiumDialogue by remember {
        mutableStateOf(false)
    }

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        containerColor = Color(backgroundColor),
        bottomBar = {

            // set up bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Button(onClick = {
                    navController.navigate("settings")
                },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonColors(
                        containerColor = Color(backgroundColor),
                        contentColor = mainTextColor,
                        disabledContentColor = mainTextColor,
                        disabledContainerColor = Color(backgroundColor)
                    ),
                    shape = CircleShape
                ) {
                    Text(text = "⚙", fontSize = 27.sp)
                }

                Button(
                    onClick = {
                        if (isPremium) {
                            isChoosingListType = true
                        } else if (databaseLists.size >= 3) {
                            promptPremiumDialogue = true
                        } else {
                            isChoosingListType = true
                        }
                },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(18.dp)
                        .size(70.dp, 70.dp),
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    ),
                    shape = CircleShape
                ) {
                    Text(text = "+", fontSize = 30.sp, textAlign = TextAlign.Center)
                }
            }
        }
    ) { innerPadding ->
        // super container setup
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(color = Color(backgroundColor))
        ) {
            HorizontalDivider(thickness = 2.dp, color = mainTextColor)

            // draw lists
            if (localLists.isNotEmpty()) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.imePadding(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = screenHeight - 200.dp // 👈 Adjust this value to allow scrolling further up
                    )
                ) {
                    items(localLists, key = { groceryList -> groceryList.id }) { groceryList ->
                        ReorderableItem(reorderableState, key = groceryList.id) { isDragging ->
                            val elevation = animateDpAsState(if (isDragging) 12.dp else 3.dp)

                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                border = BorderStroke(1.dp, Color.Gray),
                                tonalElevation = elevation.value,
                                modifier = Modifier
                                    .padding(3.dp)
                                    .animateItem()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = Color(backgroundColor))
                                ) {
                                    Text(
                                        text = "⋮⋮",
                                        fontSize = 25.sp,
                                        color = mainTextColor,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .draggableHandle()
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { navController.navigate("list/${groceryList.id}/${groceryList.type}") },
                                                onLongClick = { expandedListId = groceryList.id }
                                            )
                                    ) {
                                        Text(
                                            text = groceryList.name,
                                            fontSize = 25.sp,
                                            color = mainTextColor,
                                            modifier = Modifier.padding(10.dp)
                                        )

                                        Text(
                                            text = if (groceryList.type == "grocery") "Grocery List" else "Generic List",
                                            fontSize = 18.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                                // dropdown menu setup
                                DropdownMenu(
                                    expanded = expandedListId == groceryList.id,
                                    onDismissRequest = { expandedListId = null },
                                    modifier = Modifier.background(color = Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Delete",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            viewModel.deleteList(listId = groceryList.id)
                                        }
                                    )

                                    HorizontalDivider(thickness = 1.dp, color = Color.Black)

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Rename",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            val listToBeRenamed = databaseLists.find { it.id == groceryList.id }
                                            val listNameBruh = listToBeRenamed?.name?: ""

                                            tempGroceryListId = groceryList.id
                                            newName = TextFieldValue(text = listNameBruh, selection = TextRange(0, listNameBruh.length))
                                            isChangingListName = true
                                            expandedListId = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (!isAddingGroceryLst || !isAddingGenericLst || !isChoosingListType) {
                Box(modifier = Modifier
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Tap '+' to get started!", color = Color.Gray)
                }
            }

            // DIALOG BOXES

            // duplicate list name dialog
            if (showDuplicateListNameDialog) {
                AlertDialog(
                    onDismissRequest = { showDuplicateListNameDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDuplicateListNameDialog = false
                            },
                            colors = ButtonColors(
                                containerColor = Color.White,
                                disabledContainerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(text = "Confirm")
                        }
                    },
                    title = { Text(text = "Error") },
                    text = { Text(text = "Cannot create lists with the same name") },
                    shape = RoundedCornerShape(10.dp),
                    containerColor = dropdownColor
                )
            }

            // changing list name
            if (isChangingListName) {
                Dialog(
                    onDismissRequest = { isChangingListName = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = "Rename", textAlign = TextAlign.Center, fontSize = 21.sp, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)

                            val focusRequester = remember { FocusRequester() }

                            TextField(
                                value = newName,
                                onValueChange = { text ->
                                    if (text.text.length <= 20) {
                                        newName = text
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newName.text.isNotBlank()) {
                                            viewModel.updateListName(
                                                listId = tempGroceryListId,
                                                newName = newName.text
                                            )
                                            newName = TextFieldValue(text = "")
                                            lstName = ""
                                            tempGroceryListId = 0
                                            isChangingListName = false
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .padding(5.dp),
                                textStyle = TextStyle(fontSize = 20.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )

                            // request keyboard
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }

                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        isChangingListName = false
                                    },
                                    colors = ButtonColors(containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray)
                                ) {
                                    Text(text = "Cancel")
                                }

                                Button(
                                    onClick = {
                                        if (newName.text.isNotBlank()) {
                                            viewModel.updateListName(
                                                listId = tempGroceryListId,
                                                newName = newName.text
                                            )
                                            newName = TextFieldValue(text = "")
                                            lstName = ""
                                            tempGroceryListId = 0
                                            isChangingListName = false
                                        }
                                    },
                                    colors = ButtonColors(containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray)
                                ) {
                                    Text(text = "Confirm")
                                }
                            }
                        }
                    }
                }
            }

            // name creation on tapping '+'
            if (isChoosingListType) {
                Dialog(
                    onDismissRequest = { isChoosingListType = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 235.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .size(350.dp, 40.dp)
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "Choose a list type\n",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .size(145.dp, 165.dp)
                                        .clickable(
                                            onClick = {
                                                isAddingGroceryLst = true
                                                isChoosingListType = false
                                            }
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Grocery List",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "A list specially tailored for those looking to make shopping lists! Includes automatic bullet points, and you can cross out items by tapping them.",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            overflow = TextOverflow.Clip,
                                            textAlign = TextAlign.Left,
                                            modifier = Modifier
                                                .padding(4.dp),
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                ElevatedCard(
                                    modifier = Modifier
                                        .size(145.dp, 165.dp)
                                        .clickable(
                                            onClick = {
                                                isAddingGenericLst = true
                                                isChoosingListType = false
                                            }
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Generic List",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "The traditional list for your everyday needs. Tap anywhere to start typing, and you can add different kinds of bullet points too. Just like your typical text editor.",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            overflow = TextOverflow.Clip,
                                            textAlign = TextAlign.Left,
                                            modifier = Modifier
                                                .padding(4.dp),
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // add grocery list
            if (isAddingGroceryLst) {
                Dialog(
                    onDismissRequest = { isAddingGroceryLst = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = "Add Grocery List", textAlign = TextAlign.Center, fontSize = 21.sp, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)

                            val focusRequester = remember { FocusRequester() }

                            TextField(
                                value = lstName,
                                onValueChange = { text ->
                                    if (text.length <= 20) {
                                        lstName = text
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (lstName.isNotBlank()) {
                                            // "ANY" RETURNS A BOOLEAN OMG
                                            val exists = viewModel.lists.value.any { it.name == lstName }

                                            if (!exists) {
                                                viewModel.addList(name = lstName, type = "grocery", navController)
                                            } else {
                                                showDuplicateListNameDialog = true
                                            }
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .padding(5.dp),
                                textStyle = TextStyle(fontSize = 20.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )

                            // request keyboard
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }

                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        isAddingGroceryLst = false
                                    },
                                    colors = ButtonColors(containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray)
                                ) {
                                    Text(text = "Cancel")
                                }

                                Button(
                                    onClick = {
                                        if (lstName.isNotBlank()) {
                                            // "ANY" RETURNS A BOOLEAN OMG
                                            val exists = viewModel.lists.value.any { it.name == lstName }

                                            if (!exists) {
                                                viewModel.addList(name = lstName, type = "grocery", navController)
                                            } else {
                                                showDuplicateListNameDialog = true
                                            }
                                        }
                                    },
                                    colors = ButtonColors(containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray)
                                ) {
                                    Text(text = " Done ")
                                }
                            }
                        }
                    }
                }
            }

            // add generic list
            if (isAddingGenericLst) {
                Dialog(
                    onDismissRequest = { isAddingGenericLst = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = "Add Generic List", textAlign = TextAlign.Center, fontSize = 21.sp, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)

                            val focusRequester = remember { FocusRequester() }

                            TextField(
                                value = lstName,
                                onValueChange = { text ->
                                    if (text.length <= 20) {
                                        lstName = text
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (lstName.isNotBlank()) {
                                            // "ANY" RETURNS A BOOLEAN OMG
                                            val exists = viewModel.lists.value.any { it.name == lstName }

                                            if (!exists) {
                                                viewModel.addList(name = lstName, type = "generic", navController)
                                            } else {
                                                showDuplicateListNameDialog = true
                                            }
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .padding(5.dp),
                                textStyle = TextStyle(fontSize = 20.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )

                            // request keyboard
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }

                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        isAddingGenericLst = false
                                    },
                                    colors = ButtonColors(containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray)
                                ) {
                                    Text(text = "Cancel")
                                }

                                Button(
                                    onClick = {
                                        if (lstName.isNotBlank()) {
                                            // "ANY" RETURNS A BOOLEAN OMG
                                            val exists = viewModel.lists.value.any { it.name == lstName }

                                            if (!exists) {
                                                viewModel.addList(name = lstName, type = "generic", navController)
                                            } else {
                                                showDuplicateListNameDialog = true
                                            }
                                        }
                                    },
                                    colors = ButtonColors(containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray)
                                ) {
                                    Text(text = " Done ")
                                }
                            }
                        }
                    }
                }
            }

            // prompt premium for more lists
            if (promptPremiumDialogue) {
                Dialog(
                    onDismissRequest = { promptPremiumDialogue = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Get Premium to make more lists!",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Enjoy extra features with Premium.",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    viewModel.launchBillingFlow(activity)
                                    promptPremiumDialogue = false
                                },
                                colors = ButtonColors(
                                    contentColor = Color.Black,
                                    containerColor = Color.Cyan,
                                    disabledContentColor = Color.Black,
                                    disabledContainerColor = Color.Cyan
                                )
                            ) {
                                Text(
                                    text = "Get Premium!",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(7.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // accept privacy policy
            if (settings.acceptedPrivacyPolicy == 0) {
                Dialog(
                    onDismissRequest = { }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 550.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(7.dp)
                                .background(color = Color.White),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Privacy Policy",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "By accepting, you certify that you have read and acknowledged our policy on collecting and handling user data.",
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )

                            HorizontalDivider(thickness = 2.dp, color = Color.Black)

                            LazyColumn(
                                modifier = Modifier
                                    .size(width = 350.dp, height = 350.dp)
                                    .background(color = Color.White)
                            ) {
                                item {
                                    Text(
                                        text = "Privacy Policy for Simple-ist\n" +
                                                "Last Updated: July 2026\n\n" +
                                                "Thank you for choosing Simple-ist! Your privacy is incredibly important. This Privacy Policy outlines how Simple-ist handles your information.\n" +
                                                "The short version? We don't collect your personal data, and your notes belong strictly to you.\n\n" +
                                                "1. Information Collection and Use\n" +
                                                "Simple-ist is designed with a \"privacy-first\" philosophy.\n" +
                                                "Personal Data: We do not collect, request, or store any personal information (such as your name, email address, phone number, or location).\n" +
                                                "Your Notes: Any text, lists, or content you type into Simple-ist is processed and stored locally on your device. We do not have access to your notes, and we never transfer them to any external servers.\n\n" +
                                                "2. Data Storage and Deletion\n" +
                                                "Because your data is stored locally on your device, you have total control over it:\n" +
                                                "Deletion: You can delete your notes at any time directly within the app.\n" +
                                                "Uninstalling: If you uninstall Simple-ist, all notes stored within the app will be permanently deleted from your device. Because we do not keep cloud backups, we cannot recover deleted notes for you.\n\n" +
                                                "3. Third-Party Services and Payments\n" +
                                                "We keep things clean and minimal, but we do use Google's official infrastructure to handle in-app transactions safely:\n" +
                                                "Analytics & Ads: We do not use any third-party tracking, analytics tools, or advertising networks.\n" +
                                                "Google Play Billing: If you purchase a premium feature or subscription, the transaction is handled entirely and securely by Google Play Billing. Simple-ist never sees or stores your financial details (like credit card numbers or billing addresses).\n\n" +
                                                "4. Children’s Privacy\n" +
                                                "Simple-ist is intended for everyone. Because our app does not collect any personal information from any user, we do not knowingly or unknowingly harvest personal data from children.\n\n" +
                                                "5. Changes to This Privacy Policy\n" +
                                                "We may update this Privacy Policy from time to time. Any updates will be marked by a change to the \"Last Updated\" date at the top of this page. Since we do not collect your contact details to send notifications, we recommend reviewing this policy via the app store listing occasionally.\n\n" +
                                                "6. Contact Us\n" +
                                                "If you have any questions or feedback regarding this Privacy Policy, please feel free to reach out:\n" +
                                                "Email: kevnes522@gmail.com\n",
                                        color = Color.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 2.dp, color = Color.Black)

                            Button(
                                onClick = {
                                    viewModel.acceptPrivacyPolicy()
                                },
                                colors = ButtonColors(
                                    contentColor = Color.White,
                                    containerColor = Color.DarkGray,
                                    disabledContentColor = Color.White,
                                    disabledContainerColor = Color.DarkGray
                                )
                            ) {
                                Text(
                                    text = "Accept",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(7.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroceryListScreen(listId: Int, navController: NavController, viewModel: HomeViewModel) {
    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == listId }
    val itemLst = groceryListObj?.items?: emptyList()
    val listName = groceryListObj?.name?: ""

    // 2. Create a local mutable state buffer initialized with the database state
    var localItems by remember { mutableStateOf(emptyList<HomeViewModel.ItemList>()) }

    // 3. Keep local state synced whenever the database emits a fresh list from disk
    LaunchedEffect(itemLst) {
        localItems = itemLst
    }

    val lazyListState = rememberLazyListState()

    // 4. Update local state immediately so it doesn't snap back, then tell VM
    val reorderableState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        localItems = localItems.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        // Tell the viewmodel to write this new arrangement to disk in the background
        viewModel.updateItemOrder(localItems, listId)
    }

    val settings by viewModel.settings.collectAsState()

    var barTextColor by remember(settings) {
        mutableStateOf(Color(settings.barTextColor))
    }

    var mainTextColor by remember(settings) {
        // if (settings.darkMode) {
        //     mutableStateOf(Color.White)
        // } else {
        //     mutableStateOf(Color.Black)
        // }

        mutableStateOf(Color(settings.mainTextColor))
    }

    var backgroundColor by remember(settings) {
        // if (settings.darkMode) {
        //     mutableLongStateOf(0xFF111111L)
        // } else {
        //     mutableLongStateOf(0xFFFFFFFFL)
        // }

        mutableLongStateOf(settings.backgroundColor)
    }

    var newListName by remember {
        mutableStateOf(TextFieldValue(text = listName, selection = TextRange(0, listName.length)))
    }

    var isChangingListName by remember {
        mutableStateOf(false)
    }

    var itemName by remember {
        mutableStateOf("")
    }

    var isAddingItem by remember {
        mutableStateOf(false)
    }

    var expandableListId by remember {
        mutableStateOf<Int?>(null)
    }

    var randTextChooser by remember {
        mutableStateOf<Int?>((1..6).random())
    }

    var isReorderingItems by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(settings.barColor))
            ) { }
        }
    ) { innerPadding ->
        // screen container setup
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(color = Color(backgroundColor))
            .clickable(
                indication = null,
                onClick = {
                    isAddingItem = true
                },
                interactionSource = remember { MutableInteractionSource() }
            )
        ) {
            // top bar list name and buttons
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color(settings.barColor)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // back button
                Button(
                    onClick = { navController.navigate("home") },
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "Back", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                // list name changing
                if (isChangingListName) {
                    val focusRequester = remember { FocusRequester() }

                    BasicTextField(
                        value = newListName,
                        onValueChange = { text ->
                            if (text.text.length <= 20) {
                                newListName = text
                            } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newListName.text.isNotBlank()) {
                                    viewModel.updateListName(listId = listId, newName = newListName.text)
                                    newListName = TextFieldValue(text = newListName.text, selection = TextRange(0, newListName.text.length))
                                    isChangingListName = false
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 26.sp,
                            color = barTextColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    // request keyboard as soon as text field appears
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }

                } else {
                    Text(
                        text = listName,
                        fontSize = 26.sp,
                        color = barTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable( onClick = {
                                isChangingListName = true
                            } ),
                        textAlign = TextAlign.Center
                    )
                }

                // '+' and 'Done' buttons
                if (isAddingItem) {
                    Button(
                        onClick = {
                            if (itemName != "") {
                                viewModel.addItem(listId = listId, itemName = itemName)
                                itemName = ""
                            }
                            isAddingItem = false
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        colors = ButtonColors(
                            containerColor = Color(settings.barColor),
                            contentColor = barTextColor,
                            disabledContentColor = barTextColor,
                            disabledContainerColor = Color(settings.barColor)
                        )
                    ) {
                        Text(text = "Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isReorderingItems) {
                    Button(
                        onClick = {
                            isReorderingItems = false
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        colors = ButtonColors(
                            containerColor = Color(settings.barColor),
                            contentColor = barTextColor,
                            disabledContentColor = barTextColor,
                            disabledContainerColor = Color(settings.barColor)
                        )
                    ) {
                        Text(text = "Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            isAddingItem = true },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        colors = ButtonColors(
                            containerColor = Color(settings.barColor),
                            contentColor = barTextColor,
                            disabledContentColor = barTextColor,
                            disabledContainerColor = Color(settings.barColor)
                        )
                    ) {
                        Text(text = "+", fontSize = 28.sp)
                    }
                }
            }

            // display list items
            if (localItems.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.imePadding(),
                    state = lazyListState,
                ) {
                    items(localItems, key = { groceryItem -> groceryItem.id }) { groceryItem ->
                        ReorderableItem(reorderableState, key = groceryItem.id) { isDragging ->
                            Card(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .background(color = Color(backgroundColor)),
                                shape = RoundedCornerShape(7.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = Color(backgroundColor))
                                ) {
                                    if (isReorderingItems) {
                                        Text(
                                            text = "⋮⋮",
                                            fontSize = 19.sp,
                                            color = mainTextColor,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .draggableHandle()
                                        )
                                    }

                                    if (groceryItem.strike) {
                                        Text(
                                            text = "• " + groceryItem.itemName,
                                            fontSize = 19.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp)
                                                .combinedClickable(
                                                    onClick = {
                                                        viewModel.strikeItem(
                                                            listId = listId,
                                                            itemId = groceryItem.id
                                                        )
                                                    },
                                                    onLongClick = {
                                                        expandableListId = groceryItem.id
                                                    }
                                                ),
                                            textDecoration = TextDecoration.LineThrough,
                                            color = Color.LightGray
                                        )
                                    } else {
                                        Text(
                                            text = "• " + groceryItem.itemName,
                                            fontSize = 19.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp)
                                                .combinedClickable(
                                                    onClick = {
                                                        viewModel.strikeItem(
                                                            listId = listId,
                                                            itemId = groceryItem.id
                                                        )
                                                    },
                                                    onLongClick = {
                                                        expandableListId = groceryItem.id
                                                    }
                                                ),
                                            color = mainTextColor
                                        )
                                    }

                                    // dropdown menu setup
                                    DropdownMenu(
                                        expanded = expandableListId == groceryItem.id,
                                        onDismissRequest = { expandableListId = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Delete",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                viewModel.deleteItem(
                                                    itemId = groceryItem.id,
                                                    listId = listId
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Move",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                isReorderingItems = true
                                                expandableListId = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // allow item creation upon pressing '+'
                    if (isAddingItem) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "• ", fontSize = 19.sp, color = mainTextColor)

                                val focusRequester = remember { FocusRequester() }

                                BasicTextField(
                                    value = itemName,
                                    onValueChange = { text ->
                                        itemName = text
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (itemName != "") {
                                                viewModel.addItem(
                                                    listId = listId,
                                                    itemName = itemName
                                                )
                                                itemName = ""
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .focusRequester(focusRequester),
                                    textStyle = TextStyle(fontSize = 19.sp, color = mainTextColor),
                                    singleLine = true,
                                    cursorBrush = SolidColor(mainTextColor)
                                )
                                // request keyboard
                                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                            }
                        }
                    }
                }
            } else if (!isAddingItem) {

                // random message on empty list
                Box(modifier = Modifier
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (randTextChooser == 1) {
                        Text(text = "Tap '+' or on the screen to get started!", color = Color.Gray)
                    } else if (randTextChooser == 2) {
                        Text(text = "What are you shopping for today?", color = Color.Gray)
                    } else if (randTextChooser == 3) {
                        Text(text = "So many possibilities...", color = Color.Gray)
                    } else if (randTextChooser == 4) {
                        Text(text = "Quick! Write it down so you don't forget!", color = Color.Gray)
                    } else if (randTextChooser == 5) {
                        Text(text = "A blank space is but a limitless sky...", color = Color.Gray)
                    } else if (randTextChooser == 6) {
                        Text(text = "Tap an item to cross it out!", color = Color.Gray)
                    }
                }
            }

            // allow item creation upon pressing '+'
            if (isAddingItem && itemLst.isEmpty()) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "• ", fontSize = 19.sp, color = mainTextColor)

                    val focusRequester = remember { FocusRequester() }

                    BasicTextField(
                        value = itemName,
                        onValueChange = { text ->
                            itemName = text
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (itemName != "") {
                                    viewModel.addItem(listId = listId, itemName = itemName)
                                    itemName = ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(fontSize = 19.sp, color = mainTextColor),
                        singleLine = true,
                        cursorBrush = SolidColor(mainTextColor)
                    )
                    // request keyboard
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GenericListScreen(listId: Int, navController: NavController, viewModel: HomeViewModel, isPremium: Boolean) {
    val loadedImages by viewModel.getImagesForList(listId).collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == listId }
    val listName = groceryListObj?.name?: ""

    val contentListObj = viewModel.contentList.collectAsState().value.find { it.listId == listId }
    val content = contentListObj?.content?: ""
    val ranges = contentListObj?.transformationRanges?: emptyList()

    // 1. Grab the stream as a standard Compose state object
    val imagePaths by viewModel.getImagePathsForList(listId).collectAsState()

    val settings by viewModel.settings.collectAsState()

    var barTextColor by remember(settings) {
        mutableStateOf(Color(settings.barTextColor))
    }

    var mainTextColor by remember(settings) {
        // if (settings.darkMode) {
        //     mutableStateOf(Color.White)
        // } else {
        //     mutableStateOf(Color.Black)
        // }

        mutableStateOf(Color(settings.mainTextColor))
    }

    var backgroundColor by remember(settings) {
        // if (settings.darkMode) {
        //     mutableLongStateOf(0xFF111111L)
        // } else {
        //     mutableLongStateOf(0xFFFFFFFFL)
        // }

        mutableLongStateOf(settings.backgroundColor)
    }

    var isChangingListName by remember {
        mutableStateOf(false)
    }

    var newListName by remember {
        mutableStateOf(TextFieldValue(text = listName, selection = TextRange(0, listName.length)))
    }

    android.util.Log.d("newListName Value", "newListName = $newListName, listName = $listName")

    var showSideBar by remember {
        mutableStateOf(false)
    }

    var bulletList by remember {
        mutableStateOf(false)
    }

    var boldLetters by remember {
        mutableStateOf(false)
    }

    var italicLetters by remember {
        mutableStateOf(false)
    }

    var underlineLetters by remember {
        mutableStateOf(false)
    }

    var bigHeader by remember {
        mutableStateOf(false)
    }

    var biggerHeader by remember {
        mutableStateOf(false)
    }

    val localRanges = remember {
        mutableStateListOf<HomeViewModel.TransformationRanges>()
    }

    var listText by remember {
        mutableStateOf(TextFieldValue(text = "", selection = if (bulletList) TextRange(2) else TextRange.Zero))
    }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(content, ranges) {
        // 1. Only populate if we haven't initialized yet and database text finally arrives
        if (!isInitialized && content.isNotEmpty()) {
            listText = TextFieldValue(text = content, selection = TextRange(content.length))
            isInitialized = true
        } else if (!isInitialized && contentListObj != null && content.isEmpty()) {
            // If the database object loaded successfully but it's genuinely an empty note
            isInitialized = true
        }

        // 2. Sync down your formatting style rules cleanly
        if (localRanges.isEmpty() && ranges.isNotEmpty()) {
            localRanges.addAll(ranges)
        }
    }

    // 1. Declare this state variable directly above the tracking LaunchedEffect
    var lastTextAnchor by remember { mutableStateOf(listText.text) }

    // 2. Update the effect block to look like this:
    LaunchedEffect(listText.selection) {
        val cursor = listText.selection.start

        // Only read styles from the document if the text didn't change (Clicking/Arrows)
        if (listText.text == lastTextAnchor) {
            boldLetters = false
            italicLetters = false
            underlineLetters = false
            bigHeader = false
            biggerHeader = false

            val activeRangesAtCursor = localRanges.filter { range ->
                cursor >= range.start && cursor <= range.end
            }

            activeRangesAtCursor.forEach { range ->
                if (range.type == "bold") boldLetters = true
                if (range.type == "italic") italicLetters = true
                if (range.type == "underline") underlineLetters = true
                if (range.type == "bigHeader") bigHeader = true
                if (range.type == "biggerHeader") biggerHeader = true
            }
        } else {
            // Text changed because of typing/deleting! Skip resetting buttons and sync the anchor instead.
            lastTextAnchor = listText.text
        }
    }

    var expandedImageId by remember {
        mutableStateOf<Int?>(null)
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // 1. Pass a blank placeholder or text state to save the image to disk
            viewModel.handleSelectedImage(uri, "") { updatedTextWithPhotoTag ->
                val rawPath = updatedTextWithPhotoTag
                    .replace("\n", "")
                    .removePrefix("[[image:")
                    .removeSuffix("]]")

                // 2. ✨ SAVE TO DATABASE: Call your ViewModel to store the string directly in the table row
                viewModel.addImage(listId = listId, imagePath = rawPath)
            }
        }
    }

    var promptPremiumPhotoInsert by remember {
        mutableStateOf(false)
    }

    var promptPremiumPDF by remember {
        mutableStateOf(false)
    }

    var confirmPdfDownload by remember {
        mutableStateOf(false)
    }

    var showImagePreview by remember {
        mutableStateOf(false)
    }

    var tempImageId by remember {
        mutableIntStateOf(0)
    }

    var tempImageBitmap: ImageBitmap by remember {
        mutableStateOf(ImageBitmap(1, 1))
    }

    var showConfirmImageDeleteDialog by remember {
        mutableStateOf(false)
    }

    val density = LocalDensity.current

    val windowInfo = LocalWindowInfo.current
    val screenWidth = windowInfo.containerDpSize.width
    val screenHeight = windowInfo.containerDpSize.height

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color(backgroundColor),
        bottomBar = {
            AnimatedVisibility(
                visible = showSideBar,
                enter = slideInVertically { with(density) { 60.dp.roundToPx() } },
                exit = slideOutVertically { with(density) { 60.dp.roundToPx() } }

            ) {
                LazyRow(
                    modifier = Modifier
                        // .size(screenWidth, 60.dp)
                        .fillMaxWidth()
                        .background(color = Color.Transparent)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(1) {
                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                val selection = listText.selection
                                val originalText = listText.text
                                val lines = originalText.split("\n")
                                val bulletPrefix = "    • "
                                val prefixLen = bulletPrefix.length

                                // Calculate original absolute character indices of each line start/end
                                val originalLineSpans = mutableListOf<Pair<Int, Int>>()
                                var accum = 0
                                lines.forEach { line ->
                                    originalLineSpans.add(Pair(accum, accum + line.length))
                                    accum += line.length + 1
                                }

                                // Identify target lines overlapping with current selection
                                val isTargetLine = List(lines.size) { i ->
                                    val (lineStart, lineEnd) = originalLineSpans[i]
                                    if (selection.collapsed) {
                                        selection.start in lineStart..lineEnd
                                    } else {
                                        val overlapStart = maxOf(selection.start, lineStart)
                                        val overlapEnd = minOf(selection.end, lineEnd)
                                        overlapStart <= overlapEnd && (overlapStart < overlapEnd || selection.start == lineStart || selection.end == lineEnd)
                                    }
                                }

                                val targetLines = lines.filterIndexed { i, _ -> isTargetLine[i] }
                                val isUnbulleting = targetLines.all { it.startsWith(bulletPrefix) || it.isEmpty() }

                                // Index translation mapping function
                                fun translateIndex(origIdx: Int): Int {
                                    var origAccum = 0
                                    var newAccum = 0

                                    lines.forEachIndexed { i, line ->
                                        val lineStart = origAccum
                                        val lineEnd = origAccum + line.length

                                        val willChange = isTargetLine[i]
                                        val changeAmt = if (willChange) {
                                            if (isUnbulleting) {
                                                if (line.startsWith(bulletPrefix)) -prefixLen else 0
                                            } else {
                                                if (line.startsWith(bulletPrefix)) 0 else prefixLen
                                            }
                                        } else {
                                            0
                                        }

                                        if (origIdx <= lineEnd) {
                                            val offsetInLine = origIdx - lineStart
                                            return if (willChange && isUnbulleting && line.startsWith(bulletPrefix)) {
                                                if (offsetInLine < prefixLen) newAccum else newAccum + (offsetInLine - prefixLen)
                                            } else if (willChange && !isUnbulleting && !line.startsWith(bulletPrefix)) {
                                                newAccum + prefixLen + offsetInLine
                                            } else {
                                                newAccum + offsetInLine
                                            }
                                        }

                                        origAccum += line.length + 1
                                        newAccum += (line.length + changeAmt) + 1
                                    }
                                    return newAccum
                                }

                                // Reconstruct modified document string
                                val newLines = lines.mapIndexed { i, line ->
                                    if (isTargetLine[i]) {
                                        if (isUnbulleting) line.removePrefix(bulletPrefix) else (if (line.startsWith(bulletPrefix)) line else bulletPrefix + line)
                                    } else {
                                        line
                                    }
                                }
                                val finalFullText = newLines.joinToString("\n")

                                val finalSelectionStart = translateIndex(selection.start)
                                val finalSelectionEnd = translateIndex(selection.end)

                                // Process ranges to exclude the newly inserted bullet prefixes
                                val currentRangesSnapshot = localRanges.toList()
                                localRanges.clear()

                                currentRangesSnapshot.forEach { range ->
                                    val newStart = translateIndex(range.start)
                                    val newEnd = translateIndex(range.end)

                                    if (newEnd > newStart) {
                                        // Determine if this specific range spans over lines that just received bullets
                                        var currentNewAccum = 0
                                        val adjustedSubRanges = mutableListOf<Pair<Int, Int>>()

                                        var runningStart = newStart

                                        newLines.forEachIndexed { i, line ->
                                            val lineStart = currentNewAccum
                                            val lineEnd = currentNewAccum + line.length

                                            // If a bullet prefix was freshly added to this line, check if the range overlaps it
                                            if (isTargetLine[i] && !isUnbulleting && line.startsWith(bulletPrefix)) {
                                                val bulletStart = lineStart
                                                val bulletEnd = lineStart + prefixLen

                                                // If the range spans across or starts inside the newly added bullet, slice it out
                                                if (runningStart < bulletEnd && newEnd > bulletStart) {
                                                    if (runningStart < bulletStart) {
                                                        adjustedSubRanges.add(Pair(runningStart, bulletStart))
                                                    }
                                                    runningStart = maxOf(runningStart, bulletEnd)
                                                }
                                            }
                                            currentNewAccum += line.length + 1
                                        }

                                        if (newEnd > runningStart) {
                                            adjustedSubRanges.add(Pair(runningStart, newEnd))
                                        }

                                        // Apply adjustments back into local storage and database
                                        if (adjustedSubRanges.isEmpty()) {
                                            if (range.id != 0) viewModel.deleteTransformationRange(range.id)
                                        } else {
                                            // Update first fragment slot
                                            val firstSub = adjustedSubRanges.first()
                                            val updatedRange = range.copy(start = firstSub.first, end = firstSub.second)
                                            localRanges.add(updatedRange)
                                            if (range.id != 0) {
                                                viewModel.updateRange(range.id, updatedRange.start, updatedRange.end)
                                            }

                                            // Create separate range fragments for downstream breaks if split by multiple bullets
                                            for (k in 1 until adjustedSubRanges.size) {
                                                val extraSub = adjustedSubRanges[k]
                                                viewModel.addTransformationRange(listId, range.type, extraSub.first, extraSub.second) { realId ->
                                                    val extraRange = HomeViewModel.TransformationRanges(id = realId, listId, range.type, extraSub.first, extraSub.second)
                                                    localRanges.add(extraRange)
                                                }
                                            }
                                        }
                                    } else {
                                        if (range.id != 0) viewModel.deleteTransformationRange(range.id)
                                    }
                                }

                                listText = listText.copy(
                                    text = finalFullText,
                                    selection = TextRange(finalSelectionStart, finalSelectionEnd)
                                )
                                viewModel.updateContent(listId, finalFullText)
                                bulletList = !isUnbulleting
                            },
                            colors = ButtonColors(
                                containerColor = if (!bulletList) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!bulletList) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = ":",
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            modifier = Modifier
                                .size(55.dp, 55.dp),
                            onClick = {
                                boldLetters = !boldLetters
                            },
                            colors = ButtonColors(
                                containerColor = if (!boldLetters) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!boldLetters) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "B",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                italicLetters = !italicLetters
                            },
                            colors = ButtonColors(
                                containerColor = if (!italicLetters) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!italicLetters) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "\uD835\uDC70",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                underlineLetters = !underlineLetters
                            },
                            colors = ButtonColors(
                                containerColor = if (!underlineLetters) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!underlineLetters) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "U̲",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                bigHeader = !bigHeader
                                biggerHeader = false
                            },
                            colors = ButtonColors(
                                containerColor = if (!bigHeader) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!bigHeader) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "H̲",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                biggerHeader = !biggerHeader
                                bigHeader = false
                            },
                            colors = ButtonColors(
                                containerColor = if (!biggerHeader) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!biggerHeader) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "H̳",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                if (isPremium) {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else {
                                    promptPremiumPhotoInsert = true
                                }
                            },
                            colors = IconButtonColors(
                                containerColor = Color(backgroundColor),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = Color(backgroundColor)
                            ),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image, // ✨ Matches your document file image asset
                                contentDescription = "Export PDF",
                                modifier = Modifier.size(23.dp),
                                tint = mainTextColor // Automatically changes to your theme's font color state
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                if (isPremium) {
                                    confirmPdfDownload = true
                                } else {
                                    promptPremiumPDF = true
                                }
                            },
                            colors = IconButtonColors(
                                containerColor = Color(backgroundColor),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = Color(backgroundColor)
                            ),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload, // ✨ Matches your document file image asset
                                contentDescription = "Export PDF",
                                modifier = Modifier.size(23.dp),
                                tint = mainTextColor // Automatically changes to your theme's font color state
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // screen container setup
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(backgroundColor))
        ) {
            // top bar list name and buttons
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color(settings.barColor)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // back button
                Button(
                    onClick = {
                        navController.navigate("home")
                    },
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "Back", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                // list name changing
                if (isChangingListName) {
                    val focusRequester = remember { FocusRequester() }

                    BasicTextField(
                        value = newListName,
                        onValueChange = { text ->
                            if (text.text.length <= 20) {
                                newListName = text
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newListName.text.isNotBlank() && (newListName.text != listName)) {
                                    viewModel.updateListName(listId = listId, newName = newListName.text)
                                    newListName = TextFieldValue(text = newListName.text, selection = TextRange(0, newListName.text.length))
                                    isChangingListName = false
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 26.sp,
                            color = barTextColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    // request keyboard as soon as text field appears
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }

                } else {
                    Text(
                        text = listName,
                        fontSize = 26.sp,
                        color = barTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable( onClick = {
                                isChangingListName = true
                            } ),
                        textAlign = TextAlign.Center
                    )
                }

                // menu button
                Button(
                    onClick = { showSideBar = !showSideBar },
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "☰", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 250.dp // 👈 Adjust this value to allow scrolling further up
                )
                ) {
                if (loadedImages.isNotEmpty()) {
                    item(key = "note_images_header_${listId}") {
                        FlowRow(
                            modifier = Modifier
                                .sizeIn(
                                    minWidth = 0.dp,
                                    minHeight = 0.dp,
                                    maxWidth = screenWidth,
                                    maxHeight = screenHeight * 3
                                )
                                .padding(vertical = 8.dp)
                        ) {
                            loadedImages.forEach { imageData ->
                                Card(
                                    elevation = CardDefaults.elevatedCardElevation(5.dp, 5.dp, 5.dp, 5.dp, 5.dp, 5.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            // .padding(horizontal = 4.dp, vertical = 2.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    tempImageId = imageData.id
                                                    tempImageBitmap = imageData.bitmap
                                                    showImagePreview = true
                                                },
                                                onLongClick = { expandedImageId = imageData.id }
                                            ),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Image(
                                            bitmap = imageData.bitmap,
                                            contentDescription = "Note Image",
                                            modifier = Modifier
                                                .sizeIn(
                                                    maxHeight = screenWidth / 2 - 12.dp,
                                                    maxWidth = screenWidth / 2 - 12.dp,
                                                    minHeight = 0.dp,
                                                    minWidth = 0.dp
                                                )
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        DropdownMenu(
                                            expanded = imageData.id == expandedImageId,
                                            onDismissRequest = { expandedImageId = null }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(text = "Delete") },
                                                onClick = {
                                                    viewModel.deleteImage(imageData.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "note_text_field_${listId}") {
                    BasicTextField(
                        value = listText,
                        onValueChange = { newText ->
                            val oldText = listText.text
                            var currentText = newText.text
                            var selectionStart = newText.selection.start
                            var selectionEnd = newText.selection.end

                            // 1. DETERMINE BULLET MODIFICATIONS FIRST
                            val isBulletAddition = currentText.endsWith("\n") && bulletList
                            val isBulletDeletion = oldText.endsWith("\n    • ") && currentText.length < oldText.length

                            if (isBulletAddition) {
                                currentText += "    • "
                                selectionStart += 6
                                selectionEnd += 6
                            } else if (isBulletDeletion) {
                                currentText = currentText.dropLast(5)
                                selectionStart = maxOf(0, selectionStart - 5)
                                selectionEnd = maxOf(0, selectionEnd - 5)
                            }

                            // ✨ BULLETPROOF HEADER FIX: If a newline was added anywhere, automatically turn off header styles
                            if (currentText.count { it == '\n' } > oldText.count { it == '\n' }) {
                                bigHeader = false
                                biggerHeader = false
                            }

                            // 2. NOW EXECUTE RANGE ADJUSTMENTS BASED ON THE TRUE FINAL TEXT LENGTH
                            val lengthDifference = currentText.length - oldText.length
                            val cursorPos = selectionStart

                            if (lengthDifference > 0) {
                                val typedIndex = maxOf(0, cursorPos - lengthDifference)
                                val expandedRangeIds = mutableSetOf<Int>()

                                // Make a snapshot copy to safely read while modifying the main list
                                val currentRangesSnapshot = localRanges.toList()

                                currentRangesSnapshot.forEach { range ->
                                    if (range.id == 0) return@forEach // Skip temporary assignments

                                    // Map the specific range string type to its matching state toggle button
                                    val isButtonActive = when (range.type) {
                                        "bold" -> boldLetters
                                        "italic" -> italicLetters
                                        "underline" -> underlineLetters
                                        "bigHeader" -> bigHeader
                                        "biggerHeader" -> biggerHeader
                                        else -> false
                                    }

                                    if (typedIndex > range.start && typedIndex < range.end) {
                                        // CASE A: Cursor is strictly INSIDE the range
                                        val index = localRanges.indexOfFirst { it.id == range.id }
                                        if (index != -1) {
                                            if (isBulletAddition) {
                                                // Even if the button is active, we MUST split the range to create an unstyled gap for the bullet
                                                val leftRange = range.copy(end = typedIndex + 1) // Include the newline character
                                                localRanges[index] = leftRange
                                                viewModel.updateRange(range.id, leftRange.start, leftRange.end)

                                                // Right Half starts after the bullet points prefix
                                                val rightStart = typedIndex + 7
                                                val rightEnd = range.end + 7

                                                viewModel.addTransformationRange(listId, range.type, rightStart, rightEnd) { realId ->
                                                    val newRightRange = HomeViewModel.TransformationRanges(id = realId, listId, range.type, rightStart, rightEnd)
                                                    localRanges.add(newRightRange)
                                                }
                                            } else if (isButtonActive) {
                                                // Button is ON -> Expand the existing range forward normally
                                                val updated = range.copy(end = range.end + lengthDifference)
                                                localRanges[index] = updated
                                                viewModel.updateRange(range.id, updated.start, updated.end)
                                                expandedRangeIds.add(range.id)
                                            } else {
                                                // Button is OFF -> SPLIT THE RANGE into a left and right half
                                                val leftRange = range.copy(end = typedIndex)
                                                localRanges[index] = leftRange
                                                viewModel.updateRange(range.id, leftRange.start, leftRange.end)

                                                val rightStart = typedIndex + lengthDifference
                                                val rightEnd = range.end + lengthDifference

                                                viewModel.addTransformationRange(listId, range.type, rightStart, rightEnd) { realId ->
                                                    val newRightRange = HomeViewModel.TransformationRanges(id = realId, listId, range.type, rightStart, rightEnd)
                                                    localRanges.add(newRightRange)
                                                }
                                            }
                                        }
                                    } else if (typedIndex == range.end) {
                                        // CASE B: Cursor is exactly at the trailing edge of the range
                                        if (isButtonActive) {
                                            val index = localRanges.indexOfFirst { it.id == range.id }
                                            if (index != -1) {
                                                // If a bullet point is being added, only expand by 1 (the \n), skipping the bullet prefix
                                                val expansionAmt = if (isBulletAddition) 1 else lengthDifference
                                                val updated = range.copy(end = range.end + expansionAmt)
                                                localRanges[index] = updated
                                                viewModel.updateRange(range.id, updated.start, updated.end)
                                                expandedRangeIds.add(range.id)
                                            }
                                        }
                                    } else if (range.start >= typedIndex) {
                                        // CASE C: Range sits entirely downstream from the cursor
                                        if (range.start == typedIndex && isButtonActive && !isBulletAddition) {
                                            // Cursor is at leading edge and button is ON -> Swallow character into range
                                            val index = localRanges.indexOfFirst { it.id == range.id }
                                            if (index != -1) {
                                                val updated = range.copy(end = range.end + lengthDifference)
                                                localRanges[index] = updated
                                                viewModel.updateRange(range.id, updated.start, updated.end)
                                                expandedRangeIds.add(range.id)
                                            }
                                        } else {
                                            // Move downstream ranges cleanly forward to adjust for character offset shifting
                                            val index = localRanges.indexOfFirst { it.id == range.id }
                                            if (index != -1) {
                                                val shifted = range.copy(start = range.start + lengthDifference, end = range.end + lengthDifference)
                                                localRanges[index] = shifted
                                                viewModel.updateRange(range.id, shifted.start, shifted.end)
                                            }
                                        }
                                    }
                                }

                                // D. CHECK ACTIVE BUTTONS: IF AN ACTIVE STYLE WAS NOT EXPANDED ABOVE, INJECT A NEW LAYER
                                val activeStyles = mutableListOf<String>()
                                if (boldLetters) activeStyles.add("bold")
                                if (italicLetters) activeStyles.add("italic")
                                if (underlineLetters) activeStyles.add("underline")
                                if (bigHeader) activeStyles.add("bigHeader")
                                if (biggerHeader) activeStyles.add("biggerHeader")

                                activeStyles.forEach { style ->
                                    val alreadyExpanded = localRanges.any { it.type == style && expandedRangeIds.contains(it.id) }

                                    if (!alreadyExpanded) {
                                        // If it's a bullet addition, the new text style boundary should start AFTER the bullet prefix
                                        val styleStart = if (isBulletAddition) typedIndex + 7 else typedIndex
                                        val styleEnd = if (isBulletAddition) typedIndex + 7 else typedIndex + lengthDifference

                                        // Only create a new range asset if there is typed text to cover (handles initial enter press safely)
                                        if (styleEnd > styleStart || !isBulletAddition) {
                                            val newRange = HomeViewModel.TransformationRanges(id = 0, listId, style, styleStart, styleEnd)
                                            localRanges.add(newRange)

                                            viewModel.addTransformationRange(listId, style, styleStart, styleEnd) { realId ->
                                                val index = localRanges.indexOfFirst { it.id == 0 && it.start == styleStart && it.type == style }
                                                if (index != -1) {
                                                    localRanges[index] = localRanges[index].copy(id = realId)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else if (lengthDifference < 0) {
                                // Handle text deletions cleanly (Kept exactly identical to your fully working backspace logic)
                                val deletedCount = -lengthDifference
                                val deletionIndex = cursorPos

                                val envelopingRanges = localRanges.filter { range ->
                                    deletionIndex >= range.start && deletionIndex < range.end
                                }

                                envelopingRanges.forEach { affectedRange ->
                                    val index = localRanges.indexOf(affectedRange)
                                    if (index != -1) {
                                        val updatedRange = affectedRange.copy(end = affectedRange.end - deletedCount)
                                        if (updatedRange.end <= updatedRange.start) {
                                            localRanges.removeAt(index)
                                            viewModel.deleteTransformationRange(affectedRange.id)
                                        } else {
                                            localRanges[index] = updatedRange
                                            viewModel.updateRange(affectedRange.id, updatedRange.start, updatedRange.end)
                                        }
                                    }
                                }

                                val remainingRanges = localRanges.toList()
                                remainingRanges.forEach { range ->
                                    if (envelopingRanges.contains(range)) return@forEach

                                    if (range.start > deletionIndex && range.id != 0) {
                                        val index = localRanges.indexOf(range)
                                        if (index != -1) {
                                            val shiftedRange = range.copy(
                                                start = maxOf(0, range.start - deletedCount),
                                                end = maxOf(0, range.end - deletedCount)
                                            )

                                            if (shiftedRange.end <= shiftedRange.start) {
                                                localRanges.removeAt(index)
                                                viewModel.deleteTransformationRange(range.id)
                                            } else {
                                                localRanges[index] = shiftedRange
                                                viewModel.updateRange(range.id, shiftedRange.start, shiftedRange.end)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. APPLY UPDATE STATE WITH EXPLICIT CURSOR BOUNDS
                            listText = newText.copy(
                                text = currentText,
                                selection = androidx.compose.ui.text.TextRange(selectionStart, selectionEnd)
                            )

                            viewModel.updateContent(listId, currentText)
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Default
                        ),
                        keyboardActions = KeyboardActions(

                        ),
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            color = mainTextColor,
                            textIndent = if (bulletList) {
                                // firstLine = 0 means the line with the bullet stays left
                                // restLine = 28.sp pushes wrapped lines right to match your "    • " width
                                TextIndent(firstLine = 0.sp, restLine = 28.sp)
                            } else {
                                TextIndent.None
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        cursorBrush = SolidColor(mainTextColor),
                        visualTransformation = TextVisualTransformation(localRanges)
                    )
                }
            }

            // DIALOG BOXES

            // prompt premium for image inserts
            if (promptPremiumPhotoInsert) {
                Dialog(
                    onDismissRequest = { promptPremiumPhotoInsert = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Get Premium to insert pictures!",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Enjoy extra features with Premium.",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { viewModel.launchBillingFlow(activity) },
                                colors = ButtonColors(
                                    contentColor = Color.Black,
                                    containerColor = Color.Cyan,
                                    disabledContentColor = Color.Black,
                                    disabledContainerColor = Color.Cyan
                                )
                            ) {
                                Text(
                                    text = "Get Premium!",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(7.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // prompt premium for PDF download
            if (confirmPdfDownload) {
                Dialog(
                    onDismissRequest = { confirmPdfDownload = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Download '$listName' as PDF?",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "File will be put into your device's downloads folder.",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { confirmPdfDownload = false },
                                    colors = ButtonColors(
                                        contentColor = Color.Black,
                                        containerColor = Color.LightGray,
                                        disabledContentColor = Color.Black,
                                        disabledContainerColor = Color.LightGray
                                    ),
                                    modifier = Modifier.size(width = 137.dp, height = 50.dp)
                                ) {
                                    Text(
                                        text = "Cancel",
                                        fontSize = 17.sp,
                                        modifier = Modifier
                                            .padding(4.dp),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Button(
                                    onClick = {
                                        // Collect everything needed into a clean data container payload package
                                        val exportPayload = HomeViewModel.PdfExportData(
                                            listName = listName,
                                            content = listText.text,
                                            ranges = localRanges, // Your state list holding active formatting style indexes
                                            imagePaths = imagePaths // Your string state list holding the raw photo locations
                                        )

                                        viewModel.exportRichPdf(context = context, data = exportPayload)

                                        confirmPdfDownload = false
                                    },
                                    colors = ButtonColors(
                                        contentColor = Color.Black,
                                        containerColor = Color.Cyan,
                                        disabledContentColor = Color.Black,
                                        disabledContainerColor = Color.Cyan
                                    ),
                                    modifier = Modifier.size(width = 137.dp, height = 50.dp)
                                ) {
                                    Text(
                                        text = "Download",
                                        fontSize = 17.sp,
                                        modifier = Modifier.padding(4.dp),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // prompt premium for PDF download
            if (promptPremiumPDF) {
                Dialog(
                    onDismissRequest = { promptPremiumPDF = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Get Premium to download your list as a PDF!",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Enjoy extra features with Premium.",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    viewModel.launchBillingFlow(activity)
                                    promptPremiumPDF = false
                                },
                                colors = ButtonColors(
                                    contentColor = Color.Black,
                                    containerColor = Color.Cyan,
                                    disabledContentColor = Color.Black,
                                    disabledContainerColor = Color.Cyan
                                )
                            ) {
                                Text(
                                    text = "Get Premium!",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(7.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // expanded image
            if (showImagePreview) {
                Dialog(
                    onDismissRequest = {
                        showImagePreview = false
                        tempImageBitmap = ImageBitmap(1, 1)
                        tempImageId = 0
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    modifier = Modifier
                                        .size(55.dp, 55.dp)
                                        .padding(5.dp),
                                    onClick = {
                                        showConfirmImageDeleteDialog = true
                                    },
                                    colors = IconButtonColors(
                                        containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Image",
                                        modifier = Modifier.size(23.dp),
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    modifier = Modifier
                                        .size(55.dp, 55.dp)
                                        .padding(5.dp),
                                    onClick = {
                                        showImagePreview = false
                                        tempImageBitmap = ImageBitmap(1, 1)
                                        tempImageId = 0
                                    },
                                    colors = IconButtonColors(
                                        containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TransitEnterexit,
                                        contentDescription = "Exit",
                                        modifier = Modifier.size(23.dp),
                                        tint = Color.White
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier.padding(innerPadding)
                                ) {
                                    Image(
                                        bitmap = tempImageBitmap,
                                        contentDescription = "Note Image",
                                        modifier = Modifier
                                            .sizeIn(
                                                maxHeight = screenHeight - 125.dp,
                                                maxWidth = screenWidth - 125.dp,
                                                minHeight = 0.dp,
                                                minWidth = 0.dp
                                            )
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // confirm delete image
            if (showConfirmImageDeleteDialog) {
                Dialog(
                    onDismissRequest = { showConfirmImageDeleteDialog = false }
                ) {
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier
                            .size(275.dp, 200.dp)
                            .padding(15.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Image",
                                modifier = Modifier
                                    .size(28.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Delete Image?",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 30.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        showConfirmImageDeleteDialog = false
                                    },
                                    colors = ButtonColors(
                                        containerColor = Color.Gray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.Gray
                                    )
                                ) {
                                    Text(
                                        text = "Cancel",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        showImagePreview = false
                                        showConfirmImageDeleteDialog = false
                                        viewModel.deleteImage(tempImageId)
                                    },
                                    colors = ButtonColors(
                                        containerColor = Color.Cyan,
                                        contentColor = Color.Black,
                                        disabledContentColor = Color.Black,
                                        disabledContainerColor = Color.Cyan
                                    )
                                ) {
                                    Text(
                                        text = "Delete",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: HomeViewModel, isPremium: Boolean) {
    val windowInfo = LocalWindowInfo.current
    val screenWidth = windowInfo.containerDpSize.width
    val screenHeight = windowInfo.containerDpSize.height

    val context = LocalContext.current
    val activity = context as Activity

    val settings by viewModel.settings.collectAsState()
    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == settings.widgetDisplayListId }
    val listIds by viewModel.listIds.collectAsStateWithLifecycle()

    val colorDict = mapOf(
        "Black" to 0xFF000000L,
        "White" to 0xFFFFFFFFL,
        "Red" to 0xFFFF0000L,
        "Green" to 0xFF00FF00L,
        "Blue" to 0xFF0000FFL,
        "Yellow" to 0xFFFFFF00L,
        "Gray" to 0xFF808080L,
        "Dark Gray" to 0xFF222222L,
        "Pink" to 0xFFFF69B4L,
        "Royal Purple" to 0xFF7851A9L
    )

    val textColorDict = mapOf(
        "Black" to 0xFF000000L,
        "White" to 0xFFFFFFFFL
    )

    var mainTextColor by remember(settings) {
        // if (settings.theme != "Default") {
        //     mutableLongStateOf(0xFF000000L)
        // } else if (!settings.darkMode){
        //     mutableLongStateOf(0xFF000000L)
        // } else if (settings.darkMode) {
        //     mutableLongStateOf(0xFFFFFFFFL)
        // } else {
        //     mutableLongStateOf(settings.mainTextColor)
        // }

        mutableLongStateOf(settings.mainTextColor)
    }

    var backgroundColor by remember(settings) {
        // if (settings.theme != "Default") {
        //     mutableLongStateOf(settings.backgroundColor)
        // } else if (!settings.darkMode) {
        //     mutableLongStateOf(0xFFFFFFFFL)
        // } else if (settings.darkMode) {
        //     mutableLongStateOf(0xFF111111L)
        // } else {
        //     mutableLongStateOf(settings.backgroundColor)
        // }

        mutableLongStateOf(settings.backgroundColor)
    }

    var barColorChoice by remember(settings) {
        mutableLongStateOf(settings.barColor)
    }

    var darkModeSwitch by remember(settings) {
        mutableStateOf(settings.darkMode)
    }

    var barTextColorChoice by remember(settings) {
        mutableLongStateOf(settings.barTextColor)
    }

    var chosenWidgetListId by remember(settings) {
        mutableIntStateOf(settings.widgetDisplayListId)
    }

    var chosenWidgetListName by remember(settings) {
        mutableStateOf(groceryListObj?.name?: "None")
    }

    var barColorChoiceString by remember(settings) {
        // mutableStateOf(colorDict.entries.find { pair -> pair.value == settings.barColor }?.key?: "Yellow")
        mutableStateOf(settings.barColorString)
    }

    var barTextColorChoiceString by remember(settings) {
        // mutableStateOf(textColorDict.entries.find { pair -> pair.value == settings.barTextColor }?.key?: "Black")
        mutableStateOf(settings.barTextColorString)
    }

    var themeChoiceString by remember(settings) {
        mutableStateOf(settings.theme)
    }

    var isChoosingBarColor by remember {
        mutableStateOf(false)
    }

    var isChoosingWidgetList by remember {
        mutableStateOf(false)
    }

    var isChoosingBarTextColor by remember {
        mutableStateOf(false)
    }

    var isChoosingTheme by remember {
        mutableStateOf(false)
    }

    var promptPremium by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // set up bottom bar
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(backgroundColor)),
            horizontalAlignment = Alignment.CenterHorizontally
        )  {
            // top bar name and buttons
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color(barColorChoice)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "  Settings",
                    textAlign = TextAlign.Center,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(barTextColorChoice)
                )

                // back button
                Button(
                    onClick = {
                        navController.navigate("home")
                        viewModel.updateSetting(
                            darkMode = darkModeSwitch,
                            barColor = barColorChoice,
                            widgetDisplayListId = chosenWidgetListId,
                            barTextColor = barTextColorChoice,
                            theme = themeChoiceString,
                            backgroundColor = backgroundColor,
                            mainTextColor = mainTextColor,
                            barColorString = barColorChoiceString,
                            barTextColorString = barTextColorChoiceString
                        )
                    },
                    colors = ButtonColors(
                        containerColor = Color(barColorChoice),
                        contentColor = Color(barTextColorChoice),
                        disabledContentColor = Color(barTextColorChoice),
                        disabledContainerColor = Color(barColorChoice)
                    )
                ) {
                    Text(
                        text = "Apply",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(thickness = 2.dp, color = Color(mainTextColor))

            // SETTINGS

            // dark mode switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dark Mode", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = Color(mainTextColor))
                Switch(
                    checked = darkModeSwitch,
                    onCheckedChange = {
                        darkModeSwitch = !darkModeSwitch
                        if (darkModeSwitch) {
                            backgroundColor = 0xFF222222L
                            mainTextColor = 0xFFFFFFFFL
                        } else {
                            backgroundColor = 0xFFFFFFFFL
                            mainTextColor = 0xFF000000L
                        }

                        if (settings.barTextColorString == "Theme" || settings.barColorString == "Theme") {
                            barColorChoice = 0xFFFFFF00L
                            barTextColorChoice = 0xFF000000L
                            themeChoiceString = "Default"
                            barTextColorChoiceString = "Black"
                            barColorChoiceString = "Yellow"
                        }
                    },
                    modifier = Modifier.padding(10.dp)
                )
            }

            HorizontalDivider(thickness = 2.dp, color = Color(mainTextColor))

            // bar color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Bar Color", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = Color(mainTextColor))
                Card(
                    border = BorderStroke(2.dp, color = Color.Gray),
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .padding(10.dp)
                        .clickable(
                            onClick = {
                                isChoosingBarColor = true
                            }
                        )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = barColorChoiceString)
                    }
                    DropdownMenu(
                        expanded = isChoosingBarColor,
                        onDismissRequest = { isChoosingBarColor = false },
                        modifier = Modifier.heightIn(max = 180.dp)
                    ) {
                        for (color in colorDict.keys) {
                            DropdownMenuItem(
                                text = { Text(text = color) },
                                onClick = {
                                    barColorChoice = colorDict[color]?:0xFFFFFF00L
                                    barColorChoiceString = color

                                    when(color) {
                                        "Black", "Dark Gray" -> {
                                            barTextColorChoice = 0xFFFFFFFFL
                                            barTextColorChoiceString = "White"
                                        }

                                        "White" -> {
                                            barTextColorChoice = 0xFF000000L
                                            barTextColorChoiceString = "Black"
                                        }
                                    }

                                    if (themeChoiceString != "Default") {
                                        barTextColorChoiceString = "Black"
                                        barTextColorChoice = 0xFF000000L
                                        themeChoiceString = "Default"
                                        backgroundColor = if (!darkModeSwitch) 0xFFFFFFFFL else 0xFF000000L
                                    }

                                    isChoosingBarColor = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = Color(mainTextColor))

            // bar text color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Bar Text Color", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = Color(mainTextColor))
                Card(
                    border = BorderStroke(2.dp, color = Color.Gray),
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .padding(10.dp)
                        .clickable(
                            onClick = {
                                isChoosingBarTextColor = true
                            }
                        )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = barTextColorChoiceString)
                    }
                    DropdownMenu(
                        expanded = isChoosingBarTextColor,
                        onDismissRequest = { isChoosingBarTextColor = false },
                        modifier = Modifier.heightIn(max = 180.dp)
                    ) {
                        for (color in textColorDict.keys) {
                            DropdownMenuItem(
                                text = { Text(text = color) },
                                onClick = {
                                    barTextColorChoice = textColorDict[color]?:0xFF000000L
                                    barTextColorChoiceString = color

                                    when(color) {
                                        "Black" -> {
                                            barColorChoice = 0xFFFFFFFFL
                                            barColorChoiceString = "White"
                                        }

                                        "White" -> {
                                            barColorChoice = 0xFF000000L
                                            barColorChoiceString = "Black"
                                        }
                                    }

                                    if (themeChoiceString != "Default") {
                                        barColorChoiceString = "Yellow"
                                        barColorChoice = 0xFFFFFF00L
                                        themeChoiceString = "Default"
                                        backgroundColor = if (!darkModeSwitch) 0xFFFFFFFFL else 0xFF000000L
                                    }

                                    isChoosingBarTextColor = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = Color(mainTextColor))

            // choose list to display on widget
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Widget Display List", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = Color(mainTextColor))
                Card(
                    border = BorderStroke(2.dp, color = Color.Gray),
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .padding(10.dp)
                        .clickable(
                            onClick = {
                                isChoosingWidgetList = true
                            }
                        )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = chosenWidgetListName)
                    }
                    DropdownMenu(
                        expanded = isChoosingWidgetList,
                        onDismissRequest = { isChoosingWidgetList = false },
                        modifier = Modifier.heightIn(max = 180.dp)
                    ) {
                        for (listId in listIds) {
                            DropdownMenuItem(
                                text = { Text(text = viewModel.lists.collectAsState().value.find { it.id == listId }?.name?: "") },
                                onClick = {
                                    chosenWidgetListId = listId
                                    chosenWidgetListName = viewModel.lists.value.find { it.id == listId }?.name?: ""
                                    isChoosingWidgetList = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = Color(mainTextColor))

            // choose app theme
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Theme", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = Color(mainTextColor))
                Card(
                    border = BorderStroke(2.dp, color = Color.Gray),
                    modifier = Modifier
                        .size(200.dp, 60.dp)
                        .padding(10.dp)
                        .clickable(
                            onClick = {
                                if (isPremium) isChoosingTheme = true else promptPremium = true
                            }
                        )
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = themeChoiceString)
                    }
                    DropdownMenu(
                        expanded = isChoosingTheme,
                        onDismissRequest = { isChoosingTheme = false },
                        modifier = Modifier.heightIn(max = 180.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Default") },
                            onClick = {
                                themeChoiceString = "Default"
                                backgroundColor = 0xFFFFFFFFL
                                barColorChoice = 0xFFFFFF00L
                                mainTextColor = 0xFF000000L
                                barTextColorChoice = 0xFF000000L

                                barColorChoiceString = "Yellow"
                                barTextColorChoiceString = "Black"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Midnight OLED") },
                            onClick = {
                                themeChoiceString = "Midnight OLED"
                                backgroundColor = 0xFF000000L
                                barColorChoice = 0xFF121212L
                                mainTextColor = 0xFFFFFFFFL
                                barTextColorChoice = 0xFFBB86FCL

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Rose Gold Luxury") },
                            onClick = {
                                themeChoiceString = "Rose Gold Luxury"
                                backgroundColor = 0xFFFFF5F5L
                                barColorChoice = 0xFFE0A9A5L
                                mainTextColor = 0xFF4A3535L
                                barTextColorChoice = 0xFFFFFFFFL

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Matcha Latte") },
                            onClick = {
                                themeChoiceString = "Matcha Latte"
                                backgroundColor = 0xFFF4F7F4L
                                barColorChoice = 0xFFA3B899L
                                mainTextColor = 0xFF2D3B26L
                                barTextColorChoice = 0xFFFFFFFFL

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Cyberpunk") },
                            onClick = {
                                themeChoiceString = "Cyberpunk"
                                backgroundColor = 0xFF1A1A24L
                                barColorChoice = 0xFF252538L
                                mainTextColor = 0xFF00FFFFL
                                barTextColorChoice = 0xFFFF007FL

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Nordic Drift") },
                            onClick = {
                                themeChoiceString = "Nordic Drift"
                                backgroundColor = 0xFFF0F4F8L
                                barColorChoice = 0xFF334E68L
                                mainTextColor = 0xFF102A43L
                                barTextColorChoice = 0xFF9FB3C8L

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "Minimal Charcoal") },
                            onClick = {
                                themeChoiceString = "Minimal Charcoal"
                                backgroundColor = 0xFFF8F9FA
                                barColorChoice = 0xFF212529
                                mainTextColor = 0xFF343A40
                                barTextColorChoice = 0xFFF8F9FA

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "Creamy Green") },
                            onClick = {
                                themeChoiceString = "Creamy Green"
                                backgroundColor = 0xFFF4F6F0
                                barColorChoice = 0xFF4A5D4E
                                mainTextColor = 0xFF2A362D
                                barTextColorChoice = 0xFFF4F6F0

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "Cool Steel") },
                            onClick = {
                                themeChoiceString = "Cool Steel"
                                backgroundColor = 0xFFF1F5F9
                                barColorChoice = 0xFF1E293B
                                mainTextColor = 0xFF0F172A
                                barTextColorChoice = 0xFF38BDF8

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "Warm Terracotta") },
                            onClick = {
                                themeChoiceString = "Warm Terracotta"
                                backgroundColor = 0xFFFEFAE0
                                barColorChoice = 0xFF8D4B38
                                mainTextColor = 0xFF3D2018
                                barTextColorChoice = 0xFFFEFAE0

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "Cyber Grape") },
                            onClick = {
                                themeChoiceString = "Cyber Grape"
                                backgroundColor = 0xFF121212
                                barColorChoice = 0xFF1F1F1F
                                mainTextColor = 0xFFE1E1E1
                                barTextColorChoice = 0xFFBB86FC

                                barColorChoiceString = "Theme"
                                barTextColorChoiceString = "Theme"

                                darkModeSwitch = false
                                isChoosingTheme = false
                            }
                        )
                    }
                }
            }

            if (!isPremium) {
                val strokeGradient = Brush.linearGradient(
                    colors = listOf(
                        Color.Black,
                        Color.Gray
                    )
                )

                val backgroundGradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF87CEEBL),
                        Color(0xFF00FFFFL)
                    )
                )

                Surface(
                    border = BorderStroke(2.dp, strokeGradient),
                    modifier = Modifier
                        .size(width = screenWidth - 15.dp, height = 125.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = backgroundGradient)
                    ) {
                        Text(
                            text = "Take notes like a pro.",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        Text(
                            text = "Purchase the full app!",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )

                        Button(
                            onClick = {
                                viewModel.launchBillingFlow(activity)
                            }
                        ) {
                            Text(
                                text = "Get",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // DIALOG BOXES

            // prompt premium for themes
            if (promptPremium) {
                Dialog(
                    onDismissRequest = { promptPremium = false }
                ) {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.size(350.dp, 200.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Get Premium to use themes!",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Enjoy extra features with Premium.",
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    viewModel.launchBillingFlow(activity)
                                    promptPremium = false
                                },
                                colors = ButtonColors(
                                    contentColor = Color.Black,
                                    containerColor = Color.Cyan,
                                    disabledContentColor = Color.Black,
                                    disabledContainerColor = Color.Cyan
                                )
                            ) {
                                Text(
                                    text = "Get Premium!",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(7.dp),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleistTheme {
    }
}