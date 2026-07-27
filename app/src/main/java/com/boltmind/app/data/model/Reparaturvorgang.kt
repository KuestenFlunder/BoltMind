package com.boltmind.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "reparaturvorgang")
data class Reparaturvorgang(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fahrzeugFotoPfad: String? = null,
    val auftragsnummer: String,
    val beschreibung: String? = null,
    val status: VorgangStatus = VorgangStatus.OFFEN,
    /**
     * Beide Zeitstempel setzt das Repository aus seiner injizierten Uhr.
     * Ein Default `Instant.now()` waere eine zweite, nicht stellbare Zeitquelle
     * fuer dieselbe Zeile -- in Tests nicht deterministisch, und `erstelltAm`
     * koennte je nach Aufrufer minimal nach `aktualisiertAm` liegen.
     *
     * Der Default ist deshalb bewusst [Instant.EPOCH] und kein `now()`: er ist
     * erkennbar ein Platzhalter. Wer einen Vorgang ueber das Repository anlegt,
     * bekommt beide Werte gesetzt; taucht in der Datenbank je eine EPOCH-Zeile
     * auf, ist jemand am Repository vorbeigegangen.
     */
    val erstelltAm: Instant = Instant.EPOCH,
    val aktualisiertAm: Instant = Instant.EPOCH
)
