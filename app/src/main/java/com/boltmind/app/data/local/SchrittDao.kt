package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.boltmind.app.data.model.Schritt
import kotlinx.coroutines.flow.Flow

@Dao
interface SchrittDao {

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY reihenfolge ASC")
    fun beobachteSchritte(vorgangId: Long): Flow<List<Schritt>>

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY reihenfolge DESC")
    fun beobachteSchritteAbsteigend(vorgangId: Long): Flow<List<Schritt>>

    @Insert
    suspend fun einfuegen(schritt: Schritt): Long

    @Update
    suspend fun aktualisieren(schritt: Schritt)

    @Query("SELECT ablageortNummer FROM schritt WHERE reparaturvorgangId = :vorgangId")
    suspend fun holeBelegteAblageorte(vorgangId: Long): List<Int>
}
