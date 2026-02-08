package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReparaturvorgangDao {

    @Insert
    suspend fun insert(vorgang: Reparaturvorgang): Long

    @Delete
    suspend fun delete(vorgang: Reparaturvorgang)

    @Query("SELECT * FROM reparaturvorgang WHERE id = :id")
    suspend fun getById(id: Long): Reparaturvorgang?

    @Query(
        """
        SELECT r.*, COUNT(s.id) as schrittAnzahl
        FROM reparaturvorgang r
        LEFT JOIN schritt s ON r.id = s.reparaturvorgangId
        WHERE r.status = :status
        GROUP BY r.id
        ORDER BY r.erstelltAm DESC
        """
    )
    fun getAllByStatusMitAnzahl(status: VorgangStatus): Flow<List<ReparaturvorgangMitSchrittanzahl>>
}
