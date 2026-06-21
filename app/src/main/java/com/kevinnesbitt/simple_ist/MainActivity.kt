package com.kevinnesbitt.simple_ist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.kevinnesbitt.simple_ist.ui.theme.SimpleistTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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
                        HomeScreen(navController, viewModel)
                    }

                    composable("settings") {
                        SettingsScreen(navController, viewModel)
                    }

                    composable(
                        route = "list/{listId}",
                        arguments = listOf(navArgument("listId") { type = NavType.IntType })
                    ) { backStackEntry ->

                        val listId = backStackEntry.arguments?.getInt("listId") ?: 0

                        ListScreen(listId = listId, navController = navController, viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val lsts by viewModel.lists.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var barTextColor by remember(settings) {
        if (settings.barColor == 0xFF000000L || settings.barColor == 0xFFFF0000L || settings.barColor == 0xFF0000FFL || settings.barColor == 0xFF808080L || settings.barColor == 0xFFFF69B4L || settings.barColor == 0xFF7851A9L) {
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

    var isAddingLst by remember {
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

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        bottomBar = {

            // set up bottom bar
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(settings.barColor)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    navController.navigate("settings")
                },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "⚙", fontSize = 27.sp)
                }

                Button(onClick = {
                    isAddingLst = true
                },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "+", fontSize = 27.sp)
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
            HorizontalDivider(thickness = 2.dp, color = Color.Gray)

            // add lists
            if (lsts.isNotEmpty()) {
                LazyColumn(modifier = Modifier.imePadding()) {
                    items(lsts) { groceryList ->
                        Surface(
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(2.dp, Color.Gray),
                            tonalElevation = 3.dp,
                            modifier = Modifier
                                .padding(3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = Color.White)
                                    .combinedClickable(
                                        onClick = { navController.navigate("list/${groceryList.id}") },
                                        onLongClick = { expandedListId = groceryList.id }
                                    )
                                    .background(color = Color(backgroundColor))
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Absolute.Left,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = groceryList.name,
                                        fontSize = 25.sp,
                                        color = mainTextColor,
                                        modifier = Modifier
                                            .padding(10.dp)
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
            } else if (!isAddingLst) {
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
            if (isAddingLst) {
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
                            Text(text = "Add List", textAlign = TextAlign.Center, fontSize = 21.sp, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)

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
                                                viewModel.addList(name = lstName, onComplete = { newId ->
                                                    lstName = ""
                                                    isAddingLst = false
                                                    navController.navigate("list/${newId}")
                                                })
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
                                        isAddingLst = false
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
                                                viewModel.addList(name = lstName, onComplete = { newId ->
                                                    lstName = ""
                                                    isAddingLst = false
                                                    navController.navigate("list/${newId}")
                                                })
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

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(listId: Int, navController: NavController, viewModel: HomeViewModel) {
    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == listId }
    val itemLst = groceryListObj?.items?: emptyList()
    val listName = groceryListObj?.name?: ""

    val settings by viewModel.settings.collectAsState()

    var barTextColor by remember(settings) {
        if (settings.barColor == 0xFF000000L || settings.barColor == 0xFFFF0000L || settings.barColor == 0xFF0000FFL || settings.barColor == 0xFF808080L || settings.barColor == 0xFFFF69B4L || settings.barColor == 0xFF7851A9L) {
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
                if (!isAddingItem) {
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
                        Text(text = "+", fontSize = 24.sp)
                    }
                } else {
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
                        Text(text = "Done", fontSize = 18.sp)
                    }
                }
            }

            // display list items
            if (itemLst.isNotEmpty()) {
                LazyColumn(modifier = Modifier.imePadding()) {
                    items(itemLst) { groceryItem ->
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
                                        onLongClick = { expandableListId = groceryItem.id }
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
                                        onLongClick = { expandableListId = groceryItem.id }
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
                                text = { Text(text = "Delete", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    viewModel.deleteItem(itemId = groceryItem.id, listId = listId)
                                }
                            )
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
                        Text(text = "Tap '+' to get started!", color = Color.Gray)
                    } else if (randTextChooser == 2) {
                        Text(text = "What are you shopping for today?", color = Color.Gray)
                    } else if (randTextChooser == 3) {
                        Text(text = "So many possibilities...", color = Color.Gray)
                    } else if (randTextChooser == 4) {
                        Text(text = "Quick! Write it down so you don't forget!", color = Color.Gray)
                    } else if (randTextChooser == 5) {
                        Text(text = "A blank space is but a limitless sky...", color = Color.Gray)
                    } else if (randTextChooser == 6) {
                        Text(text = "Hi Saman!", color = Color.Gray)
                    }
                }
            }

            // allow item creation upon pressing '+'
            if (isAddingItem) {
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
                        singleLine = true
                    )
                    // request keyboard
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: HomeViewModel) {
    val settings by viewModel.settings.collectAsState()

    val colorDict = mapOf(
        "Black" to 0xFF000000L,
        "White" to 0xFFFFFFFFL,
        "Red" to 0xFFFF0000L,
        "Green" to 0xFF00FF00L,
        "Blue" to 0xFF0000FFL,
        "Yellow" to 0xFFFFFF00L,
        "Gray" to 0xFF808080L,
        "Pink" to 0xFFFF69B4L,
        "Royal Purple" to 0xFF7851A9L
    )

    var barTextColor by remember(settings) {
        if (settings.barColor == 0xFF000000L || settings.barColor == 0xFFFF0000L || settings.barColor == 0xFF0000FFL || settings.barColor == 0xFF808080L || settings.barColor == 0xFFFF69B4L || settings.barColor ==  0xFF7851A9L ) {
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

    var darkModeSwitch by remember(settings) {
        mutableStateOf(settings.darkMode)
    }

    var barColorChoice by remember(settings) {
        mutableLongStateOf(settings.barColor)
    }

    var barColorChoiceString by remember(settings) {
        mutableStateOf(colorDict.entries.find { pair -> pair.value == settings.barColor }?.key?: "Yellow")
    }

    var isChoosingBarColor by remember {
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
        ) {
            // top bar name and buttons
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
                        viewModel.updateSetting(darkMode = darkModeSwitch, barColor = barColorChoice)
                    },
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "Save", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                Text(text = "Settings",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = barTextColor
                )
                Text(text = "              ")
            }

            HorizontalDivider(thickness = 2.dp, color = mainTextColor)

            // SETTINGS

            // dark mode switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dark Mode", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = mainTextColor)
                Switch(
                    checked = darkModeSwitch,
                    onCheckedChange = {
                         darkModeSwitch = !darkModeSwitch
                    },
                    modifier = Modifier.padding(10.dp)
                )
            }

            HorizontalDivider(thickness = 2.dp, color = mainTextColor)

            // bar color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Bar Color", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = mainTextColor)
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
                        onDismissRequest = { isChoosingBarColor = false }
                    ) {
                        for (color in colorDict.keys) {
                            DropdownMenuItem(
                                text = { Text(text = color) },
                                onClick = {
                                    barColorChoice = colorDict[color]?:0xFFFFFF00L
                                    barColorChoiceString = color
                                    isChoosingBarColor = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = mainTextColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleistTheme {
    }
}