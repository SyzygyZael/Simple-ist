package com.kevinnesbitt.simple_ist

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.kevinnesbitt.simple_ist.ui.theme.SimpleistTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kevinnesbitt.simple_ist.ui.TextVisualTransformation
import kotlin.collections.emptyList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.YELLOW,
                darkScrim = android.graphics.Color.YELLOW
            )
        )
        setContent {
            SimpleistTheme {
                val navController = rememberNavController()
                val viewModel: HomeViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )

                // val isPremium by viewModel.isPremiumUser.collectAsState()
                val isPremium = true

                val settings by viewModel.settings.collectAsState()

                LaunchedEffect(settings.barColor) {
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

    fun addPhotoToContentText(currentContent: String, savedPhotoPath: String): String {
        // Using a clear delimiter like brackets makes parsing 100% reliable
        val photoToken = "\n[[image:$savedPhotoPath]]\n"
        return currentContent + photoToken
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    val activity = context as Activity

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
        mutableStateOf("")
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
        bottomBar = {

            // set up bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    modifier = Modifier.imePadding()
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
                                            tempGroceryListId = groceryList.id
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
                                    if (text.length <= 20) {
                                        newName = text
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newName.isNotBlank()) {
                                            viewModel.updateListName(
                                                listId = tempGroceryListId,
                                                newName = newName
                                            )
                                            newName = ""
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
                                        if (newName.isNotBlank()) {
                                            viewModel.updateListName(
                                                listId = tempGroceryListId,
                                                newName = newName
                                            )
                                            newName = ""
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
        mutableStateOf("  $listName")
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
                            if (text.length <= 20) {
                                newListName = text
                            } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newListName.isNotBlank()) {
                                    viewModel.updateListName(listId = listId, newName = newListName)
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
                                viewModel.updateListName(listId = listId, newName = "")
                                newListName = ""
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
        mutableStateOf("")
    }

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

    var expandedImageId by remember {
        mutableStateOf<Int?>(null)
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

    val density = LocalDensity.current

    val windowInfo = LocalWindowInfo.current
    val screenWidth = windowInfo.containerDpSize.width
    val screenHeight = windowInfo.containerDpSize.height

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor))
            ) { }
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
                            if (text.length <= 20) {
                                newListName = text
                            } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newListName.isNotBlank()) {
                                    viewModel.updateListName(listId = listId, newName = newListName)
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
                                viewModel.updateListName(listId = listId, newName = "")
                                newListName = ""
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

            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.imePadding()) {
                    if (loadedImages.isNotEmpty()) {
                        item(key = "note_images_header_${listId}") {
                            FlowRow(
                                modifier = Modifier
                                    .sizeIn(
                                        minWidth = 0.dp,
                                        minHeight = 0.dp,
                                        maxWidth = screenWidth - 60.dp,
                                        maxHeight = screenHeight * 3
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                loadedImages.forEach { imageData ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .combinedClickable(
                                                onClick = { },
                                                onLongClick = { expandedImageId = imageData.id }
                                            ),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Image(
                                            bitmap = imageData.bitmap,
                                            contentDescription = "Note Image",
                                            modifier = Modifier
                                                .size(120.dp)
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
                                    // Push the selection cursor past the generated bullet point
                                    selectionStart += 6
                                    selectionEnd += 6
                                } else if (isBulletDeletion) {
                                    currentText = currentText.dropLast(5)
                                    selectionStart = maxOf(0, selectionStart - 5)
                                    selectionEnd = maxOf(0, selectionEnd - 5)
                                }

                                // 2. NOW EXECUTE RANGE ADJUSTMENTS BASED ON THE TRUE FINAL TEXT LENGTH
                                val lengthDifference = currentText.length - oldText.length
                                val cursorPos = selectionStart

                                var decorType = ""
                                if (boldLetters) decorType += "bold"
                                if (italicLetters) decorType += "italic"
                                if (underlineLetters) decorType += "underline"
                                if (bigHeader) decorType += "bigHeader"
                                if (biggerHeader) decorType += "biggerHeader"

                                if (lengthDifference > 0) {
                                    // Find the index where the typing actually started
                                    val typedIndex = maxOf(0, cursorPos - lengthDifference)

                                    // 1. Look for an existing formatting range that the cursor is currently inside of
                                    val targetRangeIndex = localRanges.indexOfFirst { range ->
                                        typedIndex >= range.start && typedIndex <= range.end
                                    }

                                    if (targetRangeIndex != -1 && localRanges[targetRangeIndex].type == decorType) {
                                        // SCENARIO A: Expand matching style by the actual amount of characters inserted
                                        val affectedRange = localRanges[targetRangeIndex]
                                        val updatedRange = affectedRange.copy(end = affectedRange.end + lengthDifference)
                                        localRanges[targetRangeIndex] = updatedRange
                                        viewModel.updateRange(affectedRange.id, updatedRange.start, updatedRange.end)
                                    } else if (decorType.isNotEmpty()) {
                                        // SCENARIO B: Create new range spanning the added text length
                                        val newRange = HomeViewModel.TransformationRanges(id = 0, listId, decorType, typedIndex, typedIndex + lengthDifference)
                                        localRanges.add(newRange)

                                        viewModel.addTransformationRange(listId, decorType, typedIndex, typedIndex + lengthDifference) { realId ->
                                            val index = localRanges.indexOfFirst { it.id == 0 && it.start == typedIndex }
                                            if (index != -1) {
                                                localRanges[index] = localRanges[index].copy(id = realId)
                                            }
                                        }
                                    }

                                    // Shift ALL formatting ranges downstream by the real length difference
                                    for (i in localRanges.indices) {
                                        val range = localRanges[i]
                                        if (range.start >= typedIndex && i != targetRangeIndex && range.id != 0) {
                                            val shiftedRange = range.copy(start = range.start + lengthDifference, end = range.end + lengthDifference)
                                            localRanges[i] = shiftedRange
                                            viewModel.updateRange(range.id, shiftedRange.start, shiftedRange.end)
                                        }
                                    }

                                } else if (lengthDifference < 0) {
                                    val absoluteDiff = kotlin.math.abs(lengthDifference)
                                    val deletedIndex = cursorPos

                                    // Find the range containing the deleted chunk
                                    val targetRangeIndex = localRanges.indexOfFirst { range ->
                                        deletedIndex >= range.start && deletedIndex < range.end
                                    }

                                    if (targetRangeIndex != -1) {
                                        val affectedRange = localRanges[targetRangeIndex]

                                        // If the chunk removal leaves the range empty or inverted, kill it
                                        if (affectedRange.start >= affectedRange.end - absoluteDiff) {
                                            localRanges.removeAt(targetRangeIndex)
                                            viewModel.deleteTransformationRange(affectedRange.id)
                                        } else {
                                            // Shrink the boundary down cleanly
                                            val updatedRange = affectedRange.copy(end = affectedRange.end - absoluteDiff)
                                            localRanges[targetRangeIndex] = updatedRange
                                            viewModel.updateRange(affectedRange.id, updatedRange.start, updatedRange.end)
                                        }
                                    }

                                    // Pull downstream formatting ranges backward by the exact size of the deleted chunk
                                    for (i in localRanges.indices) {
                                        val range = localRanges[i]
                                        if (range.start > deletedIndex) {
                                            val shiftedRange = range.copy(start = range.start - absoluteDiff, end = range.end - absoluteDiff)
                                            localRanges[i] = shiftedRange
                                            viewModel.updateRange(range.id, shiftedRange.start, shiftedRange.end)
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
                                color = mainTextColor
                            ),
                            modifier = Modifier
                                .size(screenWidth - 60.dp, screenHeight)
                                .padding(8.dp),
                            cursorBrush = SolidColor(mainTextColor),
                            visualTransformation = TextVisualTransformation(localRanges)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showSideBar,
                    enter = slideInHorizontally { with(density) { 60.dp.roundToPx() } },
                    exit = slideOutHorizontally { with(density) { 60.dp.roundToPx() } }

                ) {
                    Column(
                        modifier = Modifier
                            .size(60.dp, screenHeight)
                            .background(color = Color(backgroundColor)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                bulletList = !bulletList

                                if (bulletList && listText.text.endsWith("\n")) {
                                    listText = TextFieldValue(text = listText.text + "    • ", selection = TextRange(listText.text.length + 6))
                                    viewModel.updateContent(listId, listText.text)
                                } else if (bulletList && listText.text.isEmpty()) {
                                    listText = TextFieldValue(text = "    • ", selection = TextRange(listText.text.length + 6))
                                    viewModel.updateContent(listId, listText.text)
                                } else if (bulletList) {
                                    listText = TextFieldValue(text = listText.text + "\n    • ", selection = TextRange(listText.text.length + 7))
                                    viewModel.updateContent(listId, listText.text)
                                }
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
                                text = "⋮",
                                fontSize = 23.sp
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

                        Button(
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
                            colors = ButtonColors(
                                containerColor = if (!biggerHeader) Color(backgroundColor) else Color.LightGray.copy(0.5f),
                                contentColor = mainTextColor,
                                disabledContentColor = mainTextColor,
                                disabledContainerColor = if (!biggerHeader) Color(backgroundColor) else Color.LightGray.copy(0.5f)
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "\uD83D\uDDBC",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            modifier = Modifier.size(55.dp, 55.dp),
                            onClick = {
                                if (isPremium) {

                                } else {
                                    promptPremiumPDF = true
                                }
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
                                text = "⭳",
                                fontSize = 23.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: HomeViewModel, isPremium: Boolean) {
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
                .background(color = Color(backgroundColor))
        )  {
            // top bar name and buttons
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color(barColorChoice)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                    Text(text = "Apply", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                Text(text = "Settings",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(barTextColorChoice)
                )
                Text(text = "              ")
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleistTheme {
    }
}