package com.boltmind.app.feature.browser

import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.service.zeiterfassung.ReferenzTyp
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService

/**
 * Die Foto- und Schrittaktionen der Demontage.
 *
 * Eigene Klasse statt weiterer Methoden im ViewModel: das haelt
 * [BrowserViewModel] unter der 200-Zeilen-Grenze aus den Coding-Regeln und macht
 * die heikelste Regel des Projekts fuer sich testbar -- ein Kamera-Abbruch darf
 * nie ein vorhandenes Foto vernichten.
 */
class BrowserFotoSteuerung(
    private val repository: ReparaturRepository,
    private val fotoManager: FotoManager,
    private val zeiterfassung: ZeiterfassungService
) {

    /**
     * Legt den naechsten Schritt an und startet dessen Zeitmessung.
     * Die Nummer ist immer MAX + 1 und wird nie wiederverwendet.
     */
    suspend fun naechstesTeil(vorgangId: Long, offenerSchritt: Schritt?): Schritt {
        offenerSchritt?.let {
            repository.schrittAbschliessen(it.id)
            zeiterfassung.stoppeFallsLaeuft(it.id, ReferenzTyp.DEMONTAGE_SCHRITT)
        }
        val neu = repository.schrittAnlegen(vorgangId)
        zeiterfassung.starten(neu.id, ReferenzTyp.DEMONTAGE_SCHRITT)
        return neu
    }

    /** Haengt ein frisch aufgenommenes Foto an den betrachteten Schritt. */
    suspend fun fotoUebernehmen(schrittId: Long, pfad: String) {
        fotoManager.entferneExifMetadaten(pfad)
        repository.fotoAnhaengen(schrittId, pfad)
    }

    /**
     * Ersetzt ein Foto an derselben Position.
     *
     * Reihenfolge ist verbindlich: das neue Foto wird zuerst gesichert, erst
     * danach verschwinden alte Zeile und alte Datei. Bricht die Kamera ab, wird
     * diese Funktion gar nicht erst aufgerufen und das alte Foto bleibt
     * unangetastet (Governance, "Wiederholen loescht erst nach Erfolg").
     */
    suspend fun fotoErsetzen(altesFotoId: Long, neuerPfad: String) {
        val altes = repository.findFoto(altesFotoId) ?: return
        fotoManager.entferneExifMetadaten(neuerPfad)
        repository.fotoErsetzen(altesFotoId, neuerPfad)
        fotoManager.loescheFoto(altes.pfad)
    }

    /**
     * Beendet die Demontage. Ein offener Schritt ohne Fotos wird verworfen statt
     * abgeschlossen -- sonst bliebe ein leeres Thumbnail zurueck und eine
     * Schrittnummer waere verbrannt.
     */
    suspend fun beenden(offenerSchritt: Schritt?) {
        offenerSchritt ?: return
        zeiterfassung.stoppeFallsLaeuft(offenerSchritt.id, ReferenzTyp.DEMONTAGE_SCHRITT)
        val fotos = repository.holeFotos(offenerSchritt.id)
        if (fotos.isEmpty()) repository.schrittVerwerfen(offenerSchritt.id)
        else repository.schrittAbschliessen(offenerSchritt.id)
    }

    /** Raeumt eine Datei weg, die die Kamera geschrieben hat, aber niemand uebernommen hat. */
    fun verwerfeDatei(pfad: String) = fotoManager.loescheFoto(pfad)
}
