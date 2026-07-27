package com.boltmind.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.boltmind.app.feature.abschluss.AbschlussScreen
import com.boltmind.app.feature.abschluss.AbschlussViewModel
import com.boltmind.app.feature.neuervorgang.NeuerVorgangRoute
import com.boltmind.app.feature.splash.SplashScreen
import com.boltmind.app.feature.uebersicht.UebersichtScreen
import com.boltmind.app.feature.uebersicht.UebersichtViewModel
import com.boltmind.app.feature.uebersicht.UebersichtZiel
import org.koin.androidx.compose.koinViewModel

/**
 * Die drei Betriebsarten des Schritt-Browsers (F-006).
 *
 * Der Browser selbst ist zustandslos und kennt keine Navigation -- der Modus
 * entscheidet nur darueber, welche Bedienelemente der Consumer einhaengt und
 * ob die Foto-Label bearbeitbar sind.
 */
enum class BrowserModus {
    /** F-003 Demontage: Label aenderbar, neue Schritte anlegbar. */
    DEMONTAGE,

    /** F-004 Montage: Label nur lesbar, Haken setzbar. */
    MONTAGE,

    /** F-001 Archiv: nur lesen. */
    ARCHIV;

    companion object {
        fun ausName(name: String?): BrowserModus =
            entries.firstOrNull { it.name == name } ?: DEMONTAGE
    }
}

object BoltMindRoutes {
    const val SPLASH = "splash"
    const val UEBERSICHT = "uebersicht"
    const val NEUER_VORGANG = "neuer_vorgang"
    const val BROWSER = "browser/{vorgangId}/{modus}"
    const val ABSCHLUSS = "abschluss/{vorgangId}"

    fun browser(vorgangId: Long, modus: BrowserModus) = "browser/$vorgangId/${modus.name}"
    fun abschluss(vorgangId: Long) = "abschluss/$vorgangId"
}

@Composable
fun BoltMindNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = BoltMindRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(BoltMindRoutes.SPLASH) {
            SplashScreen(
                onFertig = {
                    navController.navigate(BoltMindRoutes.UEBERSICHT) {
                        // Ohne popUpTo fuehrt die Zurueck-Geste in den Splash zurueck.
                        popUpTo(BoltMindRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(BoltMindRoutes.UEBERSICHT) {
            val viewModel: UebersichtViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.ziel) {
                when (val ziel = uiState.ziel) {
                    is UebersichtZiel.NeuerVorgang -> {
                        navController.navigate(BoltMindRoutes.NEUER_VORGANG)
                        viewModel.onZielVerbraucht()
                    }
                    is UebersichtZiel.Browser -> {
                        navController.navigate(BoltMindRoutes.browser(ziel.vorgangId, ziel.modus))
                        viewModel.onZielVerbraucht()
                    }
                    null -> Unit
                }
            }

            UebersichtScreen(
                uiState = uiState,
                onTabGewaehlt = viewModel::onTabGewaehlt,
                onVorgangGeoeffnet = viewModel::onVorgangGeoeffnet,
                onLoeschenAngefragt = viewModel::onLoeschenAngefragt,
                onWeiterDemontieren = viewModel::onWeiterDemontieren,
                onMontageStarten = viewModel::onMontageStarten,
                onLoeschenBestaetigt = viewModel::onLoeschenBestaetigt,
                onSheetGeschlossen = viewModel::onSheetGeschlossen,
                onNeuerVorgang = viewModel::onNeuerVorgang
            )
        }

        composable(BoltMindRoutes.NEUER_VORGANG) {
            NeuerVorgangRoute(
                onVorgangGestartet = { vorgangId ->
                    // Der Anlage-Screen ist erledigt; von der Demontage aus soll
                    // Zurueck in die Uebersicht fuehren, nicht ins Formular.
                    navController.navigate(
                        BoltMindRoutes.browser(vorgangId, BrowserModus.DEMONTAGE)
                    ) {
                        popUpTo(BoltMindRoutes.NEUER_VORGANG) { inclusive = true }
                    }
                },
                onAbgebrochen = { navController.popBackStack() }
            )
        }

        composable(
            route = BoltMindRoutes.BROWSER,
            arguments = listOf(
                navArgument("vorgangId") { type = NavType.LongType },
                navArgument("modus") { type = NavType.StringType }
            )
        ) {
            val vorgangId = it.arguments?.getLong("vorgangId") ?: 0L
            BrowserRoute(
                onVerlassen = {
                    navController.popBackStack(BoltMindRoutes.UEBERSICHT, inclusive = false)
                },
                onMontageFertig = {
                    navController.navigate(BoltMindRoutes.abschluss(vorgangId))
                }
            )
        }

        composable(
            route = BoltMindRoutes.ABSCHLUSS,
            arguments = listOf(navArgument("vorgangId") { type = NavType.LongType })
        ) {
            val viewModel: AbschlussViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.archiviert) {
                if (uiState.archiviert) {
                    viewModel.onNavigationAbgeschlossen()
                    // Archivieren beendet den Vorgang: der ganze Montage-Pfad
                    // verschwindet aus dem Stapel, Zurueck fuehrt in die Uebersicht.
                    navController.popBackStack(BoltMindRoutes.UEBERSICHT, inclusive = false)
                }
            }

            AbschlussScreen(
                auftragsnummer = uiState.auftragsnummer,
                beschreibung = uiState.beschreibung,
                anzahlTeile = uiState.anzahlTeile,
                gemesseneZeit = uiState.gemesseneZeit,
                onArchivieren = viewModel::onArchivieren
            )
        }
    }
}
