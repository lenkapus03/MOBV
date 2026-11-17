package com.example.mobvzadanie

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobvzadanie.quiz.QuizCategoryViewModel
import com.example.mobvzadanie.screens.AccountScreen
import com.example.mobvzadanie.screens.HistoryScreen
import com.example.mobvzadanie.screens.HomeScreen
import com.example.mobvzadanie.screens.QuizScreen
import com.example.mobvzadanie.screens.QuizSettingsScreen

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val isInNavBar: Boolean = false,
    val showBottomBar: Boolean = isInNavBar
) {
    HOME("home", "Domov", Icons.Default.Home, "Domov", true, true),
    ACCOUNT("account", "Konto", Icons.Default.AccountCircle, "Konto", true, true),

    QUIZ_SETTINGS("quiz_settings", "Nastavenie kvízu", Icons.Default.Home, "Nastavenie kvízu", false, false),
    QUIZ("quiz", "Kvíz", Icons.Default.Home, "Kvíz", false, false),
    HISTORY("history", "História", Icons.Default.Home, "História kvízov", false, true),
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    userAccount: UserAccount,
    context: Context,
    viewModel: QuizCategoryViewModel
) {
    val navController = rememberNavController()
    val savedName by userAccount.getName().collectAsState(initial = "")
    val accountCreated = savedName.isNotBlank()
    val startDestination = if (accountCreated) Destination.HOME else Destination.ACCOUNT
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val currentDestination = Destination.entries.find { it.route == currentRoute }
            if (currentDestination?.showBottomBar == true) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    val navigationBarItems = Destination.entries.filter { e -> e.isInNavBar }
                    navigationBarItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(route = destination.route) {
                                        popUpTo(Destination.HOME.route)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.contentDescription
                                )
                            },
                            label = { Text(destination.label) },
                            enabled = destination == Destination.ACCOUNT ||
                                    (destination == Destination.HOME && accountCreated)
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(
            navController,
            startDestination,
            modifier = Modifier.padding(contentPadding),
            userAccount,
            context,
            viewModel
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    userAccount: UserAccount,
    context: Context,
    viewModel: QuizCategoryViewModel
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME ->
                        HomeScreen(
                            modifier.padding(16.dp),
                            onNavigateToQuizSettingsScreen = {
                                navController.navigate(Destination.QUIZ_SETTINGS.route)
                            },
                            onNavigateToHistoryScreen = {
                                navController.navigate(Destination.HISTORY.route)
                            }
                        )
                    Destination.ACCOUNT ->
                        AccountScreen(
                            modifier.padding(16.dp),
                            userAccount,
                            context
                        )
                    Destination.QUIZ_SETTINGS ->
                        QuizSettingsScreen(
                            modifier = modifier.padding(16.dp),
                            onNavigateToHomeScreen = {
                                navController.navigate(Destination.QUIZ.route)
                            },
                            viewModel = viewModel
                        )
                    Destination.QUIZ ->
                        QuizScreen(
                            modifier = modifier.padding(16.dp),
                            onNavigateToHomeScreen = {
                                navController.navigate(Destination.HOME.route) {
                                    popUpTo(Destination.HOME.route) { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )

                    Destination.HISTORY -> {
                        HistoryScreen(
                            modifier = modifier.padding(16.dp),
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
