package com.boltmind.app.feature.uebersicht

import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.testutil.FakeReparaturvorgangDao
import com.boltmind.app.testutil.FakeSchrittDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeReparaturRepository : ReparaturRepository(
    reparaturvorgangDao = FakeReparaturvorgangDao(),
    schrittDao = FakeSchrittDao()
) {

    val offeneVorgaengeFlow =
        MutableStateFlow<List<ReparaturvorgangMitSchrittanzahl>>(emptyList())
    val archivierteVorgaengeFlow =
        MutableStateFlow<List<ReparaturvorgangMitSchrittanzahl>>(emptyList())

    var zuletztGeloeschterVorgang: Reparaturvorgang? = null
        private set

    override fun getAlleVorgaenge(status: VorgangStatus): Flow<List<ReparaturvorgangMitSchrittanzahl>> {
        return when (status) {
            VorgangStatus.OFFEN -> offeneVorgaengeFlow
            VorgangStatus.ARCHIVIERT -> archivierteVorgaengeFlow
        }
    }

    override suspend fun vorgangLoeschen(vorgang: Reparaturvorgang) {
        zuletztGeloeschterVorgang = vorgang
        val currentList = offeneVorgaengeFlow.value.toMutableList()
        currentList.removeAll { it.vorgang.id == vorgang.id }
        offeneVorgaengeFlow.value = currentList
    }
}
