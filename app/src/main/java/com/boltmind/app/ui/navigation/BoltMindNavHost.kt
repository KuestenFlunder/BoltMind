package com.boltmind.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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
            Platzhalter("Splash")
        }

        composable(BoltMindRoutes.UEBERSICHT) {
            Platzhalter("Uebersicht")
        }

        composable(BoltMindRoutes.NEUER_VORGANG) {
            Platzhalter("Neuer Auftrag")
        }

        composable(
            route = BoltMindRoutes.BROWSER,
            arguments = listOf(
                navArgument("vorgangId") { type = NavType.LongType },
                navArgument("modus") { type = NavType.StringType }
            )
        ) { eintrag ->
            val modus = BrowserModus.ausName(eintrag.arguments?.getString("modus"))
            Platzhalter("Browser $modus")
        }

        composable(
            route = BoltMindRoutes.ABSCHLUSS,
            arguments = listOf(navArgument("vorgangId") { type = NavType.LongType })
        ) {
            Platzhalter("Abschluss")
        }
    }
}

@Composable
private fun Platzhalter(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(name) }
}
