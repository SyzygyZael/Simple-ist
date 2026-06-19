package com.kevinnesbitt.simple_ist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        enableEdgeToEdge()
        setContent {
            SimpleistTheme {

                val navController = rememberNavController()
                val viewModel: HomeViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(navController, viewModel)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val lsts by viewModel.lists.collectAsState()

    var lstName by remember {
        mutableStateOf("")
    }

    var isAddingLst by remember {
        mutableStateOf(false)
    }

    var expandedListId by remember {
        mutableStateOf<Int?>(null)
    }

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        bottomBar = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    isAddingLst = true
                },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    colors = ButtonColors(
                        containerColor = Color.Yellow,
                        contentColor = Color.Black,
                        disabledContentColor = Color.Black,
                        disabledContainerColor = Color.Yellow
                    )
                ) {
                    Text(text = "+", fontSize = 27.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            lsts.forEach { groceryList ->
                Box {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .combinedClickable(
                                onClick = { navController.navigate("list/${groceryList.id}") },
                                onLongClick = { expandedListId = groceryList.id }
                            ),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Absolute.Left,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = groceryList.name,
                                fontSize = 32.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    DropdownMenu(
                        expanded = expandedListId == groceryList.id,
                        onDismissRequest = { expandedListId = null }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                viewModel.deleteList(listId = groceryList.id)
                            }
                        )
                    }
                }
            }

            if (isAddingLst) {
                TextField(
                    value = lstName,
                    onValueChange = { text ->
                        lstName = text
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (lstName.isNotBlank()) {
                                viewModel.addList(lstName)
                                lstName = ""
                                isAddingLst = false
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 20.sp),
                    singleLine = true
                )
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

    var newListName by remember {
        mutableStateOf("  $listName")
    }

    var isChangingListName by remember {
        mutableStateOf(false)
    }

    var itemName by remember {
        mutableStateOf("• ")
    }

    var isAddingItem by remember {
        mutableStateOf(false)
    }

    var expandableListId by remember {
        mutableStateOf<Int?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
        // topBar = {
        //     Row(modifier = Modifier
        //         .fillMaxWidth()
        //         .background(color = Color.Yellow)) {}
        // }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.End) {
                    if (isChangingListName) {
                        BasicTextField(
                            value = newListName,
                            onValueChange = { text ->
                                newListName = text
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.updateListName(listId = listId, newName = newListName)
                                    isChangingListName = false
                                }
                            ),
                            textStyle = TextStyle(
                                fontSize = 28.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            ),
                            singleLine = true
                        )
                    } else {
                        Text(
                            text = "  $listName",
                            fontSize = 28.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable( onClick = {
                                    isChangingListName = true
                                } )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    if (!isAddingItem) {
                        Button(
                            onClick = {
                                isAddingItem = true
                            },
                            modifier = Modifier.align(Alignment.CenterVertically),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Black,
                                disabledContainerColor = Color.Yellow
                            )
                        ) {
                            Text(text = "+", fontSize = 24.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                isAddingItem = false
                            },
                            modifier = Modifier.align(Alignment.CenterVertically),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                contentColor = Color.Black,
                                disabledContentColor = Color.Black,
                                disabledContainerColor = Color.Yellow
                            )
                        ) {
                            Text(text = "Done", fontSize = 18.sp)
                        }
                    }
                }
            }

            if (itemLst.isNotEmpty()) {
                LazyColumn {
                    items(itemLst) { groceryItem ->
                        if (groceryItem.strike) {
                            Text(
                                text = groceryItem.itemName,
                                fontSize = 21.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .combinedClickable(
                                        onClick = {
                                            viewModel.strikeItem(listId = listId, itemId = groceryItem.id)
                                        },
                                        onLongClick = { expandableListId = groceryItem.id }
                                    ),
                                textDecoration = TextDecoration.LineThrough,
                                color = Color.LightGray
                            )
                        } else {
                            Text(
                                text = groceryItem.itemName,
                                fontSize = 21.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .combinedClickable(
                                        onClick = {
                                            viewModel.strikeItem(listId = listId, itemId = groceryItem.id)
                                        },
                                        onLongClick = { expandableListId = groceryItem.id }
                                    )
                            )
                        }
                        DropdownMenu(
                            expanded = expandableListId == groceryItem.id,
                            onDismissRequest = { expandableListId = null }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    viewModel.deleteItem(itemId = groceryItem.id, listId = listId)
                                }
                            )
                        }
                    }
                }
            }

            if (isAddingItem) {
                BasicTextField(
                    value = itemName,
                    onValueChange = {text ->
                        itemName = text
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.addItem(listId = listId, itemName = itemName)
                            itemName = "• "
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    textStyle = TextStyle(fontSize = 21.sp),
                    singleLine = true
                )
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