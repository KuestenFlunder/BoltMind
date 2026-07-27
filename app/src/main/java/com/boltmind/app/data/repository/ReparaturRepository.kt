package com.boltmind.app.data.repository

import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.local.SchrittFotoDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittFoto
import com.boltmind.app.data.model.SchrittMitFotos
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * Einziger Schreibweg auf Vorgaenge, Schritte und Fotos.
 *
 * **Invariante:** Jede datenveraendernde Operation zieht
 * [Reparaturvorgang.aktualisiertAm] nach. Die Uebersicht sortiert beide Listen
 * danach -- ohne das rutscht ein Vorgang nach getaner Arbeit nicht nach oben.
 * Durchgesetzt wird das ueber den einen Trichter [beruehrend]; jede neue
 * schreibende Methode muss ihn benutzen.
 */
class ReparaturRepository(
    private val vorgangDao: ReparaturvorgangDao,
    private val schrittDao: SchrittDao,
    private val fotoDao: SchrittFotoDao,
    private val transaktion: TransaktionsLauf = DirekterLauf(),
    private val uhr: () -> Instant = Instant::now
) {

    // ------------------------------------------------------------------
    // Der Trichter
    // ------------------------------------------------------------------

    /**
     * Fuehrt [block] aus und setzt danach `aktualisiertAm` des betroffenen Vorgangs.
     * Ist [vorgangId] null (der Vorgang existiert nicht mehr), wird nur [block]
     * ausgefuehrt.
     */
    private suspend fun <T> beruehrend(vorgangId: Long?, block: suspend () -> T): T =
        transaktion.inTransaktion {
            val ergebnis = block()
            if (vorgangId != null) {
                vorgangDao.beruehre(vorgangId, uhr().toEpochMilli())
            }
            ergebnis
        }

    // ------------------------------------------------------------------
    // Vorgaenge
    // ------------------------------------------------------------------

    fun beobachteOffeneVorgaenge(): Flow<List<Reparaturvorgang>> =
        vorgangDao.beobachteNachStatus(VorgangStatus.OFFEN)

    fun beobachteArchivierteVorgaenge(): Flow<List<Reparaturvorgang>> =
        vorgangDao.beobachteNachStatus(VorgangStatus.ARCHIVIERT)

    fun beobachteOffeneVorgaengeMitAnzahl(): Flow<List<ReparaturvorgangMitAnzahl>> =
        vorgangDao.beobachteNachStatusMitAnzahl(VorgangStatus.OFFEN, uhr().toEpochMilli())

    fun beobachteArchivierteVorgaengeMitAnzahl(): Flow<List<ReparaturvorgangMitAnzahl>> =
        vorgangDao.beobachteNachStatusMitAnzahl(VorgangStatus.ARCHIVIERT, uhr().toEpochMilli())

    suspend fun findVorgangById(id: Long): Reparaturvorgang? = vorgangDao.findById(id)

    suspend fun zaehleSchritte(vorgangId: Long): Int = vorgangDao.zaehleSchritte(vorgangId)

    suspend fun zaehleEingebaute(vorgangId: Long): Int = schrittDao.zaehleEingebaute(vorgangId)

    /** Neuanlage. `aktualisiertAm` setzt der Aufrufer ueber die Entity selbst. */
    suspend fun erstelleVorgang(vorgang: Reparaturvorgang): Long =
        vorgangDao.einfuegen(vorgang.copy(aktualisiertAm = uhr()))

    suspend fun aktualisiereVorgang(vorgang: Reparaturvorgang) =
        vorgangDao.aktualisieren(vorgang.copy(aktualisiertAm = uhr()))

    suspend fun loescheVorgang(id: Long) = vorgangDao.loeschen(id)

    /** Nimmt den Vorgang aus der aktiven Liste. Nur ueber den Abschluss-Screen. */
    suspend fun archiviereVorgang(vorgangId: Long) =
        vorgangDao.setzeStatus(vorgangId, VorgangStatus.ARCHIVIERT, uhr().toEpochMilli())

    // ------------------------------------------------------------------
    // Schritte
    // ------------------------------------------------------------------

    fun beobachteSchritte(vorgangId: Long): Flow<List<Schritt>> =
        schrittDao.beobachteSchritte(vorgangId)

    /** Eingabestruktur des Schritt-Browsers, Fotos nach `reihenfolge` sortiert. */
    fun beobachteSchritteMitFotos(vorgangId: Long): Flow<List<SchrittMitFotos>> =
        schrittDao.beobachteSchritteMitFotos(vorgangId)
            .map { liste -> liste.map { it.sortiert() } }

    suspend fun holeSchritteMitFotos(vorgangId: Long): List<SchrittMitFotos> =
        schrittDao.holeSchritteMitFotos(vorgangId).map { it.sortiert() }

    suspend fun holeSchritte(vorgangId: Long): List<Schritt> = schrittDao.holeSchritte(vorgangId)

    suspend fun findSchritt(schrittId: Long): Schritt? = schrittDao.findById(schrittId)

    suspend fun findUnabgeschlossenenSchritt(vorgangId: Long): Schritt? =
        schrittDao.findUnabgeschlossenenSchritt(vorgangId)

    suspend fun holeSchrittIds(vorgangId: Long): List<Long> = schrittDao.holeSchrittIds(vorgangId)

    /**
     * Legt den naechsten Schritt an. Die Nummer ist immer `MAX + 1` und wird nie
     * wiederverwendet, damit die Korrelation zum physischen Ablageort haelt.
     */
    suspend fun schrittAnlegen(vorgangId: Long): Schritt = beruehrend(vorgangId) {
        val schritt = Schritt(
            reparaturvorgangId = vorgangId,
            schrittNummer = schrittDao.holeNaechsteSchrittNummer(vorgangId),
            gestartetAm = uhr()
        )
        schritt.copy(id = schrittDao.einfuegen(schritt))
    }

    suspend fun schrittAbschliessen(schrittId: Long) {
        val vorgangId = schrittDao.findeVorgangId(schrittId)
        beruehrend(vorgangId) {
            schrittDao.setzeAbschluss(schrittId, uhr().toEpochMilli())
        }
    }

    /**
     * Verwirft einen Schritt vollstaendig. Wird beim "Beenden" eines offenen Schritts
     * ohne Fotos benutzt: sonst bliebe ein leeres Thumbnail zurueck und eine
     * Schrittnummer waere verbrannt.
     */
    suspend fun schrittVerwerfen(schrittId: Long) {
        val vorgangId = schrittDao.findeVorgangId(schrittId)
        beruehrend(vorgangId) { schrittDao.loeschen(schrittId) }
    }

    suspend fun setzeEingebaut(schrittId: Long, eingebaut: Boolean) {
        val vorgangId = schrittDao.findeVorgangId(schrittId)
        beruehrend(vorgangId) { schrittDao.setzeEingebaut(schrittId, eingebaut) }
    }

    // ------------------------------------------------------------------
    // Fotos
    // ------------------------------------------------------------------

    fun beobachteFotos(schrittId: Long): Flow<List<SchrittFoto>> =
        fotoDao.beobachteFotos(schrittId)

    suspend fun holeFotos(schrittId: Long): List<SchrittFoto> = fotoDao.holeFotos(schrittId)

    suspend fun findFoto(fotoId: Long): SchrittFoto? = fotoDao.findById(fotoId)

    suspend fun holeAlleFotoPfade(): List<String> = fotoDao.holeAllePfade()

    suspend fun holeAlleFahrzeugFotoPfade(): List<String> = vorgangDao.holeAlleFahrzeugFotoPfade()

    /**
     * Haengt ein Foto an das Ende des Schritts. Das Default-Label ist Bauteil.
     */
    suspend fun fotoAnhaengen(schrittId: Long, pfad: String): SchrittFoto {
        val vorgangId = schrittDao.findeVorgangId(schrittId)
        return beruehrend(vorgangId) {
            val foto = SchrittFoto(
                schrittId = schrittId,
                pfad = pfad,
                reihenfolge = fotoDao.naechsteReihenfolge(schrittId),
                aufgenommenAm = uhr()
            )
            foto.copy(id = fotoDao.einfuegen(foto))
        }
    }

    /**
     * Loescht ein Foto und schliesst die Luecke in der Reihenfolge.
     *
     * Bewusst **ohne** Dateiloeschung: die Datei raeumt der Aufrufer weg, und beim
     * Wiederholen erst nach bestaetigter Neuaufnahme (Governance: ein Kamera-Abbruch
     * darf nie ein vorhandenes Foto vernichten).
     */
    suspend fun fotoLoeschen(fotoId: Long) {
        val foto = fotoDao.findById(fotoId) ?: return
        val vorgangId = fotoDao.findeVorgangId(fotoId)
        beruehrend(vorgangId) {
            fotoDao.loeschen(fotoId)
            fotoDao.rueckeNach(foto.schrittId, foto.reihenfolge)
        }
    }

    /**
     * Setzt die drei Label eines Fotos. Alle drei abgewaehlt ist erlaubt.
     */
    suspend fun setzeLabel(
        fotoId: Long,
        istBauteil: Boolean,
        istUebersicht: Boolean,
        istAblageort: Boolean
    ) {
        val vorgangId = fotoDao.findeVorgangId(fotoId)
        beruehrend(vorgangId) {
            fotoDao.setzeLabel(fotoId, istBauteil, istUebersicht, istAblageort)
        }
    }

    /**
     * Ersetzt ein Foto an derselben Position: das neue Foto uebernimmt die
     * `reihenfolge` des alten, danach verschwindet der alte Datensatz.
     *
     * Reihenfolge ist Absicht -- erst das neue Foto sichern, dann das alte loeschen.
     */
    suspend fun fotoErsetzen(altesFotoId: Long, neuerPfad: String): SchrittFoto? {
        val altes = fotoDao.findById(altesFotoId) ?: return null
        val vorgangId = fotoDao.findeVorgangId(altesFotoId)
        return beruehrend(vorgangId) {
            val neues = SchrittFoto(
                schrittId = altes.schrittId,
                pfad = neuerPfad,
                reihenfolge = altes.reihenfolge,
                istBauteil = altes.istBauteil,
                istUebersicht = altes.istUebersicht,
                istAblageort = altes.istAblageort,
                aufgenommenAm = uhr()
            )
            val id = fotoDao.einfuegen(neues)
            fotoDao.loeschen(altesFotoId)
            neues.copy(id = id)
        }
    }
}
