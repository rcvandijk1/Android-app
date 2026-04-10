package com.homehub.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homehub.app.ui.HomeHubViewModels
import com.homehub.app.ui.edit.EditDeviceScreen
import com.homehub.app.ui.edit.EditDeviceViewModel
import com.homehub.app.ui.home.HomeScreen
import com.homehub.app.ui.home.HomeViewModel
import com.homehub.app.ui.rooms.ManageRoomsScreen
import com.homehub.app.ui.rooms.ManageRoomsViewModel

object Routes {
    const val HOME = "home"
    const val EDIT = "edit/{deviceId}"
    const val ROOMS = "rooms"

    /** Sentinel id for "new device". */
    const val NEW_DEVICE_ID: Long = -1L

    fun edit(deviceId: Long) = "edit/$deviceId"
}

@Composable
fun HomeHubNavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = HomeHubViewModels.Factory)
            HomeScreen(
                viewModel = vm,
                onAddDevice = { nav.navigate(Routes.edit(Routes.NEW_DEVICE_ID)) },
                onEditDevice = { id -> nav.navigate(Routes.edit(id)) },
                onManageRooms = { nav.navigate(Routes.ROOMS) },
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType }),
        ) { entry ->
            val deviceId = entry.arguments?.getLong("deviceId") ?: Routes.NEW_DEVICE_ID
            val vm: EditDeviceViewModel = viewModel(factory = HomeHubViewModels.Factory)
            EditDeviceScreen(
                viewModel = vm,
                deviceId = deviceId,
                onDone = { nav.popBackStack() },
            )
        }

        composable(Routes.ROOMS) {
            val vm: ManageRoomsViewModel = viewModel(factory = HomeHubViewModels.Factory)
            ManageRoomsScreen(
                viewModel = vm,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
