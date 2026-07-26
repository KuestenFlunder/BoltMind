# Timer-Service: Interface, Entity und User Stories

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

**referenzTyp:** Freitext-String, definiert vom Consumer. Der Service interpretiert den Typ nicht, er speichert ihn nur. Beispielwerte (der Service schreibt keinem Consumer einen Wert vor):
- `"DEMONTAGE_SCHRITT"`
- `"MONTAGE_SCHRITT"`

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
     * @throws IllegalStateException wenn Messung bereits gestoppt oder unbekannt (US-005.6)
     */
    suspend fun stoppen(messungId: Long): Duration

    /**
     * Gibt die Dauer einer abgeschlossenen Messung zurueck.
     * @return Duration, oder null wenn noch nicht gestoppt oder unbekannt (US-005.3)
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

**Wichtig:** Der Service selbst macht kein Cleanup. Offene Messungen (`gestopptAm = null`) bleiben bestehen. Der Consumer ist verantwortlich dafuer, offene Messungen beim Fortsetzen zu behandeln. Als pruefbare Kriterien steht das in US-005.5.

### Mehrere gleichzeitige Messungen

Der Service unterstuetzt mehrere gleichzeitige Messungen. Jeder `starten()`-Aufruf erzeugt eine neue `ZeitMessung`. Es gibt keinen globalen Singleton-Timer.

**MVP-Einschraenkung:** Pro `referenzId` + `referenzTyp` sollte maximal eine offene Messung existieren. Dies wird nicht vom Service erzwungen, sondern ist Consumer-Verantwortung. Als pruefbares Kriterium steht das im letzten AK von US-005.1.

---

## User Stories

F-005 hat keine UI und keinen menschlichen Bediener: Der Mechaniker startet und stoppt den Timer nie selbst (siehe "Nicht-funktionale Anforderungen"). Der Nutzer im Sinne dieser Stories ist deshalb das **Consumer-Feature**, das den Service aufruft — nicht der Mechaniker. Der Nutzen ist entsprechend der Nutzen fuer den Consumer, der seine fachliche Aufgabe damit erfuellen kann.

Die Stories beschreiben ausschliesslich das in "Service-Interface" definierte Verhalten. Es kommt keine Methode vor, die dort nicht steht. Wann ein Consumer startet und stoppt, legen die Consumer-Specs fest (siehe [README.md](README.md), Abschnitt "Consumer") und ist ausdruecklich **nicht** Gegenstand dieser Stories.

**Traceability:** Jede Story wird als `@Nested inner class` in `ZeiterfassungServiceTest` abgebildet, jedes Akzeptanzkriterium als ein `@Test` (siehe `docs/CODING_RULES.md`). Alle Kriterien sind ohne UI und ohne Emulator pruefbar (Room In-Memory-DB).

---

### US-005.1: Eine Zeitmessung starten

**Als** Consumer-Feature
**moechte ich** fuer eine beliebige Entity eine Zeitmessung starten und dafuer eine ID zurueckbekommen
**damit** ich die laufende Messung spaeter gezielt wieder stoppen kann, ohne selbst Zeitstempel verwalten zu muessen.

#### Akzeptanzkriterien

- **Given** die Tabelle `zeit_messung` enthaelt keine Messung fuer `referenzId = 42` und `referenzTyp = "DEMONTAGE_SCHRITT"`
  **When** der Consumer `starten(42, "DEMONTAGE_SCHRITT")` aufruft
  **Then** existiert genau eine neue `ZeitMessung` mit `referenzId = 42`, `referenzTyp = "DEMONTAGE_SCHRITT"`, `gestartetAm` = Zeitpunkt des Aufrufs und `gestopptAm = null`
  **And** der Rueckgabewert ist deren `id`

- **Given** der Consumer ruft `starten(...)` auf
  **When** der Aufruf zurueckkehrt
  **Then** ist der Datensatz bereits geschrieben (Sofort-Save) und bleibt erhalten, auch wenn die App unmittelbar danach beendet wird

- **Given** der Consumer uebergibt einen beliebigen `referenzTyp`-String, zum Beispiel `"MONTAGE_SCHRITT"`
  **When** `starten(...)` aufgerufen wird
  **Then** wird der String unveraendert gespeichert
  **And** der Service validiert, uebersetzt und interpretiert ihn nicht (keine Whitelist erlaubter Typen)

- **Given** fuer `referenzId = 42` und `referenzTyp = "DEMONTAGE_SCHRITT"` und fuer `referenzId = 43` desselben Typs wird je eine Messung gestartet
  **When** beide Aufrufe erfolgt sind
  **Then** laufen beide Messungen unabhaengig nebeneinander und haben verschiedene IDs (es gibt keinen globalen Singleton-Timer)

- **Given** fuer `referenzId = 42` und `referenzTyp = "DEMONTAGE_SCHRITT"` existiert bereits eine offene Messung
  **When** der Consumer fuer dieselbe Kombination erneut `starten(...)` aufruft
  **Then** legt der Service eine zweite, ebenfalls offene Messung an und wirft **keine** Exception
  > Die Regel "hoechstens eine offene Messung pro `referenzId` + `referenzTyp`" ist bewusst Consumer-Verantwortung und wird vom Service nicht erzwungen (siehe "Mehrere gleichzeitige Messungen"). Der Service still zu verweigern oder die alte Messung zu stoppen waere fuer den Consumer ueberraschender als ein zweiter Datensatz, den er in `messungenFuer()` sieht.

---

### US-005.2: Eine laufende Zeitmessung stoppen

**Als** Consumer-Feature
**moechte ich** eine laufende Messung stoppen und die gemessene Dauer sofort zurueckbekommen
**damit** ich sie ohne zweite Abfrage anzeigen oder weiterverarbeiten kann.

#### Akzeptanzkriterien

- **Given** eine Messung mit `id = 7` laeuft (`gestopptAm = null`)
  **When** der Consumer `stoppen(7)` aufruft
  **Then** ist `gestopptAm` dieser Messung auf den Zeitpunkt des Aufrufs gesetzt
  **And** der Rueckgabewert ist die Duration `gestopptAm - gestartetAm`

- **Given** `stoppen(7)` wurde ausgefuehrt
  **When** der Datensatz danach erneut gelesen wird
  **Then** ist `gestopptAm` persistiert (Sofort-Save) und `gestartetAm` unveraendert
  **And** es wurde keine zweite Zeile angelegt

- **Given** eine Messung wurde gestartet und im selben Moment wieder gestoppt
  **When** der Consumer die Duration erhaelt
  **Then** ist sie nicht negativ (minimal `Duration.ZERO`; fuer die Anzeige reicht Sekunden-Genauigkeit)

- **Given** zwei Messungen laufen gleichzeitig
  **When** der Consumer genau eine davon stoppt
  **Then** ist nur diese gestoppt und die andere bleibt unveraendert offen

- **Given** der Consumer kennt nur `referenzId` und `referenzTyp`, weil er die von `starten()` gelieferte `messungId` verloren hat (z.B. nach Prozess-Tod)
  **When** er die `id` der offenen Messung ueber `messungenFuer(referenzId, referenzTyp)` ermittelt und damit `stoppen(...)` aufruft
  **Then** wird die Messung normal gestoppt und die Duration zurueckgegeben
  **And** `stoppen()` nimmt weiterhin ausschliesslich eine `messungId` entgegen — der Service bietet bewusst keine Stopp-Variante auf Basis von `referenzId` an

---

### US-005.3: Die Dauer einer Messung abfragen

**Als** Consumer-Feature
**moechte ich** die Dauer einer Messung jederzeit nachtraeglich abfragen koennen
**damit** ich sie anzeigen kann, ohne mir die Duration aus dem Stopp-Aufruf gemerkt zu haben.

#### Akzeptanzkriterien

- **Given** Messung `7` ist gestoppt
  **When** der Consumer `dauer(7)` aufruft
  **Then** erhaelt er `gestopptAm - gestartetAm` — denselben Wert, den `stoppen(7)` zurueckgegeben hat

- **Given** Messung `8` laeuft noch (`gestopptAm = null`)
  **When** der Consumer `dauer(8)` aufruft
  **Then** erhaelt er `null`
  **And** es wird **keine** bisher verstrichene Zeit berechnet und keine Exception geworfen (eine laufende Messung hat noch keine Dauer; ob und wie der Consumer eine laufende Messung anzeigt, entscheidet er selbst)

- **Given** zu `messungId = 999` existiert kein Datensatz
  **When** der Consumer `dauer(999)` aufruft
  **Then** erhaelt er `null` und es wird keine Exception geworfen
  > Lesende Aufrufe auf eine unbekannte `id` liefern `null`; schreibende Aufrufe scheitern dagegen laut (US-005.6). Diese Trennung gilt fuer den ganzen Service.

- **Given** Messung `7` ist gestoppt
  **When** der Consumer `dauer(7)` mehrfach aufruft
  **Then** liefert jeder Aufruf denselben Wert
  **And** kein Aufruf veraendert einen Datensatz (`dauer()` ist frei von Seiteneffekten)

---

### US-005.4: Alle Messungen zu einer Referenz abfragen

**Als** Consumer-Feature
**moechte ich** alle Messungen zu einer Referenz als Flow beobachten
**damit** ich daraus eine Gesamtdauer bilden kann und meine Anzeige sich bei neuen Messungen von selbst aktualisiert.

#### Akzeptanzkriterien

- **Given** zu `referenzId = 42` und `referenzTyp = "DEMONTAGE_SCHRITT"` existieren drei Messungen
  **When** der Consumer `messungenFuer(42, "DEMONTAGE_SCHRITT")` sammelt
  **Then** enthaelt die erste Emission genau diese drei `ZeitMessung`-Datensaetze

- **Given** zu `referenzId = 42` existieren Messungen mit `referenzTyp = "DEMONTAGE_SCHRITT"` **und** mit `referenzTyp = "MONTAGE_SCHRITT"`
  **When** der Consumer `messungenFuer(42, "DEMONTAGE_SCHRITT")` sammelt
  **Then** enthaelt das Ergebnis ausschliesslich die Messungen mit diesem Typ (die `referenzId` allein ist nicht eindeutig, weil verschiedene Consumer dieselben IDs verwenden koennen)

- **Given** zu einer Referenz existiert eine gestoppte **und** eine offene Messung
  **When** der Consumer `messungenFuer(...)` sammelt
  **Then** sind **beide** enthalten
  **And** der Service filtert offene Messungen nicht heraus (ob eine offene Messung in eine Summe eingeht, entscheidet der Consumer)

- **Given** zu einer Referenz existiert keine einzige Messung
  **When** der Consumer `messungenFuer(...)` sammelt
  **Then** emittiert der Flow eine **leere Liste** — nicht `null` und keine Exception

- **Given** ein Consumer sammelt `messungenFuer(42, "DEMONTAGE_SCHRITT")`
  **When** danach fuer dieselbe Referenz eine Messung gestartet oder gestoppt wird
  **Then** emittiert der Flow erneut mit dem aktualisierten Stand, ohne dass der Consumer neu abfragen muss

- **Given** ein Consumer braucht die Gesamtdauer ueber mehrere Referenzen
  **When** er sie bilden will
  **Then** ruft er `messungenFuer(...)` je Referenz auf und summiert selbst — der Service bietet **keine** Aggregation ueber mehrere Referenzen an (siehe "Muster B")

---

### US-005.5: Eine offene Messung uebersteht die App-Unterbrechung

**Als** Consumer-Feature
**moechte ich** eine beim Beenden der App noch laufende Messung nach dem Neustart unveraendert vorfinden
**damit** ich selbst entscheiden kann, ob ich sie fortfuehre oder stoppe, und keine bereits gemessene Zeit verloren geht.

#### Akzeptanzkriterien

- **Given** eine Messung laeuft (`gestopptAm = null`) und die App wird beendet (auch bei Prozess-Tod)
  **When** die App neu gestartet wird
  **Then** existiert die Messung unveraendert mit demselben `gestartetAm` und weiterhin `gestopptAm = null`

- **Given** beim App-Start existieren eine oder mehrere offene Messungen
  **When** der Service initialisiert wird
  **Then** schreibt er nichts in `zeit_messung`: er stoppt nichts, loescht nichts und korrigiert keinen Zeitstempel (der Service macht kein Cleanup)

- **Given** nach dem Neustart existiert eine offene Messung
  **When** der Consumer `stoppen(messungId)` aufruft
  **Then** umfasst die zurueckgegebene Duration auch die Zeit, in der die App nicht lief
  > Der Timer laeuft konzeptionell durch, weil der Mechaniker waehrend der App-Pause physisch weiterarbeitet. Eine Pause-Funktion gibt es nicht (siehe README, Offene Fragen).

- **Given** nach dem Neustart existiert eine offene Messung
  **When** der Consumer sie bewusst offen laesst
  **Then** bleibt sie offen
  **And** `dauer(messungId)` liefert weiterhin `null` (US-005.3)

- **Given** zu derselben Referenz existieren nach dem Neustart mehrere offene Messungen
  **When** der Consumer nur eine davon stoppt
  **Then** bleiben die uebrigen offen und werden vom Service nicht angetastet (das Aufraeumen ist Consumer-Verantwortung)

---

### US-005.6: Doppeltes Stoppen als Fehler erkennen

**Als** Consumer-Feature
**moechte ich** beim Stoppen einer bereits gestoppten Messung einen lauten Fehler bekommen
**damit** ein Fehler in meiner Ablaufsteuerung sofort auffaellt und eine bereits gemessene Dauer nicht stillschweigend ueberschrieben wird.

#### Akzeptanzkriterien

- **Given** Messung `7` ist bereits gestoppt (`gestopptAm` gesetzt)
  **When** der Consumer `stoppen(7)` erneut aufruft
  **Then** wirft der Service eine `IllegalStateException`

- **Given** `stoppen(7)` hat die `IllegalStateException` geworfen
  **When** der Datensatz danach gelesen wird
  **Then** ist `gestopptAm` unveraendert auf dem urspruenglichen Wert
  **And** `dauer(7)` liefert weiterhin die urspruengliche Duration (der fehlgeschlagene Aufruf hat nichts geschrieben)

- **Given** zu `messungId = 999` existiert kein Datensatz
  **When** der Consumer `stoppen(999)` aufruft
  **Then** wirft der Service ebenfalls eine `IllegalStateException`
  **And** es wird keine Zeile angelegt oder veraendert
  > Ein Stopp-Aufruf auf eine unbekannte `id` ist ein Programmierfehler des Consumers und scheitert deshalb laut — anders als der lesende `dauer(999)`, der `null` liefert (US-005.3).

- **Given** eine offene Messung
  **When** der Consumer `stoppen()` genau einmal aufruft
  **Then** wird keine Exception geworfen und die Duration wird normal zurueckgegeben (US-005.2)

---

## Nicht-funktionale Anforderungen

- **Sofort-Save:** Start und Stopp werden sofort in die DB geschrieben (Governance: Sofort-Save Strategie)
- **Kein UI:** Der Service hat keine eigene UI. Timer-Anzeige ist Consumer-Verantwortung
- **Sekunden-Genauigkeit:** `Instant`-basierte Timestamps. Fuer die Anzeige reichen Sekunden
- **Keine manuelle Interaktion:** Der Mechaniker startet/stoppt den Timer nie direkt. Consumer-Code steuert den Lifecycle automatisch

## Nutzungsmuster (generisch)

Der Service kennt seine Consumer nicht (Governance: Service-Architektur, Punkte 2 und 3). Die folgenden Muster beschreiben deshalb ausschliesslich den Umgang mit dem Interface — ohne Feature-Namen, ohne Annahmen ueber fremde Tabellen. **Verbindlich ist jeweils die Consumer-Spec**, in der Trigger und Anzeige festgelegt sind.

### Muster A: Eine Messung pro Referenz-Entity

```
Consumer betritt den messbaren Abschnitt (Trigger definiert der Consumer):
  → starten(referenzId, referenzTyp)
  → Consumer merkt sich die zurueckgegebene messungId in seinem State

Consumer verlaesst den messbaren Abschnitt (Trigger definiert der Consumer):
  → stoppen(messungId)
  → Duration wird zurueckgegeben (Consumer kann sie anzeigen oder ignorieren)
```

### Muster B: Gesamtdauer ueber mehrere Referenzen

```
Consumer kennt die referenzIds, die zu einer Auswertung gehoeren:
  → messungenFuer(referenzId, referenzTyp) je Referenz
  → Consumer summiert die Dauern der gestoppten Messungen
```

Der Service bietet **keine** Aggregation ueber mehrere Referenzen an und formuliert **keine** Query auf Consumer-Tabellen. Welche `referenzId`s zusammengehoeren, weiss nur der Consumer.

Rein lesende Consumer haengen nur an `messungenFuer()` bzw. an der Tabelle `zeit_messung`, nicht am Start/Stopp-Lifecycle.

### Wo die konkrete Integration steht

Die Zuordnung Consumer → Spec steht in [README.md](README.md), Abschnitt "Consumer". Aendert ein Consumer seinen Ablauf oder seine Screens, aendert sich diese Datei nicht.
