package com.boltmind.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Ein einzelnes Foto eines [Schritt]s. Ein Schritt haelt 0..n Fotos.
 *
 * Die drei Label sind unabhaengig voneinander und frei kombinierbar. Ein frisch
 * aufgenommenes Foto ist per Default "Bauteil". Alle drei abgewaehlt ist ein
 * gueltiger Zustand.
 *
 * Spec: docs/specs/F-003-demontage/README.md, Abschnitt "Entity-Definitionen"
 */
@Entity(
    tableName = "schritt_foto",
    foreignKeys = [
        ForeignKey(
            entity = Schritt::class,
            parentColumns = ["id"],
            childColumns = ["schrittId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schrittId")]
)
data class SchrittFoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val schrittId: Long,

    /** Pfad zur Datei unterhalb von `photos/`. */
    val pfad: String,

    /** Position im Foto-Karussell des Schritts. 0-basiert, lueckenlos. */
    val reihenfolge: Int,

    val istBauteil: Boolean = true,
    val istUebersicht: Boolean = false,
    val istAblageort: Boolean = false,

    /** Zeitpunkt der Rueckkehr aus der System-Kamera. */
    val aufgenommenAm: Instant
)
