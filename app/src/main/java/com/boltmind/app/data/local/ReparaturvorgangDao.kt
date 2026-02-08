package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReparaturvorgangDao {

    @Query("SELECT * FROM reparaturvorgang WHERE status = :status ORDER BY aktualisiertAm DESC")
    fun beobachteNachStatus(status: VorgangStatus): Flow<List<Reparaturvorgang>>

    @Query("SELECT * FROM reparaturvorgang WHERE id = :id")
    suspend fun findById(id: Long): Reparaturvorgang?

    @Insert
    suspend fun einfuegen(vorgang: Reparaturvorgang): Long

    @Update
    suspend fun aktualisieren(vorgang: Reparaturvorgang)

    @Query("DELETE FROM reparaturvorgang WHERE id = :id")
    suspend fun loeschen(id: Long)

    @Query("SELECT COUNT(*) FROM schritt WHERE reparaturvorgangId = :vorgangId")
    suspend fun zaehleSchritte(vorgangId: Long): Int
}
