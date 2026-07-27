package com.boltmind.app.service.zeiterfassung

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Zugriff auf die Zeitmessungen. Kennt die Consumer-Tabellen nicht -- alle Abfragen
 * laufen ueber das generische Paar (`referenzTyp`, `referenzId`).
 *
 * Spec: docs/specs/F-005-zeiterfassung/service.md, Abschnitt "DAO"
 */
@Dao
interface ZeitMessungDao {

    @Insert
    suspend fun einfuegen(messung: ZeitMessung): Long

    /**
     * Setzt den Stopp-Zeitpunkt. Die Bedingung `gestopptAm IS NULL` macht den Aufruf
     * idempotent-sicher: ein zweiter Stopp trifft keine Zeile und liefert 0.
     * Darauf stuetzt sich die Fehlererkennung in US-005.6.
     */
    @Query("UPDATE zeit_messung SET gestopptAm = :zeitpunkt WHERE id = :id AND gestopptAm IS NULL")
    suspend fun stoppen(id: Long, zeitpunkt: Instant): Int

    @Query("SELECT * FROM zeit_messung WHERE id = :id")
    suspend fun findById(id: Long): ZeitMessung?

    @Query("SELECT * FROM zeit_messung WHERE referenzId = :refId AND referenzTyp = :refTyp ORDER BY gestartetAm ASC")
    fun findByReferenz(refId: Long, refTyp: String): Flow<List<ZeitMessung>>

    @Query("SELECT * FROM zeit_messung WHERE referenzId = :refId AND referenzTyp = :refTyp ORDER BY gestartetAm ASC")
    suspend fun holeFuerReferenz(refId: Long, refTyp: String): List<ZeitMessung>

    /** Die zuletzt gestartete offene Messung einer Referenz, falls es sie gibt. */
    @Query(
        """
        SELECT * FROM zeit_messung
        WHERE referenzId = :refId AND referenzTyp = :refTyp AND gestopptAm IS NULL
        ORDER BY gestartetAm DESC LIMIT 1
        """
    )
    suspend fun findeOffene(refId: Long, refTyp: String): ZeitMessung?

    /** Alle offenen Messungen -- fuer die Fortsetzung nach App-Unterbrechung (US-005.5). */
    @Query("SELECT * FROM zeit_messung WHERE gestopptAm IS NULL")
    suspend fun holeAlleOffenen(): List<ZeitMessung>

    /**
     * Summe der Dauern einer Referenz in Millisekunden. Eine noch laufende Messung
     * wird bis [jetztMillis] gerechnet.
     */
    @Query(
        """
        SELECT COALESCE(SUM(COALESCE(gestopptAm, :jetztMillis) - gestartetAm), 0)
        FROM zeit_messung
        WHERE referenzId = :refId AND referenzTyp = :refTyp
        """
    )
    suspend fun summeMillis(refId: Long, refTyp: String, jetztMillis: Long): Long

    @Query("DELETE FROM zeit_messung WHERE referenzId = :refId AND referenzTyp = :refTyp")
    suspend fun loescheFuerReferenz(refId: Long, refTyp: String)
}
