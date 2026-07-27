# Schritt-Ansicht

## Zweck

Der einzige App-Screen des Demontage-Flows. Er zeigt den **betrachteten** Schritt: dessen Schrittnummer als grosse Ziffer, dessen Fotos als bildschirmfuellendes Karussell, die Label-Chips am sichtbaren Foto, den Timer-Chip und die Thumbnail-Leiste ueber alle Schritte des Vorgangs. Bedient wird er ueber drei Rundbuttons unten rechts und den Chip **„FEIERABEND"** oben rechts.

Betrachtet wird normalerweise der **offene** Schritt (`abgeschlossenAm = null`). Nach einem Sprung ueber die Thumbnail-Leiste (F-006) kann es auch ein bereits abgeschlossener Schritt sein; dann wechselt der grosse Rundbutton seine Bedeutung und „FEIERABEND" verschwindet (siehe „Der grosse Rundbutton").

Farben, Schriften, Glasrezepte und Maße stehen in [../../design-system.md](../../design-system.md); diese Spec nennt nur Wortlaute, Sichtbarkeit und Wirkung.

## Aufbau

Das Foto liegt ganzflaechig, alles Uebrige darueber:

| Ort | Element |
|---|---|
| oben links | **Schrittnummer**, zweistellig („05"), als 118sp-Ziffer — das groesste Element der Ansicht |
| oben links daneben | das Wort (woertlich) **„SCHRITT"** in der Leitfarbe, darunter **„#{Auftragsnummer}"** und die Beschreibung des Vorgangs (das Feld „WAS IST ZU TUN?" aus F-002) |
| oben rechts | Chip (woertlich) **„FEIERABEND"** — nur sichtbar, solange der betrachtete Schritt der offene ist |
| oben links, direkt unter der Kopfzeile | **Timer-Chip** mit `mm:ss` (US-003.7) |
| links unten | **Label-Chips** (woertlich) **„BAUTEIL"**, **„ÜBERSICHT"**, **„ABLAGEORT"** und darunter der Foto-Indikator, beides aus F-006 |
| rechter Rand, mittig | **Thumbnail-Leiste** ueber alle Schritte, aus F-006 |
| unten rechts | die drei **Rundbuttons** |

Aus F-006 eingebettet und dort spezifiziert — hier bewusst **nicht** wiederholt:

- **Foto-Karussell** des Schritts, horizontal wischbar ueber die N Fotos in `reihenfolge`. Das Wischen wechselt **nur das Foto innerhalb des Schritts**, nie den Schritt. Tap oeffnet die Vollbild-Ansicht (F-006 US-006.4 und US-006.5).
- **Leerzustand** „Keine Fotos zu diesem Schritt" (F-006 US-006.9). Das Platzhalter-Bild erscheint davon getrennt nur bei einer **fehlenden Datei** zu einem vorhandenen Foto-Datensatz (F-006 US-006.8).
- **Label-Chips** am aktuell sichtbaren Foto, im bearbeitbaren Modus (F-006 US-006.6). F-003 reagiert nur auf das gemeldete Label-Ereignis und persistiert sofort.
- **Foto-Indikator** „FOTO 1/3" bzw. „KEIN FOTO" und der Wisch-Hinweis „← WISCHEN".
- **Thumbnail-Leiste** und Thumbnail-Sprung (F-006 US-006.1 bis US-006.3).

### Die drei Rundbuttons

Sie liegen unten rechts nebeneinander, von links nach rechts:

| Kreis | Durchmesser | Beschriftung | Sichtbar | Wirkung |
|---|---|---|---|---|
| klein | 60dp | (woertlich) **„↺"** | nur wenn der betrachtete Schritt mindestens ein Foto hat | „Wiederholen" am sichtbaren Foto |
| mittel | 86dp | (woertlich) **„NOCH'N FOTO"** | immer | System-Kamera fuer ein weiteres Foto am **betrachteten** Schritt |
| gross | 124dp | zweistellige Nummer plus (woertlich) **„NÄCHSTES"** bzw. (woertlich) **„ZURÜCK ZU"** | immer | siehe unten |

Eine Aktionszeile oder -leiste gibt es nicht; die Rundbuttons sind die Bedienung.

### Der grosse Rundbutton: „NÄCHSTES nn" oder „ZURÜCK ZU nn"

Der grosse Kreis behaelt Groesse und Position, wechselt aber Bedeutung, Beschriftung und Flaeche, je nachdem ob der betrachtete Schritt der offene ist:

| Betrachteter Schritt | Kreis | Flaeche | `nn` |
|---|---|---|---|
| der offene Schritt N | „NÄCHSTES nn" | orange | die Nummer, die der naechste Schritt bekaeme (hoechste vergebene Nummer + 1) |
| ein abgeschlossener Schritt M | „ZURÜCK ZU nn" | neutral | die Nummer des offenen Schritts N |

**Begruendung (design-system.md, K-07):** Waere der Kreis immer „NÄCHSTES", haengt ein Fehltipp beim Nachschlagen eines alten Schritts eine Schrittnummer an, die moeglicherweise schon auf einem physischen Etikett klebt. Die Nummer ist die Korrelation zum Ablageort und wird nie umnummeriert.

Zusammen mit dem Kreis verschwindet am abgeschlossenen Schritt auch der Chip „FEIERABEND": beide Aktionen wirken auf den offenen Schritt, den der Mechaniker in diesem Moment nicht vor sich hat.

> **[OFFEN]** Hat der Vorgang gar keinen offenen Schritt — alle Schritte sind abgeschlossen und der Mechaniker steigt ueber „Weiter demontieren" (F-001) wieder ein —, hat „ZURÜCK ZU nn" kein Ziel und „NÄCHSTES nn" erscheint nicht. Ob in diesem Fall beim Einstieg ein neuer Schritt angelegt wird (so beschreibt es [../workflow.md](../workflow.md), Abschnitt „Entry-Bedingungen") oder der Kreis „NÄCHSTES nn" zeigt, ist zu entscheiden.

### Zwei Bedienelement-Gruppen

| Gruppe | Eigentuemer | Bedienelemente | Wirkung |
|---|---|---|---|
| **Schritt-Navigation** | F-006 (US-006.2 Thumbnail-Sprung) | Thumbnail-Leiste | Wechselt nur, **welcher** Schritt betrachtet wird. Kein DB-Write |
| **Bedienung** | F-003 (US-003.2) | „↺", „NOCH'N FOTO", grosser Kreis, „FEIERABEND" | Aendert Daten oder verlaesst den Flow |

Verbindlich:

- Die Rundbuttons liegen **ausserhalb** des F-006-Rahmens, die Thumbnail-Leiste am gegenueberliegenden Rand. Die Gruppen sind raeumlich getrennt.
- Die Demontage dockt die F-006-Bedienelemente **„Zurueck"/„Weiter"** (F-006 US-006.10) **nicht** an — sie sind eine Andockstelle, die der Consumer nutzt oder nicht (design-system.md, K-05). Schrittweises Blaettern gibt es in der Demontage nicht; Schritt-Navigation laeuft ueber die Thumbnail-Leiste und ueber „ZURÜCK ZU nn". Damit ist auch die Verwechslung von „Weiter" und „Naechster Schritt" gegenstandslos.
- Einen Sprung-Dialog mit Nummerneingabe gibt es nicht.
- „↺" und die Label-Chips haengen immer am **sichtbaren Foto** des betrachteten Schritts, nie am offenen Schritt.

## Verhalten + DB-Interaktion

**Zaehlweise (verbindlich fuer diese Spec):** `SchrittFoto.reihenfolge` ist **0-basiert** — das erste Foto eines Schritts hat `reihenfolge = 0`. Fachlich wird vom „ersten", „zweiten", „dritten" Foto gesprochen; der Positions-Indikator von F-006 zaehlt 1-basiert („FOTO 1/3"). Das Wort „Position" wird nur zusammen mit einem konkreten `reihenfolge`-Wert verwendet.

### US-003.1: Foto zum Schritt aufnehmen

**Als** Mechaniker
**moechte ich** mit moeglichst wenigen Handgriffen ein oder mehrere Fotos zu einem Schritt aufnehmen
**damit** ich den Zustand am Fahrzeug dokumentiere, ohne meine Arbeit lange zu unterbrechen

#### Akzeptanzkriterien

##### AK 1: Einstieg in den Flow

- **Given** ein Reparaturvorgang ist im Status OFFEN und hat einen Schritt mit `abgeschlossenAm = null`
  **When** der Mechaniker den Demontage-Flow betritt (aus der Uebersicht oder direkt nach der Anlage in F-002)
  **Then** zeigt die Schritt-Ansicht diesen offenen Schritt mit dessen Fotos
  **And** die System-Kamera wird beim Einstieg **nicht** automatisch gestartet — der Mechaniker loest sie ueber „NOCH'N FOTO" aus
  **And** hat der Schritt noch kein Foto, zeigt der Karussell-Bereich den Leer-Zustand aus F-006 (US-006.9)

> **[OFFEN]** Derselbe Punkt wie in [../../F-002-vorgang-anlegen/anlegen.md](../../F-002-vorgang-anlegen/anlegen.md), Abschnitt „[OFFEN] Kamera-Autostart fuer Schritt 1": der Design-Prototyp startet die Kamera direkt nach „LOS GEHT'S" fuer Schritt 1, der gebaute Flow nicht. Zu entscheiden.

##### AK 2: Foto bestaetigt — sofort persistiert

- **Given** die System-Kamera wurde fuer ein **neues** Foto geoeffnet („NOCH'N FOTO" oder Schritt-Start ueber „NÄCHSTES nn", **nicht** „↺")
  **When** der Mechaniker die Aufnahme in der System-Kamera bestaetigt
  **Then** ist das neue Foto als letztes im Karussell des Schritts sichtbar — es bekommt `reihenfolge` = Anzahl der bisherigen Fotos dieses Schritts
  **And** das Label „BAUTEIL" ist gesetzt, „ÜBERSICHT" und „ABLAGEORT" nicht
  **And** es erscheint keine app-eigene Nachfrage zum Bestaetigen des Fotos
  **And** das Foto ist nach einem App-Neustart unveraendert vorhanden (Sofort-Save)

  *(Nach „↺" gilt stattdessen AK 6: das Ersatzfoto uebernimmt die `reihenfolge` des ersetzten Fotos und wird **nicht** hinten angehaengt.)*

##### AK 3: System-Kamera wird abgebrochen

- **Given** die System-Kamera wurde fuer ein **neues** Foto geoeffnet (**nicht** „↺")
  **When** der Mechaniker die Aufnahme abbricht (Back-Taste oder Abbrechen)
  **Then** wird kein Foto zum Schritt hinzugefuegt und keine Datei behalten
  **And** die Schritt-Ansicht wird angezeigt
  **And** hat der Schritt noch kein Foto, zeigt der Karussell-Bereich den Leer-Zustand aus F-006 (US-006.9)

##### AK 4: Keine Kamera-App verfuegbar

- **Given** auf dem Geraet ist keine App installiert, die eine Foto-Aufnahme bedienen kann
  **When** die System-Kamera gestartet werden soll
  **Then** erscheint ein Hinweis mit dem Text (woertlich) **„Keine Kamera-App gefunden"** — derselbe Wortlaut wie im Anlage-Flow (F-002)
  **And** nach dem Schliessen wird die Schritt-Ansicht angezeigt, ohne dass ein Foto entsteht
  **And** die vorbereitete Zieldatei wird geloescht

##### AK 5: „NOCH'N FOTO" am selben Schritt

- **Given** die Schritt-Ansicht zeigt einen Schritt mit mindestens einem Foto
  **When** der Mechaniker „NOCH'N FOTO" antippt und die Aufnahme in der System-Kamera bestaetigt
  **Then** haengt das neue Foto als letztes im Karussell desselben Schritts
  **And** die Schrittnummer bleibt unveraendert
  **And** das Karussell enthaelt beide Fotos

##### AK 6: Foto wiederholen — das Ersatzfoto behaelt seinen Platz

*(Reihenfolge der Operationen: zuerst die System-Kamera, **erst nach** einer erfolgreichen Aufnahme wird das alte Foto ersetzt — projektweite Regel, siehe [../../governance.md](../../governance.md), Abschnitt „Kamera", bestaetigt als design-system.md K-02.)*

- **Given** ein Schritt hat 3 Fotos mit `reihenfolge` 0, 1 und 2 und das Karussell zeigt das **erste** Foto (`reihenfolge = 0`)
  **When** der Mechaniker „↺" antippt
  **Then** wird die System-Kamera gestartet, ohne dass etwas geloescht wird — das alte Foto und seine Datei bleiben zunaechst bestehen

- **Given** der Mechaniker hat „↺" am **ersten** Foto (`reihenfolge = 0`) eines Schritts mit 3 Fotos ausgeloest
  **When** er die neue Aufnahme in der System-Kamera bestaetigt
  **Then** wird erst jetzt ersetzt: das neue Foto bekommt `reihenfolge = 0`, danach sind das alte Foto und seine Datei geloescht
  **And** das zweite und das dritte Foto behalten `reihenfolge` 1 und 2 — es wird weder umnummeriert noch hinten angehaengt
  **And** das Karussell zeigt weiterhin das erste Foto (Indikator „FOTO 1/3")
  **And** das Thumbnail des Schritts (F-006 zeigt das erste Foto) zeigt das neue Foto

- **Given** der Mechaniker hat „↺" am **ersten** Foto (`reihenfolge = 0`) eines Schritts mit 3 Fotos ausgeloest
  **When** er die System-Kamera abbricht (oder es ist keine Kamera-App vorhanden)
  **Then** hat der Schritt unveraendert 3 Fotos mit lueckenloser `reihenfolge` 0, 1 und 2 — es wurde nichts geloescht und nichts eingefuegt
  **And** das alte Foto an `reihenfolge = 0` ist samt Datei weiterhin vorhanden
  **And** geloescht wird nur die vorbereitete, leer gebliebene Zieldatei

##### AK 7: Schritt ohne Foto

- **Given** die Schritt-Ansicht zeigt einen Schritt ohne Foto
  **When** die Ansicht dargestellt wird
  **Then** ist der kleine Kreis „↺" **nicht sichtbar** und die Label-Chips entfallen (F-006 US-006.9)
  **And** „NOCH'N FOTO" startet die System-Kamera; nach Bestaetigung ist das Foto das erste Foto dieses Schritts

##### AK 8: „NOCH'N FOTO" wirkt auf den betrachteten Schritt

- **Given** die Schritt-Ansicht zeigt nach einem Thumbnail-Sprung den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker „NOCH'N FOTO" antippt und die Aufnahme bestaetigt
  **Then** haengt das neue Foto an **Schritt 2**, als dessen letztes Foto
  **And** Schritt 2 bleibt abgeschlossen (`abgeschlossenAm` unveraendert) und Schritt 5 bleibt offen
  **And** die Ansicht zeigt weiterhin Schritt 2

##### AK 9: Fortsetzen legt keinen zweiten offenen Schritt an

- **Given** ein Reparaturvorgang ist im Status OFFEN und hat bereits einen Schritt 5 mit `abgeschlossenAm = null`
  **When** der Mechaniker den Demontage-Flow startet
  **Then** wird **kein** neuer `Schritt` angelegt — Schritt 5 wird geladen und ist zugleich der betrachtete und der offene Schritt
  **And** der Vorgang hat danach weiterhin genau einen Schritt mit `abgeschlossenAm = null`
  **And** die Schritt-Ansicht zeigt die Ziffer „05"

---

### US-003.2: Schritt abschliessen und weiterarbeiten

**Als** Mechaniker
**moechte ich** nach dem Dokumentieren eines Bauteils direkt entscheiden koennen, ob ich noch ein Foto brauche, zum naechsten Teil gehe oder Feierabend mache
**damit** ich den Ablauf ohne Zwischendialoge in meinem Arbeitstempo steuere

**Abgrenzung zu US-003.5:** Was beim Verlassen des Flows mit dem offenen Schritt passiert — abschliessen, fotolosen Schritt verwerfen, Rueckkehr zur Uebersicht — gehoert US-003.5 und steht ausschliesslich in [../workflow.md](../workflow.md). Die Aktion, die dort **„Beenden"** heisst, traegt in der Oberflaeche den Wortlaut **„FEIERABEND"** (design-system.md, K-08). Hier steht nur, wann sie sichtbar ist und wie sie nachfragt.

#### Akzeptanzkriterien

##### AK 1: Bedienung am offenen Schritt

- **Given** der Demontage-Flow ist aktiv und die Ansicht zeigt den offenen Schritt N mit mindestens einem Foto
  **When** die Schritt-Ansicht angezeigt wird
  **Then** sind unten rechts genau drei Rundbuttons sichtbar: „↺", „NOCH'N FOTO" und der orange Kreis mit der Nummer N+1 und dem Wort „NÄCHSTES"
  **And** oben rechts ist der Chip „FEIERABEND" sichtbar
  **And** die drei Label-Chips sind sichtbar
  **And** die Thumbnail-Leiste zeigt alle Schritte des Vorgangs

##### AK 2: „NÄCHSTES nn"

- **Given** die Schritt-Ansicht zeigt den offenen Schritt N
  **When** der Mechaniker den orangen Kreis „NÄCHSTES nn" antippt
  **Then** wird `abgeschlossenAm` am Schritt N gesetzt
  **And** ein neuer `Schritt` mit `schrittNummer` = N+1 und `gestartetAm` wird angelegt
  **And** die System-Kamera startet automatisch fuer den neuen Schritt
  **And** nach der Rueckkehr zeigt die Ansicht den neuen Schritt, und der Kreis traegt jetzt die Nummer N+2

##### AK 3: Debounce

- **Given** die Schritt-Ansicht wird angezeigt
  **When** der Mechaniker einen Rundbutton oder den Chip „FEIERABEND" antippt
  **Then** ist dieses Bedienelement fuer 300ms deaktiviert
  **And** ein Doppel-Tap loest die Aktion nur einmal aus

##### AK 4: Betrachteter abgeschlossener Schritt

- **Given** Schritt 5 ist offen und der Mechaniker springt ueber die Thumbnail-Leiste auf den abgeschlossenen Schritt 2
  **When** die Schritt-Ansicht Schritt 2 zeigt
  **Then** traegt der grosse Kreis die Nummer 5 und das Wort „ZURÜCK ZU" und ist neutral statt orange gefaerbt
  **And** der Chip „FEIERABEND" ist **nicht sichtbar** (ausgeblendet, nicht nur deaktiviert)
  **And** „NOCH'N FOTO" bleibt sichtbar und wirkt auf Schritt 2; „↺" bleibt sichtbar, sofern Schritt 2 Fotos hat
  **And** die Thumbnail-Leiste bleibt unveraendert sichtbar und nutzbar

##### AK 5: „ZURÜCK ZU nn"

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker den Kreis „ZURÜCK ZU 05" antippt
  **Then** zeigt die Schritt-Ansicht wieder Schritt 5 mit dessen Fotos
  **And** der Kreis ist wieder orange und traegt „NÄCHSTES 06", der Chip „FEIERABEND" ist wieder sichtbar
  **And** es wurde kein Schritt abgeschlossen, angelegt oder geloescht

##### AK 6: Foto-Aktionen bleiben am betrachteten Schritt

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 2 mit zwei Fotos
  **When** der Mechaniker am sichtbaren Foto ein Label umschaltet oder „↺" antippt
  **Then** wirkt die Aktion auf das sichtbare Foto von Schritt 2
  **And** der offene Schritt 5 bleibt unveraendert

##### AK 7: Rueckkehr ueber die Thumbnail-Leiste

- **Given** die Schritt-Ansicht zeigt den abgeschlossenen Schritt 2, waehrend Schritt 5 offen ist
  **When** der Mechaniker in der Thumbnail-Leiste das Thumbnail von Schritt 5 antippt
  **Then** gilt wieder die Bedienung aus AK 1 — der Weg des Wechsels spielt fuer die Sichtbarkeit keine Rolle

##### AK 8: „FEIERABEND" fragt nach

- **Given** die Schritt-Ansicht zeigt den offenen Schritt
  **When** der Mechaniker „FEIERABEND" antippt
  **Then** erscheint ein Bottom-Sheet mit dem Titel (woertlich) **„FEIERABEND?"**, dem Text (woertlich) **„Alles ist gespeichert — du kannst jederzeit weitermachen."** und den beiden Aktionen (woertlich) **„WEITER ARBEITEN"** und **„JA, FEIERABEND"**
  **And** es wurde noch nichts geschrieben

- **Given** das Feierabend-Sheet ist offen
  **When** der Mechaniker „WEITER ARBEITEN" antippt oder neben das Sheet tippt
  **Then** schliesst sich das Sheet, der Flow bleibt aktiv und es wurde kein Schritt abgeschlossen

- **Given** das Feierabend-Sheet ist offen
  **When** der Mechaniker „JA, FEIERABEND" antippt
  **Then** tritt die Wirkung aus US-003.5 ein ([../workflow.md](../workflow.md), Abschnitt „Abschluss der Demontage") und die Uebersicht (F-001) wird angezeigt

---

### US-003.3: Fotos labeln

**Als** Mechaniker
**moechte ich** an jedem Foto markieren, ob es das Bauteil, eine Uebersicht oder den Ablageort zeigt
**damit** ich beim Zusammenbau sofort erkenne, wo das Teil liegt und wie es eingebaut war

**Abgrenzung zu F-006:** Darstellung und Bedienverhalten der Chips — Vorauswahl „BAUTEIL", Kombinierbarkeit, Abwaehlbarkeit aller Label und das Nachfuehren beim Wischen — sind in **F-006 US-006.6** spezifiziert. F-003 ist Consumer: es reagiert auf die gemeldete Label-Aenderung und persistiert sie sofort. Die Label sind **nur in der Demontage** aenderbar; Montage und Archiv zeigen sie unveraenderlich (F-006 US-006.7).

#### Akzeptanzkriterien

##### AK 1: Label setzen wird sofort gespeichert

- **Given** das Karussell zeigt ein Foto
  **When** der Mechaniker den Chip „ABLAGEORT" antippt
  **Then** wird `istAblageort = true` sofort am `SchrittFoto` dieses Fotos gespeichert — ohne Sammel-Bestaetigung und ohne „Speichern"-Button
  **And** die Aenderung ist nach einem App-Neustart noch vorhanden

##### AK 2: Auch das Abwaehlen wird sofort gespeichert

- **Given** das Karussell zeigt ein Foto mit genau einem gesetzten Label
  **When** der Mechaniker dieses Label abwaehlt
  **Then** ist das Foto ohne Label gespeichert und bleibt im Karussell sichtbar
  **And** die Aenderung ist nach einem App-Neustart noch vorhanden

##### AK 3: Ablageort eines Schritts ist ableitbar

- **Given** ein Schritt hat mindestens ein Foto mit dem Label „ABLAGEORT"
  **When** der Schritt in der Thumbnail-Leiste oder in der Uebersicht dargestellt wird
  **Then** gilt er als Schritt mit Ablageort
  **And** ein Schritt ohne solches Foto gilt als Schritt ohne Ablageort

---

### US-003.7: Die Zeit am Schritt messen

**Als** Mechaniker
**moechte ich** die Zeit am laufenden Schritt sehen und sie von Hand starten und anhalten koennen
**damit** die gemessene Zeit zur tatsaechlichen Arbeit passt, auch wenn ich zwischendurch etwas anderes tun muss

Die Messung selbst gehoert dem Service F-005 ([../../F-005-zeiterfassung/service.md](../../F-005-zeiterfassung/service.md), Abschnitt „Service-Interface"). F-003 ist Consumer und beschreibt hier nur seine Nutzung: `referenzId` ist die `Schritt.id`, `referenzTyp` ist der Demontage-Schritt. Dass der Timer von Hand angehalten werden darf, ist in design-system.md als K-03 entschieden — jedes Start/Stopp-Paar ist eine eigene `ZeitMessung`, die Gesamtdauer ist ihre Summe.

#### Akzeptanzkriterien

##### AK 1: Der Chip zeigt die Zeit des betrachteten Schritts

- **Given** die Schritt-Ansicht zeigt einen Schritt
  **When** die Ansicht dargestellt wird
  **Then** zeigt der Timer-Chip die Gesamtdauer dieses Schritts als `mm:ss`
  **And** daneben steht (woertlich) **„LÄUFT"** oder (woertlich) **„STEHT"**, je nachdem ob fuer diesen Schritt gerade eine Messung offen ist

##### AK 2: Starten

- **Given** der Timer-Chip zeigt „STEHT"
  **When** der Mechaniker ihn antippt
  **Then** wird eine neue Zeitmessung fuer diesen Schritt gestartet
  **And** der Chip zeigt „LÄUFT" und zaehlt im Sekundentakt hoch

##### AK 3: Anhalten und fortsetzen

- **Given** der Timer-Chip zeigt „LÄUFT"
  **When** der Mechaniker ihn antippt
  **Then** wird die laufende Messung gestoppt und der Chip zeigt „STEHT"
  **And** der angezeigte Wert bleibt stehen und geht nicht verloren
  **And** ein erneutes Starten setzt die Zaehlung fort, statt bei `00:00` zu beginnen

##### AK 4: „NÄCHSTES nn" zieht die Messung mit

- **Given** die Zeitmessung fuer den offenen Schritt N laeuft
  **When** der Mechaniker „NÄCHSTES nn" antippt
  **Then** wird die Messung an Schritt N gestoppt
  **And** fuer den neu angelegten Schritt N+1 laeuft die Messung automatisch

##### AK 5: Feierabend stoppt die Messung

- **Given** eine Zeitmessung laeuft
  **When** der Mechaniker den Flow ueber „JA, FEIERABEND" verlaesst
  **Then** ist danach keine Messung dieses Vorgangs mehr offen

> **[OFFEN]** Springt der Mechaniker per Thumbnail auf einen anderen Schritt, waehrend eine Messung laeuft, zeigt der Chip die Zeit des betrachteten Schritts und „STEHT" — die Messung des verlassenen Schritts laeuft im Hintergrund weiter. Ob der Sprung die laufende Messung stoppen soll, ist zu entscheiden.

---

### Aus US-003.4 AK 1: Schrittnummer sichtbar

- **Given** der Demontage-Flow ist aktiv
  **When** die Schritt-Ansicht angezeigt wird
  **Then** steht die Schrittnummer des betrachteten Schritts zweistellig oben links („05")
  **And** sie ist das groesste Element der Ansicht — der Mechaniker soll sie im Vorbeigehen auf ein physisches Etikett uebertragen koennen

### Aus US-003.4 AK 2: Schrittnummer bleibt gross und sichtbar

- **Given** die Schritt-Ansicht zeigt Schritt 3
  **When** der Mechaniker im Karussell wischt oder eine Vollbild-Anzeige schliesst
  **Then** ist „03" unveraendert sichtbar
  **And** die Ansicht nennt an keiner Stelle einen Fortschritt in der Form „Schritt 3 von 15" — Schrittnummer und Fortschritt sind zwei verschiedene Dinge

*(Der Story-Text von US-003.4 und die Akzeptanzkriterien zur Vergabe und Inkrementierung der Schrittnummer stehen in [../workflow.md](../workflow.md).)*

## DB-Interaktion

| Aktion | DB-Operation |
|--------|-------------|
| Schritt beginnt („NÄCHSTES nn", oder Schritt 1 durch F-002) | Neuen `Schritt` anlegen: `reparaturvorgangId`, `schrittNummer`, `gestartetAm` |
| Foto in der System-Kamera bestaetigt — Kamera kam aus „NOCH'N FOTO" oder aus „NÄCHSTES nn" | Neues `SchrittFoto`: `schrittId` des **betrachteten** Schritts, `pfad`, `reihenfolge` = Anzahl bisheriger Fotos dieses Schritts (0-basiert, also hinten angehaengt), `istBauteil = true`, `istUebersicht = false`, `istAblageort = false`, `aufgenommenAm` |
| Foto in der System-Kamera bestaetigt — Kamera kam aus **„↺"** | **Erst jetzt** wird ersetzt, in einer Operation: neues `SchrittFoto` mit denselben Feldern, aber `reihenfolge` = `p` (die `reihenfolge` des ersetzten Fotos); die alte `SchrittFoto`-Zeile und die alte Datei werden geloescht. Die uebrigen Fotos behalten ihre `reihenfolge` |
| System-Kamera abgebrochen / keine Kamera-App | Kein DB-Write. Vorbereitete Zieldatei loeschen. Kam die Kamera aus „↺", bleiben das alte Foto und seine Datei unveraendert erhalten |
| Label-Chip umgeschaltet | Update auf dem `SchrittFoto` des sichtbaren Fotos (`istBauteil` / `istUebersicht` / `istAblageort`) |
| „↺" angetippt | **Kein DB-Write beim Tap.** Gemerkt werden nur die `SchrittFoto`-Id und `p` = deren `reihenfolge`; ersetzt wird erst nach erfolgreicher Neuaufnahme |
| „NOCH'N FOTO" angetippt | Kein DB-Write beim Tap |
| Karussell wischen / Foto in Vollbild oeffnen | Kein DB-Write |
| Thumbnail eines anderen Schritts antippen (F-006) | Kein DB-Write. Fotos des gewaehlten Schritts werden geladen |
| „ZURÜCK ZU nn" | Kein DB-Write. Fotos des offenen Schritts werden geladen |
| „NÄCHSTES nn" | `abgeschlossenAm` am offenen Schritt setzen **und** neuen `Schritt` (N+1) anlegen |
| „JA, FEIERABEND" | Wirkung und DB-Operation stehen bei US-003.5 in [../workflow.md](../workflow.md) |
| Timer-Chip angetippt | Kein Schreibvorgang an `Schritt` oder `SchrittFoto`. Der Service F-005 legt in seiner eigenen Tabelle eine `ZeitMessung` an bzw. schliesst die offene |

**`aktualisiertAm`:** Jede DB-Aktion dieser Tabelle setzt in derselben Operation `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion. Das ist die projektweite Invariante aus [../../governance.md](../../governance.md) („Invariante: `aktualisiertAm`") und wird hier nur genannt, nicht neu formuliert. Aktionen ohne DB-Write beruehren das Feld nicht.

## Nicht-funktionale Anforderungen

- **Touch-Targets:** Die verbindlichen Mindestmaße stehen in [../../governance.md](../../governance.md), Abschnitt „Touch-Targets"; die konkreten Groessen dieses Screens in [../../design-system.md](../../design-system.md), Abschnitt „Maße und Trefferflaechen". Die drei Rundbuttons liegen deutlich darueber — mit Handschuhen ist der grosse Kreis das leichteste Ziel des Screens, und das ist die haeufigste Aktion.
- **Abstand zwischen den Gruppen:** Rundbuttons unten rechts, Thumbnail-Leiste am rechten Rand, Label-Chips unten links — keine zwei Gruppen duerfen als eine gelesen werden.
- **Kein vorausgewaehlter Default, keine versteckte Option** unter den sichtbaren Bedienelementen.
- **Debounce:** 300ms fuer Rundbuttons, „FEIERABEND" und Timer-Chip (fuer Label-Chips und Thumbnails bringt F-006 den Debounce mit).
- **Foto-Speicherung darf die UI nicht blockieren** (async).
- **Fehlende Foto-Datei:** Platzhalter-Bild statt Crash — Darstellung liegt bei F-006 (US-006.8).
- **Foto-Qualitaet:** Erwartungswert und Umgang mit der von der System-Kamera gelieferten Datei stehen in [../../governance.md](../../governance.md), Abschnitt „Qualitaet".
- **Schrittnummer bleibt sichtbar,** auch waehrend im Karussell gewischt wird.

## Technische Hinweise

- **System-Kamera:** `ActivityResultContracts.TakePicture()` + `FileProvider` — kein CameraX, keine `CAMERA`-Permission
- **Zieldatei:** Wird vor dem Intent in `photos/` angelegt und als FileProvider-URI uebergeben. Kein `photos/temp/`, kein Verschiebe-Schritt nach der Aufnahme
- **Auto-Start:** Der Kamera-Intent wird beim Anlegen eines Schritts ueber „NÄCHSTES nn" automatisch ausgeloest. Beim Betreten des Flows wird er **nicht** ausgeloest (US-003.1 AK 1)
- **EXIF:** Metadaten werden nach der Rueckkehr aus der System-Kamera entfernt, bevor die `SchrittFoto`-Zeile geschrieben wird
- **F-006-Einbettung:** Foto-Karussell, Thumbnail-Leiste, Label-Chips, Foto-Indikator und Vollbild-Ansicht kommen als Komponenten aus F-006 (bearbeitbarer Modus). Die Bedienelemente „Zurueck"/„Weiter" (F-006 US-006.10) werden **nicht** angedockt. Kopfzeile, Timer-Chip und die Rundbuttons haengt F-003 in die dafuer vorgesehenen Slots
- **Anzeige-Reihenfolge:** F-003 uebergibt die Schritte des Vorgangs **aufsteigend nach `schrittNummer`**
- **Betrachteter vs. offener Schritt:** Das ViewModel haelt beides getrennt. Der **betrachtete** Schritt steuert Anzeige, Foto-Aktionen, „NOCH'N FOTO" und den Timer-Chip; der **offene** Schritt (`abgeschlossenAm = null`) ist Ziel von „NÄCHSTES nn" und „FEIERABEND". Sind beide identisch, ist der grosse Kreis orange und „FEIERABEND" sichtbar, sonst zeigt der Kreis „ZURÜCK ZU nn" und „FEIERABEND" entfaellt
- **Sichtbares Foto:** Die Karussell-Position ist der State, an dem Label-Chips und „↺" haengen. Sie wird im ViewModel gehalten und mit dem von F-006 gemeldeten sichtbaren Foto synchron gehalten
- **Zeiterfassung:** ueber das Interface aus F-005 mit `referenzId = Schritt.id` und dem Referenztyp des Demontage-Schritts. Die Timer-Daten liegen in der eigenen Tabelle des Service-Features, nie am `Schritt` (Governance: keine Dual-Purpose-Felder)
- **State Hoisting:** Der Screen erhaelt State (Schrittnummer, Fotoliste mit Labels, Karussell-Position, Schrittliste fuer die Thumbnail-Leiste, Index des betrachteten Schritts, Nummer des offenen Schritts, Timer-Stand) und Callbacks vom ViewModel

## Aenderungshistorie

| Datum | Aenderung |
|---|---|
| 2026-07-27 | Auf das Design-System nachgezogen: drei Rundbuttons („↺" 60dp, „NOCH'N FOTO" 86dp, grosser Kreis 124dp) statt einer Aktionszeile; der grosse Kreis zeigt „NÄCHSTES nn" nur am offenen Schritt und sonst neutral „ZURÜCK ZU nn" (K-07), „FEIERABEND" verschwindet dabei; „Beenden" heisst in der Oberflaeche „FEIERABEND" und fragt ueber ein Bottom-Sheet nach (K-08); Schrittnummer als 118sp-Ziffer oben links; „Zurueck"/„Weiter" aus F-006 werden nicht angedockt (K-05); neue US-003.7 fuer den Timer-Chip (K-03, K-04); Kamera-Autostart nur noch bei „NÄCHSTES nn". |
