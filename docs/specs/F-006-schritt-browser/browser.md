# Schritt-Browser: Navigation und Foto-Anzeige

Der Browser ist eine zustandslose Compose-Komponente unter `ui/schrittbrowser/`. Drei Screens komponieren ihn (Demontage, Montage, Archiv); er selbst navigiert nicht, laedt nichts und schreibt nichts.

## Aufbau der Komponente

Der Browser fuellt den Bildschirm. Das Foto des aktuellen Schritts liegt formatfuellend darunter, alles Uebrige liegt darueber.

```
+---------------------------------------------------------------+
|  [ kopfzeile ]                                                 |
|                                                     +--------+ |
|                                                     |  +2 ^  | |
|              Foto-Karussell des aktuellen           | [ 05 ] | |  <- Thumbnail-Leiste,
|              Schritts, waagerecht wischbar          |[ *06* ]| |     senkrecht am
|              Tippen oeffnet das Vollbild            | [ 07 ] | |     rechten Rand
|                                                     |  +6 v  | |
|  [ ueberLabels ]                                    +--------+ |
|  [x] BAUTEIL                                                   |  <- Label des sichtbaren
|  [ ] UEBERSICHT                                                |     Fotos, nur bei Fotos
|  [x] ABLAGEORT                                                 |
|  [ unterLabels ]                                  [ bedien-  ] |
|  ***  FOTO 2/4  <- WISCHEN                        [ kreise   ] |
+---------------------------------------------------------------+
```

Der Browser bringt mit: Karussell, Thumbnail-Leiste, Label-Spalte, Punkt-Indikatoren mit Zaehler und Wisch-Hinweis sowie das Vollbild. Alles Weitere haengt der Consumer in vier Slots ein:

| Slot | Ort | Was Consumer dort einhaengen |
|---|---|---|
| `kopfzeile` | oben, ganze Breite | Schrittnummer, Modus-Wort, Vorgangszeile, Ausstieg („FEIERABEND" / „✕") |
| `ueberLabels` | ueber der Label-Spalte | Timer-Kapsel (F-005), Fortschrittsblock der Montage (F-004) |
| `unterLabels` | unter der Label-Spalte | „AM FAHRZEUG GEBLIEBEN" (F-004), „ARCHIV · NUR LESEN" (F-001) |
| `bedienkreise` | unten rechts | die Aktionskreise des Flows — **einschliesslich Vor/Zurueck** |

Ein nicht befuellter Slot belegt keinen Platz.

**Wischgeste (verbindlich):** Waagerechtes Wischen im Bildbereich wechselt das **Foto innerhalb des Schritts**, niemals den Schritt (US-006.4). Den Schritt wechseln nur der Thumbnail-Sprung (US-006.2) und ein vom Consumer eingehaengtes Vor/Zurueck (US-006.10).

### Betriebsarten

Im Code `BrowserBetriebsart`:

| Betriebsart | Consumer | Label des sichtbaren Fotos | Erledigt-Haken in der Leiste | Vor/Zurueck im Slot |
|---|---|---|---|---|
| `BEARBEITBAR` | F-003 Demontage | aenderbar (Chips) | nein | keins — der Thumbnail-Sprung ist der Weg |
| `LESEND_MIT_AKTIONEN` | F-004 Montage | nur sichtbar (Plaketten) | ja | „ZURÜCK" |
| `NUR_LESEN` | F-001 Archiv | nur sichtbar (Plaketten) | nein | „‹" und „›" |

**Label werden ausschliesslich in der Demontage gesetzt.** In den beiden lesenden Modi zeigt der Browser die gespeicherten Label an, nimmt keine Aenderung entgegen und meldet nichts.

Thumbnail-Leiste, Karussell und Vollbild sind in allen drei Betriebsarten identisch.

---

## User Stories

### US-006.1: Alle Schritte auf einen Blick sehen

**Als** Mechaniker
**moechte ich** die Schritte eines Vorgangs als Bilderleiste am Bildschirmrand sehen
**damit** ich mich sofort orientieren kann, wo ich gerade bin und was vorher und nachher kam.

#### Akzeptanzkriterien

- **Given** ein Vorgang mit 15 Schritten ist geoeffnet
  **When** der Browser angezeigt wird
  **Then** zeigt die senkrechte Leiste am rechten Rand hoechstens **vier** Kacheln aus der Anzeige-Reihenfolge des Consumers

- **Given** die Hoehe der Leiste reicht nur fuer zwei Kacheln
  **When** die Leiste dargestellt wird
  **Then** zeigt sie zwei Kacheln statt vier
  **And** keine Kachel wird angeschnitten

- **Given** 15 Schritte und der aktuelle steht an Position 7
  **When** die Leiste dargestellt wird
  **Then** liegt das Fenster so, dass die aktive Kachel moeglichst mittig steht
  **And** am Anfang bzw. Ende der Liste rueckt das Fenster an den Rand, statt ueber die Liste hinauszulaufen

- **Given** oberhalb des Fensters liegen 6 Schritte, unterhalb 2
  **When** die Leiste dargestellt wird
  **Then** steht ueber der obersten Kachel „+6 ↑" und unter der untersten „+2 ↓"

- **Given** oberhalb des Fensters liegt kein Schritt
  **When** die Leiste dargestellt wird
  **Then** erscheint kein oberer Ueberlaufzaehler (unten entsprechend)

- **Given** ein Schritt hat mehrere Fotos
  **When** sein Thumbnail dargestellt wird
  **Then** zeigt es das **erste** Foto (niedrigste `reihenfolge`)
  **And** rechts unten steht die Gesamtzahl seiner Fotos

- **Given** ein Schritt hat genau ein Foto
  **When** sein Thumbnail dargestellt wird
  **Then** erscheint keine Anzahl-Plakette

- **Given** Schritt 7 ist der aktuelle
  **When** die Leiste dargestellt wird
  **Then** ist seine Kachel 66dp gross, jede andere 54dp
  **And** um die aktive Kachel laeuft ein atmender oranger Rand (2,6 s je Durchlauf)
  **And** keine andere Kachel ist hervorgehoben

- **Given** eine beliebige Kachel
  **When** sie dargestellt wird
  **Then** ist ihre Trefferflaeche mindestens 56dp hoch und breit — unabhaengig von der optischen Groesse (`governance.md`, Abschnitt „Optische Groesse vs. Trefferflaeche")

- **Given** die Betriebsart ist `LESEND_MIT_AKTIONEN` und ein Schritt ist eingebaut
  **When** sein Thumbnail dargestellt wird
  **Then** traegt es oben links einen gruenen Haken

- **Given** die Betriebsart ist `BEARBEITBAR` oder `NUR_LESEN`
  **When** die Leiste dargestellt wird
  **Then** traegt keine Kachel einen Haken

---

### US-006.2: Direkt zu einem beliebigen Schritt springen

**Als** Mechaniker
**moechte ich** durch Antippen einer Kachel direkt zu diesem Schritt springen
**damit** ich schnell nachschauen kann, ohne mich durchzublaettern oder eine Nummer eintippen zu muessen.

Ein Sprung-Dialog mit Nummerneingabe existiert nicht.

#### Akzeptanzkriterien

- **Given** Schritt 3 wird angezeigt
  **When** der Mechaniker die Kachel von Schritt 11 antippt
  **Then** meldet der Browser den Index von Schritt 11 ueber `onSchrittGewaehlt`
  **And** der Browser wechselt den Schritt nicht selbst

- **Given** der Consumer hat den Index uebernommen
  **When** die Ansicht aktualisiert wird
  **Then** zeigt das Karussell das erste Foto des neuen Schritts
  **And** die Kachel des neuen Schritts ist hervorgehoben und liegt im Fenster der Leiste

- **Given** ein beliebiger Schritt wird angezeigt
  **When** der Mechaniker zu einem anderen Schritt springt
  **Then** werden keine Daten veraendert (kein Schritt wird abgeschlossen, abgehakt oder geloescht)

- **Given** eine beliebige Betriebsart
  **When** der Mechaniker eine Kachel antippt
  **Then** springt die Ansicht genauso — der Sprung ist in allen drei Betriebsarten verfuegbar

---

### US-006.3: Schritte mit Ablageort in der Leiste erkennen

**Als** Mechaniker
**moechte ich** in der Leiste farblich erkennen, welche Schritte ein Foto vom Ablageort haben
**damit** ich beim Zusammenbau sofort sehe, fuer welche Teile ich einen Ablageplatz suchen muss und welche am Fahrzeug geblieben sind.

Die Kennzeichnung ist ein **Farbstreifen am unteren Rand der Kachel** — 5dp an der aktiven, 4dp an den uebrigen. Groesse und atmender Rand sind allein der Hervorhebung des aktuellen Schritts vorbehalten (US-006.1); beide Kennzeichnungen sind dadurch gleichzeitig sichtbar.

| Kategorie | Bedingung | Streifenfarbe |
|---|---|---|
| Ablageort | mindestens ein Foto mit `istAblageort` | `BoltOrange` `#FF7A1A` |
| Uebersicht | kein Ablageort, aber mindestens ein Foto mit `istUebersicht` | `BoltBlau` `#3B9DFF` |
| Bauteil | weder Ablageort noch Uebersicht, aber mindestens ein Foto mit `istBauteil` | `BoltGruen` `#24C48A` |
| ohne | kein Foto, oder an allen Fotos alle drei Label abgewaehlt | `BoltKategorieOhne` `#5A6167` |

Die Reihenfolge der Tabelle ist die Auswerteregel: **Ablageort vor Uebersicht vor Bauteil** (Governance).

#### Akzeptanzkriterien

- **Given** ein Schritt hat mindestens ein Foto mit „Ablageort"
  **When** seine Kachel dargestellt wird
  **Then** ist ihr Streifen orange

- **Given** ein Schritt hat kein Ablageort-Foto, aber mindestens ein Uebersichtsfoto
  **When** seine Kachel dargestellt wird
  **Then** ist ihr Streifen blau

- **Given** ein Schritt hat nur Bauteilfotos
  **When** seine Kachel dargestellt wird
  **Then** ist ihr Streifen gruen

- **Given** ein Schritt hat kein Foto oder an allen Fotos sind alle drei Label abgewaehlt
  **When** seine Kachel dargestellt wird
  **Then** ist ihr Streifen grau

- **Given** ein Foto traegt gleichzeitig „Bauteil" und „Ablageort"
  **When** die Kategorie des Schritts bestimmt wird
  **Then** gilt Ablageort

- **Given** der Mechaniker setzt in `BEARBEITBAR` am sichtbaren Foto das Label „Ablageort"
  **When** der Consumer die Aenderung uebernommen hat
  **Then** wechselt der Streifen der zugehoerigen Kachel sofort, ohne dass der Screen neu geladen wird

---

### US-006.4: Alle Fotos eines Schritts durchwischen

**Als** Mechaniker
**moechte ich** die Fotos eines Schritts durch Wischen nacheinander ansehen
**damit** ich mehrere Perspektiven auf dasselbe Bauteil pruefen kann (Detail, Uebersicht, Ablageort).

**Verbindliche Gestenbelegung:** Die waagerechte Wischgeste im Bildbereich ist ausschliesslich mit dem Foto-Wechsel innerhalb des Schritts belegt — auch am Rand des Karussells und auch bei nur einem Foto. Consumer duerfen sie im Bildbereich nicht anders belegen.

#### Akzeptanzkriterien

- **Given** der aktuelle Schritt hat 4 Fotos
  **When** der Schritt angezeigt wird
  **Then** zeigt das Karussell das erste Foto formatfuellend (beschnitten)
  **And** unter der Label-Spalte stehen vier Punkte, der erste breit und orange
  **And** der Zaehler zeigt „FOTO 1/4"

- **Given** das Karussell zeigt Foto 1 von 4
  **When** der Mechaniker nach links wischt
  **Then** zeigt es Foto 2
  **And** der Zaehler zeigt „FOTO 2/4"
  **And** der Browser meldet den neuen Foto-Index ueber `onFotoGewaehlt`

- **Given** das Karussell zeigt das letzte Foto des Schritts
  **When** der Mechaniker weiter in dieselbe Richtung wischt
  **Then** bleibt dieses Foto sichtbar
  **And** der angezeigte Schritt bleibt derselbe

- **Given** der aktuelle Schritt hat mehr als ein Foto und es wurde noch nicht gewischt
  **When** der Schritt angezeigt wird
  **Then** steht neben dem Zaehler der pulsierende Hinweis „← WISCHEN"

- **Given** der Hinweis ist sichtbar
  **When** der Mechaniker einmal gewischt oder den Schritt gewechselt hat
  **Then** verschwindet er fuer den Rest der Sitzung

- **Given** der aktuelle Schritt hat genau ein Foto
  **When** die Ansicht dargestellt wird
  **Then** erscheint kein Wisch-Hinweis
  **And** ein Wisch wechselt weder Foto noch Schritt

---

### US-006.5: Ein Foto als Vollbild ansehen

**Als** Mechaniker
**moechte ich** ein Foto mit einem Tap gross ansehen
**damit** ich Details wie Schraubenpositionen oder Steckerbelegungen erkennen kann.

#### Akzeptanzkriterien

- **Given** das Karussell zeigt ein Foto
  **When** der Mechaniker es antippt
  **Then** liegt das Foto **vollstaendig** (nicht beschnitten) ueber dem Screen
  **And** die Kopfzeile zeigt „SCHRITT 07" und darunter die Label des Fotos als „Bauteil · Ablageort" bzw. „ohne Label"

- **Given** das Vollbild ist offen
  **When** der Mechaniker das Schliesskreuz „✕" oben rechts antippt **oder** die Zurueck-Geste ausfuehrt
  **Then** kehrt er zur Schritt-Ansicht zurueck
  **And** dasselbe Foto ist im Karussell weiterhin sichtbar

- **Given** das Vollbild ist **nicht** offen
  **When** der Mechaniker die Zurueck-Geste ausfuehrt
  **Then** greift der Browser nicht ein — die Geste gehoert dem Consumer

- **Given** das Vollbild ist offen
  **When** der Mechaniker waagerecht wischt
  **Then** wechselt dasselbe Foto wie im Karussell darunter
  **And** die Punktleiste am unteren Rand folgt

- **Given** das Vollbild ist offen
  **When** es dargestellt wird
  **Then** verdecken weder Label-Spalte noch Thumbnail-Leiste noch die Slot-Inhalte des Consumers das Foto

- **Given** eine beliebige Betriebsart
  **When** der Mechaniker ein Foto antippt
  **Then** oeffnet das Vollbild genauso

---

### US-006.6: Fotos beschriften

**Als** Mechaniker
**moechte ich** direkt am angezeigten Foto ankreuzen, was es zeigt
**damit** ich beim Zusammenbau sofort erkenne, welches Bild mir den Ablageort verraet und welches das Bauteil.

Gilt **ausschliesslich** fuer `BEARBEITBAR` (F-003 Demontage).

#### Akzeptanzkriterien

- **Given** `BEARBEITBAR` und das Karussell zeigt ein Foto
  **When** die Ansicht dargestellt wird
  **Then** stehen links unten drei bedienbare Chips untereinander: „BAUTEIL", „ÜBERSICHT", „ABLAGEORT"
  **And** jeder Chip zeigt den gespeicherten Zustand genau dieses Fotos (Haken und Farbe wenn gesetzt)

- **Given** am sichtbaren Foto ist nur „BAUTEIL" gesetzt
  **When** der Mechaniker „ABLAGEORT" antippt
  **Then** meldet der Browser `onLabelUmgeschaltet` mit diesem Foto und diesem Label
  **And** „BAUTEIL" bleibt gesetzt (die drei Label sind frei kombinierbar)

- **Given** am sichtbaren Foto sind alle drei Label gesetzt
  **When** der Mechaniker alle drei abwaehlt
  **Then** wird jede Abwahl gemeldet
  **And** es erscheint keine Fehlermeldung — kein Label ist ein gueltiger Zustand

- **Given** der Mechaniker aendert ein Label
  **When** die Aenderung erfolgt ist
  **Then** gibt es weder Sammel-Bestaetigung noch „Speichern"-Button (Sofort-Save, siehe `governance.md`)

- **Given** das Karussell zeigt Foto 2 mit „ABLAGEORT"
  **When** der Mechaniker zu Foto 3 wischt, das nur „BAUTEIL" traegt
  **Then** zeigen die Chips den Zustand von Foto 3

---

### US-006.7: Sehen, was ein Foto zeigt, ohne es aendern zu koennen

**Als** Mechaniker
**moechte ich** beim Zusammenbauen und beim Nachschlagen am Foto ablesen koennen, ob es Bauteil, Uebersicht oder Ablageort zeigt
**damit** ich den Ablageort sofort erkenne und die Dokumentation trotzdem nicht versehentlich verfaelsche.

#### Akzeptanzkriterien

- **Given** `LESEND_MIT_AKTIONEN` oder `NUR_LESEN` und das Karussell zeigt ein Foto
  **When** die Ansicht dargestellt wird
  **Then** stehen dieselben drei Label als flache, schmale Plaketten mit farbigem Punkt dort, wo in `BEARBEITBAR` die Chips sitzen
  **And** sie zeigen den gespeicherten Zustand genau dieses Fotos

- **Given** eine lesende Betriebsart und das sichtbare Foto traegt „ABLAGEORT"
  **When** die Ansicht dargestellt wird
  **Then** ist am Foto erkennbar, dass es den Ablageort zeigt (F-004 US-004.1 stuetzt sich darauf)

- **Given** eine lesende Betriebsart
  **When** der Mechaniker eine Plakette antippt
  **Then** aendert sich nichts
  **And** es wird kein `onLabelUmgeschaltet` gemeldet

- **Given** `NUR_LESEN`
  **When** der Mechaniker springt, blaettert, wischt und ein Foto als Vollbild oeffnet und die Ansicht danach erneut oeffnet
  **Then** sind Schritte, Fotos und Label unveraendert

---

### US-006.8: Fehlende Foto-Dateien abfangen

**Als** Mechaniker
**moechte ich** die Dokumentation auch dann durchblaettern koennen, wenn eine Bilddatei fehlt
**damit** ein einzelnes verlorenes Foto nicht den gesamten Vorgang unbrauchbar macht.

#### Akzeptanzkriterien

- **Given** ein Foto-Datensatz verweist auf eine nicht mehr vorhandene Datei
  **When** das Karussell dieses Foto anzeigt
  **Then** stuerzt die App nicht ab

- **Given** eine Datei fehlt und die Betriebsart ist `BEARBEITBAR`
  **When** die Ansicht dargestellt wird
  **Then** bleiben Label-Chips, Wischen und Vollbild uneingeschraenkt bedienbar — die Metadaten des Fotos existieren weiter

- **Given** das erste Foto eines Schritts fehlt als Datei
  **When** seine Kachel dargestellt wird
  **Then** bleiben Schrittnummer, Farbstreifen und Trefferflaeche unveraendert erhalten

> **[OFFEN]** Womit die fehlende Datei ersetzt wird, ist nicht entschieden. `governance.md`, Abschnitt „Fehlende Dateien", verlangt ein Platzhalter-Bild; der gebaute Stand setzt an `AsyncImage` weder `placeholder` noch `error` und zeigt eine leere Flaeche. Vor der Umsetzung festlegen: App-Icon, Stahltextur oder eigenes Symbol.

---

### US-006.9: Schritt ohne Fotos verstaendlich anzeigen

**Als** Mechaniker
**moechte ich** bei einem Schritt ohne Fotos eine klare Aussage sehen
**damit** ich weiss, dass hier nichts dokumentiert wurde, und nicht an einen Fehler der App glaube.

Ein Schritt ohne Fotos ist waehrend der Arbeit ein gueltiger Zustand: der Schritt entsteht, bevor das erste Foto da ist.

#### Akzeptanzkriterien

- **Given** der aktuelle Schritt hat keine Fotos
  **When** er angezeigt wird
  **Then** zeigt der Bildbereich ein gestricheltes Quadrat mit der zweistelligen Schrittnummer und darunter den Text „Keine Fotos zu diesem Schritt"
  **And** der Zaehler zeigt „KEIN FOTO"
  **And** es erscheint kein Punkt-Indikator

- **Given** der aktuelle Schritt hat keine Fotos
  **When** er in einer beliebigen Betriebsart angezeigt wird
  **Then** ist keine Label-Spalte sichtbar — es gibt kein Foto zum Beschriften

- **Given** ein Schritt ohne Fotos
  **When** seine Kachel in der Leiste dargestellt wird
  **Then** zeigt sie eine dunkle Flaeche mit der Schrittnummer und einem grauen Streifen

- **Given** der Vorgang enthaelt ueberhaupt keine Schritte
  **When** der Browser angezeigt wird
  **Then** erscheint keine Thumbnail-Leiste
  **And** der Bildbereich zeigt den Leer-Zustand
  **And** der Browser meldet keinen Fehler (ein erklaerender Hinweis auf Screen-Ebene ist Consumer-Sache)

- **Given** der Consumer uebergibt einen Schritt- oder Foto-Index ausserhalb des gueltigen Bereichs
  **When** der Browser angezeigt wird
  **Then** klemmt er den Index fuer die Darstellung und stuerzt nicht ab
  **And** er korrigiert den Zustand des Consumers nicht

---

### US-006.10: Schrittweise vor- und zurueckblaettern, wo der Flow es hergibt

**Als** Mechaniker
**moechte ich** dort, wo es zum Ablauf passt, mit einem Tap zum benachbarten Schritt wechseln
**damit** ich mich der Reihe nach durch die Dokumentation arbeiten kann, ohne die richtige Kachel treffen zu muessen.

Vor/Zurueck ist eine **Andockstelle**, kein fester Bestandteil des Browsers: der Consumer haengt die Bedienelemente in `bedienkreise` ein und entscheidet, welche es gibt (siehe Tabelle „Betriebsarten"). Der Browser stellt dafuer `istErster` und `istLetzter` bereit, damit die Grenzpruefung nicht dreimal neu entsteht. Den Index schreibt der Consumer fort.

#### Akzeptanzkriterien

- **Given** `BEARBEITBAR` (Demontage)
  **When** die Ansicht dargestellt wird
  **Then** gibt es kein Vor/Zurueck — der Schrittwechsel laeuft ueber die Thumbnail-Leiste

- **Given** `LESEND_MIT_AKTIONEN` (Montage)
  **When** die Ansicht dargestellt wird
  **Then** gibt es genau ein Blaetter-Element „ZURÜCK"; vorwaerts kommt der Mechaniker ueber die Schritt-Aktion oder eine Kachel (F-004)

- **Given** `NUR_LESEN` (Archiv)
  **When** die Ansicht dargestellt wird
  **Then** gibt es beide Elemente, „‹" und „›"

- **Given** der aktuelle Schritt ist der erste der Anzeige-Reihenfolge
  **When** die Ansicht dargestellt wird
  **Then** ist „Zurueck" sichtbar, aber **sichtbar deaktiviert** (abgesenkt dargestellt)
  **And** ein Tap darauf loest nichts aus
  **And** das Layout verschiebt sich nicht

- **Given** der aktuelle Schritt ist der letzte der Anzeige-Reihenfolge
  **When** die Ansicht dargestellt wird
  **Then** ist „Weiter" sichtbar deaktiviert und ohne Wirkung
  **And** weder Beschriftung noch Bedeutung des Elements aendern sich (ein Abschluss- oder Archivieren-Verhalten am letzten Schritt gehoert dem Consumer, siehe `F-004-montage/montage.md`)

- **Given** der Vorgang hat genau einen Schritt oder gar keinen
  **When** die Ansicht dargestellt wird
  **Then** sind beide Elemente deaktiviert

- **Given** ein beliebiger Schritt wird angezeigt
  **When** der Mechaniker blaettert
  **Then** werden keine Daten veraendert
  **And** das Karussell zeigt danach das erste Foto des neuen Schritts

---

### US-006.11: Die Aktionen des Flows immer an derselben Stelle finden

**Als** Mechaniker
**moechte ich** die Bedienelemente meines Ablaufs in jedem Modus an derselben Stelle finden
**damit** ich mit Handschuhen blind danach greifen kann, egal ob ich demontiere, montiere oder nachschlage.

Der Browser fuehrt keine dieser Aktionen aus und kennt ihre Bedeutung nicht. Er haelt die vier Slots frei; welches Foto sichtbar ist, weiss der Consumer aus dem Zustand, den er selbst liefert.

#### Akzeptanzkriterien

- **Given** ein Consumer haengt Aktionskreise in `bedienkreise`
  **When** der Browser dargestellt wird
  **Then** liegen sie unten rechts nebeneinander, von klein nach gross zur Ecke hin, vor der dekorativen Kugel

- **Given** ein Consumer befuellt einen Slot nicht
  **When** der Browser dargestellt wird
  **Then** bleibt der Bereich leer und belegt keinen Platz

- **Given** der Mechaniker tippt ein Element in einem Slot an
  **When** die Aktion ausgefuehrt wird
  **Then** aendert der Browser von sich aus weder Foto noch Schritt noch Label

- **Given** F-003 haengt „Wiederholen" (↺) ein und der aktuelle Schritt hat kein Foto
  **When** die Ansicht dargestellt wird
  **Then** entscheidet der Consumer ueber die Sichtbarkeit — der Browser blendet nichts aus und nichts ein

---

## Nicht-funktionale Anforderungen

**Bedienbarkeit (Quality Goal #1):**

- Jede Trefferflaeche haelt die Mindestmasse aus `governance.md`, Abschnitt „Touch-Targets" ein; die Ausnahme fuer die inaktiven Kacheln steht dort im Unterabschnitt „Optische Groesse vs. Trefferflaeche"
- 300ms Debounce auf jedem Tap — kommt aus `boltKlick`, nicht aus eigenem Code der Komponente
- Deaktivierte Bedienelemente sind sichtbar abgesenkt, nicht still klemmend
- Die Wischgeste folgt dem Finger und rastet auf ganze Fotos ein
- Ein Tap genuegt fuer Sprung, Blaettern, Vollbild und Label-Wechsel

**Performance (Quality Goal #3):**

- Nur die Fotos des aktuellen Schritts liegen im Karussell; es gibt keinen schrittuebergreifenden Pager
- Kacheln laden beschnitten (Crop), Karussell und Vollbild vollstaendig (Fit)
- Eine Leiste ueber 50 Schritte zeichnet nur das Fenster von hoechstens vier Kacheln
- Der Schrittwechsel laeuft ohne spuerbare Verzoegerung

**Zuverlaessigkeit (Quality Goal #2):**

- Der Browser haelt keinen persistenten Zustand; es gibt nichts, was verloren gehen kann
- Label-Aenderungen werden sofort gemeldet, damit der Consumer sofort speichern kann (Sofort-Save)
- In den lesenden Betriebsarten kann der Browser keine Datenaenderung ausloesen
- Ein Index ausserhalb des Bereichs fuehrt zum Leer-Zustand, nie zum Absturz

---

## Technische Hinweise

### Zustandslosigkeit

Stateless Composable mit State Hoisting: kein ViewModel, kein Repository- oder DAO-Zugriff, keine schreibende Coroutine. Einziger komponenteninterner Zustand ist die Pager-Position; auch das Vollbild-Flag liegt beim Consumer.

### Interface

```kotlin
enum class BrowserBetriebsart { BEARBEITBAR, LESEND_MIT_AKTIONEN, NUR_LESEN }

enum class LabelArt { BAUTEIL, UEBERSICHT, ABLAGEORT }

@Immutable
data class SchrittBrowserZustand(
    val schritte: List<SchrittMitFotos> = emptyList(), // Anzeige-Reihenfolge: Consumer
    val aktiverIndex: Int = 0,
    val aktivesFoto: Int = 0,
    val betriebsart: BrowserBetriebsart = BrowserBetriebsart.NUR_LESEN,
    val vollbild: Boolean = false,
    val zeigeWischHinweis: Boolean = false,
    val zeigeErledigt: Boolean = false   // Haken in der Leiste, nur Montage
) {
    val aktiverSchritt: SchrittMitFotos?
    val fotos: List<SchrittFoto>
    val fotoIndex: Int          // auf die tatsaechliche Fotozahl geklemmt
    val sichtbaresFoto: SchrittFoto?
    val hatFotos: Boolean
    val istErster: Boolean
    val istLetzter: Boolean
}

@Immutable
data class SchrittBrowserAktionen(
    val onSchrittGewaehlt: (index: Int) -> Unit = {},
    val onFotoGewaehlt: (index: Int) -> Unit = {},
    val onVollbildOeffnen: () -> Unit = {},
    val onVollbildSchliessen: () -> Unit = {},
    val onLabelUmgeschaltet: (foto: SchrittFoto, art: LabelArt) -> Unit = { _, _ -> }
)

@Composable
fun SchrittBrowser(
    zustand: SchrittBrowserZustand,
    aktionen: SchrittBrowserAktionen,
    modifier: Modifier = Modifier,
    kopfzeile: @Composable BoxScope.() -> Unit = {},
    ueberLabels: @Composable ColumnScope.() -> Unit = {},
    unterLabels: @Composable ColumnScope.() -> Unit = {},
    bedienkreise: @Composable BoxScope.() -> Unit = {}
)
```

`SchrittMitFotos` ist eine Room-`@Relation` aus `Schritt` und `List<SchrittFoto>` und gehoert der Data-Layer (Datenmodell siehe `F-003-demontage/README.md`). Der Browser liest daraus `schrittNummer`, `eingebautBeiMontage` (nur fuer den Haken) sowie je Foto `id`, `pfad`, `reihenfolge` und die drei Label-Flags. Die `id` ist Pflichtfeld: sie geht in `onLabelUmgeschaltet` zurueck.

**Index-Grenzfaelle** — der Browser klemmt fuer die Darstellung und korrigiert den Zustand des Consumers nie:

| Situation | Darstellung |
|---|---|
| `schritte` leer | keine Leiste, Leer-Zustand im Bildbereich, `istErster` und `istLetzter` beide `true` |
| Schritt ohne Fotos | Leer-Zustand, keine Label-Spalte, Zaehler „KEIN FOTO"; Leiste unveraendert nutzbar |
| Index ausserhalb des Bereichs | wie der jeweilige Leer-Zustand, kein Absturz, keine Rueckmeldung |

**Reset des sichtbaren Fotos:** Beim Schrittwechsel setzt der **Consumer** den Foto-Index auf `0` (`onSchrittGewaehlt` → `aktivesFoto = 0`). Der Pager springt auf diese Seite und meldet die eingerastete Seite ueber `onFotoGewaehlt` zurueck. Es gibt damit genau einen Schreiber pro Wechsel.

### Kategorie-Ableitung fuer die Einfaerbung

Reine Funktion an `SchrittMitFotos`, ohne Compose-Bezug und als Unit-Test pruefbar:

```kotlin
val kategorie: FotoKategorie get() = when {
    fotos.any { it.istAblageort } -> FotoKategorie.ABLAGEORT
    fotos.any { it.istUebersicht } -> FotoKategorie.UEBERSICHT
    fotos.any { it.istBauteil } -> FotoKategorie.BAUTEIL
    else -> FotoKategorie.OHNE
}
```

Die Zuordnung `FotoKategorie` → Streifenfarbe steht in US-006.3 und im Code als `FotoKategorie.farbe`.

### Compose-Bausteine

- **Thumbnail-Leiste:** `BoxWithConstraints` + `Column`. Aus der verfuegbaren Hoehe geteilt durch Kachel plus Abstand ergibt sich, wie viele Kacheln passen; das Fenster ist das Minimum aus `THUMB_FENSTER` (4), dieser Zahl und der Schrittzahl. Kein `LazyRow`, kein Scrollen — das Fenster wandert mit dem aktiven Schritt.
- **Karussell:** `HorizontalPager` ueber die Fotos **eines** Schritts. Der Pager wird von aussen ueber `aktivesFoto` gesetzt und meldet seine eingerastete Seite zurueck.
- **Vollbild:** Overlay innerhalb derselben Komponente, keine eigene Navigations-Route — sonst entstuende Kopplung an den NavHost des Consumers. Der `BackHandler` ist **nur** im Vollbild aktiv; eine unbedingte Sperre wuerde das Vollbild unschliessbar machen.
- **Label:** `LabelZeile` rendert je nach `betriebsart.labelAenderbar` Chips (56dp, Kaestchen mit Haken) oder Plaketten (34dp, Punkt). Ohne Foto wird der Block gar nicht komponiert.
- **Masse und Glas:** ausschliesslich aus `ui/theme/Dimensions.kt` und `ui/theme/GlasRezepte.kt`, keine Magic Numbers im Komponenten-Code.

### Bildladen

Coil `AsyncImage` auf `File(pfad)`; Kacheln `ContentScale.Crop`, Karussell `Crop`, Vollbild `Fit`. Kein Laden in Originalaufloesung ausserhalb des Vollbilds.

### Package und Dateien

`com.boltmind.app.ui.schrittbrowser/` mit `SchrittBrowser.kt` (Rahmen, Slots, Vollbild), `FotoKarussell.kt` (Pager, Leer-Zustand, Punkte/Zaehler/Hinweis), `ThumbnailLeiste.kt`, `LabelZeile.kt` und `SchrittBrowserZustand.kt`. Kein `*ViewModel.kt` und kein `*UiState.kt`.

### Woertliche UI-Texte

Verbindlich fuer UI-Tests. Alle in `res/values/strings_browser.xml`:

| Text | Ressource | Ort |
|---|---|---|
| `Keine Fotos zu diesem Schritt` | `browser_keine_fotos` | Leer-Zustand |
| `KEIN FOTO` | `browser_kein_foto` | Zaehler ohne Fotos |
| `FOTO %1$d/%2$d` | `browser_foto_zaehler` | Zaehler |
| `← WISCHEN` | `browser_wisch_hinweis` | Wisch-Hinweis |
| `+%d ↑` / `+%d ↓` | `browser_ueberlauf_oben` / `_unten` | Ueberlaufzaehler der Leiste |
| `BAUTEIL` / `ÜBERSICHT` / `ABLAGEORT` | `label_bauteil` / `_uebersicht` / `_ablageort` | Label-Chips und -Plaketten |
| `SCHRITT %d` | `vollbild_schritt` | Kopfzeile im Vollbild |
| `Bauteil` / `Übersicht` / `Ablageort` / `ohne Label` | `label_*_lang` / `label_ohne` | Labelzeile im Vollbild (Schreibweise weicht bewusst von den Chips ab) |
| `✓` / `✕` | `zeichen_haken` / `zeichen_kreuz` | Erledigt-Haken, Schliesser |

Die Beschriftungen der Slot-Inhalte („ZURÜCK", „‹", „›", „RAUS", …) gehoeren dem jeweiligen Consumer und stehen in `strings_browser_aktionen.xml`.

### Tests

- Kategorie-Ableitung, Fensterberechnung der Leiste und die Index-Grenzfaelle sind reine Unit-Tests (keine DB, kein Emulator)
- Je User Story eine `@Nested inner class` (siehe `docs/CODING_RULES.md`)
- UI-Verhalten (Sprung, Wischen, Vollbild oeffnen und ueber die Zurueck-Geste schliessen, Chip vs. Plakette je Betriebsart, deaktiviertes Blaettern an den Raendern) als Compose-UI-Test
- Je lesender Betriebsart ein Test, der belegt, dass kein `onLabelUmgeschaltet` ausgeloest werden kann

---

## Offene Fragen

- **[OFFEN]** Ersatzdarstellung fuer eine fehlende Foto-Datei — siehe US-006.8.
- **[OFFEN]** `README.md` dieses Ordners beschreibt Schnittstelle und Schritt-Navigation noch ohne den Entwurf (waagerechte Leiste, `FotoAktion`-Parameter, `-1` als Leer-Index). Bis er nachgezogen ist, gilt diese Datei.
- Die drei MVP-gebundenen Fragen (Symbol zusaetzlich zur Farbe, Pinch-Zoom im Vollbild, Filter bei sehr vielen Schritten) stehen unveraendert in [README.md](README.md#offene-fragen).

---

## Aenderungshistorie

| Datum | Aenderung |
|---|---|
| 2026-07-27 | Vor/Zurueck ist eine Andockstelle im Slot `bedienkreise`, je Betriebsart verschieden besetzt und an den Raendern sichtbar deaktiviert (K-05, US-006.10). |
| 2026-07-27 | Thumbnail-Leiste senkrecht mit Fenster von hoechstens vier Kacheln, berechneter Kachelzahl, Ueberlaufzaehlern, 66dp/54dp und atmendem Rand (US-006.1). |
| 2026-07-27 | Kategorie-Kennzeichnung als Farbstreifen unter der Kachel statt als Rahmen; Farben aus dem Design-System (US-006.3). |
| 2026-07-27 | Karussell mit Punkt-Indikatoren, Zaehler „FOTO n/m" und Wisch-Hinweis; Vollbild mit Kopfzeile, Punktleiste und `BackHandler` nur im Vollbild (US-006.4, US-006.5). |
| 2026-07-27 | Interface auf den gebauten Stand gezogen: `SchrittBrowserZustand` / `SchrittBrowserAktionen`, vier Consumer-Slots, geklemmte Indizes statt `-1`-Protokoll. |
| 2026-07-27 | US-006.11 beschreibt die Consumer-Slots statt eines `FotoAktion`-Parameters. |
