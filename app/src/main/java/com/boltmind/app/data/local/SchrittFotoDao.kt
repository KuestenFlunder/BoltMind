package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.boltmind.app.data.model.SchrittFoto
import kotlinx.coroutines.flow.Flow

@Dao
interface SchrittFotoDao {

    @Query("SELECT * FROM schritt_foto WHERE schrittId = :schrittId ORDER BY reihenfolge ASC")
    fun beobachteFotos(schrittId: Long): Flow<List<SchrittFoto>>

    @Query("SELECT * FROM schritt_foto WHERE schrittId = :schrittId ORDER BY reihenfolge ASC")
    suspend fun holeFotos(schrittId: Long): List<SchrittFoto>

    @Query("SELECT * FROM schritt_foto WHERE id = :fotoId")
    suspend fun findById(fotoId: Long): SchrittFoto?

    @Query("SELECT COUNT(*) FROM schritt_foto WHERE schrittId = :schrittId")
    suspend fun zaehleFotos(schrittId: Long): Int

    /** Alle Pfade ueber alle Schritte -- Grundlage fuer den Cleanup verwaister Dateien. */
    @Query("SELECT pfad FROM schritt_foto")
    suspend fun holeAllePfade(): List<String>

    /**
     * Der Vorgang, zu dem ein Foto gehoert -- in einer Abfrage statt zweier.
     * Wird gebraucht, um nach jeder Foto-Aenderung `Reparaturvorgang.aktualisiertAm`
     * nachzuziehen.
     */
    @Query(
        """
        SELECT s.reparaturvorgangId FROM schritt s
        JOIN schritt_foto f ON f.schrittId = s.id
        WHERE f.id = :fotoId
        """
    )
    suspend fun findeVorgangId(fotoId: Long): Long?

    /** Naechste freie Position innerhalb eines Schritts (0-basiert). */
    @Query("SELECT COALESCE(MAX(reihenfolge), -1) + 1 FROM schritt_foto WHERE schrittId = :schrittId")
    suspend fun naechsteReihenfolge(schrittId: Long): Int

    @Insert
    suspend fun einfuegen(foto: SchrittFoto): Long

    @Update
    suspend fun aktualisieren(foto: SchrittFoto)

    @Query("DELETE FROM schritt_foto WHERE id = :fotoId")
    suspend fun loeschen(fotoId: Long)

    @Query(
        """
        UPDATE schritt_foto
        SET istBauteil = :istBauteil, istUebersicht = :istUebersicht, istAblageort = :istAblageort
        WHERE id = :fotoId
        """
    )
    suspend fun setzeLabel(
        fotoId: Long,
        istBauteil: Boolean,
        istUebersicht: Boolean,
        istAblageort: Boolean
    )

    @Query("UPDATE schritt_foto SET reihenfolge = :reihenfolge WHERE id = :fotoId")
    suspend fun setzeReihenfolge(fotoId: Long, reihenfolge: Int)

    /**
     * Schliesst die Luecke, die nach dem Loeschen eines Fotos entsteht:
     * alle Fotos hinter [abReihenfolge] ruecken eine Position auf.
     * Haelt die Reihenfolge 0-basiert und lueckenlos.
     */
    @Query(
        """
        UPDATE schritt_foto SET reihenfolge = reihenfolge - 1
        WHERE schrittId = :schrittId AND reihenfolge > :abReihenfolge
        """
    )
    suspend fun rueckeNach(schrittId: Long, abReihenfolge: Int)
}
