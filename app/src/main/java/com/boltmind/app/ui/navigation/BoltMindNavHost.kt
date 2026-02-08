package com.boltmind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.boltmind.app.feature.neuervorgang.NeuerVorgangScreen
import com.boltmind.app.feature.neuervorgang.NeuerVorgangViewModel
import com.boltmind.app.feature.uebersicht.UebersichtScreen
import com.boltmind.app.feature.uebersicht.UebersichtViewModel
import org.koin.androidx.compose.koinViewModel

object BoltMindRoutes {
    const val UEBERSICHT = "uebersicht"
    const val NEUER_VORGANG = "neuer_vorgang"
}

@Composable
fun BoltMindNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = BoltMindRoutes.UEBERSICHT,
        modifier = modifier,
    ) {
        composable(BoltMindRoutes.UEBERSICHT) {
            val viewModel: UebersichtViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()
            UebersichtScreen(
                uiState = uiState,
                onVorgangGetippt = { viewModel.onVorgangGetippt(it) },
                onNeuerVorgangGetippt = {
                    navController.navigate(BoltMindRoutes.NEUER_VORGANG)
                },
            )
        }
        composable(BoltMindRoutes.NEUER_VORGANG) {
            val viewModel: NeuerVorgangViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.erstellterVorgangId) {
                uiState.erstellterVorgangId?.let {
                    viewModel.onNavigationAbgeschlossen()
                    navController.popBackStack()
                }
            }

            NeuerVorgangScreen(
                uiState = uiState,
                onFotoAufgenommen = viewModel::onFotoAufgenommen,
                onBildWiederholen = viewModel::onBildWiederholen,
                onKameraAbgebrochen = viewModel::onKameraAbgebrochen,
                onAuftragsnummerGeaendert = viewModel::onAuftragsnummerGeaendert,
                onBeschreibungGeaendert = viewModel::onBeschreibungGeaendert,
                onStartenGetippt = viewModel::onStartenGetippt,
                onZurueck = { navController.popBackStack() },
            )
        }
    }
}
