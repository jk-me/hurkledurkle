package com.hurkledurkle.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hurkledurkle.app.HurkleApplication
import com.hurkledurkle.app.ui.screen.dashboard.DashboardScreen
import com.hurkledurkle.app.ui.screen.dashboard.DashboardViewModel
import com.hurkledurkle.app.ui.screen.log.LogSessionScreen
import com.hurkledurkle.app.ui.screen.log.LogSessionViewModel
import com.hurkledurkle.app.ui.screen.settings.SettingsScreen
import com.hurkledurkle.app.ui.screen.settings.SettingsViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object LogSession : Screen("log_session?sessionId={sessionId}") {
        fun route(sessionId: Long? = null) =
            if (sessionId != null) "log_session?sessionId=$sessionId" else "log_session"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as HurkleApplication

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            val vm: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(app.repository, app.userPreferences)
            )
            DashboardScreen(
                viewModel = vm,
                onAddSession = { navController.navigate(Screen.LogSession.route()) },
                onEditSession = { id -> navController.navigate(Screen.LogSession.route(id)) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = "log_session?sessionId={sessionId}",
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
            val editId = if (rawId == -1L) null else rawId
            val vm: LogSessionViewModel = viewModel(
                factory = LogSessionViewModel.Factory(app.repository, app.userPreferences, editId)
            )
            LogSessionScreen(
                viewModel = vm,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(app.userPreferences)
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
