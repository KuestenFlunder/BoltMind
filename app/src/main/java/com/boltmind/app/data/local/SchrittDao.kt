package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.boltmind.app.data.model.Schritt
import kotlinx.coroutines.flow.Flow

@Dao
interface SchrittDao {

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer ASC")
    fun beobachteSchritte(vorgangId: Long): Flow<List<Schritt>>

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer DESC")
    fun beobachteSchritteAbsteigend(vorgangId: Long): Flow<List<Schritt>>

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer ASC")
    suspend fun holeSchritte(vorgangId: Long): List<Schritt>

    @Insert
    suspend fun einfuegen(schritt: Schritt): Long

    @Update
    suspend fun aktualisieren(schritt: Schritt)

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId AND abgeschlossenAm IS NULL LIMIT 1")
    suspend fun findUnabgeschlossenenSchritt(vorgangId: Long): Schritt?

    @Query("SELECT COALESCE(MAX(schrittNummer), 0) + 1 FROM schritt WHERE reparaturvorgangId = :vorgangId")
    suspend fun holeNaechsteSchrittNummer(vorgangId: Long): Int

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer DESC LIMIT 1")
    suspend fun holeLetztenSchritt(vorgangId: Long): Schritt?

    @Query("UPDATE schritt SET bauteilFotoPfad = :fotoPfad WHERE id = :schrittId")
    suspend fun aktualisiereBauteilFoto(schrittId: Long, fotoPfad: String)

    @Query("UPDATE schritt SET ablageortFotoPfad = :fotoPfad, abgeschlossenAm = :abgeschlossenAm WHERE id = :schrittId")
    suspend fun aktualisiereAblageortFotoUndAbschluss(schrittId: Long, fotoPfad: String, abgeschlossenAm: Long)

    @Query("UPDATE schritt SET typ = :typ WHERE id = :schrittId")
    suspend fun aktualisiereTyp(schrittId: Long, typ: String)

    @Query("UPDATE schritt SET typ = :typ, abgeschlossenAm = :abgeschlossenAm WHERE id = :schrittId")
    suspend fun aktualisiereTypUndAbschluss(schrittId: Long, typ: String, abgeschlossenAm: Long)
}
