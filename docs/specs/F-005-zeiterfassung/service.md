# Timer-Service: Interface + Entity

## Entity-Definition

### ZeitMessung (Room Entity)

```kotlin
@Entity(tableName = "zeit_messung")
data class ZeitMessung(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val referenzId: Long,              // ID der gemessenen Entity (z.B. Schritt.id)
    val referenzTyp: String,           // Typ der Referenz (z.B. "DEMONTAGE_SCHRITT", "MONTAGE_SCHRITT")

    val gestartetAm: Instant,          // Zeitpunkt des Timer-Starts
    val gestopptAm: Instant? = null    // Zeitpunkt des Timer-Stopps (null = laeuft noch)
)
```

**Berechnete Dauer:** `gestopptAm - gestartetAm`. Wird nicht separat gespeichert — eine Duration ist immer ableitbar aus den Timestamps.

**referenzTyp:** Freitext-String, definiert vom Consumer. Der Service interpretiert den Typ nicht, er speichert ihn nur. Beispielwerte:
- `"DEMONTAGE_SCHRITT"` — gesetzt von F-003
- `"MONTAGE_SCHRITT"` — gesetzt von F-004 (spaeter)

**referenzId:** Primaerschluessel der referenzierten Entity. Kein Foreign Key Constraint in der DB (der Service kennt die Consumer-Tabellen nicht).

## Service-Interface

```kotlin
class ZeiterfassungService(private val dao: ZeitMessungDao) {

    /**
     * Startet eine neue Zeitmessung.
     * @param referenzId ID der zu messenden Entity
     * @param referenzTyp Typ-String zur Kategorisierung
     * @return ID der angelegten ZeitMessung
     */
    suspend fun starten(referenzId: Long, referenzTyp: String): Long

    /**
     * Stoppt eine laufende Zeitmessung.
     * @param messungId ID der ZeitMessung (von starten() zurueckgegeben)
     * @return Duration zwischen Start und Stopp
     * @throws IllegalStateException wenn Messung bereits gestoppt
     */
    suspend fun stoppen(messungId: Long): Duration

    /**
     * Gibt die Dauer einer abgeschlossenen Messung zurueck.
     * @return Duration oder null wenn noch nicht gestoppt
     */
    suspend fun dauer(messungId: Long): Duration?

    /**
     * Gibt alle Messungen fuer eine Referenz zurueck.
     * Nützlich fuer Gesamtdauer-Berechnung durch Consumer.
     */
    fun messungenFuer(referenzId: Long, referenzTyp: String): Flow<List<ZeitMessung>>
}
```

## DAO

```kotlin
@Dao
interface ZeitMessungDao {

    @Insert
    suspend fun einfuegen(messung: ZeitMessung): Long

    @Query("UPDATE zeit_messung SET gestopptAm = :zeitpunkt WHERE id = :id")
    suspend fun stoppen(id: Long, zeitpunkt: Instant)

    @Query("SELECT * FROM zeit_messung WHERE id = :id")
    suspend fun findById(id: Long): ZeitMessung?

    @Query("SELECT * FROM zeit_messung WHERE referenzId = :refId AND referenzTyp = :refTyp")
    fun findByReferenz(refId: Long, refTyp: String): Flow<List<ZeitMessung>>
}
```

## Lifecycle

### Normaler Ablauf

```
Consumer ruft starten(referenzId, referenzTyp)
  → ZeitMessung wird in DB angelegt (gestartetAm = now, gestopptAm = null)
  → ID wird an Consumer zurueckgegeben

Consumer ruft stoppen(messungId)
  → gestopptAm wird gesetzt (now)
  → Duration wird berechnet und zurueckgegeben
```

### App-Unterbrechung

| Situation | Zustand in DB | Verhalten |
|---|---|---|
| Timer laeuft, App wird geschlossen | `ZeitMessung` mit `gestopptAm = null` | Timer laeuft konzeptionell weiter (Mechaniker arbeitet physisch) |
| Timer laeuft, App wird neu gestartet | `ZeitMessung` mit `gestopptAm = null` | Consumer entscheidet: Fortsetzen oder Stoppen |
| Timer gestoppt, App wird geschlossen | `ZeitMessung` mit `gestopptAm` gesetzt | Keine Aktion noetig, Daten sind persistiert |

**Wichtig:** Der Service selbst macht kein Cleanup. Offene Messungen (`gestopptAm = null`) bleiben bestehen. Der Consumer ist verantwortlich dafuer, offene Messungen beim Fortsetzen zu behandeln.

### Mehrere gleichzeitige Messungen

Der Service unterstuetzt mehrere gleichzeitige Messungen. Jeder `starten()`-Aufruf erzeugt eine neue `ZeitMessung`. Es gibt keinen globalen Singleton-Timer.

**MVP-Einschraenkung:** Pro `referenzId` + `referenzTyp` sollte maximal eine offene Messung existieren. Dies wird nicht vom Service erzwungen, sondern ist Consumer-Verantwortung.

## Nicht-funktionale Anforderungen

- **Sofort-Save:** Start und Stopp werden sofort in die DB geschrieben (Governance: Sofort-Save Strategie)
- **Kein UI:** Der Service hat keine eigene UI. Timer-Anzeige ist Consumer-Verantwortung
- **Sekunden-Genauigkeit:** `Instant`-basierte Timestamps. Fuer die Anzeige reichen Sekunden
- **Keine manuelle Interaktion:** Der Mechaniker startet/stoppt den Timer nie direkt. Consumer-Code steuert den Lifecycle automatisch

## Typische Consumer-Integration

### Beispiel: F-003 Demontage (Arbeitsphase-Screen)

```
Arbeitsphase-Screen wird betreten:
  → Consumer ruft zeiterfassungService.starten(schritt.id, "DEMONTAGE_SCHRITT")
  → Consumer merkt sich messungId im ViewModel

"Ausgebaut"-Button wird getippt:
  → Consumer ruft zeiterfassungService.stoppen(messungId)
  → Duration wird zurueckgegeben (Consumer kann sie anzeigen oder ignorieren)
```

### Beispiel: F-001 Uebersicht (Gesamtdauer)

```
Uebersicht laedt Vorgang:
  → Repository liest alle ZeitMessungen fuer den Vorgang
  → Query: SELECT * FROM zeit_messung WHERE referenzTyp = "DEMONTAGE_SCHRITT"
           AND referenzId IN (SELECT id FROM schritt WHERE reparaturvorgangId = :vorgangId)
  → Summiert alle Dauern → zeigt Gesamtdauer an
```

**Hinweis:** F-001 liest nur Daten. Es gibt keine Abhaengigkeit auf den Timer-Service selbst, nur auf die Tabelle `zeit_messung`.
