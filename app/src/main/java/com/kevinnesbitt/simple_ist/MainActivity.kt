package com.kevinnesbitt.simple_ist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
                            "generic" -> GenericListScreen(listId = listId, navController = navController, viewModel)
                        }
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
        mutableStateOf(Color(settings.barTextColor))
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
                    isChoosingListType = true
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

            // add lists
            if (lsts.isNotEmpty()) {
                LazyColumn(modifier = Modifier.imePadding()) {
                    items(lsts) { groceryList ->
                        Surface(
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                            tonalElevation = 3.dp,
                            modifier = Modifier
                                .padding(3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = Color.White)
                                    .combinedClickable(
                                        onClick = { navController.navigate("list/${groceryList.id}/${groceryList.type}") },
                                        onLongClick = { expandedListId = groceryList.id }
                                    )
                                    .background(color = Color(backgroundColor))
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = groceryList.name,
                                        fontSize = 25.sp,
                                        color = mainTextColor,
                                        modifier = Modifier
                                            .padding(10.dp)
                                    )

                                    Text(
                                        text = if (groceryList.type == "grocery") "Grocery List" else "Generic List",
                                        fontSize = 18.sp,
                                        color = Color.Gray,
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
                                                viewModel.addList(name = lstName, type = "grocery", onComplete = { newId ->
                                                    lstName = ""
                                                    isAddingGroceryLst = false
                                                    navController.navigate("list/${newId}/grocery")
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
                                                viewModel.addList(name = lstName, type = "grocery",onComplete = { newId ->
                                                    lstName = ""
                                                    isAddingGroceryLst = false
                                                    navController.navigate("list/${newId}/grocery")
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
                                                viewModel.addList(name = lstName, type = "generic", onComplete = { newId ->
                                                    lstName = ""
                                                    isAddingGenericLst = false
                                                    navController.navigate("list/${newId}/generic")
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
                                                viewModel.addList(name = lstName, type = "generic", onComplete = { newId ->
                                                    lstName = ""
                                                    isAddingGenericLst = false
                                                    navController.navigate("list/${newId}/generic")
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
fun GroceryListScreen(listId: Int, navController: NavController, viewModel: HomeViewModel) {
    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == listId }
    val itemLst = groceryListObj?.items?: emptyList()
    val listName = groceryListObj?.name?: ""

    val settings by viewModel.settings.collectAsState()

    var barTextColor by remember(settings) {
        mutableStateOf(Color(settings.barTextColor))
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
                        Text(text = "+", fontSize = 28.sp)
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
                        Text(text = "Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
fun GenericListScreen(listId: Int, navController: NavController, viewModel: HomeViewModel) {
    val groceryListObj = viewModel.lists.collectAsState().value.find { it.id == listId }
    val listName = groceryListObj?.name?: ""

    val contentListObj = viewModel.contentList.collectAsState().value.find { it.listId == listId }
    val content = contentListObj?.content?: ""
    val ranges = contentListObj?.transformationRanges?: emptyList()

    val settings by viewModel.settings.collectAsState()

    var barTextColor by remember(settings) {
        mutableStateOf(Color(settings.barTextColor))
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

    var isChangingListName by remember {
        mutableStateOf(false)
    }

    var newListName by remember {
        mutableStateOf("")
    }

    var showSideBar by remember {
        mutableStateOf(false)
    }

    var listText by remember {
        mutableStateOf(TextFieldValue(text = ""))
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

    LaunchedEffect(content, ranges) {
        // Only overwrite the text field if it's currently empty (e.g., initial cold launch)
        if (listText.text.isEmpty() && content.isNotEmpty()) {
            listText = TextFieldValue(text = content, selection = TextRange(content.length))
        }

        // Sync down your formatting style rules cleanly
        if (localRanges.isEmpty() && ranges.isNotEmpty()) {
            localRanges.addAll(ranges)
        }
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
                    item {
                        BasicTextField(
                            value = listText,
                            onValueChange = { newText ->
                                val currentText = newText.text
                                val oldText = listText.text
                                val cursorPos = newText.selection.start

                                var decorType = ""
                                if (boldLetters) decorType += "bold"
                                if (italicLetters) decorType += "italic"
                                if (underlineLetters) decorType += "underline"
                                if (bigHeader) decorType += "bigHeader"
                                if (biggerHeader) decorType += "biggerHeader"
                                if (currentText.length > oldText.length && (boldLetters || italicLetters || underlineLetters || bigHeader || biggerHeader)) {
                                    val typedIndex = cursorPos - 1
                                    val lastRange = localRanges.lastOrNull()

                                    if (lastRange != null && lastRange.end == typedIndex && lastRange.type == decorType) {
                                        // extend existing range
                                        val updatedRange = lastRange.copy(end = cursorPos)
                                        localRanges[localRanges.lastIndex] = updatedRange
                                        viewModel.updateRange(lastRange.id, lastRange.start, cursorPos)
                                    } else {
                                        // create new range
                                        android.util.Log.d("Decor Type", "type = $decorType")
                                        val newRange = HomeViewModel.TransformationRanges(id = 0, listId, decorType, typedIndex, cursorPos)
                                        localRanges.add(newRange)

                                        viewModel.addTransformationRange(listId, decorType, typedIndex, cursorPos) { realId ->
                                            // update the localRange with the real database id
                                            val index = localRanges.indexOfFirst { it.id == 0 && it.start == typedIndex }
                                            if (index != -1) {
                                                localRanges[index] = localRanges[index].copy(id = realId)
                                            }
                                        }
                                    }
                                } else if (currentText.length < oldText.length) {
                                    val deletedIndex = cursorPos

                                    // 2. Find the index of the range that contained the deleted letter
                                    val targetRangeIndex = localRanges.indexOfFirst { range ->
                                        deletedIndex >= range.start && deletedIndex < range.end
                                    }

                                    if (targetRangeIndex != -1) {
                                        val affectedRange = localRanges[targetRangeIndex]

                                        // If the range only had 1 character left, remove it entirely
                                        if (affectedRange.start == affectedRange.end - 1) {
                                            localRanges.removeAt(targetRangeIndex)
                                            viewModel.deleteTransformationRange(affectedRange.id) // Call your DAO delete query
                                        } else {
                                            // Shrink the affected range by 1
                                            val updatedRange = affectedRange.copy(end = affectedRange.end - 1)
                                            localRanges[targetRangeIndex] = updatedRange
                                            viewModel.updateRange(affectedRange.id, updatedRange.start, updatedRange.end)
                                        }
                                    }

                                    // 3. CRITICAL STEP: Shift ALL formatting ranges that come after the deletion point backward by 1
                                    for (i in localRanges.indices) {
                                        val range = localRanges[i]
                                        if (range.start > deletedIndex) {
                                            val shiftedRange = range.copy(start = range.start - 1, end = range.end - 1)
                                            localRanges[i] = shiftedRange
                                            // Update the database to reflect the shifted positions
                                            viewModel.updateRange(range.id, shiftedRange.start, shiftedRange.end)
                                        }
                                    }
                                }

                                val updatedText = when {
                                    currentText.endsWith("\n") && bulletList-> {
                                        "$currentText    • "
                                    }

                                    oldText.endsWith("\n    • ") && currentText.length < oldText.length -> {
                                        currentText.dropLast(5)
                                    }

                                    currentText.isEmpty() -> {
                                        ""
                                    }

                                    else -> {
                                        currentText
                                    }
                                }

                                listText = TextFieldValue(text = updatedText, selection = TextRange(updatedText.length))

                                viewModel.updateContent(listId, updatedText)
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
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController, viewModel: HomeViewModel) {
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
        "Dark Gray" to 0xFF111111L,
        "Pink" to 0xFFFF69B4L,
        "Royal Purple" to 0xFF7851A9L
    )

    val textColorDict = mapOf(
        "Black" to 0xFF000000L,
        "White" to 0xFFFFFFFFL
    )

    var barTextColor by remember(settings) {
        mutableStateOf(Color(settings.barTextColor))
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
        mutableStateOf(colorDict.entries.find { pair -> pair.value == settings.barColor }?.key?: "Yellow")
    }

    var barTextColorChoiceString by remember(settings) {
        mutableStateOf(textColorDict.entries.find { pair -> pair.value == settings.barTextColor }?.key?: "Black")
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
                        viewModel.updateSetting(darkMode = darkModeSwitch, barColor = barColorChoice, widgetDisplayListId = chosenWidgetListId, barTextColor = barTextColorChoice)
                    },
                    colors = ButtonColors(
                        containerColor = Color(settings.barColor),
                        contentColor = barTextColor,
                        disabledContentColor = barTextColor,
                        disabledContainerColor = Color(settings.barColor)
                    )
                ) {
                    Text(text = "Apply", fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
                        onDismissRequest = { isChoosingBarColor = false },
                        modifier = Modifier.heightIn(max = 180.dp)
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

            // bar text color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Bar Text Color", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = mainTextColor)
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
                        onDismissRequest = { isChoosingBarTextColor = false }
                    ) {
                        for (color in textColorDict.keys) {
                            DropdownMenuItem(
                                text = { Text(text = color) },
                                onClick = {
                                    barTextColorChoice = textColorDict[color]?:0xFF000000L
                                    barTextColorChoiceString = color
                                    isChoosingBarTextColor = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = mainTextColor)

            // choose list to display on widget
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(backgroundColor)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Widget Display List", modifier = Modifier.padding(21.dp), fontSize = 18.sp, color = mainTextColor)
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
                        onDismissRequest = { isChoosingWidgetList = false }
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