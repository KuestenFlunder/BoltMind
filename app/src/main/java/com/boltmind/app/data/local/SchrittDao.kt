package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boltmind.app.data.model.Schritt
import kotlinx.coroutines.flow.Flow

@Dao
interface SchrittDao {

    @Insert
    suspend fun insert(schritt: Schritt): Long

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY reihenfolge")
    fun getAllByVorgangId(vorgangId: Long): Flow<List<Schritt>>

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId")
    suspend fun getAllByVorgangIdEinmalig(vorgangId: Long): List<Schritt>

    @Query("SELECT COUNT(*) FROM schritt WHERE reparaturvorgangId = :vorgangId")
    fun getAnzahlByVorgangId(vorgangId: Long): Flow<Int>
}
