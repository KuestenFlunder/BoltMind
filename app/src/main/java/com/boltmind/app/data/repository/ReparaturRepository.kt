package com.boltmind.app.data.repository

import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

open class ReparaturRepository(
    private val reparaturvorgangDao: ReparaturvorgangDao,
    private val schrittDao: SchrittDao
) {

    open fun getAlleVorgaenge(status: VorgangStatus): Flow<List<ReparaturvorgangMitSchrittanzahl>> =
        reparaturvorgangDao.getAllByStatusMitAnzahl(status)

    open suspend fun getVorgangById(id: Long): Reparaturvorgang? =
        reparaturvorgangDao.getById(id)

    open suspend fun vorgangAnlegen(vorgang: Reparaturvorgang): Long =
        reparaturvorgangDao.insert(vorgang)

    open suspend fun vorgangLoeschen(vorgang: Reparaturvorgang) {
        val schritte = schrittDao.getAllByVorgangIdEinmalig(vorgang.id)
        schritte.forEach { schritt ->
            File(schritt.fotoPath).delete()
        }
        reparaturvorgangDao.delete(vorgang)
    }

    open fun getSchrittAnzahl(vorgangId: Long): Flow<Int> =
        schrittDao.getAnzahlByVorgangId(vorgangId)
}
