package com.boltmind.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Ein Demontage-Schritt. Haelt selbst **keine** Foto-Pfade -- die Fotos liegen
 * als 0..n [SchrittFoto] in der Tabelle `schritt_foto`.
 *
 * [schrittNummer] ist die Demontage-Nummer und wird nie umnummeriert; sie ist die
 * Korrelation zum physischen Ablageort. Der Montage-Fortschritt ist davon getrennt
 * ([eingebautBeiMontage]).
 *
 * Ein Schritt ohne Fotos ist waehrend der Arbeit gueltig (Kamera abgebrochen oder
 * App unterbrochen). Beim "Beenden" wird ein offener Schritt ohne Fotos verworfen,
 * damit keine Schrittnummer verbrannt wird.
 *
 * Spec: docs/specs/F-003-demontage/README.md, Abschnitt "Entity-Definitionen"
 */
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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val reparaturvorgangId: Long,

    /** Demontage-Nummer, 1-basiert, wird nie umnummeriert. */
    val schrittNummer: Int,

    /** Wird vom Montage-Flow (F-004) gesetzt. */
    val eingebautBeiMontage: Boolean = false,

    /** Zeitpunkt, zu dem der Schritt angelegt wurde. */
    val gestartetAm: Instant,

    /** `null` = der Schritt ist noch offen. */
    val abgeschlossenAm: Instant? = null
)
