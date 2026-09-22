package ge.merabi.jumpstart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ge.merabi.jumpstart.ui.screens.HistoryScreen
import ge.merabi.jumpstart.ui.screens.HomeScreen
import ge.merabi.jumpstart.ui.screens.NewOrderScreen
import ge.merabi.jumpstart.ui.screens.OrderDetailScreen
import ge.merabi.jumpstart.viewmodel.OrderViewModel

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val NEW_ORDER = "new_order?orderId={orderId}"
    const val DETAIL = "detail/{orderId}"

    fun newOrder(orderId: Long? = null) = "new_order?orderId=${orderId ?: -1L}"
    fun detail(orderId: Long) = "detail/$orderId"
}

@Composable
fun AppNavigation(viewModel: OrderViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onNewOrder = { navController.navigate(Routes.newOrder()) },
                onHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenOrder = { id -> navController.navigate(Routes.detail(id)) }
            )
        }

        composable(
            route = Routes.NEW_ORDER,
            arguments = listOf(navArgument("orderId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val idArg = backStackEntry.arguments?.getLong("orderId") ?: -1L
            NewOrderScreen(
                orderId = if (idArg == -1L) null else idArg,
                viewModel = viewModel,
                onDone = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = id,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { editId -> navController.navigate(Routes.newOrder(editId)) }
            )
        }
    }
}
