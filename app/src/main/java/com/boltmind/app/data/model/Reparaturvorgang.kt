package com.boltmind.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "reparaturvorgang")
data class Reparaturvorgang(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fahrzeug: String,
    val auftragsnummer: String,
    val beschreibung: String,
    val anzahlAblageorte: Int,
    val status: VorgangStatus,
    val erstelltAm: Instant
)
