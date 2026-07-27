package com.boltmind.app.service.zeiterfassung

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Eine einzelne Zeitmessung.
 *
 * Die Dauer wird nicht gespeichert, sondern aus den Timestamps abgeleitet. Eine
 * Referenz kann mehrere Messungen haben: jedes Start/Stopp-Paar ist eine Zeile,
 * die Gesamtdauer ist ihre Summe. Damit funktioniert Pausieren und Fortsetzen ohne
 * ein zusaetzliches Feld.
 *
 * Bewusst **kein** Foreign Key: der Service kennt die Consumer-Tabellen nicht
 * (Governance: Service-Architektur, Abhaengigkeitsrichtung Consumer -> Modul).
 *
 * Spec: docs/specs/F-005-zeiterfassung/service.md, Abschnitt "Entity-Definition"
 */
@Entity(
    tableName = "zeit_messung",
    indices = [Index("referenzTyp", "referenzId")]
)
data class ZeitMessung(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Primaerschluessel der gemessenen Entity, z.B. `Schritt.id`. */
    val referenzId: Long,

    /** Typ der Referenz, definiert vom Consumer. Siehe [ReferenzTyp]. */
    val referenzTyp: String,

    val gestartetAm: Instant,

    /** `null` = die Messung laeuft noch. */
    val gestopptAm: Instant? = null
)

/**
 * Die im Projekt vergebenen Werte fuer [ZeitMessung.referenzTyp].
 *
 * Bewusst Konstanten statt Enum: der Service schreibt keinem Consumer einen Wert
 * vor, die Werte gehoeren den Consumern.
 */
object ReferenzTyp {
    const val DEMONTAGE_SCHRITT = "DEMONTAGE_SCHRITT"
    const val MONTAGE_SCHRITT = "MONTAGE_SCHRITT"
}
