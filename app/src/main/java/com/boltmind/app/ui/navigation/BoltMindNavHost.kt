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
import com.boltmind.app.feature.uebersicht.NavigationsZiel
import com.boltmind.app.feature.uebersicht.UebersichtScreen
import com.boltmind.app.feature.uebersicht.UebersichtViewModel
import org.koin.androidx.compose.koinViewModel

object BoltMindRoutes {
    const val UEBERSICHT = "uebersicht"
    const val NEUER_VORGANG = "neuer_vorgang"
    const val DEMONTAGE = "demontage/{vorgangId}"
    const val MONTAGE = "montage/{vorgangId}"

    fun demontage(vorgangId: Long) = "demontage/$vorgangId"
    fun montage(vorgangId: Long) = "montage/$vorgangId"
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

            LaunchedEffect(uiState.navigationsZiel) {
                when (val ziel = uiState.navigationsZiel) {
                    is NavigationsZiel.Demontage -> {
                        navController.navigate(BoltMindRoutes.demontage(ziel.vorgangId))
                        viewModel.onNavigationAbgeschlossen()
                    }
                    is NavigationsZiel.Montage -> {
                        navController.navigate(BoltMindRoutes.montage(ziel.vorgangId))
                        viewModel.onNavigationAbgeschlossen()
                    }
                    null -> { /* kein Navigationsziel */ }
                }
            }

            UebersichtScreen(
                uiState = uiState,
                onVorgangGetippt = viewModel::onVorgangGetippt,
                onNeuerVorgangGetippt = {
                    navController.navigate(BoltMindRoutes.NEUER_VORGANG)
                },
                onWeiterDemontieren = viewModel::onWeiterDemontierenGewaehlt,
                onMontageStarten = viewModel::onMontageStartenGewaehlt,
                onDialogVerwerfen = viewModel::onDialogVerworfen,
                onLoeschenAngefragt = viewModel::onLoeschenAngefragt,
                onLoeschenBestaetigt = viewModel::onLoeschenBestaetigt,
                onLoeschenAbgebrochen = viewModel::onLoeschenAbgebrochen,
                onTabGewaehlt = viewModel::onTabGewaehlt,
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
