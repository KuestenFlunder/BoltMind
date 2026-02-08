package com.boltmind.app.testutil

import com.boltmind.app.data.local.SchrittDao
import com.boltmind.app.data.model.Schritt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSchrittDao : SchrittDao {

    override suspend fun insert(schritt: Schritt): Long = 0L

    override fun getAllByVorgangId(vorgangId: Long): Flow<List<Schritt>> = flowOf(emptyList())

    override suspend fun getAllByVorgangIdEinmalig(vorgangId: Long): List<Schritt> = emptyList()

    override fun getAnzahlByVorgangId(vorgangId: Long): Flow<Int> = flowOf(0)

    override suspend fun getLetzerByVorgangId(vorgangId: Long): Schritt? = null

    override suspend fun getAnzahlByVorgangIdEinmalig(vorgangId: Long): Int = 0
}
