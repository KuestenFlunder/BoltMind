package com.boltmind.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "schritt",
    foreignKeys = [
        ForeignKey(
            entity = Reparaturvorgang::class,
            parentColumns = ["id"],
            childColumns = ["reparaturvorgangId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reparaturvorgangId")]
)
data class Schritt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reparaturvorgangId: Long,
    val fotoPath: String,
    val ablageortNummer: String,
    val reihenfolge: Int,
    val startedAt: Instant? = null,
    val completedAt: Instant? = null
)
