package com.boltmind.app.testutil

import com.boltmind.app.data.local.ReparaturvorgangDao
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeReparaturvorgangDao : ReparaturvorgangDao {

    override suspend fun insert(vorgang: Reparaturvorgang): Long = 0L

    override suspend fun delete(vorgang: Reparaturvorgang) {}

    override suspend fun getById(id: Long): Reparaturvorgang? = null

    override fun getAllByStatusMitAnzahl(
        status: VorgangStatus
    ): Flow<List<ReparaturvorgangMitSchrittanzahl>> = flowOf(emptyList())
}
