package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.boltmind.app.data.model.Schritt
import com.boltmind.app.data.model.SchrittMitFotos
import kotlinx.coroutines.flow.Flow

@Dao
interface SchrittDao {

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer ASC")
    fun beobachteSchritte(vorgangId: Long): Flow<List<Schritt>>

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer ASC")
    suspend fun holeSchritte(vorgangId: Long): List<Schritt>

    @Query("SELECT * FROM schritt WHERE id = :schrittId")
    suspend fun findById(schrittId: Long): Schritt?

    /**
     * Die Eingabestruktur des Schritt-Browsers: alle Schritte eines Vorgangs mit
     * ihren Fotos, aufsteigend nach Schrittnummer.
     *
     * Room sortiert `@Relation`-Listen nicht, deshalb sortiert der Consumer die
     * Fotos ueber [SchrittMitFotos.sortiert].
     */
    @Transaction
    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer ASC")
    fun beobachteSchritteMitFotos(vorgangId: Long): Flow<List<SchrittMitFotos>>

    @Transaction
    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer ASC")
    suspend fun holeSchritteMitFotos(vorgangId: Long): List<SchrittMitFotos>

    @Insert
    suspend fun einfuegen(schritt: Schritt): Long

    @Update
    suspend fun aktualisieren(schritt: Schritt)

    @Query("DELETE FROM schritt WHERE id = :schrittId")
    suspend fun loeschen(schrittId: Long)

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId AND abgeschlossenAm IS NULL ORDER BY schrittNummer ASC LIMIT 1")
    suspend fun findUnabgeschlossenenSchritt(vorgangId: Long): Schritt?

    @Query("SELECT COALESCE(MAX(schrittNummer), 0) + 1 FROM schritt WHERE reparaturvorgangId = :vorgangId")
    suspend fun holeNaechsteSchrittNummer(vorgangId: Long): Int

    @Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer DESC LIMIT 1")
    suspend fun holeLetztenSchritt(vorgangId: Long): Schritt?

    @Query("UPDATE schritt SET abgeschlossenAm = :abgeschlossenAm WHERE id = :schrittId")
    suspend fun setzeAbschluss(schrittId: Long, abgeschlossenAm: Long?)

    @Query("UPDATE schritt SET eingebautBeiMontage = :eingebaut WHERE id = :schrittId")
    suspend fun setzeEingebaut(schrittId: Long, eingebaut: Boolean)

    @Query("SELECT COUNT(*) FROM schritt WHERE reparaturvorgangId = :vorgangId AND eingebautBeiMontage = 1")
    suspend fun zaehleEingebaute(vorgangId: Long): Int

    /** IDs aller Schritte eines Vorgangs -- Eingabe fuer die Zeit-Summenbildung. */
    @Query("SELECT id FROM schritt WHERE reparaturvorgangId = :vorgangId")
    suspend fun holeSchrittIds(vorgangId: Long): List<Long>

    /**
     * Der Vorgang, zu dem ein Schritt gehoert. Wird gebraucht, um nach jeder Aenderung
     * an einem Schritt `Reparaturvorgang.aktualisiertAm` nachzuziehen.
     */
    @Query("SELECT reparaturvorgangId FROM schritt WHERE id = :schrittId")
    suspend fun findeVorgangId(schrittId: Long): Long?
}
