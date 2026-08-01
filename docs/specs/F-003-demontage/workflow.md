# Workflow: Demontage-Flow

## States (= Views/Phasen)

| State | View | Beschreibung |
|-------|------|-------------|
| `KAMERA` (transient) | System-Kamera (fremde Activity) | Kein App-Screen. Der `ActivityResultContracts.TakePicture()`-Intent laeuft; Ausloesen und Bestaetigen passieren in der System-Kamera. Der State endet immer -- mit Foto oder mit Abbruch -- und fuehrt in beiden Faellen nach `SCHRITT_ANSICHT`. |
| `SCHRITT_ANSICHT` | Schritt-Ansicht | Der einzige App-Screen des Flows: Schrittnummer, Foto-Karussell des Schritts, Label-Checkboxen am sichtbaren Foto, Thumbnail-Leiste ueber alle Schritte und die Schritt-Navigation "Zurueck"/"Weiter" (beides F-006), Aktionszeile. "Zurueck"/"Weiter" sind immer sichtbar und nur an den Raendern deaktiviert; welche Aktionen die Aktionszeile enthaelt, haengt davon ab, ob der betrachtete Schritt der offene ist (siehe Transitions). |

**Hinweis:** Die System-Kamera wird automatisch per Intent gestartet, sobald ein Schritt beginnt oder "Weiteres Foto" / "Wiederholen" gewaehlt wird. Es ist kein Button-Tap zum Oeffnen der Kamera noetig.

**Hinweis:** Es gibt **keine app-eigene Foto-Bestaetigung** mehr. Die frueheren States `PREVIEW_BAUTEIL`, `PREVIEW_ABLAGEORT`, `ARBEITSPHASE` und `DIALOG` entfallen; die Arbeitsphase geht in `SCHRITT_ANSICHT` auf und bleibt der Ankerpunkt fuer den spaeteren Timer (F-005).

## Transitions

| Von | Event | Nach | Bedingung | DB-Aktion |
|-----|-------|------|-----------|-----------|
| -- (Entry) | Flow starten | `KAMERA` | Reparaturvorgang ist OFFEN, kein offener Schritt vorhanden -- gilt gleichermassen fuer den frisch angelegten Vorgang aus F-002 (der **keinen** Schritt mitbringt) und fuer den Wiedereinstieg ueber F-001, nachdem alle Schritte abgeschlossen sind | `Schritt` anlegen: `schrittNummer` = hoechste Nummer + 1, `gestartetAm` = jetzt. Der betrachtete Schritt ist dieser neue Schritt. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| -- (Entry) | Flow fortsetzen | `SCHRITT_ANSICHT` | Es existiert ein Schritt mit `abgeschlossenAm = null` | Keine (bestehender Schritt wird geladen) |
| `KAMERA` | Foto in der System-Kamera bestaetigt | `SCHRITT_ANSICHT` | Die Kamera kam aus dem Schritt-Start oder aus "Weiteres Foto" | `SchrittFoto` anlegen: `schrittId` des Schritts, der die Kamera gestartet hat (nach einem Sprung der betrachtete, sonst der offene Schritt), `pfad`, `reihenfolge` = Anzahl bisheriger Fotos des Schritts (0-basiert, also hinten angehaengt), `istBauteil = true`, `istUebersicht = false`, `istAblageort = false`, `aufgenommenAm` = jetzt. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `KAMERA` | Foto in der System-Kamera bestaetigt | `SCHRITT_ANSICHT` | Die Kamera kam aus **"Wiederholen"** | **Jetzt erst** wird ersetzt, in einer Operation: neues `SchrittFoto` mit denselben Feldern wie oben, aber `reihenfolge` = `p` (die `reihenfolge` des ersetzten Fotos); die alte `SchrittFoto`-Zeile und die alte Datei werden geloescht. Die uebrigen Fotos behalten ihre `reihenfolge` -- es wird weder umnummeriert noch hinten angehaengt (Foto-Flow Logik Punkt 7). Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `KAMERA` | Abgebrochen | `SCHRITT_ANSICHT` | Die Kamera kam aus "Weiteres Foto" oder "Wiederholen", oder der Schritt hat bereits Fotos | Keine. Angelegte Zieldatei wird geloescht, kein `SchrittFoto`. Kam die Kamera aus "Wiederholen", bleibt das alte Foto unveraendert erhalten |
| `KAMERA` | Abgebrochen | `SCHRITT_ANSICHT` (Vorgaenger N) | Die Kamera kam aus dem **Schritt-Start** (Entry oder "Naechster Schritt") und der eben angelegte Schritt N+1 hat **kein Foto** | **Rollback**, siehe Abschnitt "Rollback beim Abbruch am frischen Schritt": Schritt N+1 loeschen, Schritt N wieder oeffnen (`abgeschlossenAm = null`). Zieldatei loeschen. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `KAMERA` | Keine Kamera-App gefunden | `SCHRITT_ANSICHT` | -- | Wie "Abgebrochen", einschliesslich Rollback. Zusaetzlich Hinweis-Dialog "Keine Kamera-App gefunden" |
| `SCHRITT_ANSICHT` | Label-Checkbox umgeschaltet | `SCHRITT_ANSICHT` | Debounce 300ms, mind. 1 Foto sichtbar | `SchrittFoto` des sichtbaren Fotos updaten: `istBauteil` / `istUebersicht` / `istAblageort`. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `SCHRITT_ANSICHT` | Foto im Karussell gewischt | `SCHRITT_ANSICHT` | -- | Keine (Checkboxen zeigen die Flags des neu sichtbaren Fotos) |
| `SCHRITT_ANSICHT` | "Wiederholen" am sichtbaren Foto | `KAMERA` | Debounce 300ms, mind. 1 Foto sichtbar | **Keine.** Das alte Foto und seine Datei bleiben unveraendert; gemerkt werden nur die `SchrittFoto`-Id und `p` = deren `reihenfolge` fuer die Ersetzung nach erfolgreicher Aufnahme (Governance-Regel "Kamera", siehe [../governance.md](../governance.md)) |
| `SCHRITT_ANSICHT` | "Weiteres Foto" | `KAMERA` | Debounce 300ms | Keine -- das Foto wird an den **betrachteten** Schritt gehaengt, die Schrittnummer aendert sich nicht |
| `SCHRITT_ANSICHT` | Thumbnail eines anderen Schritts (F-006) | `SCHRITT_ANSICHT` (Schritt M) | Debounce 300ms | Keine -- kein Schritt wird abgeschlossen, keine neue Schrittnummer |
| `SCHRITT_ANSICHT` (Schritt an Index i) | **"Zurueck"** der F-006-Schritt-Navigation | `SCHRITT_ANSICHT` (Schritt an Index i-1) | Debounce 300ms; nur aktiv, wenn `i > 0` -- sonst ist das Element sichtbar, aber deaktiviert (F-006 US-006.10) | Keine -- reiner Wechsel des betrachteten Schritts, kein Schritt wird angelegt, abgeschlossen oder geloescht |
| `SCHRITT_ANSICHT` (Schritt an Index i) | **"Weiter"** der F-006-Schritt-Navigation | `SCHRITT_ANSICHT` (Schritt an Index i+1) | Debounce 300ms; nur aktiv, wenn `i < letzter Index` -- beim offenen Schritt also nie, weil dieser immer der letzte der Anzeige-Reihenfolge ist | Keine -- reiner Wechsel des betrachteten Schritts. **Nicht** zu verwechseln mit der Aktion "Naechster Schritt" |
| `SCHRITT_ANSICHT` (abgeschlossener Schritt M sichtbar) | "Zurueck zu Schritt N" | `SCHRITT_ANSICHT` (offener Schritt N) | Debounce 300ms | Keine -- reiner State-Wechsel im ViewModel |
| `SCHRITT_ANSICHT` | Foto antippen (Vollbild, F-006) | `SCHRITT_ANSICHT` (Vollbild offen) | -- | Keine |
| `SCHRITT_ANSICHT` (Vollbild offen, F-006) | Schliessen-Element oder Android-Zurueck-Taste | `SCHRITT_ANSICHT` (Vollbild geschlossen) | -- | Keine -- das Vollbild konsumiert die Back-Geste zuerst |
| `SCHRITT_ANSICHT` (offener Schritt N sichtbar) | "Naechster Schritt" | `KAMERA` | Debounce 300ms; der Button ist nur sichtbar, wenn der betrachtete Schritt der offene ist | `abgeschlossenAm` = jetzt am Schritt N setzen **und** neuen `Schritt` N+1 anlegen (`schrittNummer` = N+1, `gestartetAm` = jetzt). **Der betrachtete Schritt wechselt dabei auf N+1**, bevor die Kamera startet -- siehe Abschnitt "Reihenfolge beim Schritt-Start". Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `SCHRITT_ANSICHT` (offener Schritt N sichtbar, mind. 1 Foto) | "Beenden" | Uebersicht (F-001) | Debounce 300ms; der Button ist nur sichtbar, wenn der betrachtete Schritt der offene ist | `abgeschlossenAm` = jetzt am Schritt N setzen. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `SCHRITT_ANSICHT` (offener Schritt N sichtbar, **ohne Fotos**) | "Beenden" | Uebersicht (F-001) | Debounce 300ms | Schritt N **loeschen** statt abschliessen; die Nummer N wird beim naechsten Flow-Start erneut vergeben. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| `SCHRITT_ANSICHT` | Android-Zurueck-Taste (kein Vollbild offen) | `SCHRITT_ANSICHT` | -- | Keine -- der Flow wird nicht verlassen (US-003.6) |

**`aktualisiertAm`:** Jede DB-Aktion dieser Tabelle setzt in derselben Operation `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion. Das ist die projektweite Invariante aus [../governance.md](../governance.md) ("Invariante: `aktualisiertAm`") und wird hier nur genannt, nicht neu formuliert. Transitions ohne DB-Write beruehren das Feld nicht.

**Zwei Auspraegungen der Aktionszeile:** Welche Schritt-Aktionen die Ansicht anbietet, haengt allein davon ab, ob der **betrachtete** Schritt der offene ist:

| Betrachteter Schritt | Sichtbare Schritt-Aktionen |
|---|---|
| Der offene Schritt (`abgeschlossenAm = null`) | "Weiteres Foto", "Naechster Schritt", "Beenden" |
| Ein bereits abgeschlossener Schritt (nach Thumbnail-Sprung oder nach "Zurueck"/"Weiter") | "Weiteres Foto", "Zurueck zu Schritt N" (N = der offene Schritt). "Naechster Schritt" und "Beenden" sind **ausgeblendet**, nicht deaktiviert |

Damit bezieht sich jede sichtbare Aktion immer auf den Schritt, den der Mechaniker gerade vor sich hat. Auf welchem Weg der betrachtete Schritt gewechselt wurde, spielt dabei keine Rolle. Die Foto-Aktionen (Label-Checkboxen, "Wiederholen") haengen unveraendert am sichtbaren Foto des betrachteten Schritts. Die Akzeptanzkriterien zur Sichtbarkeit stehen in [views/schritt-ansicht.md](views/schritt-ansicht.md).

**Getrennt davon: die Schritt-Navigation von F-006.** Die Aktionszeile ist **nicht** die einzige Bedienelement-Gruppe der Schritt-Ansicht. Thumbnail-Leiste sowie "Zurueck" und "Weiter" (F-006 US-006.2 und US-006.10) sind in allen drei F-006-Modi komponiert und daher auch in der Demontage **immer sichtbar**; an den Raendern der Anzeige-Reihenfolge sind sie lediglich deaktiviert. Sie wechseln nur den betrachteten Schritt und schreiben nichts in die DB. Die Abgrenzung zu den Schritt-Aktionen -- insbesondere "Weiter" gegenueber "Naechster Schritt" und "Zurueck" gegenueber "Zurueck zu Schritt N" -- ist in [views/schritt-ansicht.md](views/schritt-ansicht.md), Abschnitt "Zwei Bedienelement-Gruppen", verbindlich festgelegt.

## Reihenfolge beim Schritt-Start

Immer wenn ein Schritt **beginnt** -- beim Entry ohne offenen Schritt und bei "Naechster Schritt" -- gilt diese Reihenfolge verbindlich:

1. Den Vorgaenger abschliessen, falls es einen offenen gibt (`abgeschlossenAm` = jetzt).
2. Den neuen `Schritt` anlegen und seine Zeitmessung starten (F-005).
3. Erst jetzt die System-Kamera starten, mit der Id des neuen Schritts als Ziel.

Punkt 2 vor Punkt 3 ist keine Kosmetik. Startet die Kamera parallel zur Schritt-Anlage, gibt es bei ihrer Rueckkehr keinen verlaesslichen Schritt, an den das Foto gehoert -- es landete am zuletzt betrachteten, und der ist in diesem Moment der eben abgeschlossene.

**Das Ziel der Aufnahme ist die Schritt-Id, die beim Kamera-Start feststand** -- nicht der Schritt, der bei der Rueckkehr gerade betrachtet wird. Die Id festzuhalten macht die Zuordnung unabhaengig davon, was waehrend der Aufnahme sonst noch passiert.

**Der betrachtete Schritt zieht getrennt davon nach.** Er wechselt nicht im selben Atemzug auf den neuen Schritt, sondern sobald dieser ueber die Datenmeldung ankommt. Das ist bewusst ein eigener Weg und keine Reihenfolge-Bedingung: die Zuordnung des Fotos haengt an der festgehaltenen Id, nicht daran, was die Ansicht gerade zeigt.

**Navigiert der Mechaniker in der Zwischenzeit selbst** -- er tippt in dem Moment ein Thumbnail an --, wird der vorgemerkte Wechsel **verworfen**. Wer selbst navigiert, hat das letzte Wort; ihn eine Sekunde spaeter von einer Datenmeldung wegziehen zu lassen, waere derselbe Fehler wie ein Sprung beim Eintreffen eines Fotos (US-006.4).

## Rollback beim Abbruch am frischen Schritt

Bricht der Mechaniker die Kamera ab, die **den Schritt eroeffnet hat**, und hat dieser Schritt kein einziges Foto, wird der Schritt-Start vollstaendig zurueckgenommen:

- Der eben angelegte Schritt N+1 wird **geloescht**. Seine Nummer wird damit wieder frei und beim naechsten Schritt-Start erneut vergeben.
- Der Vorgaenger N wird **wieder geoeffnet** (`abgeschlossenAm` zurueck auf `null`) und ist danach wieder der betrachtete Schritt.
- Die Zeitmessung des verworfenen Schritts wird gestoppt; die des wieder geoeffneten Schritts laeuft weiter wie vor dem Tap.

**Warum geloescht statt stehengelassen:** Weil die Kamera den Schritt eroeffnet, ist ihr Abbruch die einzige Moeglichkeit, "doch nicht" zu sagen. Ein leerer Schritt, der dabei zurueckbliebe, waere kein bewusst uebersprungener Schritt, sondern Muell -- er verbraucht eine Nummer, erscheint als leeres Thumbnail und schiebt die Nummerierung aller folgenden Teile um eins. Ohne Foto ist zu diesem Zeitpunkt auch nichts dokumentiert, das eine Nummer tragen koennte.

**Warum der Vorgaenger wieder geoeffnet wird:** "Naechster Schritt" hat ihn abgeschlossen. Bliebe er abgeschlossen, haette der Vorgang nach dem Rollback **keinen** offenen Schritt mehr -- die Aktionszeile zeigte "Zurueck zu Schritt N" ins Leere und der Mechaniker kaeme nicht mehr weiter. Das Rollback stellt damit die Invariante wieder her, die den ganzen Flow traegt:

> **Invariante:** Solange die Demontage laeuft, hat der Vorgang genau einen offenen Schritt.

**Zwei Faelle, in denen nicht zurueckgerollt wird:**

| Fall | Verhalten |
|---|---|
| Der Schritt hat bereits Fotos ("Weiteres Foto" abgebrochen, oder Abbruch nach einer geglueckten Aufnahme) | Es wird nichts geloescht und nichts wieder geoeffnet. Nur die leere Zieldatei verschwindet |
| Es gibt keinen Vorgaenger (Schritt 1 eines neuen Vorgangs) | Schritt 1 bleibt leer und offen stehen -- es gibt kein Ziel, auf das zurueckgerollt werden koennte. Der Mechaniker holt die Aufnahme per "Weiteres Foto" nach oder verlaesst den Flow ueber "Beenden", das den fotolosen Schritt ohnehin verwirft |

## Navigations-Diagramm

```
[Entry: Flow starten]
  -> kein offener Schritt vorhanden -> Schritt N anlegen, Ansicht auf N -> System-Kamera (automatisch)
     (gilt fuer den neuen Vorgang aus F-002 und fuer den Wiedereinstieg nach Feierabend)
  -> offener Schritt vorhanden      -> Schritt-Ansicht (offener Schritt N), keine Kamera

System-Kamera (transient, kein App-Screen)
  -> [Foto bestaetigt] -> Datei in photos/, SchrittFoto am Ziel-Schritt anlegen  -> Schritt-Ansicht
                          (Label Bauteil; Ziel ist die beim Kamera-Start
                           festgehaltene Schritt-Id)
                          (kam sie aus "Wiederholen": neues Foto auf Position p,
                           danach altes SchrittFoto + alte Datei loeschen)
  -> [Abgebrochen]     -> Zieldatei verwerfen, keine DB-Zeile                   -> Schritt-Ansicht
                          (kam sie aus "Wiederholen": altes Foto bleibt erhalten)
                          (kam sie aus dem Schritt-Start und der Schritt ist
                           fotolos: Rollback -> Schritt-Ansicht (Vorgaenger N))
  -> [Keine Kamera-App]-> Hinweis-Dialog, sonst wie [Abgebrochen]               -> Schritt-Ansicht

Schritt-Ansicht, offener Schritt N sichtbar
  -> [Label-Checkbox]        -> SchrittFoto-Update (sofort)          -> Schritt-Ansicht
  -> [Karussell wischen]     -> kein DB-Write                        -> Schritt-Ansicht
  -> [Foto antippen]         -> Vollbild (F-006)                     -> Schritt-Ansicht
  -> [Wiederholen am Foto]   -> kein DB-Write (Position p merken)    -> System-Kamera
  -> [Weiteres Foto]         -> kein DB-Write                        -> System-Kamera (Schritt N)
  -> [Thumbnail Schritt M]   -> kein DB-Write                        -> Schritt-Ansicht (Schritt M)
  -> [F-006 "Zurueck"]       -> kein DB-Write                        -> Schritt-Ansicht (Schritt N-1)
  -> [F-006 "Weiter"]        -> deaktiviert (N ist der letzte Schritt der Anzeige-Reihenfolge)
  -> [Naechster Schritt]     -> abgeschlossenAm(N), Schritt N+1,
                                Ansicht folgt auf N+1                 -> System-Kamera (Schritt N+1)
  -> [Beenden]               -> abgeschlossenAm(N) bzw. Schritt N loeschen, falls ohne Fotos
                                                                      -> Uebersicht (F-001)

Schritt-Ansicht, abgeschlossener Schritt M sichtbar (nach Sprung oder nach "Zurueck"/"Weiter")
  -> [Label-Checkbox]        -> SchrittFoto-Update (sofort)          -> Schritt-Ansicht (Schritt M)
  -> [Wiederholen am Foto]   -> kein DB-Write (Position p merken)    -> System-Kamera (Schritt M)
  -> [Weiteres Foto]         -> kein DB-Write                        -> System-Kamera (Schritt M)
  -> [Thumbnail Schritt M2]  -> kein DB-Write                        -> Schritt-Ansicht (Schritt M2)
  -> [F-006 "Zurueck"]       -> kein DB-Write                        -> Schritt-Ansicht (Schritt M-1)
  -> [F-006 "Weiter"]        -> kein DB-Write                        -> Schritt-Ansicht (Schritt M+1)
  -> [Zurueck zu Schritt N]  -> kein DB-Write                        -> Schritt-Ansicht (offener Schritt N)
  ("Naechster Schritt" und "Beenden" werden hier nicht angeboten.
   "Zurueck"/"Weiter" und die Thumbnail-Leiste bleiben unveraendert sichtbar.)

Back-Taste: schliesst zuerst ein offenes Vollbild (F-006). Ist keines offen, bewirkt sie nichts --
der Flow wird nicht verlassen. Die Navigation *zwischen* Schritten ist frei (Thumbnail-Leiste,
F-006-"Zurueck"/"Weiter", "Zurueck zu Schritt N"); nur der Ausstieg aus dem Flow ist an
"Beenden" gebunden.
```

## Schrittnummer-Logik

### US-003.4: Schrittnummer automatisch vergeben und im Blick behalten

**Als** Mechaniker
**moechte ich** dass jeder Schritt automatisch eine fortlaufende Nummer bekommt, die ich waehrend der Arbeit sehe
**damit** ich meine physischen Ablageorte damit beschriften kann und beim Zusammenbau weiss, wie weit ich bin

Die Akzeptanzkriterien zur **Anzeige** der Schrittnummer stehen in [views/schritt-ansicht.md](views/schritt-ansicht.md) (AK 1 und AK 2). Die Kriterien zur **Vergabe und Inkrementierung** stehen hier (AK 3 bis AK 6).

- `schrittNummer` wird beim Anlegen des `Schritt`-Entity gesetzt (= hoechste vorhandene Nummer des Vorgangs + 1)
- Wird von der DB geladen beim Fortsetzen (bei offenem Schritt dessen Nummer, sonst hoechste Nummer + 1)
- Keine manuelle Eingabe moeglich, rein auto-increment
- Keine Obergrenze

### Inkrementierung

Die Schrittnummer wird **nur** inkrementiert, wenn "Naechster Schritt" gewaehlt wird (Schritt N wird abgeschlossen, Schritt N+1 angelegt).

Die Schrittnummer wird **nicht** inkrementiert bei:
- "Weiteres Foto" (das Foto haengt am selben Schritt)
- "Wiederholen" (Foto wird ersetzt, Schritt bleibt derselbe)
- Aendern eines Labels
- Sprung zu einem anderen Schritt ueber die Thumbnail-Leiste (F-006)
- "Zurueck zu Schritt N" (Rueckkehr zum offenen Schritt nach einem Sprung)
- "Beenden" (kein neuer Schritt). Wird dabei ein Schritt ohne Fotos verworfen, sinkt die naechste vergebene Nummer wieder auf dessen Nummer
- Abbruch der System-Kamera. Eroeffnete diese Kamera-Runde den Schritt und blieb er fotolos, wird der Schritt sogar zurueckgenommen -- die Nummer sinkt wieder auf seine (Abschnitt "Rollback beim Abbruch am frischen Schritt")

### Aus US-003.4 AK 3: Schrittnummer bei neuem Vorgang

- **Given** ein neuer Reparaturvorgang ohne Schritte existiert
  **When** der Demontage-Flow gestartet wird
  **Then** wird ein Schritt mit `schrittNummer = 1` angelegt
  **And** die Schritt-Ansicht zeigt "Schritt 1"

### Aus US-003.4 AK 4: Schrittnummer nach "Naechster Schritt"

- **Given** die Schritt-Ansicht zeigt Schritt 3
  **When** der Mechaniker "Naechster Schritt" antippt
  **Then** wird ein Schritt mit `schrittNummer = 4` angelegt
  **And** nach Rueckkehr aus der System-Kamera zeigt die Schritt-Ansicht "Schritt 4"

### Aus US-003.4 AK 7: Rollback nimmt die Nummer wieder zurueck

- **Given** die Schritt-Ansicht zeigt den offenen Schritt 4 mit einem Foto
  **When** der Mechaniker "Naechster Schritt" antippt und die System-Kamera dann abbricht
  **Then** ist Schritt 5 wieder geloescht und Schritt 4 wieder offen
  **And** die Schritt-Ansicht zeigt Schritt 4 mit seinem Foto
  **And** die Aktionszeile bietet erneut "Naechster Schritt" an, beschriftet mit der Nummer 5

### Aus US-003.4 AK 8: Ohne Vorgaenger wird nicht zurueckgerollt

- **Given** ein neuer Reparaturvorgang ohne Schritte wird geoeffnet, Schritt 1 wird angelegt und die System-Kamera startet
  **When** der Mechaniker die Kamera abbricht
  **Then** bleibt Schritt 1 offen und ohne Fotos bestehen
  **And** die Schritt-Ansicht zeigt Schritt 1 im Leer-Zustand des Karussells (F-006 US-006.9)
  **And** es wird kein Schritt geloescht -- es gibt keinen Vorgaenger, auf den zurueckgerollt werden koennte

### Aus US-003.4 AK 5: Keine Inkrementierung bei weiterem Foto

- **Given** die Schritt-Ansicht zeigt Schritt 3 mit einem Foto
  **When** der Mechaniker "Weiteres Foto" antippt und die Aufnahme bestaetigt
  **Then** zeigt die Schritt-Ansicht weiterhin "Schritt 3"
  **And** das neue Foto liegt am selben Schritt (zweites Foto im Karussell)

## Timestamp-Semantik

| Feld | Wird gesetzt wenn... | Bedeutung |
|------|---------------------|-----------|
| `gestartetAm` | Der `Schritt` angelegt wird (unmittelbar bevor die System-Kamera automatisch startet) | Beginn der Arbeit am Schritt |
| `abgeschlossenAm` | "Naechster Schritt" oder "Beenden" getippt wird | Schritt ist abgeschlossen. `null` = Schritt ist offen und wird beim Fortsetzen wieder angezeigt |

Die beiden Timestamps markieren jetzt die **Klammer um den gesamten Schritt** inklusive aller seiner Fotos -- nicht mehr den Weg zwischen zwei Foto-Bestaetigungen. Der Zeitpunkt einer einzelnen Aufnahme steht in `SchrittFoto.aufgenommenAm`.

**Sonderfall:** Schliesst der Mechaniker die App, bevor er "Naechster Schritt" oder "Beenden" tippt, bleibt `abgeschlossenAm = null`. Beim Fortsetzen wird die Schritt-Ansicht fuer genau diesen Schritt wieder angezeigt, mit allen bereits aufgenommenen Fotos.

**Abgrenzung zu F-005:** `gestartetAm` und `abgeschlossenAm` sind Workflow-Timestamps, keine Zeitmessung. Die Zeiterfassung hat ihre eigene Tabelle (Governance: keine Dual-Purpose-Felder).

## Foto-Flow Logik

**Zaehlweise:** `SchrittFoto.reihenfolge` ist **0-basiert** -- das erste Foto eines Schritts hat `reihenfolge = 0`. Die Variable `p` in Punkt 7 ist immer ein `reihenfolge`-Wert und damit ebenfalls 0-basiert. Fachlich wird vom "ersten"/"zweiten" Foto gesprochen; der Indikator von F-006 zaehlt 1-basiert ("1 von 3").

1. **Schritt anlegen:** `Schritt` mit `schrittNummer` und `gestartetAm` sofort in die DB schreiben, danach System-Kamera per Intent starten.
2. **Zieldatei vorbereiten:** Vor dem Intent wird eine Datei in `photos/` angelegt und als FileProvider-URI an die System-Kamera uebergeben. Es gibt kein `photos/temp/` und keinen Verschiebe-Schritt.
3. **Aufnahme und Bestaetigung:** Passieren vollstaendig in der System-Kamera. Die App zeigt dazu keine eigene Vorschau und keinen eigenen Bestaetigen-Button.
4. **Rueckkehr mit Foto** (Kamera kam aus dem Schritt-Start oder aus "Weiteres Foto"): EXIF-Daten strippen, dann sofort eine `SchrittFoto`-Zeile anlegen (`schrittId` des betrachteten Schritts, `pfad`, `reihenfolge` = Anzahl der bisherigen Fotos dieses Schritts -- das Foto wird also **hinten angehaengt** --, `istBauteil = true`, `istUebersicht = false`, `istAblageort = false`, `aufgenommenAm`). Danach Schritt-Ansicht mit dem neuen Foto als sichtbarem Karussell-Eintrag. **Kam die Kamera aus "Wiederholen", gilt stattdessen Punkt 7.4** -- dort wird nicht angehaengt.
5. **Rueckkehr ohne Foto (Abbruch):** Die vorbereitete Zieldatei wird geloescht, es entsteht **keine** `SchrittFoto`-Zeile. Der Mechaniker landet in der Schritt-Ansicht; hat der Schritt kein Foto, zeigt der Karussell-Bereich den Leer-Zustand aus F-006 (US-006.9).
6. **Label aendern:** Checkbox-Umschaltung schreibt sofort ein Update auf die `SchrittFoto`-Zeile des sichtbaren Fotos.
7. **Wiederholen:** Das Ersatzfoto uebernimmt die Position des ersetzten Fotos. **Die Reihenfolge ist verbindlich: zuerst die Kamera, erst nach erfolgreicher Aufnahme loeschen** (projektweite Regel, siehe [../governance.md](../governance.md), Abschnitt "Kamera"). Im Einzelnen:
   1. Beim Tap wird **nichts** geloescht. Das sichtbare Foto (Position `p`) bleibt mit Zeile und Datei unveraendert bestehen.
   2. Gemerkt werden fuer die laufende Kamera-Runde die `SchrittFoto`-Id des zu ersetzenden Fotos und `p` = dessen `reihenfolge`.
   3. Weiter ab Punkt 2 (neue Zieldatei vorbereiten, Kamera erneut starten).
   4. **Rueckkehr mit Foto:** Erst jetzt wird ersetzt, in einer Operation: das neue `SchrittFoto` wird mit `reihenfolge = p` angelegt, die gemerkte alte Zeile und die alte Datei werden geloescht. Die uebrigen Fotos behalten ihre `reihenfolge` -- es wird weder umnummeriert noch verschoben, und die Liste bleibt luecklos, weil genau eine Position eins zu eins ersetzt wird. Das Karussell zeigt danach das Foto mit `reihenfolge = p`. Punkt 4 der Hauptliste ("Rueckkehr mit Foto") gilt hier ausdruecklich **nicht**: es wird nicht ans Ende angehaengt -- ein wiederholtes erstes Foto (`p = 0`) bleibt das erste und damit auch das Thumbnail des Schritts (F-006 US-006.1).
   5. **Rueckkehr ohne Foto (Abbruch oder keine Kamera-App):** Es wird nichts geloescht und nichts eingefuegt. Der Schritt hat unveraendert dieselben Fotos in derselben `reihenfolge` wie vor dem Tap; das alte Foto an Position `p` ist weiterhin vorhanden und bleibt sichtbar. Geloescht wird nur die vorbereitete, leer gebliebene Zieldatei (Punkt 5 der Hauptliste).
8. **Weiteres Foto:** Weiter ab Punkt 2 mit der `schrittId` des **betrachteten** Schritts -- nach einem Thumbnail-Sprung also mit der des abgeschlossenen Schritts M, sonst mit der des offenen Schritts N. Der Schritt und seine Nummer bleiben unveraendert.

## Sofort-Save Strategie

- **Schritt-Entity:** Wird beim Start des Schritts sofort in die DB angelegt (mit `gestartetAm`, ohne Fotos)
- **Foto:** Wird nach der System-Bestaetigung sofort als `SchrittFoto`-Zeile persistiert -- kein Zwischenzustand, kein temporaeres Verzeichnis
- **Label:** Wird bei jeder Checkbox-Umschaltung sofort in die DB geschrieben
- **Wiederholen:** Der Tap schreibt nichts. Ersetzt wird erst nach erfolgreicher neuer Aufnahme -- dann aber sofort und in einer Operation: neue Zeile auf Position `p` anlegen, alte Zeile und alte Datei loeschen. Bei Abbruch bleibt alles, wie es war (Governance-Regel "Kamera")
- **`aktualisiertAm`:** Jeder dieser Schreibvorgaenge zieht `Reparaturvorgang.aktualisiertAm` mit (projektweite Invariante, siehe [../governance.md](../governance.md))
- **Abschluss:** `abgeschlossenAm` wird bei "Naechster Schritt" bzw. "Beenden" sofort geschrieben
- **Leerer Schritt beim Beenden:** Hat der offene Schritt beim "Beenden" **kein einziges Foto** (Kamera abgebrochen, keine Kamera-App), wird er **geloescht** statt abgeschlossen. Sonst bliebe ein Schritt ohne Fotos in der DB, erschiene als leeres Thumbnail und wuerde eine Schrittnummer verbrauchen, die der Mechaniker bereits auf ein physisches Label geschrieben haben koennte. Die Nummer wird beim naechsten Flow-Start erneut vergeben
- **Leerer Schritt nach abgebrochenem Schritt-Start:** Wird **verworfen**, und der Vorgaenger wird wieder geoeffnet (Abschnitt "Rollback beim Abbruch am frischen Schritt"). Diese Regel ersetzt die frueher hier stehende Gegenregel ("wird nicht verworfen, der Mechaniker geht bewusst weiter"). Deren Begruendung setzte voraus, dass die Kamera **nicht** von selbst aufgeht -- dann war das Weitergehen eine eigene Entscheidung und der leere Schritt ein bewusst uebersprungener. Seit die Kamera den Schritt eroeffnet, ist ihr Abbruch das Gegenteil davon: ein "doch nicht"
- **Nachtragen bleibt moeglich:** Ein Schritt, der ein Foto bekommen hat und spaeter ergaenzt werden soll, wird per Thumbnail-Sprung und "Weiteres Foto" nachgetragen. Verworfen wird ausschliesslich der Schritt, der **nie** eines hatte
- **Unterbrechung:** Ein Schritt ohne `abgeschlossenAm` wird beim naechsten Start erkannt und fortgesetzt
- **Orphaned Schritte:** Unterbrochene Schritte bleiben in der DB und werden beim Fortsetzen weiterbearbeitet (kein Loeschen). Verworfen wird ausschliesslich der fotolose Schritt beim "Beenden"
- **Orphaned Dateien:** Dateien in `photos/`, auf die keine DB-Zeile verweist, fallen unter die projektweite Cleanup-Regel (siehe [../governance.md](../governance.md))

## Abschluss der Demontage

### US-003.5: Demontage beenden

**Als** Mechaniker
**moechte ich** die Demontage jederzeit sauber beenden koennen
**damit** kein halber Schritt zurueckbleibt und ich den Vorgang spaeter genau dort fortsetze, wo ich aufgehoert habe

Die Akzeptanzkriterien dieser Story sind auf zwei Abschnitte verteilt: AK 1 und AK 4 stehen hier, AK 2 und AK 3 unter "Entry-Bedingungen" -- sie beschreiben, was der Mechaniker nach dem Beenden vorfindet.

**US-003.5 ist der alleinige Eigentuemer der Aktion "Beenden".** Die Wirkung des Buttons wird ausschliesslich hier beschrieben; [views/schritt-ansicht.md](views/schritt-ansicht.md) legt nur fest, **wann** er in der Aktionszeile sichtbar ist (US-003.2 AK 1 und AK 4).

### Aus US-003.5 AK 1: Beenden markiert den Schritt als abgeschlossen

- **Given** die Schritt-Ansicht zeigt den offenen Schritt mit mindestens einem Foto
  **When** der Mechaniker "Beenden" antippt
  **Then** wird der aktuelle Schritt als abgeschlossen markiert (`abgeschlossenAm` gesetzt)
  **And** es wird kein neuer Schritt angelegt
  **And** die Demontage-Ansicht schliesst sich
  **And** die Vorgangs-Uebersicht (F-001) wird angezeigt

### Aus US-003.5 AK 4: Ein Schritt ohne Fotos wird beim Beenden verworfen

- **Given** der Mechaniker hat 4 Schritte dokumentiert, danach startete die System-Kamera fuer Schritt 5 und er hat sie abgebrochen
  **When** er in der Schritt-Ansicht "Beenden" antippt
  **Then** wird Schritt 5 geloescht statt abgeschlossen
  **And** die Uebersicht zeigt genau 4 Schritte, kein leeres Thumbnail
  **And** beim naechsten "Weiter demontieren" wird wieder ein Schritt mit `schrittNummer = 5` angelegt

## Entry-Bedingungen

Der Demontage-Flow wird gestartet:
- Aus der Vorgangs-Uebersicht (F-001): Der Mechaniker tippt auf den Vorgang. Hat der Vorgang noch keinen Schritt, oeffnet sich der Demontage-Flow direkt; ab dem ersten Schritt erscheint der Auswahl-Dialog und der Mechaniker waehlt **"Weiter demontieren"**. Dialog und Beschriftung gehoeren F-001 (US-001.2) -- F-003 uebernimmt den dortigen Wortlaut unveraendert
- Direkt nach Vorgang-Anlage (F-002): Automatischer Uebergang in den Demontage-Flow. F-002 bringt **keinen** Schritt mit -- der Vorgang ist leer, die Entry-Transition "Flow starten" greift, Schritt 1 entsteht hier und die System-Kamera startet automatisch

### Aus US-003.4 AK 6: Fortsetzung nach Unterbrechung

- **Given** der Demontage-Flow wurde bei Schritt 5 unterbrochen (App geschlossen)
  **When** der Mechaniker den Vorgang erneut oeffnet und die Demontage fortsetzt
  **Then** prueft die App, ob Schritt 5 abgeschlossen ist (`abgeschlossenAm` vorhanden):
  - **Falls ja:** Schritt 6 wird angelegt und die System-Kamera startet automatisch
  - **Falls nein:** Die Schritt-Ansicht fuer Schritt 5 wird angezeigt, mit allen bereits aufgenommenen Fotos

### Aus US-003.5 AK 2: Alle Schritte sichtbar nach Beenden

- **Given** der Mechaniker hat 5 Schritte dokumentiert und die Demontage beendet
  **When** die Vorgangs-Uebersicht angezeigt wird
  **Then** sind alle 5 Schritte mit ihren Fotos im Vorgang sichtbar

### Aus US-003.5 AK 3: Fortsetzung mit korrekter Nummer

- **Given** die Demontage wurde beendet und die Uebersicht zeigt 5 Schritte
  **When** der Mechaniker den Vorgang erneut antippt und im Auswahl-Dialog (F-001) "Weiter demontieren" waehlt
  **Then** wird Schritt 6 angelegt und die System-Kamera startet automatisch
  **And** die Schritt-Ansicht zeigt danach "Schritt 6"

## App-Unterbrechungs-Verhalten

| Unterbrechung bei... | Persistierter Zustand | Verhalten beim Fortsetzen |
|----------------------|----------------------|--------------------------|
| `KAMERA` (System-Kamera im Vordergrund, kein Foto bestaetigt) | `Schritt` in DB (`abgeschlossenAm = null`), alle vorher aufgenommenen Fotos als `SchrittFoto` -- bei einer laufenden "Wiederholen"-Runde also auch das noch nicht ersetzte alte Foto; vorbereitete Zieldatei ohne DB-Zeile | Schritt-Ansicht fuer den **offenen** Schritt, mit unveraendertem Foto-Bestand. Die verwaiste Zieldatei faellt unter die Cleanup-Regel in [../governance.md](../governance.md) |
| `SCHRITT_ANSICHT` | `Schritt` in DB (`abgeschlossenAm = null`), alle Fotos und Labels persistiert. Der **betrachtete** Schritt wird nicht persistiert | Schritt-Ansicht fuer den **offenen** Schritt (`abgeschlossenAm = null`), Karussell zeigt dessen Fotos in ihrer `reihenfolge` |
| Direkt nach "Naechster Schritt" (Schritt N+1 noch ohne Foto) | Schritt N abgeschlossen, Schritt N+1 offen ohne Fotos | Schritt-Ansicht fuer Schritt N+1 (Karussell im Leer-Zustand, F-006 US-006.9). **Kein Rollback:** ein Prozess-Tod ist kein Abbruch -- die App hat nie ein Abbruch-Ergebnis der Kamera gesehen und darf einen Schritt nicht auf Verdacht loeschen. Der Mechaniker holt die Aufnahme per "Weiteres Foto" nach |
| Direkt nach "Beenden" | Alle Schritte abgeschlossen; ein fotoloser letzter Schritt wurde dabei verworfen | Beim Fortsetzen wird ein neuer Schritt angelegt und die System-Kamera startet automatisch |

**Der betrachtete Schritt ueberlebt eine Unterbrechung nicht.** Ein Sprung ueber die Thumbnail-Leiste oder ueber "Zurueck"/"Weiter" ist ein reiner ViewModel-State und wird bewusst **nicht** persistiert. Schliesst der Mechaniker die App, waehrend er Schritt 2 betrachtet und Schritt 5 offen ist, zeigt die Schritt-Ansicht nach dem Neustart **Schritt 5** -- also denselben Zustand wie die Entry-Transition "Flow fortsetzen". Begruendung: Der Mechaniker nimmt seine Arbeit dort wieder auf, wo sie unfertig ist; ein Wiedereinstieg mitten in einer abgeschlossenen Dokumentation wuerde ihn ueberraschen. Zu Schritt 2 kommt er mit einem Tap zurueck.

## Navigation zwischen Schritten und Flow-Ausstieg (US-003.6)

### US-003.6: Zwischen Schritten navigieren und den Flow gezielt verlassen

**Als** Mechaniker
**moechte ich** waehrend der Demontage frei zwischen meinen Schritten springen koennen, ohne den Flow versehentlich zu verlassen
**damit** ich einen frueheren Schritt nachschlagen oder ergaenzen kann und trotzdem keinen unfertigen Schritt zuruecklasse

**Wege der Schritt-Navigation:** Es gibt in der Demontage genau drei -- die **Thumbnail-Leiste** (F-006 US-006.2), die Bedienelemente **"Zurueck"/"Weiter"** (F-006 US-006.10) und die Schritt-Aktion **"Zurueck zu Schritt N"** (F-003). Die ersten beiden gehoeren vollstaendig F-006 und werden im bearbeitbaren Modus genauso komponiert wie in den lesenden Modi; F-003 spezifiziert dafuer keine eigenen Bedienelemente, sondern reagiert nur auf die Callbacks und schreibt den Index des betrachteten Schritts fort. Die Abgrenzung zur Aktionszeile steht in [views/schritt-ansicht.md](views/schritt-ansicht.md), Abschnitt "Zwei Bedienelement-Gruppen".

#### Akzeptanzkriterien

- **Given** die Schritt-Ansicht ist aktiv
  **When** der Mechaniker die Android-Zurueck-Taste drueckt
  **Then** bleibt die Schritt-Ansicht sichtbar und der Demontage-Flow aktiv
  **And** es wird kein Schritt abgeschlossen und keine Aenderung verworfen

- **Given** der Vorgang hat mehrere Schritte und die Schritt-Ansicht zeigt den offenen Schritt 5
  **When** der Mechaniker in der Thumbnail-Leiste das Thumbnail von Schritt 2 antippt
  **Then** zeigt die Schritt-Ansicht Schritt 2 mit dessen Schrittnummer und dessen Fotos
  **And** Schritt 5 bleibt offen (`abgeschlossenAm` unveraendert `null`)
  **And** es wird kein neuer Schritt angelegt

- **Given** die Schritt-Ansicht zeigt nach einem Sprung den frueheren Schritt 2
  **When** der Mechaniker in der Thumbnail-Leiste das Thumbnail von Schritt 5 antippt
  **Then** zeigt die Schritt-Ansicht wieder Schritt 5 mit dessen Fotos

- **Given** der Vorgang hat 5 Schritte und die Schritt-Ansicht zeigt den offenen Schritt 5
  **When** der Mechaniker "Zurueck" der F-006-Schritt-Navigation antippt
  **Then** zeigt die Schritt-Ansicht Schritt 4 mit dessen Schrittnummer und dessen Fotos
  **And** Schritt 5 bleibt offen (`abgeschlossenAm` unveraendert `null`), es wird kein Schritt angelegt, abgeschlossen oder geloescht
  **And** der Back-Stack ist unveraendert (auch dieser Wechsel ist ein State-Wechsel, keine Navigation)

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker "Weiter" der F-006-Schritt-Navigation antippt
  **Then** zeigt die Schritt-Ansicht Schritt 3 mit dessen Fotos
  **And** es wird kein Schritt abgeschlossen und keiner angelegt -- "Weiter" blaettert nur, es ist **nicht** die Aktion "Naechster Schritt"

- **Given** die Schritt-Ansicht zeigt den **offenen** Schritt 5, der die hoechste `schrittNummer` des Vorgangs hat
  **When** die Schritt-Ansicht dargestellt wird
  **Then** ist "Weiter" sichtbar, aber **deaktiviert** (F-006 US-006.10) -- der offene Schritt ist immer der letzte der Anzeige-Reihenfolge
  **And** ein Tap darauf bewirkt nichts
  **And** die Schritt-Aktion "Naechster Schritt" ist davon unabhaengig sichtbar und aktiv

- **Given** die Schritt-Ansicht zeigt den **ersten** Schritt des Vorgangs (`schrittNummer = 1`)
  **When** die Schritt-Ansicht dargestellt wird
  **Then** ist "Zurueck" sichtbar, aber **deaktiviert** (F-006 US-006.10)
  **And** es wird nicht ausgeblendet -- das Layout der Schritt-Ansicht verschiebt sich beim Blaettern nicht

- **Given** der Vorgang hat genau einen Schritt
  **When** die Schritt-Ansicht dargestellt wird
  **Then** sind "Zurueck" und "Weiter" beide sichtbar und beide deaktiviert
  **And** die Thumbnail-Leiste zeigt genau ein Thumbnail

- **Given** die Schritt-Ansicht zeigt nach einem Sprung den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker "Zurueck zu Schritt 5" antippt
  **Then** zeigt die Schritt-Ansicht wieder den offenen Schritt 5 mit dessen Fotos
  **And** es wird kein Schritt abgeschlossen, keiner angelegt und keiner geloescht
  **And** der Back-Stack ist unveraendert (der Sprung war ein State-Wechsel, keine Navigation)

- **Given** die Vollbild-Anzeige eines Fotos (F-006) ist geoeffnet
  **When** der Mechaniker die Android-Zurueck-Taste drueckt
  **Then** schliesst sich die Vollbild-Anzeige
  **And** die Schritt-Ansicht bleibt sichtbar und der Flow aktiv

- **Given** der Demontage-Flow ist aktiv
  **When** der Mechaniker die Demontage verlassen moechte
  **Then** ist "Beenden" in der Schritt-Ansicht der einzige Weg zurueck zur Vorgangs-Uebersicht (F-001)

**Hinweis:** Back und Schritt-Navigation sind zwei verschiedene Dinge und werden hier bewusst getrennt:

- **Back verlaesst den Flow nicht.** Ist die F-006-Vollbild-Anzeige geoeffnet, konsumiert sie die Back-Geste und schliesst sich; sonst bewirkt Back nichts. Es wird dabei nie ein Schritt abgeschlossen oder eine Aenderung verworfen.
- **Die Navigation zwischen Schritten ist frei.** Sie laeuft ueber die Thumbnail-Leiste (F-006), ueber "Zurueck"/"Weiter" (F-006) und ueber "Zurueck zu Schritt N" (F-003) -- nicht ueber Back und nicht ueber horizontales Wischen (Wischen wechselt das Foto innerhalb des Schritts, siehe [views/schritt-ansicht.md](views/schritt-ansicht.md)).
- **Der Ausstieg** aus dem Flow bleibt an "Beenden" gebunden, damit kein Schritt ohne `abgeschlossenAm` zurueckbleibt. Weder "Zurueck" noch die Back-Taste verlaesst den Flow; "Zurueck" am ersten Schritt ist deaktiviert und fuehrt insbesondere **nicht** zur Uebersicht.

**Hinweis:** Eine Eingabe der Zielschrittnummer gibt es nicht. Der Sprung geschieht ueber das Antippen eines Thumbnails oder schrittweise ueber "Zurueck"/"Weiter"; zurueck zum offenen Schritt fuehrt zusaetzlich der Button "Zurueck zu Schritt N".

### Technisches Detail

- Navigation ueber `NavController` (Jetpack Navigation)
- State Hoisting: ViewModel haelt State, Screen ist stateless
- `BackHandler` in der Schritt-Ansicht: faengt die Back-Geste ab, ohne den Flow zu verlassen. Er ist **nur aktiv, solange die F-006-Vollbild-Anzeige geschlossen ist** -- das Vollbild ist ein Overlay/Dialog innerhalb der Schritt-Ansicht und konsumiert Back zuerst. Ein unbedingter `BackHandler` wuerde das Vollbild unschliessbar machen
- Der Sprung ueber die Thumbnail-Leiste, "Zurueck"/"Weiter" und "Zurueck zu Schritt N" sind keine Navigations-Ereignisse, sondern State-Wechsel im ViewModel (welcher `Schritt` wird angezeigt) -- der Back-Stack bleibt unveraendert
- Das ViewModel unterscheidet den **betrachteten** Schritt (Anzeige, Ziel von "Weiteres Foto") vom **offenen** Schritt (`abgeschlossenAm = null`, Ziel von "Naechster Schritt"/"Beenden"). Sind beide identisch, zeigt die Aktionszeile die drei Standard-Buttons; sonst die beiden Sprung-Aktionen
- Thumbnail-Leiste, Foto-Karussell, die Bedienelemente "Zurueck"/"Weiter" und die Vollbild-Ansicht kommen aus F-006 (bearbeitbarer Modus). F-003 uebergibt die Schritte **aufsteigend nach `schrittNummer`** und haelt den Index des betrachteten Schritts; die Grenzpruefung fuer "Zurueck"/"Weiter" macht F-006
- Die F-006-Callbacks `onSchrittGewaehlt`, `onVorherigerSchritt` und `onNaechsterSchritt` setzen im ViewModel ausschliesslich den betrachteten Schritt neu. `onNaechsterSchritt` ist trotz des Namens **nicht** die Schritt-Aktion "Naechster Schritt" und darf nie mit deren Handler verdrahtet werden
