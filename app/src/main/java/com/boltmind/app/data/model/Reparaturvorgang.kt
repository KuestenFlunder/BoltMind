package com.boltmind.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "reparaturvorgang")
data class Reparaturvorgang(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fahrzeugFotoPfad: String,
    val auftragsnummer: String,
    val beschreibung: String? = null,
    val status: VorgangStatus = VorgangStatus.OFFEN,
    val erstelltAm: Instant = Instant.now(),
    val aktualisiertAm: Instant = Instant.now()
)
