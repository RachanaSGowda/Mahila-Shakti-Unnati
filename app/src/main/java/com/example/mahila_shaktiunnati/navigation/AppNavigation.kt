package com.example.mahila_shaktiunnati.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mahila_shaktiunnati.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController, 
        startDestination = "home",
        enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn() },
        exitTransition = { slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(400)) + fadeOut() },
        popEnterTransition = { slideInVertically(initialOffsetY = { -it }, animationSpec = tween(400)) + fadeIn() },
        popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut() }
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable(
            route = "members_list/{tabIndex}",
            arguments = listOf(navArgument("tabIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            MembersListScreen(navController = navController, startTab = tabIndex)
        }
        composable("add_member") {
            AddMemberScreen(navController = navController)
        }
        composable(
            route = "member_details/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            MemberDetailsScreen(navController = navController, memberId = memberId)
        }
        composable(
            route = "savings/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            SavingsScreen(navController = navController, memberId = memberId)
        }
        composable(
            route = "loan/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.IntType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getInt("memberId") ?: 0
            LoanScreen(navController = navController, memberId = memberId)
        }
    }
}
