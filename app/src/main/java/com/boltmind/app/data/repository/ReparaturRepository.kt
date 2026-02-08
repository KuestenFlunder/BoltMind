package com.boltmind.app.data.repository

import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow

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

    suspend fun erstelleSchritt(schritt: Schritt): Long =
        schrittDao.einfuegen(schritt)

    suspend fun aktualisiereSchritt(schritt: Schritt) =
        schrittDao.aktualisieren(schritt)

    suspend fun holeBelegteAblageorte(vorgangId: Long): List<Int> =
        schrittDao.holeBelegteAblageorte(vorgangId)
}
