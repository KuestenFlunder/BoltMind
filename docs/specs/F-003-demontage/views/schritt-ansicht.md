# Schritt-Ansicht

## Zweck

Der einzige App-Screen des Demontage-Flows. Er zeigt den **betrachteten** Schritt: dessen Schrittnummer, dessen Fotos als Karussell, die Label-Checkboxen am sichtbaren Foto sowie die Thumbnail-Leiste und die Bedienelemente "Zurueck"/"Weiter" ueber alle Schritte des Vorgangs. Von hier aus wird die System-Kamera gestartet, der Schritt abgeschlossen oder der Flow beendet.

Betrachtet wird normalerweise der **offene** Schritt (`abgeschlossenAm = null`). Nach einem Wechsel ueber die Schritt-Navigation von F-006 -- Thumbnail-Sprung oder "Zurueck"/"Weiter" -- kann es auch ein bereits abgeschlossener Schritt sein; dann bietet die Aktionszeile nur die beiden Aktionen an, die eindeutig zuzuordnen sind (siehe "Aktionszeile").

Die frueheren Screens Preview-View (Foto-Bestaetigung), Arbeitsphase-View und Dialog-View gehen vollstaendig in dieser View auf. Die Schritt-Ansicht ist damit auch der Ankerpunkt fuer die spaetere Arbeitsphase-Erweiterung (Timer F-005, Kommentare, Sprachnotizen).

## UI-Elemente

- **Schrittnummer** (gross, prominent, deutlich lesbar -- damit der Mechaniker die Nummer auf ein physisches Label uebertragen kann)
- **Foto-Karussell des Schritts** -- horizontal wischbar ueber die N Fotos dieses Schritts, in `reihenfolge`. Das Wischen wechselt **nur das Foto innerhalb des Schritts**, nie den Schritt. Tap auf ein Foto oeffnet die Vollbild-Ansicht. Bereitgestellt von **F-006** (bearbeitbarer Modus).
- **Leerzustand des Karussells** -- hat der Schritt noch kein Foto (System-Kamera abgebrochen oder keine Kamera-App), zeigt F-006 den Leer-Zustand "Keine Fotos zu diesem Schritt"; die Label-Checkboxen entfallen dann (F-006 US-006.9). Das Platzhalter-Bild (App-Icon) erscheint davon getrennt nur bei einer **fehlenden Datei** zu einem vorhandenen Foto-Datensatz (F-006 US-006.8).
- **Label-Checkboxen** ("Bauteil" / "Uebersicht" / "Ablageort") am **aktuell sichtbaren** Foto -- eingebettet aus **F-006** (bearbeitbarer Modus). Darstellung, Vorauswahl, Kombinierbarkeit und Umschaltverhalten sind in **F-006 US-006.6** spezifiziert und werden hier bewusst **nicht** wiederholt. F-003 reagiert nur auf das gemeldete Label-Ereignis und persistiert sofort.
- **Aktion "Wiederholen"** am aktuell sichtbaren Foto -- startet die System-Kamera erneut und ersetzt dieses Foto **erst nach einer erfolgreichen neuen Aufnahme**; bei Abbruch bleibt das alte Foto unveraendert erhalten (projektweite Regel, siehe [../../governance.md](../../governance.md), Abschnitt "Kamera"). Sie ist keine der Schritt-Aktionen, sondern eine **Foto-Aktion des Consumers**: F-006 stellt im bearbeitbaren Modus den Platz und den Callback fuer eine Consumer-Aktion am sichtbaren Foto bereit, Beschriftung und Verhalten liefert F-003. Welches Foto gemeint ist, ergibt sich aus dem von F-006 gemeldeten sichtbaren Foto -- F-003 rendert nichts selbst in das Karussell hinein.
- **Thumbnail-Leiste** ueber alle Schritte des Vorgangs -- eingebettet aus **F-006**, Aufbau und Verhalten dort spezifiziert.
- **Schritt-Navigation "Zurueck" / "Weiter"** -- eingebettet aus **F-006** (US-006.10), Teil des Browser-Rahmens. Ein Tap wechselt den **betrachteten** Schritt um genau eine Position in der Anzeige-Reihenfolge; F-003 liefert die Schritte aufsteigend nach `schrittNummer`, "Weiter" fuehrt also zur naechsthoeheren Schrittnummer. Beide Bedienelemente werden im bearbeitbaren Modus genauso komponiert wie in den beiden lesenden Modi und sind **immer sichtbar**; an den Raendern der Anzeige-Reihenfolge sind sie **deaktiviert, nicht ausgeblendet**. Sie schreiben nichts in die DB.
- **Aktionszeile (Schritt-Aktionen von F-003)** -- eine von der F-006-Schritt-Navigation **getrennte** Bedienelement-Gruppe (siehe "Zwei Bedienelement-Gruppen"). Welche Schritt-Aktionen sichtbar sind, haengt allein davon ab, ob der betrachtete Schritt der offene ist:

  | Betrachteter Schritt | Sichtbare Schritt-Aktionen |
  |---|---|
  | Der offene Schritt N (`abgeschlossenAm = null`) | "Weiteres Foto" (Foto an Schritt N), "Naechster Schritt" (Schritt N abschliessen, N+1 beginnen), "Beenden" (Schritt N abschliessen, zurueck zur Uebersicht F-001) |
  | Ein abgeschlossener Schritt M (nach Thumbnail-Sprung oder nach "Zurueck"/"Weiter") | "Weiteres Foto" (Foto an den **betrachteten** Schritt M), "Zurueck zu Schritt N" (zurueck zum offenen Schritt N) |

  Im zweiten Fall sind "Naechster Schritt" und "Beenden" **ausgeblendet**, nicht deaktiviert -- so kann nicht verwechselt werden, auf welchen Schritt sich eine Aktion bezieht. Auf welchem Weg der betrachtete Schritt gewechselt wurde, spielt fuer die Aktionszeile keine Rolle.

## Zwei Bedienelement-Gruppen

Die Schritt-Ansicht enthaelt **zwei** klar getrennte Gruppen von Bedienelementen. Sie werden weder in der Beschriftung noch im Layout vermischt:

| Gruppe | Eigentuemer | Bedienelemente | Wirkung |
|---|---|---|---|
| **Schritt-Navigation** | F-006 (US-006.2 Thumbnail-Sprung, US-006.10 Vor/Zurueck) | Thumbnail-Leiste, "Zurueck", "Weiter" | Wechselt nur, **welcher** Schritt betrachtet wird. Kein DB-Write; es wird kein Schritt angelegt, abgeschlossen oder geloescht |
| **Aktionszeile** | F-003 (US-003.2) | "Weiteres Foto", "Naechster Schritt", "Beenden" bzw. "Weiteres Foto", "Zurueck zu Schritt N" | Aendert Daten oder verlaesst den Flow: Kamera starten, Schritt abschliessen, Schritt N+1 anlegen, zurueck zur Uebersicht |

Damit eine Verwechslung ausgeschlossen ist, gilt verbindlich:

- Die Aktionszeile liegt **ausserhalb** des F-006-Rahmens. "Zurueck" und "Weiter" gehoeren sichtbar zum Browser-Bereich (Karussell + Thumbnail-Leiste), die Aktionszeile bildet einen eigenen Block.
- Die Beschriftungen sind eindeutig verschieden: das Navigations-Element heisst **"Weiter"**, die Schritt-Aktion **"Naechster Schritt"**. Das Navigations-Element heisst **"Zurueck"**, die Schritt-Aktion nennt immer die Nummer des offenen Schritts -- **"Zurueck zu Schritt 5"**.
- Solange der betrachtete Schritt der offene ist -- der Normalfall der Demontage -- ist **"Weiter" deaktiviert**: der offene Schritt hat immer die hoechste `schrittNummer` und ist damit der letzte der Anzeige-Reihenfolge. Ein aktives "Weiter" gibt es in der Demontage nur, waehrend ein frueherer Schritt betrachtet wird. "Naechster Schritt" ist in genau diesem Zustand umgekehrt ausgeblendet.
- Der F-006-Callback fuer "Weiter" heisst `onNaechsterSchritt` (F-006 browser.md). Das ist ein reiner Navigations-Callback und hat mit der F-003-Aktion "Naechster Schritt" nichts zu tun; F-003 verdrahtet ihn ausschliesslich auf den Wechsel des betrachteten Schritts.
- Die Sichtbarkeits-Regel aus der Aktionszeilen-Tabelle gilt fuer **jeden** Wechsel des betrachteten Schritts -- Thumbnail-Sprung wie "Zurueck"/"Weiter".

## Verhalten + DB-Interaktion

**Zaehlweise (verbindlich fuer diese Spec):** `SchrittFoto.reihenfolge` ist **0-basiert** -- das erste Foto eines Schritts hat `reihenfolge = 0`. Fachlich wird vom "ersten", "zweiten", "dritten" Foto gesprochen; der Positions-Indikator von F-006 zaehlt 1-basiert ("1 von 3"). Das Wort "Position" wird nur zusammen mit einem konkreten `reihenfolge`-Wert verwendet. Eine Formulierung wie "Position 1" fuer das erste Foto kommt nicht vor.

### US-003.1: Foto zum Schritt aufnehmen

**Als** Mechaniker
**moechte ich** mit moeglichst wenigen Handgriffen ein oder mehrere Fotos zu einem Schritt aufnehmen
**damit** ich den Zustand am Fahrzeug dokumentiere, ohne meine Arbeit lange zu unterbrechen

#### Akzeptanzkriterien

##### AK 1: System-Kamera startet automatisch beim Schritt-Start

- **Given** ein Reparaturvorgang existiert, ist im Status OFFEN und hat **keinen** Schritt mit `abgeschlossenAm = null`
  **When** der Mechaniker den Demontage-Flow startet (ueber die Uebersicht oder direkt nach der Anlage)
  **Then** wird ein neuer `Schritt` in der DB angelegt mit `gestartetAm` = aktueller Timestamp
  **And** die System-Kamera wird automatisch gestartet (kein Button-Tap zum Oeffnen noetig)

  *(Den Gegenfall -- es existiert bereits ein offener Schritt -- regelt AK 9.)*

##### AK 2: Foto bestaetigt -- sofort persistiert

- **Given** die System-Kamera wurde fuer ein **neues** Foto geoeffnet (Schritt-Start oder "Weiteres Foto", **nicht** "Wiederholen")
  **When** der Mechaniker die Aufnahme in der System-Kamera bestaetigt
  **Then** ist das neue Foto als letztes im Karussell des Schritts sichtbar -- es bekommt `reihenfolge` = Anzahl der bisherigen Fotos dieses Schritts
  **And** das Label "Bauteil" ist angehakt, "Uebersicht" und "Ablageort" nicht
  **And** es erscheint keine app-eigene Nachfrage zum Bestaetigen des Fotos
  **And** das Foto ist nach einem App-Neustart unveraendert vorhanden (Sofort-Save)

  *(Nach "Wiederholen" gilt stattdessen AK 6: das Ersatzfoto uebernimmt die `reihenfolge` des ersetzten Fotos und wird **nicht** hinten angehaengt. Die zugehoerigen Felder und DB-Operationen stehen gesammelt im Abschnitt "DB-Interaktion".)*

##### AK 3: System-Kamera wird abgebrochen

- **Given** die System-Kamera wurde fuer ein **neues** Foto geoeffnet (Schritt-Start oder "Weiteres Foto", **nicht** "Wiederholen")
  **When** der Mechaniker die Aufnahme abbricht (Back-Taste oder Abbrechen)
  **Then** wird kein Foto zum Schritt hinzugefuegt und keine Datei behalten
  **And** die Schritt-Ansicht wird angezeigt
  **And** hat der Schritt noch kein Foto, zeigt der Karussell-Bereich den Leer-Zustand aus F-006 (US-006.9)

  *(Den Abbruch nach "Wiederholen" -- das alte Foto bleibt dabei unveraendert erhalten -- regelt AK 6.)*

##### AK 4: Keine Kamera-App verfuegbar

- **Given** auf dem Geraet ist keine App installiert, die eine Foto-Aufnahme bedienen kann
  **When** die System-Kamera gestartet werden soll
  **Then** erscheint ein Hinweis-Dialog "Keine Kamera-App gefunden"
  **And** die Schritt-Ansicht wird angezeigt, ohne dass ein Foto entsteht

##### AK 5: Weiteres Foto am selben Schritt

- **Given** die Schritt-Ansicht zeigt einen Schritt mit mindestens einem Foto
  **When** der Mechaniker "Weiteres Foto" antippt und die Aufnahme in der System-Kamera bestaetigt
  **Then** haengt das neue Foto als letztes im Karussell desselben Schritts
  **And** die Schrittnummer bleibt unveraendert
  **And** das Karussell enthaelt beide Fotos

##### AK 6: Foto wiederholen -- das Ersatzfoto behaelt seinen Platz

*(Zaehlweise siehe Abschnittsanfang: `reihenfolge` ist 0-basiert, das **erste** Foto hat `reihenfolge = 0`.)*

*(Reihenfolge der Operationen: zuerst die System-Kamera, **erst nach** einer erfolgreichen Aufnahme wird das alte Foto ersetzt -- projektweite Regel, siehe [../../governance.md](../../governance.md), Abschnitt "Kamera".)*

- **Given** ein Schritt hat 3 Fotos mit `reihenfolge` 0, 1 und 2 und das Karussell zeigt das **erste** Foto (`reihenfolge = 0`)
  **When** der Mechaniker "Wiederholen" an diesem Foto antippt
  **Then** wird die System-Kamera gestartet, ohne dass etwas geloescht wird -- das alte Foto und seine Datei bleiben zunaechst bestehen

- **Given** ein Schritt hat 3 Fotos mit `reihenfolge` 0, 1 und 2 und der Mechaniker hat "Wiederholen" am **ersten** Foto (`reihenfolge = 0`) ausgeloest
  **When** er die neue Aufnahme in der System-Kamera bestaetigt
  **Then** wird erst jetzt ersetzt: das neue Foto bekommt `reihenfolge = 0`, danach sind das alte Foto und seine Datei geloescht
  **And** das zweite und das dritte Foto behalten `reihenfolge` 1 und 2 -- es wird weder umnummeriert noch hinten angehaengt
  **And** das Karussell zeigt weiterhin das erste Foto (F-006-Indikator "1 von 3")
  **And** das Thumbnail des Schritts (F-006 zeigt das erste Foto) zeigt das neue Foto

- **Given** ein Schritt hat 3 Fotos mit `reihenfolge` 0, 1 und 2 und der Mechaniker hat "Wiederholen" am **ersten** Foto (`reihenfolge = 0`) ausgeloest
  **When** er die System-Kamera abbricht (oder es ist keine Kamera-App vorhanden)
  **Then** hat der Schritt unveraendert 3 Fotos mit lueckenloser `reihenfolge` 0, 1 und 2 -- es wurde nichts geloescht und nichts eingefuegt
  **And** das alte Foto an `reihenfolge = 0` ist samt Datei weiterhin vorhanden
  **And** das Karussell zeigt weiterhin genau dieses Foto (F-006-Indikator "1 von 3")
  **And** geloescht wird nur die vorbereitete, leer gebliebene Zieldatei

##### AK 7: Schritt ohne Foto

- **Given** die Schritt-Ansicht zeigt einen Schritt ohne Foto
  **When** der Mechaniker "Weiteres Foto" antippt
  **Then** wird die System-Kamera gestartet
  **And** nach Bestaetigung ist das Foto das erste Foto dieses Schritts

##### AK 8: "Weiteres Foto" wirkt auf den betrachteten Schritt

- **Given** die Schritt-Ansicht zeigt nach einem Thumbnail-Sprung den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker "Weiteres Foto" antippt und die Aufnahme bestaetigt
  **Then** haengt das neue Foto an **Schritt 2**, als dessen letztes Foto
  **And** Schritt 2 bleibt abgeschlossen (`abgeschlossenAm` unveraendert) und Schritt 5 bleibt offen
  **And** die Ansicht zeigt weiterhin Schritt 2

##### AK 9: Fortsetzen legt keinen zweiten offenen Schritt an

- **Given** ein Reparaturvorgang ist im Status OFFEN und hat bereits einen Schritt 5 mit `abgeschlossenAm = null`
  **When** der Mechaniker den Demontage-Flow startet (ueber die Uebersicht oder direkt nach der Anlage)
  **Then** wird **kein** neuer `Schritt` angelegt -- Schritt 5 wird geladen und ist zugleich der betrachtete und der offene Schritt
  **And** die System-Kamera wird **nicht** automatisch gestartet: der Mechaniker sieht zuerst die bereits aufgenommenen Fotos von Schritt 5
  **And** der Vorgang hat danach weiterhin genau einen Schritt mit `abgeschlossenAm = null`
  **And** die Schritt-Ansicht zeigt "Schritt 5"

---

### US-003.2: Schritt abschliessen und weiterarbeiten

**Als** Mechaniker
**moechte ich** nach dem Dokumentieren eines Bauteils direkt entscheiden koennen, ob ich noch ein Foto brauche, zum naechsten Schritt gehe oder aufhoere
**damit** ich den Ablauf ohne Zwischendialoge in meinem Arbeitstempo steuere

**Abgrenzung zu US-003.5:** Die Akzeptanzkriterien der Aktion **"Beenden"** -- Schritt mit Foto abschliessen, fotolosen Schritt verwerfen, Rueckkehr zur Vorgangs-Uebersicht -- gehoeren der Story US-003.5 und stehen ausschliesslich in [workflow.md](../workflow.md) (AK 1 und AK 4). Hier wird nur festgelegt, **wann** der Button "Beenden" in der Aktionszeile sichtbar ist (AK 1 und AK 4).

#### Akzeptanzkriterien

##### AK 1: Schritt-Ansicht Anzeige

- **Given** der Demontage-Flow ist aktiv und die Ansicht zeigt den offenen Schritt
  **When** die Schritt-Ansicht angezeigt wird
  **Then** enthaelt sie die Schrittnummer, das Foto-Karussell dieses Schritts, die Thumbnail-Leiste ueber alle Schritte und die F-006-Schritt-Navigation "Zurueck"/"Weiter"
  **And** die Aktionszeile enthaelt genau die drei Schritt-Aktionen "Weiteres Foto", "Naechster Schritt" und "Beenden"
  **And** "Weiter" ist sichtbar, aber deaktiviert (der offene Schritt ist immer der letzte der Anzeige-Reihenfolge); "Zurueck" ist aktiv, sofern der Vorgang mehr als einen Schritt hat
  **And** die drei Label-Checkboxen sind sichtbar, sofern der Schritt mindestens ein Foto hat (bei einem Schritt ohne Fotos entfallen sie, F-006 US-006.9)

##### AK 2: Naechster Schritt

- **Given** die Schritt-Ansicht zeigt den offenen Schritt N
  **When** der Mechaniker "Naechster Schritt" antippt
  **Then** wird `abgeschlossenAm` am Schritt N gesetzt
  **And** ein neuer `Schritt` mit `schrittNummer` = N+1 und `gestartetAm` wird angelegt
  **And** die System-Kamera startet automatisch fuer den neuen Schritt

##### AK 3: Debounce der Aktions-Buttons

- **Given** die Schritt-Ansicht wird angezeigt
  **When** der Mechaniker einen Aktions-Button antippt
  **Then** ist dieser Button fuer 300ms deaktiviert
  **And** ein Doppel-Tap loest die Aktion nur einmal aus

##### AK 4: Betrachteter abgeschlossener Schritt -- nur zwei Schritt-Aktionen sichtbar

- **Given** Schritt 5 ist offen und der Mechaniker springt ueber die Thumbnail-Leiste auf den abgeschlossenen Schritt 2
  **When** die Schritt-Ansicht Schritt 2 zeigt
  **Then** enthaelt die Aktionszeile genau zwei Schritt-Aktionen: "Weiteres Foto" und "Zurueck zu Schritt 5"
  **And** "Naechster Schritt" und "Beenden" sind **nicht sichtbar** (ausgeblendet, nicht nur deaktiviert)
  **And** die Beschriftung "Zurueck zu Schritt 5" nennt die Nummer des offenen Schritts
  **And** die F-006-Schritt-Navigation ("Zurueck", "Weiter", Thumbnail-Leiste) bleibt davon unberuehrt sichtbar und nutzbar -- sie gehoert nicht zur Aktionszeile

##### AK 5: Zurueck zum offenen Schritt

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker "Zurueck zu Schritt 5" antippt
  **Then** zeigt die Schritt-Ansicht wieder Schritt 5 mit dessen Fotos
  **And** die Aktionszeile enthaelt wieder "Weiteres Foto", "Naechster Schritt" und "Beenden"
  **And** es wurde kein Schritt abgeschlossen, angelegt oder geloescht

##### AK 6: Foto-Aktionen bleiben am betrachteten Schritt

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 2 mit zwei Fotos
  **When** der Mechaniker am sichtbaren Foto ein Label umschaltet oder "Wiederholen" antippt
  **Then** wirkt die Aktion auf das sichtbare Foto von Schritt 2
  **And** der offene Schritt 5 bleibt unveraendert

##### AK 7: Aktionszeile nach einem Wechsel ueber "Zurueck" / "Weiter"

*(Das Verhalten der Navigation selbst -- Zielschritt, Deaktivierung an den Raendern, kein DB-Write -- steht bei US-003.6 in [workflow.md](../workflow.md). Hier geht es nur um die Aktionszeile.)*

- **Given** die Schritt-Ansicht zeigt den offenen Schritt 5 eines Vorgangs mit 5 Schritten
  **When** der Mechaniker "Zurueck" der F-006-Schritt-Navigation antippt und die Ansicht Schritt 4 zeigt
  **Then** enthaelt die Aktionszeile genau "Weiteres Foto" und "Zurueck zu Schritt 5"
  **And** "Naechster Schritt" und "Beenden" sind ausgeblendet -- dieselbe Regel wie nach einem Thumbnail-Sprung (AK 4)

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 4, waehrend Schritt 5 offen ist
  **When** der Mechaniker "Weiter" der F-006-Schritt-Navigation antippt und die Ansicht Schritt 5 zeigt
  **Then** enthaelt die Aktionszeile wieder "Weiteres Foto", "Naechster Schritt" und "Beenden"
  **And** "Weiter" ist jetzt deaktiviert, "Naechster Schritt" dagegen aktiv -- die beiden Bedienelemente sind nie gleichzeitig verwechselbar

---

### US-003.3: Fotos labeln

**Als** Mechaniker
**moechte ich** an jedem Foto markieren, ob es das Bauteil, eine Uebersicht oder den Ablageort zeigt
**damit** ich beim Zusammenbau sofort erkenne, wo das Teil liegt und wie es eingebaut war

*(Diese Story ersetzt die frueher eigenstaendige Ablageort-Foto-Story. Der Ablageort ist kein eigener Schritt und keine eigene View mehr, sondern ein Label an einem Foto.)*

**Abgrenzung zu F-006:** Darstellung und Bedienverhalten der Checkboxen -- Vorauswahl "Bauteil", Kombinierbarkeit, Abwaehlbarkeit aller Labels und das Nachfuehren beim Wischen -- sind in **F-006 US-006.6** spezifiziert und werden hier nicht wiederholt. F-003 ist Consumer: es reagiert auf die gemeldete Label-Aenderung und persistiert sie sofort. Die folgenden Kriterien beschreiben deshalb nur die Consumer-Verantwortung.

#### Akzeptanzkriterien

##### AK 1: Label setzen wird sofort gespeichert

- **Given** das Karussell zeigt ein Foto
  **When** der Mechaniker die Checkbox "Ablageort" aktiviert
  **Then** wird `istAblageort = true` sofort am `SchrittFoto` dieses Fotos gespeichert -- ohne Sammel-Bestaetigung und ohne "Speichern"-Button
  **And** die Aenderung ist nach einem App-Neustart noch vorhanden

##### AK 2: Auch das Abwaehlen wird sofort gespeichert

- **Given** das Karussell zeigt ein Foto mit genau einem aktivierten Label
  **When** der Mechaniker dieses Label abwaehlt
  **Then** ist das Foto ohne Label gespeichert und bleibt im Karussell sichtbar
  **And** die Aenderung ist nach einem App-Neustart noch vorhanden

##### AK 3: Ablageort eines Schritts ist ableitbar

- **Given** ein Schritt hat mindestens ein Foto mit dem Label "Ablageort"
  **When** der Schritt in der Thumbnail-Leiste oder in der Uebersicht dargestellt wird
  **Then** gilt er als Schritt mit Ablageort
  **And** ein Schritt ohne solches Foto gilt als Schritt ohne Ablageort

---

### Aus US-003.4 AK 1: Schrittnummer sichtbar

- **Given** der Demontage-Flow ist aktiv
  **When** die Schritt-Ansicht angezeigt wird
  **Then** wird die Schrittnummer des angezeigten Schritts prominent dargestellt (z.B. "Schritt 3")

### Aus US-003.4 AK 2: Schrittnummer bleibt gross und sichtbar

- **Given** die Schritt-Ansicht zeigt Schritt 3
  **When** der Mechaniker im Karussell wischt oder eine Vollbild-Anzeige schliesst
  **Then** ist "Schritt 3" unveraendert sichtbar
  **And** die Schrittnummer ist das groesste Text-Element der Schritt-Ansicht (der Mechaniker soll sie im Vorbeigehen auf ein physisches Label uebertragen koennen)

*(Der Story-Text von US-003.4 und die Akzeptanzkriterien zur Vergabe und Inkrementierung der Schrittnummer stehen in [workflow.md](../workflow.md).)*

## DB-Interaktion

| Aktion | DB-Operation |
|--------|-------------|
| Schritt beginnt (Flow-Start oder "Naechster Schritt") | Neuen `Schritt` anlegen: `reparaturvorgangId`, `schrittNummer`, `gestartetAm`. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| Foto in der System-Kamera bestaetigt -- Kamera kam aus Schritt-Start oder "Weiteres Foto" | Neues `SchrittFoto`: `schrittId` des **betrachteten** Schritts, `pfad`, `reihenfolge` = Anzahl bisheriger Fotos dieses Schritts (0-basiert, das Foto wird also hinten angehaengt), `istBauteil = true`, `istUebersicht = false`, `istAblageort = false`, `aufgenommenAm`. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| Foto in der System-Kamera bestaetigt -- Kamera kam aus **"Wiederholen"** | **Erst jetzt** wird ersetzt, in einer Operation: neues `SchrittFoto` mit denselben Feldern, aber `reihenfolge` = `p` (die `reihenfolge` des ersetzten Fotos); die alte `SchrittFoto`-Zeile und die alte Datei werden geloescht. Die uebrigen Fotos behalten ihre `reihenfolge` -- es wird weder umnummeriert noch hinten angehaengt (Details: [workflow.md](../workflow.md), "Foto-Flow Logik" Punkt 7). Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| System-Kamera abgebrochen / keine Kamera-App | Kein DB-Write. Vorbereitete Zieldatei loeschen. Kam die Kamera aus "Wiederholen", bleiben das alte Foto und seine Datei unveraendert erhalten |
| Label-Checkbox umgeschaltet | Update auf dem `SchrittFoto` des sichtbaren Fotos (`istBauteil` / `istUebersicht` / `istAblageort`). Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| "Wiederholen" am sichtbaren Foto | **Kein DB-Write beim Tap.** Das alte Foto und seine Datei bleiben unveraendert; gemerkt werden nur die `SchrittFoto`-Id und `p` = deren `reihenfolge`. Ersetzt wird erst nach einer erfolgreichen neuen Aufnahme (siehe Zeile oben; Governance-Regel "Kamera", [../../governance.md](../../governance.md)) |
| "Weiteres Foto" | Kein DB-Write beim Tap. Das Foto wird nach der System-Bestaetigung am betrachteten Schritt angelegt (siehe oben) |
| Karussell wischen / Foto in Vollbild oeffnen | Kein DB-Write |
| Thumbnail eines anderen Schritts antippen (F-006) | Kein DB-Write. Fotos des gewaehlten Schritts werden geladen |
| "Zurueck" / "Weiter" der F-006-Schritt-Navigation | Kein DB-Write. Der betrachtete Schritt wechselt um eine Position in der Anzeige-Reihenfolge, dessen Fotos werden geladen. Kein Schritt wird angelegt, abgeschlossen oder geloescht |
| "Zurueck zu Schritt N" | Kein DB-Write. Fotos des offenen Schritts werden geladen |
| "Naechster Schritt" | `abgeschlossenAm` am offenen Schritt setzen **und** neuen `Schritt` (N+1) anlegen. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |
| "Beenden" | `abgeschlossenAm` am offenen Schritt setzen -- hat dieser Schritt kein Foto, wird er stattdessen geloescht. Zusaetzlich `Reparaturvorgang.aktualisiertAm` = jetzt |

**`aktualisiertAm`:** Jede DB-Aktion dieser Tabelle setzt in derselben Operation `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion. Das ist die projektweite Invariante aus [../../governance.md](../../governance.md) ("Invariante: `aktualisiertAm`") und wird hier nur genannt, nicht neu formuliert. Aktionen ohne DB-Write beruehren das Feld nicht.

## Nicht-funktionale Anforderungen

- **Touch-Targets:** Aktions-Buttons und "Wiederholen" muessen mit dreckigen Haenden/Handschuhen bedienbar sein. Die verbindlichen Mindestmasse (Hoehe und Abstand) stehen in [../../governance.md](../../governance.md); hier werden keine eigenen Zahlen genannt. Fuer die Label-Checkboxen, die Thumbnails und "Zurueck"/"Weiter" gilt dasselbe ueber F-006
- **Alle sichtbaren Aktions-Buttons gleich gut erreichbar:** kein vorausgewaehlter Default, keine versteckte Option
- **Abstand zwischen den Gruppen:** Die Aktionszeile muss optisch klar von "Zurueck"/"Weiter" abgesetzt sein, damit "Weiter" und "Naechster Schritt" nicht als ein Paar gelesen werden (siehe "Zwei Bedienelement-Gruppen")
- **Debounce:** 300ms fuer die Aktions-Buttons der Aktionszeile und "Wiederholen" (Label-Checkboxen, Thumbnails und "Zurueck"/"Weiter" bringt F-006 mit)
- **Foto-Speicherung darf UI nicht blockieren** (async)
- **Fehlende Foto-Datei:** Platzhalter-Bild statt Crash -- Darstellung liegt bei F-006 (US-006.8)
- **Foto-Qualitaet:** Erwartungswert und Umgang mit der von der System-Kamera gelieferten Datei stehen in [../../governance.md](../../governance.md)
- **Schrittnummer bleibt sichtbar,** auch wenn im Karussell gewischt wird

## Technische Hinweise

- **System-Kamera:** `ActivityResultContracts.TakePicture()` + `FileProvider` -- KEIN CameraX, keine `CAMERA`-Permission
- **Auto-Start:** Der Kamera-Intent wird beim Beginn eines Schritts automatisch ausgeloest (z.B. via `LaunchedEffect`), nicht durch einen Button-Tap
- **Zieldatei:** Wird vor dem Intent in `photos/` angelegt und als FileProvider-URI uebergeben. Kein `photos/temp/`, kein Verschiebe-Schritt nach der Aufnahme
- **Intent-Fehlerbehandlung:** Fehlt eine Kamera-App, wird der Hinweis-Dialog gezeigt und in die Schritt-Ansicht zurueckgekehrt
- **EXIF:** Metadaten werden nach der Rueckkehr aus der System-Kamera entfernt, bevor die `SchrittFoto`-Zeile geschrieben wird
- **F-006-Einbettung:** Foto-Karussell, Thumbnail-Leiste, die Schritt-Navigation "Zurueck"/"Weiter", Label-Checkboxen und Vollbild-Ansicht werden als Komponenten aus F-006 eingebunden (bearbeitbarer Modus). Diese View-Spec beschreibt nur, **welche** Komponenten sie einbettet und wie sie auf deren Ereignisse reagiert -- nicht deren internes Verhalten
- **Anzeige-Reihenfolge:** F-003 uebergibt die Schritte des Vorgangs **aufsteigend nach `schrittNummer`**. Daraus folgt die Grenzpruefung von F-006: der offene Schritt ist immer der letzte der Liste, "Weiter" ist dort deaktiviert
- **Verdrahtung der Navigations-Callbacks:** `onSchrittGewaehlt`, `onVorherigerSchritt` und `onNaechsterSchritt` (F-006) setzen im ViewModel ausschliesslich den **betrachteten** Schritt neu und loesen das Laden von dessen Fotos aus. Sie duerfen nie die Aktion "Naechster Schritt" ausloesen -- die Namensaehnlichkeit von `onNaechsterSchritt` ist rein zufaellig
- **Consumer-Aktion am sichtbaren Foto:** "Wiederholen" wird ueber die von F-006 bereitgestellte Consumer-Aktion am sichtbaren Foto eingehaengt (Beschriftung und Callback kommen aus F-003). F-003 zeichnet selbst nichts ins Karussell. Die Aktionszeile mit den Schritt-Aktionen bleibt davon getrennt und liegt ausserhalb des Browsers
- **Sichtbares Foto:** Die Karussell-Position ist der State, an dem Label-Checkboxen und "Wiederholen" haengen. Sie wird im ViewModel gehalten und mit dem von F-006 gemeldeten sichtbaren Foto synchron gehalten
- **Betrachteter vs. offener Schritt:** Das ViewModel haelt beides getrennt. Der **betrachtete** Schritt steuert Anzeige, Foto-Aktionen und "Weiteres Foto"; der **offene** Schritt (`abgeschlossenAm = null`) ist Ziel von "Naechster Schritt" und "Beenden". Sind beide identisch, zeigt die Aktionszeile die drei Standard-Buttons, sonst "Weiteres Foto" und "Zurueck zu Schritt N". Welcher Weg den betrachteten Schritt geaendert hat -- Thumbnail-Sprung oder "Zurueck"/"Weiter" -- ist dafuer ohne Bedeutung
- **State Hoisting:** Der Screen erhaelt State (Schrittnummer, Fotoliste mit Labels, Karussell-Position, Schrittliste fuer die Thumbnail-Leiste, Index des betrachteten Schritts, Nummer des offenen Schritts) und Callbacks vom ViewModel
- **Ankerpunkt F-005:** Die spaetere Zeiterfassung setzt an dieser View an; die Timer-Daten liegen in der eigenen Tabelle des Service-Features, nicht am `Schritt`
