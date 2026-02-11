package com.boltmind.app.data.repository

import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittTyp
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class ReparaturRepository(
    private val vorgangDao: ReparaturvorgangDao,
    private val schrittDao: SchrittDao
) {

    fun beobachteOffeneVorgaenge(): Flow<List<Reparaturvorgang>> =
        vorgangDao.beobachteNachStatus(VorgangStatus.OFFEN)

    fun beobachteOffeneVorgaengeMitAnzahl(): Flow<List<ReparaturvorgangMitAnzahl>> =
        vorgangDao.beobachteNachStatusMitAnzahl(VorgangStatus.OFFEN)

    fun beobachteArchivierteVorgaenge(): Flow<List<Reparaturvorgang>> =
        vorgangDao.beobachteNachStatus(VorgangStatus.ARCHIVIERT)

    fun beobachteArchivierteVorgaengeMitAnzahl(): Flow<List<ReparaturvorgangMitAnzahl>> =
        vorgangDao.beobachteNachStatusMitAnzahl(VorgangStatus.ARCHIVIERT)

    suspend fun findVorgangById(id: Long): Reparaturvorgang? =
        vorgangDao.findById(id)

    suspend fun erstelleVorgang(vorgang: Reparaturvorgang): Long =
        vorgangDao.einfuegen(vorgang)

    suspend fun aktualisiereVorgang(vorgang: Reparaturvorgang) =
        vorgangDao.aktualisieren(vorgang)

    suspend fun loescheVorgang(id: Long) =
        vorgangDao.loeschen(id)

    suspend fun zaehleSchritte(vorgangId: Long): Int =
        vorgangDao.zaehleSchritte(vorgangId)

    fun beobachteSchritte(vorgangId: Long): Flow<List<Schritt>> =
        schrittDao.beobachteSchritte(vorgangId)

    suspend fun holeSchritte(vorgangId: Long): List<Schritt> =
        schrittDao.holeSchritte(vorgangId)

    suspend fun erstelleSchritt(schritt: Schritt): Long =
        schrittDao.einfuegen(schritt)

    suspend fun aktualisiereSchritt(schritt: Schritt) =
        schrittDao.aktualisieren(schritt)

    // Demontage-Operationen

    suspend fun schrittAnlegen(vorgangId: Long): Schritt {
        val nummer = schrittDao.holeNaechsteSchrittNummer(vorgangId)
        val schritt = Schritt(
            reparaturvorgangId = vorgangId,
            schrittNummer = nummer,
            gestartetAm = Instant.now()
        )
        val id = schrittDao.einfuegen(schritt)
        return schritt.copy(id = id)
    }

    suspend fun bauteilFotoBestaetigen(schrittId: Long, fotoPfad: String) {
        schrittDao.aktualisiereBauteilFoto(schrittId, fotoPfad)
    }

    suspend fun ablageortFotoBestaetigen(schrittId: Long, fotoPfad: String) {
        schrittDao.aktualisiereAblageortFotoUndAbschluss(
            schrittId, fotoPfad, Instant.now().toEpochMilli()
        )
    }

    suspend fun schrittTypSetzen(schrittId: Long, typ: SchrittTyp) {
        schrittDao.aktualisiereTyp(schrittId, typ.name)
    }

    suspend fun schrittAbschliessen(schrittId: Long, typ: SchrittTyp) {
        schrittDao.aktualisiereTypUndAbschluss(
            schrittId, typ.name, Instant.now().toEpochMilli()
        )
    }

    suspend fun findUnabgeschlossenenSchritt(vorgangId: Long): Schritt? {
        return schrittDao.findUnabgeschlossenenSchritt(vorgangId)
    }

}
