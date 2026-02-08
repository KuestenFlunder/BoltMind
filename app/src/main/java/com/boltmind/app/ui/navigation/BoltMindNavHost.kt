package com.boltmind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.boltmind.app.feature.demontage.DemontageScreen
import com.boltmind.app.feature.neuervorgang.NeuerVorgangScreen
import com.boltmind.app.feature.uebersicht.UebersichtScreen

enum class BoltMindRoute(val route: String) {
    UEBERSICHT("uebersicht"),
    NEUER_VORGANG("neuerVorgang"),
    DEMONTAGE("demontage/{vorgangId}"),
    MONTAGE("montage/{vorgangId}");

    companion object {
        fun demontageRoute(vorgangId: Long): String = "demontage/$vorgangId"
        fun montageRoute(vorgangId: Long): String = "montage/$vorgangId"
    }
}

@Composable
fun BoltMindNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BoltMindRoute.UEBERSICHT.route,
        modifier = modifier
    ) {
        composable(BoltMindRoute.UEBERSICHT.route) {
            UebersichtScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(BoltMindRoute.NEUER_VORGANG.route) {
            NeuerVorgangScreen(
                onNavigateToDemontage = { vorgangId ->
                    navController.navigate(BoltMindRoute.demontageRoute(vorgangId)) {
                        popUpTo(BoltMindRoute.UEBERSICHT.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = BoltMindRoute.DEMONTAGE.route,
            arguments = listOf(navArgument("vorgangId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vorgangId = backStackEntry.arguments?.getLong("vorgangId") ?: return@composable
            DemontageScreen(
                vorgangId = vorgangId,
                onNavigateBack = {
                    navController.popBackStack(BoltMindRoute.UEBERSICHT.route, inclusive = false)
                }
            )
        }

        composable(
            route = BoltMindRoute.MONTAGE.route,
            arguments = listOf(navArgument("vorgangId") { type = NavType.LongType })
        ) {
            // Placeholder - wird in Sprint 2 implementiert
        }
    }
}
