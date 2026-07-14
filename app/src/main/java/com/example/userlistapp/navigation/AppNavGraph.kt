package com.example.userlistapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.userlistapp.ui.screen.UserDetailScreen
import com.example.userlistapp.ui.screen.UserListScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.USER_LIST,
        modifier = modifier,
    ) {
        composable(Routes.USER_LIST) {
            UserListScreen(
                onUserClick = { user ->
                    navController.navigate(Routes.userDetail(user.id))
                },
            )
        }
        composable(
            route = Routes.USER_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            UserDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
