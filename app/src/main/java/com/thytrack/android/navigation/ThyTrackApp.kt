package com.thytrack.android.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thytrack.android.R
import com.thytrack.android.ui.MedicationScreen
import com.thytrack.android.ui.RecordsScreen
import com.thytrack.android.ui.SettingsScreen
import com.thytrack.android.ui.TrendsScreen

private data class TopLevelRoute(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
)

private val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute("records", R.string.tab_records, android.R.drawable.ic_menu_agenda),
    TopLevelRoute("trends", R.string.tab_trends, android.R.drawable.ic_menu_today),
    TopLevelRoute("medications", R.string.tab_medications, android.R.drawable.ic_menu_sort_by_size),
    TopLevelRoute("settings", R.string.tab_settings, android.R.drawable.ic_menu_preferences),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThyTrackApp() {
    val navController = rememberNavController()
    var selected by remember { mutableIntStateOf(0) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            // 对齐 Flutter seed 色 #4A90D9
            primary = Color(0xFF4A90D9),
        ),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) })
            },
            bottomBar = {
                NavigationBar {
                    TOP_LEVEL_ROUTES.forEachIndexed { index, route ->
                        NavigationBarItem(
                            selected = selected == index,
                            onClick = {
                                selected = index
                                navController.navigate(route.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(route.iconRes),
                                    contentDescription = stringResource(route.labelRes),
                                )
                            },
                            label = { Text(stringResource(route.labelRes)) },
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* TODO Phase 2: 新增记录 */ }) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_add),
                        contentDescription = stringResource(R.string.action_add),
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "records",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                composable("records") { RecordsScreen() }
                composable("trends") { TrendsScreen() }
                composable("medications") { MedicationScreen() }
                composable("settings") { SettingsScreen() }
            }
        }
    }
}
