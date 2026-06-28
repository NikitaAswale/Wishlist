package com.example.wishlist.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wishlist.ui.screens.AddEditWishScreen
import com.example.wishlist.ui.screens.CalendarScreen
import com.example.wishlist.ui.screens.HomeScreen
import com.example.wishlist.ui.screens.WishDetailScreen

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Wishes : BottomNavItem(
        route = Screen.Home.route,
        label = "Wishes",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )
    data object Calendar : BottomNavItem(
        route = Screen.Calendar.route,
        label = "Calendar",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )
}

@Composable
fun WishlistNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Calendar.route)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onWishClick = { wishId ->
                        navController.navigate(Screen.WishDetail.createRoute(wishId))
                    },
                    onAddWishClick = {
                        navController.navigate(Screen.AddWish.route)
                    },
                    onCalendarClick = {
                        navController.navigate(Screen.Calendar.route)
                    }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onWishClick = { wishId ->
                        navController.navigate(Screen.WishDetail.createRoute(wishId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AddWish.route) {
                AddEditWishScreen(
                    onSaved = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.EditWish.route,
                arguments = listOf(navArgument("wishId") { type = NavType.LongType })
            ) { backStackEntry ->
                val wishId = backStackEntry.arguments?.getLong("wishId")
                AddEditWishScreen(
                    wishId = wishId,
                    onSaved = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.WishDetail.route,
                arguments = listOf(navArgument("wishId") { type = NavType.LongType })
            ) { backStackEntry ->
                val wishId = backStackEntry.arguments?.getLong("wishId") ?: -1L
                WishDetailScreen(
                    wishId = wishId,
                    onEdit = {
                        navController.navigate(Screen.EditWish.createRoute(wishId))
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onDelete = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(BottomNavItem.Wishes, BottomNavItem.Calendar)

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}
