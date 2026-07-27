package com.boltmind.app.service.zeiterfassung

import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant

/**
 * Misst Zeitspannen fuer beliebige Referenz-Entities.
 *
 * Der Service kennt seine Consumer nicht: er interpretiert [ZeitMessung.referenzTyp]
 * nicht und haelt keine Fremdschluessel. Wann eine Messung startet oder stoppt,
 * entscheidet allein der Consumer.
 *
 * Pausieren und Fortsetzen entsteht dadurch, dass eine Referenz mehrere Messungen
 * haben darf. Die Gesamtdauer ist die Summe -- es gibt kein Feld "aufgelaufene Zeit",
 * das man konsistent halten muesste.
 *
 * Spec: docs/specs/F-005-zeiterfassung/service.md
 */
class ZeiterfassungService(
    private val dao: ZeitMessungDao,
    private val uhr: () -> Instant = Instant::now
) {

    /** Startet eine neue Messung und liefert deren ID (US-005.1). */
    suspend fun starten(referenzId: Long, referenzTyp: String): Long =
        dao.einfuegen(
            ZeitMessung(
                referenzId = referenzId,
                referenzTyp = referenzTyp,
                gestartetAm = uhr()
            )
        )

    /**
     * Stoppt eine laufende Messung und liefert ihre Dauer (US-005.2).
     *
     * @throws IllegalStateException wenn die Messung unbekannt oder bereits
     *   gestoppt ist (US-005.6). Ein zweiter Stopp ist ein Programmierfehler und
     *   soll nicht stillschweigend die alte Dauer zurueckgeben.
     */
    suspend fun stoppen(messungId: Long): Duration {
        val jetzt = uhr()
        val geaendert = dao.stoppen(messungId, jetzt)
        if (geaendert == 0) {
            val vorhanden = dao.findById(messungId)
            throw IllegalStateException(
                if (vorhanden == null) "Zeitmessung $messungId ist unbekannt."
                else "Zeitmessung $messungId wurde bereits um ${vorhanden.gestopptAm} gestoppt."
            )
        }
        val messung = dao.findById(messungId)
            ?: throw IllegalStateException("Zeitmessung $messungId ist unbekannt.")
        return Duration.between(messung.gestartetAm, messung.gestopptAm ?: jetzt)
    }

    /** Dauer einer abgeschlossenen Messung, oder `null` wenn sie noch laeuft (US-005.3). */
    suspend fun dauer(messungId: Long): Duration? {
        val messung = dao.findById(messungId) ?: return null
        val ende = messung.gestopptAm ?: return null
        return Duration.between(messung.gestartetAm, ende)
    }

    /** Alle Messungen einer Referenz (US-005.4). */
    fun messungenFuer(referenzId: Long, referenzTyp: String): Flow<List<ZeitMessung>> =
        dao.findByReferenz(referenzId, referenzTyp)

    // --- Bequemlichkeit fuer Consumer, die einen Start/Pause-Schalter anbieten ---

    /** Die offene Messung einer Referenz, falls gerade eine laeuft. */
    suspend fun laufendeMessung(referenzId: Long, referenzTyp: String): ZeitMessung? =
        dao.findeOffene(referenzId, referenzTyp)

    suspend fun laeuft(referenzId: Long, referenzTyp: String): Boolean =
        dao.findeOffene(referenzId, referenzTyp) != null

    /**
     * Schaltet die Messung einer Referenz um und liefert den neuen Zustand:
     * `true` = laeuft jetzt, `false` = steht jetzt.
     */
    suspend fun umschalten(referenzId: Long, referenzTyp: String): Boolean {
        val offen = dao.findeOffene(referenzId, referenzTyp)
        return if (offen == null) {
            starten(referenzId, referenzTyp)
            true
        } else {
            stoppen(offen.id)
            false
        }
    }

    /**
     * Stoppt eine eventuell laufende Messung dieser Referenz. Tut nichts, wenn keine
     * laeuft -- anders als [stoppen] ist das hier kein Fehler, sondern der uebliche
     * Aufruf beim Verlassen eines Schritts.
     */
    suspend fun stoppeFallsLaeuft(referenzId: Long, referenzTyp: String): Duration? {
        val offen = dao.findeOffene(referenzId, referenzTyp) ?: return null
        return stoppen(offen.id)
    }

    /** Stoppt alle offenen Messungen. Wird beim Verlassen eines Vorgangs benutzt. */
    suspend fun stoppeAlleOffenen() {
        dao.holeAlleOffenen().forEach { runCatching { stoppen(it.id) } }
    }

    /**
     * Summe aller Messungen einer Referenz. Eine noch laufende Messung wird bis jetzt
     * gerechnet.
     */
    suspend fun gesamtdauer(referenzId: Long, referenzTyp: String): Duration =
        Duration.ofMillis(dao.summeMillis(referenzId, referenzTyp, uhr().toEpochMilli()))
}
