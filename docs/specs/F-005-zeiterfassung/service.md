# Timer-Service: Interface, Entity und User Stories

## Entity-Definition

### ZeitMessung (Room Entity)

```kotlin
@Entity(
    tableName = "zeit_messung",
    indices = [Index("referenzTyp", "referenzId")]
)
data class ZeitMessung(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val referenzId: Long,              // ID der gemessenen Entity (z.B. Schritt.id)
    val referenzTyp: String,           // Typ der Referenz, definiert vom Consumer

    val gestartetAm: Instant,          // Zeitpunkt des Timer-Starts
    val gestopptAm: Instant? = null    // Zeitpunkt des Timer-Stopps (null = laeuft noch)
)
```

**Berechnete Dauer:** `gestopptAm - gestartetAm`. Wird nicht separat gespeichert — eine Duration ist immer ableitbar aus den Timestamps.

**Eine Referenz darf mehrere Messungen haben.** Jedes Start/Stopp-Paar ist eine eigene Zeile; die Dauer einer Referenz ist die Summe ihrer Zeilen. Damit traegt das Modell **Anhalten und Fortsetzen** ohne ein zusaetzliches Feld: eine Pause ist schlicht die Luecke zwischen zwei Zeilen. Ein Feld "aufgelaufene Zeit", das man konsistent halten muesste, gibt es bewusst nicht.

**referenzTyp:** Freitext-String, definiert vom Consumer. Der Service interpretiert den Typ nicht, er speichert ihn nur. Die im Projekt vergebenen Werte stehen als Konstanten (`ReferenzTyp`) beim Service, gehoeren aber inhaltlich den Consumern:

| Wert | Consumer | Referenz |
|---|---|---|
| `"DEMONTAGE_SCHRITT"` | F-003 Demontage | `Schritt.id` |
| `"MONTAGE_SCHRITT"` | F-004 Montage | `Schritt.id` |

Derselbe Schritt kann also zwei getrennte Zeitreihen tragen — Ausbauzeit und Einbauzeit werden nie vermischt.

**referenzId:** Primaerschluessel der referenzierten Entity. Kein Foreign Key Constraint in der DB (der Service kennt die Consumer-Tabellen nicht).

## Service-Interface

```kotlin
class ZeiterfassungService(
    private val dao: ZeitMessungDao,
    private val uhr: () -> Instant = Instant::now
) {

    /**
     * Startet eine neue Zeitmessung.
     * @return ID der angelegten ZeitMessung
     */
    suspend fun starten(referenzId: Long, referenzTyp: String): Long

    /**
     * Stoppt eine laufende Zeitmessung.
     * @throws IllegalStateException wenn Messung bereits gestoppt oder unbekannt (US-005.6)
     */
    suspend fun stoppen(messungId: Long): Duration

    /** Dauer einer abgeschlossenen Messung, oder null wenn noch nicht gestoppt (US-005.3). */
    suspend fun dauer(messungId: Long): Duration?

    /** Alle Messungen fuer eine Referenz (US-005.4). */
    fun messungenFuer(referenzId: Long, referenzTyp: String): Flow<List<ZeitMessung>>

    // --- Fuer Consumer mit einem Start/Pause-Schalter (US-005.7) ---

    /** Die offene Messung einer Referenz, falls gerade eine laeuft. */
    suspend fun laufendeMessung(referenzId: Long, referenzTyp: String): ZeitMessung?

    suspend fun laeuft(referenzId: Long, referenzTyp: String): Boolean

    /** Schaltet um. Rueckgabe: true = laeuft jetzt, false = steht jetzt. */
    suspend fun umschalten(referenzId: Long, referenzTyp: String): Boolean

    // --- Fuer das Verlassen eines Abschnitts (US-005.8) ---

    /** Stoppt eine eventuell laufende Messung. Kein Fehler, wenn keine laeuft. */
    suspend fun stoppeFallsLaeuft(referenzId: Long, referenzTyp: String): Duration?

    /** Stoppt alle offenen Messungen. */
    suspend fun stoppeAlleOffenen()

    // --- Fuer die Anzeige (US-005.9) ---

    /** Summe aller Messungen einer Referenz; eine laufende wird bis jetzt gerechnet. */
    suspend fun gesamtdauer(referenzId: Long, referenzTyp: String): Duration
}
```

Die `uhr` ist ein Konstruktor-Parameter, damit die Tests ohne Warten auskommen. Produktiv ist sie `Instant::now`.

## DAO

```kotlin
@Dao
interface ZeitMessungDao {

    @Insert
    suspend fun einfuegen(messung: ZeitMessung): Long

    /**
     * Setzt den Stopp-Zeitpunkt und liefert die Zahl geaenderter Zeilen.
     * Die Bedingung `gestopptAm IS NULL` traegt die Fehlererkennung aus US-005.6:
     * ein zweiter Stopp trifft keine Zeile und liefert 0.
     */
    @Query("UPDATE zeit_messung SET gestopptAm = :zeitpunkt WHERE id = :id AND gestopptAm IS NULL")
    suspend fun stoppen(id: Long, zeitpunkt: Instant): Int

    @Query("SELECT * FROM zeit_messung WHERE id = :id")
    suspend fun findById(id: Long): ZeitMessung?

    @Query("SELECT * FROM zeit_messung WHERE referenzId = :refId AND referenzTyp = :refTyp ORDER BY gestartetAm ASC")
    fun findByReferenz(refId: Long, refTyp: String): Flow<List<ZeitMessung>>

    /** Die zuletzt gestartete offene Messung einer Referenz. */
    @Query(
        """
        SELECT * FROM zeit_messung
        WHERE referenzId = :refId AND referenzTyp = :refTyp AND gestopptAm IS NULL
        ORDER BY gestartetAm DESC LIMIT 1
        """
    )
    suspend fun findeOffene(refId: Long, refTyp: String): ZeitMessung?

    @Query("SELECT * FROM zeit_messung WHERE gestopptAm IS NULL")
    suspend fun holeAlleOffenen(): List<ZeitMessung>

    /** Summe der Dauern in Millisekunden; eine laufende Messung bis [jetztMillis]. */
    @Query(
        """
        SELECT COALESCE(SUM(COALESCE(gestopptAm, :jetztMillis) - gestartetAm), 0)
        FROM zeit_messung
        WHERE referenzId = :refId AND referenzTyp = :refTyp
        """
    )
    suspend fun summeMillis(refId: Long, refTyp: String, jetztMillis: Long): Long
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

### Anhalten und Fortsetzen

Der Timer **ist** anhaltbar. Der Mechaniker bedient dazu einen Schalter im Timer-Chip des Consumers (F-003 und F-004).

```
Schalter auf "steht" gestellt  → stoppen der offenen Messung  → Zeile 1 abgeschlossen
Schalter auf "laeuft" gestellt → starten                      → Zeile 2 wird angelegt
Dauer der Referenz            = Zeile 1 + Zeile 2
```

Am Modell aendert das nichts: Fortsetzen oeffnet **nie** eine abgeschlossene Messung wieder, sondern legt eine neue an. Der Service haelt keinen Pause-Zustand — ob eine Referenz gerade laeuft, ist die Frage, ob sie eine offene Messung hat (`laeuft()`).

### App-Unterbrechung

| Situation | Zustand in DB | Verhalten |
|---|---|---|
| Timer laeuft, App wird geschlossen | `ZeitMessung` mit `gestopptAm = null` | Messung bleibt offen, der Service greift nicht ein |
| Timer laeuft, App wird neu gestartet | `ZeitMessung` mit `gestopptAm = null` | Consumer entscheidet: Fortsetzen oder Stoppen |
| Timer gestoppt, App wird geschlossen | `ZeitMessung` mit `gestopptAm` gesetzt | Keine Aktion noetig, Daten sind persistiert |

**Wichtig:** Der Service selbst macht kein Cleanup. Offene Messungen (`gestopptAm = null`) bleiben bestehen. Der Consumer ist verantwortlich dafuer, offene Messungen beim Fortsetzen zu behandeln. Als pruefbare Kriterien steht das in US-005.5.

**[OFFEN]** Ob die Zeit zwischen App-Ende und Neustart als Arbeitszeit zaehlt. Technisch umfasst die Dauer sie (US-005.5), weil `stoppen()` gegen `gestartetAm` rechnet. Fachlich ist offen, was ein Consumer mit einer Messung tun soll, die eine Nacht oder ein Wochenende ueberdauert hat: unveraendert weiterlaufen lassen, beim naechsten Start stumm stoppen oder verwerfen. Das ist eine Produktentscheidung und wird hier **nicht** getroffen; bis dahin gibt es keine Regel, gegen die ein Consumer implementieren koennte.

### Mehrere gleichzeitige Messungen

Der Service unterstuetzt mehrere gleichzeitige Messungen. Jeder `starten()`-Aufruf erzeugt eine neue `ZeitMessung`. Es gibt keinen globalen Singleton-Timer.

**Einschraenkung:** Pro `referenzId` + `referenzTyp` sollte hoechstens eine **offene** Messung existieren. Dies wird nicht vom Service erzwungen, sondern ist Consumer-Verantwortung; `umschalten()` und `stoppeFallsLaeuft()` sind die beiden Aufrufe, die das von sich aus einhalten. Als pruefbares Kriterium steht das im letzten AK von US-005.1.

---

## User Stories

F-005 hat keine eigene UI. Der Mechaniker bedient die Zeiterfassung zwar mittelbar — ueber den Schalter im Timer-Chip des Consumers — aber jeder Tap wird vom Consumer in einen Aufruf dieses Interface uebersetzt. Der Nutzer im Sinne dieser Stories ist deshalb das **Consumer-Feature**, und der Nutzen ist der Nutzen fuer den Consumer.

Die Stories beschreiben ausschliesslich das in "Service-Interface" definierte Verhalten. Es kommt keine Methode vor, die dort nicht steht. **Wann** ein Consumer startet und stoppt, steht als Uebersicht unter "Consumer-Trigger" und verbindlich in der jeweiligen Consumer-Spec.

**Traceability:** Jede Story wird als `@Nested inner class` in `ZeiterfassungServiceTest` abgebildet, jedes Akzeptanzkriterium als ein `@Test` (siehe `docs/CODING_RULES.md`). Alle Kriterien sind ohne UI und ohne Emulator pruefbar (Room In-Memory-DB, injizierte Uhr).

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
  > Die Regel "hoechstens eine offene Messung pro `referenzId` + `referenzTyp`" ist bewusst Consumer-Verantwortung und wird vom Service nicht erzwungen (siehe "Mehrere gleichzeitige Messungen"). Der Service still zu verweigern oder die alte Messung zu stoppen waere fuer den Consumer ueberraschender als ein zweiter Datensatz, den er in `messungenFuer()` sieht. Wer das nicht selbst pruefen will, benutzt `umschalten()` (US-005.7).

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
  **When** er die `id` der offenen Messung ueber `laufendeMessung(...)` oder `messungenFuer(...)` ermittelt und damit `stoppen(...)` aufruft
  **Then** wird die Messung normal gestoppt und die Duration zurueckgegeben
  **And** `stoppen()` nimmt weiterhin ausschliesslich eine `messungId` entgegen — die referenzbasierte Variante heisst `stoppeFallsLaeuft()` (US-005.8) und ist bewusst eine eigene Methode mit eigener Fehlersemantik

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
  **And** es wird **keine** bisher verstrichene Zeit berechnet und keine Exception geworfen (eine laufende Messung hat noch keine abgeschlossene Dauer; wer den laufenden Wert anzeigen will, nimmt `gesamtdauer()` aus US-005.9)

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
  **And** sie sind nach `gestartetAm` aufsteigend sortiert

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
  **Then** ruft er `gesamtdauer(...)` bzw. `messungenFuer(...)` je Referenz auf und summiert selbst — der Service bietet **keine** Aggregation ueber mehrere Referenzen an (siehe "Muster B")

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
  > Das ist die technische Folge daraus, dass gegen `gestartetAm` gerechnet wird. Ob diese Zwischenzeit als Arbeitszeit gelten soll, ist **[OFFEN]** (siehe "App-Unterbrechung"). Der Service trifft die Entscheidung nicht und rundet nichts weg.

- **Given** nach dem Neustart existiert eine offene Messung
  **When** der Consumer sie bewusst offen laesst
  **Then** bleibt sie offen
  **And** `dauer(messungId)` liefert weiterhin `null` (US-005.3)

- **Given** zu derselben Referenz existieren nach dem Neustart mehrere offene Messungen
  **When** der Consumer nur eine davon stoppt
  **Then** bleiben die uebrigen offen und werden vom Service nicht angetastet (das Aufraeumen ist Consumer-Verantwortung, `stoppeAlleOffenen()` aus US-005.8 nimmt es ihm ab)

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

- **Given** eine bereits gestoppte Messung
  **When** der Service den Fehlerfall erkennt
  **Then** stuetzt er sich auf die Zahl geaenderter Zeilen des Stopp-Updates, nicht auf einen vorgeschalteten Lesevorgang (zwei nebenlaeufige Stopps duerfen nicht beide durchgehen)

---

### US-005.7: Eine Messung von Hand anhalten und fortsetzen

**Als** Consumer-Feature
**moechte ich** die Messung einer Referenz mit einem Aufruf umschalten und ihren Zustand abfragen koennen
**damit** ich einen Start/Pause-Schalter anbieten kann, ohne selbst Buch darueber zu fuehren, welche Messung gerade offen ist.

Der Timer ist anhaltbar. Der Mechaniker stellt ihn im Timer-Chip des Consumers an und aus (siehe [../design-system.md](../design-system.md), Abschnitt "Bewusste Abweichungen vom Prototyp", K-03).

#### Akzeptanzkriterien

- **Given** zu `referenzId = 42` und `referenzTyp = "MONTAGE_SCHRITT"` laeuft keine Messung
  **When** der Consumer `umschalten(42, "MONTAGE_SCHRITT")` aufruft
  **Then** wird eine neue Messung gestartet
  **And** der Rueckgabewert ist `true`
  **And** `laeuft(42, "MONTAGE_SCHRITT")` liefert danach `true`

- **Given** zu einer Referenz laeuft eine Messung
  **When** der Consumer `umschalten(...)` aufruft
  **Then** wird genau diese Messung gestoppt
  **And** der Rueckgabewert ist `false`
  **And** `laeuft(...)` liefert danach `false`

- **Given** eine Referenz wurde gestartet, gestoppt und wieder gestartet
  **When** die Messungen dieser Referenz gelesen werden
  **Then** existieren **zwei** Zeilen
  **And** die erste ist unveraendert gestoppt geblieben (Fortsetzen oeffnet keine abgeschlossene Messung wieder)

- **Given** zu einer Referenz laeuft eine Messung
  **When** der Consumer `laufendeMessung(...)` aufruft
  **Then** erhaelt er genau diese `ZeitMessung`
  **And** existieren mehrere offene Messungen, ist es die zuletzt gestartete

- **Given** zu einer Referenz laeuft keine Messung
  **When** der Consumer `laufendeMessung(...)` aufruft
  **Then** erhaelt er `null` und es wird keine Exception geworfen

- **Given** zu `referenzId = 42` laeuft eine Messung mit `referenzTyp = "DEMONTAGE_SCHRITT"`
  **When** der Consumer `laeuft(42, "MONTAGE_SCHRITT")` abfragt
  **Then** erhaelt er `false` (die beiden Typen sind getrennte Zeitreihen)

---

### US-005.8: Beim Verlassen geraeuschlos stoppen

**Als** Consumer-Feature
**moechte ich** eine eventuell laufende Messung beenden koennen, ohne vorher zu pruefen, ob ueberhaupt eine laeuft
**damit** der Aufruf beim Verlassen eines Schritts oder eines Flows nicht davon abhaengt, wie der Nutzer dorthin gekommen ist.

`stoppen()` scheitert laut, weil ein doppelter Stopp ein Programmierfehler ist (US-005.6). Beim Verlassen ist die Lage eine andere: dass keine Messung laeuft, ist dort der Normalfall und kein Fehler. Deshalb zwei getrennte Methoden statt eines Schalters.

#### Akzeptanzkriterien

- **Given** zu einer Referenz laeuft eine Messung
  **When** der Consumer `stoppeFallsLaeuft(referenzId, referenzTyp)` aufruft
  **Then** wird sie gestoppt und ihre Duration zurueckgegeben

- **Given** zu einer Referenz laeuft keine Messung
  **When** der Consumer `stoppeFallsLaeuft(...)` aufruft
  **Then** erhaelt er `null`
  **And** es wird **keine** Exception geworfen und nichts geschrieben — genau die Lage, in der `stoppen()` scheitern wuerde

- **Given** es existieren offene Messungen zu mehreren Referenzen und mehreren Typen
  **When** der Consumer `stoppeAlleOffenen()` aufruft
  **Then** ist danach keine Messung mehr offen
  **And** bereits gestoppte Messungen bleiben unveraendert

- **Given** es existiert keine einzige offene Messung
  **When** der Consumer `stoppeAlleOffenen()` aufruft
  **Then** passiert nichts und es wird keine Exception geworfen

---

### US-005.9: Die Gesamtdauer einer Referenz abfragen

**Als** Consumer-Feature
**moechte ich** die Summe aller Messungen einer Referenz als eine Zahl bekommen
**damit** ich eine Zeit anzeigen kann, ohne die Zeilen selbst zusammenzuzaehlen und ohne den Sonderfall "laeuft gerade" von Hand zu behandeln.

#### Akzeptanzkriterien

- **Given** zu einer Referenz existieren drei abgeschlossene Messungen
  **When** der Consumer `gesamtdauer(referenzId, referenzTyp)` aufruft
  **Then** erhaelt er die Summe ihrer Dauern

- **Given** zu einer Referenz existiert keine Messung
  **When** der Consumer `gesamtdauer(...)` aufruft
  **Then** erhaelt er `Duration.ZERO` — nicht `null` und keine Exception

- **Given** zu einer Referenz existieren eine abgeschlossene und eine laufende Messung
  **When** der Consumer `gesamtdauer(...)` aufruft
  **Then** wird die laufende bis zum aktuellen Zeitpunkt mitgerechnet
  **And** wiederholte Aufrufe liefern einen wachsenden Wert, ohne dass etwas geschrieben wird

- **Given** zu derselben `referenzId` existieren Messungen beider Typen
  **When** der Consumer `gesamtdauer(referenzId, "MONTAGE_SCHRITT")` aufruft
  **Then** gehen nur die Messungen dieses Typs in die Summe ein

---

## Nicht-funktionale Anforderungen

- **Sofort-Save:** Start und Stopp werden sofort in die DB geschrieben (Governance: Sofort-Save Strategie)
- **Kein UI:** Der Service hat keine eigene UI. Timer-Anzeige und Schalter sind Consumer-Verantwortung
- **Sekunden-Genauigkeit:** `Instant`-basierte Timestamps. Fuer die Anzeige reichen Sekunden
- **Manuelles Umschalten ist vorgesehen:** Der Consumer darf jederzeit `umschalten()` aufrufen, weil der Mechaniker einen Schalter bedient. Der Service unterscheidet dabei nicht zwischen automatischen und manuellen Aufrufen — er sieht nur Start und Stopp
- **Testbare Uhr:** Die Zeitquelle wird injiziert, damit alle Kriterien ohne Warten pruefbar sind

## Nutzungsmuster (generisch)

Der Service kennt seine Consumer nicht (Governance: Service-Architektur, Punkte 2 und 3). Die folgenden Muster beschreiben deshalb ausschliesslich den Umgang mit dem Interface — ohne Feature-Namen, ohne Annahmen ueber fremde Tabellen. **Verbindlich ist jeweils die Consumer-Spec**, in der Trigger und Anzeige festgelegt sind.

### Muster A: Ein messbarer Abschnitt, mit Pausen

```
Consumer betritt den messbaren Abschnitt:
  → starten(referenzId, referenzTyp)

Nutzer haelt an / setzt fort (Schalter):
  → umschalten(referenzId, referenzTyp)   // liefert den neuen Zustand fuer die Anzeige

Consumer verlaesst den messbaren Abschnitt:
  → stoppeFallsLaeuft(referenzId, referenzTyp)

Anzeige der aufgelaufenen Zeit:
  → gesamtdauer(referenzId, referenzTyp)
```

Der Consumer muss sich keine `messungId` merken: alle drei Aufrufe arbeiten ueber das Paar (`referenzId`, `referenzTyp`) und ueberstehen damit auch einen Prozess-Tod.

### Muster B: Gesamtdauer ueber mehrere Referenzen

```
Consumer kennt die referenzIds, die zu einer Auswertung gehoeren:
  → gesamtdauer(referenzId, referenzTyp) je Referenz und Typ
  → Consumer summiert selbst
```

Der Service bietet **keine** Aggregation ueber mehrere Referenzen an und formuliert **keine** Query auf Consumer-Tabellen. Welche `referenzId`s zusammengehoeren, weiss nur der Consumer.

Rein lesende Consumer haengen nur an `messungenFuer()` bzw. `gesamtdauer()`, nicht am Start/Stopp-Lifecycle.

## Consumer-Trigger

Diese Tabelle ist eine **Uebersicht, kein Vertrag des Service**. Verbindlich ist jeweils die Consumer-Spec; sie steht hier, weil die Trigger sonst ueber mehrere Dateien verstreut sind und niemand sie gegeneinander pruefen kann. Aendert ein Consumer seinen Ablauf, aendert sich seine Spec — und diese Zeile zieht nach.

| Consumer | Ereignis | Aufruf |
|---|---|---|
| F-003 Demontage | Ein Schritt wird angelegt ("NÄCHSTES") | `starten(schritt.id, "DEMONTAGE_SCHRITT")` |
| F-003 Demontage | Der Schritt wird abgeschlossen | `stoppeFallsLaeuft(schritt.id, "DEMONTAGE_SCHRITT")` |
| F-003 Demontage | Feierabend (Flow verlassen) | `stoppeAlleOffenen()` |
| F-004 Montage | Ein Montage-Schritt wird betreten | `starten(schritt.id, "MONTAGE_SCHRITT")` |
| F-004 Montage | "SITZT!" (Schritt abgehakt) | `stoppeFallsLaeuft(schritt.id, "MONTAGE_SCHRITT")` |
| F-004 Montage | Flow verlassen ("RAUS") oder archivieren | `stoppeAlleOffenen()` |
| F-003 und F-004 | Schalter im Timer-Chip, jederzeit | `umschalten(schritt.id, typ)` |
| F-001 Uebersicht, Archiv | Anzeige einer Dauer | nur lesend: `gesamtdauer(...)` |

**[OFFEN]** Was mit einer laufenden Messung geschieht, wenn der Mechaniker den betrachteten Schritt **von Hand** wechselt (Thumbnail-Sprung, "ZURÜCK"). Sie stoppen, sie mitwandern lassen oder sie auf dem verlassenen Schritt weiterlaufen lassen sind drei verschiedene Auswertungen derselben Arbeit. Der Service kann alle drei; entschieden ist keine. Dieselbe Luecke steht in [../F-004-montage/montage.md](../F-004-montage/montage.md), US-004.7.

### Wo die konkrete Integration steht

Die Zuordnung Consumer → Spec steht in [README.md](README.md), Abschnitt "Consumer". Die Anzeige — Timer-Chip, Zeitformat, Summenzeile — gehoert vollstaendig den Consumer-Specs und nicht dieser Datei.

---

## Aenderungshistorie

| Datum | Aenderung |
| --- | --- |
| 2026-07-27 | Der Timer ist anhaltbar. Die bisherige Aussage "keine manuelle Interaktion" und die Bemerkung "eine Pause-Funktion gibt es nicht" sind gestrichen (design-system.md, K-03). |
| 2026-07-27 | US-005.7 "Eine Messung von Hand anhalten und fortsetzen" ergaenzt (`laeuft`, `laufendeMessung`, `umschalten`). |
| 2026-07-27 | US-005.8 "Beim Verlassen geraeuschlos stoppen" ergaenzt (`stoppeFallsLaeuft`, `stoppeAlleOffenen`). |
| 2026-07-27 | US-005.9 "Die Gesamtdauer einer Referenz abfragen" ergaenzt (`gesamtdauer`). |
| 2026-07-27 | Die Montage misst mit `referenzTyp = "MONTAGE_SCHRITT"`; der Wert ist damit vergeben statt beispielhaft (design-system.md, K-04). |
| 2026-07-27 | Abschnitt "Consumer-Trigger" mit der Start/Stopp-Tabelle aller Consumer ergaenzt. |
| 2026-07-27 | Die Frage, ob die Zeit einer App-Unterbrechung als Arbeitszeit zaehlt, ist als `[OFFEN]` markiert statt beantwortet. |
