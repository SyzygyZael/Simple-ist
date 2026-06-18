package com.kevinnesbitt.simple_ist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kevinnesbitt.simple_ist.ui.theme.SimpleistTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleistTheme {

                val navController = rememberNavController()
                val viewModel: HomeViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(navController, viewModel)
                    }

                    composable(
                        route = "list/{listName}"
                    ) { backStackEntry ->

                        val listName =
                            backStackEntry.arguments?.getString("listName") ?: ""

                        ListScreen(listName = listName, navController = navController, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val lsts = viewModel.lists

    var lstName by remember {
        mutableStateOf("")
    }

    var isAddingLst by remember {
        mutableStateOf(false)
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
                Button(
                    onClick = {
                        navController.navigate("list/${groceryList.id}")
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContentColor = Color.Black,
                        disabledContainerColor = Color.White
                    )
                ) {
                    Text(text = groceryList.name, fontSize = 20.sp, textAlign = TextAlign.Left)
                }
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ListScreen(listName: String, navController: NavController, viewModel: HomeViewModel) {
    val itemLst = viewModel.lists

    var itemName by remember {
        mutableStateOf("")
    }

    var isAddingItem by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Yellow),
                horizontalArrangement = Arrangement.End
            ) {
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
                        Text(text = "Done", fontSize = 20.sp)
                    }
                }
            }

            if (itemLst.isNotEmpty()) {
                LazyColumn {
                    items(itemLst) { currentName ->
                        Text(
                            text = "- $currentName",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
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
                            itemName = ""
                        }
                    )
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