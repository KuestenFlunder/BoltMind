package com.boltmind.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitAnzahl
import com.boltmind.app.data.model.VorgangStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReparaturvorgangDao {

    @Query("SELECT * FROM reparaturvorgang WHERE status = :status ORDER BY aktualisiertAm DESC")
    fun beobachteNachStatus(status: VorgangStatus): Flow<List<Reparaturvorgang>>

    /**
     * Liste inklusive Schrittzahl und gemessener Gesamtdauer -- eine Abfrage statt
     * einer pro Karte.
     *
     * `COUNT(DISTINCT s.id)` ist zwingend: durch den zweiten LEFT JOIN auf
     * `zeit_messung` erscheint ein Schritt mit n Messungen n-mal in der Ergebnismenge.
     * Ein einfaches `COUNT(s.id)` wuerde die Schritte dann mehrfach zaehlen.
     *
     * Die Richtung ist erlaubt: der Consumer (F-001) kennt die Service-Tabelle,
     * nicht umgekehrt.
     *
     * Gezaehlt werden nur **abgeschlossene** Messungen. Eine laufende haette in
     * einer Liste keine sinnvolle Dauer: sie waechst, waehrend man hinschaut.
     * Frueher stand hier ein Zeitpunkt-Parameter -- der wurde beim Erzeugen des
     * Flows einmal ausgewertet und blieb dann eingefroren, sodass die Dauer nie
     * wuchs und nach einer langen Sitzung systematisch zu klein war.
     */
    @Query(
        """
        SELECT r.*,
               COUNT(DISTINCT s.id) AS schrittAnzahl,
               COALESCE(SUM(z.gestopptAm - z.gestartetAm), 0) AS dauerMillis
        FROM reparaturvorgang r
        LEFT JOIN schritt s ON r.id = s.reparaturvorgangId
        LEFT JOIN zeit_messung z ON z.referenzId = s.id
             AND z.referenzTyp IN ('DEMONTAGE_SCHRITT', 'MONTAGE_SCHRITT')
             AND z.gestopptAm IS NOT NULL
        WHERE r.status = :status
        GROUP BY r.id
        ORDER BY r.aktualisiertAm DESC
        """
    )
    fun beobachteNachStatusMitAnzahl(status: VorgangStatus): Flow<List<ReparaturvorgangMitAnzahl>>

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

    @Query("UPDATE reparaturvorgang SET status = :status, aktualisiertAm = :aktualisiertAm WHERE id = :id")
    suspend fun setzeStatus(id: Long, status: VorgangStatus, aktualisiertAm: Long)

    /**
     * Setzt [Reparaturvorgang.aktualisiertAm]. Wird vom Transaktions-Helper des
     * Repositories nach **jeder** datenveraendernden Aktion aufgerufen -- die
     * Uebersicht sortiert beide Listen danach.
     */
    @Query("UPDATE reparaturvorgang SET aktualisiertAm = :aktualisiertAm WHERE id = :id")
    suspend fun beruehre(id: Long, aktualisiertAm: Long)

    /** Alle Fahrzeugfoto-Pfade -- Grundlage fuer den Cleanup verwaister Dateien. */
    @Query("SELECT fahrzeugFotoPfad FROM reparaturvorgang WHERE fahrzeugFotoPfad IS NOT NULL")
    suspend fun holeAlleFahrzeugFotoPfade(): List<String>
}
