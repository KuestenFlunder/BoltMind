package com.boltmind.app.feature.browser

import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
import com.boltmind.app.ui.navigation.BrowserModus

/**
 * Die Zeiterfassung des Browsers.
 *
 * Eigene Klasse aus demselben Grund wie [BrowserFotoSteuerung]: sie nimmt dem
 * ViewModel eine ganze Verantwortung ab und ist fuer sich testbar. Vor allem
 * aber kennt danach **nur noch sie** den [ReferenzTyp] -- welcher Modus welche
 * Referenz misst, ist eine Frage der Zeiterfassung und keine des Screens.
 *
 * Der Service selbst kennt seine Consumer nicht (`governance.md`,
 * "Service-Architektur"). Diese Klasse ist die Consumer-Seite: sie uebersetzt
 * den Modus in den Referenztyp und rechnet die drei Zahlen der Timer-Kapsel aus.
 */
class BrowserZeitsteuerung(
    private val repository: ReparaturRepository,
    private val zeiterfassung: ZeiterfassungService,
    private val modus: BrowserModus
) {

    /**
     * Die drei Zahlen der Timer-Kapsel.
     *
     * [gesamtSekunden] zaehlt Demontage **und** Montage zusammen -- im Archiv
     * steht dort die Gesamtdauer des Vorgangs, nicht die eines Modus.
     */
    data class Zeitstand(
        val schrittSekunden: Long = 0,
        val laeuft: Boolean = false,
        val gesamtSekunden: Long = 0
    )

    private val referenzTyp: String
        get() = if (modus == BrowserModus.MONTAGE) ReferenzTyp.MONTAGE_SCHRITT
        else ReferenzTyp.DEMONTAGE_SCHRITT

    suspend fun stand(vorgangId: Long, schrittId: Long?): Zeitstand = Zeitstand(
        schrittSekunden = schrittId
            ?.let { zeiterfassung.gesamtdauer(it, referenzTyp).seconds } ?: 0L,
        laeuft = schrittId?.let { zeiterfassung.laeuft(it, referenzTyp) } ?: false,
        gesamtSekunden = repository.holeSchrittIds(vorgangId).sumOf { id ->
            zeiterfassung.gesamtdauer(id, ReferenzTyp.DEMONTAGE_SCHRITT).seconds +
                zeiterfassung.gesamtdauer(id, ReferenzTyp.MONTAGE_SCHRITT).seconds
        }
    )

    suspend fun umschalten(schrittId: Long) = zeiterfassung.umschalten(schrittId, referenzTyp)

    suspend fun stoppeSchritt(schrittId: Long) =
        zeiterfassung.stoppeFallsLaeuft(schrittId, referenzTyp)

    suspend fun stoppeAlle() = zeiterfassung.stoppeAlleOffenen()
}
