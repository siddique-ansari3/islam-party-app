package com.islamparty.karyakarta.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.islamparty.karyakarta.data.AppContainer
import com.islamparty.karyakarta.ui.screens.businesscard.BusinessCardScreen
import com.islamparty.karyakarta.ui.screens.login.LoginScreen
import com.islamparty.karyakarta.ui.screens.messaging.MessagingScreen
import com.islamparty.karyakarta.ui.screens.workerdetail.WorkerDetailScreen
import com.islamparty.karyakarta.ui.screens.workerform.WorkerFormScreen
import com.islamparty.karyakarta.ui.screens.workerlist.WorkerListScreen
import com.islamparty.karyakarta.BuildConfig

@Composable
fun AppNavGraph(container: AppContainer, startDestination: String) {
    val navController: NavHostController = rememberNavController()
    val baseUrl = BuildConfig.API_BASE_URL

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authRepository = container.authRepository,
                onLoginSuccess = {
                    navController.navigate(Routes.WORKER_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.WORKER_LIST) {
            WorkerListScreen(
                repository = container.workerRepository,
                authBaseUrl = baseUrl,
                onAddWorker = { navController.navigate(Routes.workerFormNew()) },
                onOpenWorker = { navController.navigate(Routes.workerDetail(it)) },
                onSendMessage = { ids -> navController.navigate(Routes.messaging(ids)) }
            )
        }

        composable(
            route = Routes.WORKER_DETAIL,
            arguments = listOf(navArgument("workerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("workerId").orEmpty()
            WorkerDetailScreen(
                workerId = id,
                repository = container.workerRepository,
                baseUrl = baseUrl,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.workerFormEdit(it)) },
                onGenerateCard = { navController.navigate(Routes.businessCard(it)) }
            )
        }

        composable(
            route = Routes.WORKER_FORM,
            arguments = listOf(navArgument("workerId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("workerId")
            WorkerFormScreen(
                repository = container.workerRepository,
                authRepository = container.authRepository,
                editingWorkerId = id,
                onSaved = {
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.MESSAGING,
            arguments = listOf(navArgument("workerIds") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val idsParam = backStackEntry.arguments?.getString("workerIds")
            val ids = idsParam?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            MessagingScreen(
                repository = container.messageRepository,
                workerIds = ids,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.BUSINESS_CARD,
            arguments = listOf(navArgument("workerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("workerId").orEmpty()
            BusinessCardScreen(
                workerId = id,
                repository = container.workerRepository,
                baseUrl = baseUrl,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
