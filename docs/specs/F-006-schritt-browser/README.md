# F-006: Schritt-Browser

## Intention

Der Schritt-Browser ist die **gemeinsame UI-Komponente** fuer das Navigieren durch die Schritte eines Reparaturvorgangs und das Anzeigen der Fotos eines Schritts. Er wird von drei Features genutzt (Demontage, Montage, Archiv-Detailansicht) und existiert genau einmal.

## Problem

Demontage, Montage und Archiv brauchen dieselbe Faehigkeit: durch die Schritte eines Vorgangs blaettern und die Fotos eines Schritts ansehen. Ohne gemeinsame Komponente entstuende dieselbe Logik dreimal — mit drei unterschiedlichen Bedienkonzepten (frueher diskutiert: animierte Kreis-Kette in der Montage, Sprung-Dialog mit Nummernfeld in der Demontage, Foto-Liste im Archiv). Der Mechaniker muesste in jedem Flow etwas anderes lernen, und jede Aenderung an der Foto-Darstellung muesste dreimal nachgezogen werden.

## Loesung

Eine wiederverwendbare, zustandslose Komponente mit klarem Interface:

- Eine **senkrechte Thumbnail-Leiste** am rechten Rand. Sie zeigt nicht alle Schritte, sondern ein Fenster von hoechstens vier Kacheln, das mit dem aktuellen Schritt wandert. Was darueber und darunter liegt, erscheint als Ueberlaufzaehler an den Enden. Jede Kachel zeigt das erste Foto des Schritts, seine Schrittnummer und einen Farbstreifen fuer die Kategorie. Ein Tap springt direkt zu diesem Schritt.
- Ein **Bildkarussell** fuer die N Fotos des aktuellen Schritts, waagerecht wischbar, darunter Punkt-Indikatoren und ein Zaehler. Ein Tap auf ein Foto oeffnet es als Vollbild.
- **Label-Anzeige** am aktuell sichtbaren Foto (Bauteil / Uebersicht / Ablageort) — in allen Betriebsarten sichtbar, aenderbar nur in `BEARBEITBAR`. Hat der Schritt kein Foto, gibt es nichts zu beschriften und die Label-Spalte entfaellt.
- Vier **Slots**, in die der Consumer seine eigenen Bedienelemente einhaengt: `kopfzeile`, `ueberLabels`, `unterLabels`, `bedienkreise`.

**Vor/Zurueck ist ein Slot-Element und gehoert dem Consumer.** Der Browser rendert dafuer kein eigenes Bedienelement; er stellt nur `istErster` und `istLetzter` bereit, damit die Randpruefung nicht dreimal neu entsteht. Der Grund ist der Entwurf: die drei Betriebsarten haben unterschiedliche Button-Saetze an derselben Stelle (Demontage „NOCH'N FOTO" und „NÄCHSTES nn", Montage „RAUS", „ZURÜCK" und „SITZT!", Archiv zwei schlichte Pfeile). Ein einheitliches Vor/Zurueck aus dem Browser haette daneben gestanden statt darin.

Der Thumbnail-Sprung ist damit das einzige Bedienelement fuer den Schritt-Wechsel, das der Browser selbst rendert. Frueher diskutierte Alternativen (Kreis-Kette in F-004, Nummernfeld-Dialog in F-003) sind hinfaellig; einen Sprung-Dialog mit Nummerneingabe gibt es nicht.

**Wischgeste (verbindlich):** Waagerechtes Wischen im Bildbereich wechselt ausschliesslich das **Foto innerhalb des aktuellen Schritts**. Es wechselt **nie** den Schritt. Der Schritt-Wechsel laeuft ueber die Thumbnail-Leiste und die Vor/Zurueck-Elemente des Consumers. Diese Zuordnung ist die Kollisionsaufloesung gegenueber F-004, das dieselbe Geste frueher fuer den Schritt-Wechsel vorgesehen hatte.

**Architektur-Entscheidung:** F-006 ist **kein Service** im Sinne von F-005. Der Browser besitzt keine eigene Tabelle, kein eigenes ViewModel und greift nicht auf Repository oder DAO zu. Er ist eine zustandslose Compose-Komponente mit State Hoisting: der Consumer liefert den Zustand hinein und reagiert auf Rueckrufe. Grund: Die Komponente hat keine eigenen Daten — sie stellt Daten dar, die dem Consumer gehoeren, und meldet Nutzerabsichten zurueck. Persistenz bleibt dort, wo die fachliche Entscheidung faellt.

Eine Ausnahme von „haelt selbst nichts": der Pager des Karussells fuehrt seine Position als eigenen Compose-Zustand. Er wird von aussen nachgezogen, wenn sich der Index oder die Fotoanzahl aendert, und meldet jedes Wischen zurueck. Anders ist ein Pager nicht zu bauen.

**Gemeinsam mit F-005 gilt:** Der Browser kennt seine Consumer nicht. Die Integration wird in den Consumer-Specs beschrieben, nicht hier.

## Primaerer Nutzer

Mechaniker in der Werkstatt — beim Dokumentieren (Demontage), beim Zusammenbauen (Montage) und beim Nachschlagen in einem archivierten Vorgang.

## Kernfaehigkeiten

- Senkrechte Thumbnail-Leiste mit wanderndem Fenster von hoechstens vier Kacheln, aktueller Schritt hervorgehoben und mit atmendem Ring
- Ueberlaufzaehler an beiden Enden, wenn mehr Schritte existieren als das Fenster fasst
- Sprung zu einem beliebigen sichtbaren Schritt per Tap auf sein Thumbnail
- Farbstreifen am unteren Rand des Thumbnails nach Foto-Kategorie des Schritts (Ablageort > Uebersicht > Bauteil)
- Bildkarussell ueber die N Fotos des aktuellen Schritts, waagerecht wischbar (wechselt nur das Foto, nie den Schritt)
- Punkt-Indikatoren, Foto-Zaehler und ein Wisch-Hinweis, solange noch nie gewischt wurde
- Vollbild-Anzeige eines Fotos per Tap, Schliessen ueber die Zurueck-Geste
- Label-Anzeige am sichtbaren Foto in allen Betriebsarten; bedienbar nur in `BEARBEITBAR`
- Drei Betriebsarten: `BEARBEITBAR`, `LESEND_MIT_AKTIONEN`, `NUR_LESEN`
- Platzhalter-Bild bei fehlender Foto-Datei, davon unterschiedener Leer-Zustand ohne Fotos
- Grenzflags `istErster` / `istLetzter` fuer die Vor/Zurueck-Elemente des Consumers

## Betriebsarten

Die drei Betriebsarten unterscheiden sich im Browser **ausschliesslich** darin, ob die Label des sichtbaren Fotos bedienbar sind (`labelAenderbar`). Alles andere — Thumbnail-Leiste, Karussell, Vollbild, Label-Anzeige — ist in allen dreien gleich.

Was ein Consumer daraus macht, entscheidet er selbst: „Foto aufnehmen" und die Schritt-Aktionen sind Slot-Elemente und tauchen im Browser gar nicht auf. Die Tabelle beschreibt deshalb das **Gesamtbild je Consumer**, nicht drei Schalter im Browser.

| Betriebsart | Consumer | Label | Foto aufnehmen | Schritt-Aktionen |
|---|---|---|---|---|
| `BEARBEITBAR` | F-003 Demontage | bedienbar | ja (Consumer-Slot) | ja (Consumer-Slot) |
| `LESEND_MIT_AKTIONEN` | F-004 Montage | sichtbar, nicht aenderbar | nein | ja, „SITZT!" (Consumer-Slot) |
| `NUR_LESEN` | F-001 Archiv | sichtbar, nicht aenderbar | nein | nein |

**Label werden ausschliesslich in der Demontage gesetzt.** In den beiden lesenden Betriebsarten zeigt der Browser die gespeicherten Label des sichtbaren Fotos flach an und nimmt keine Aenderung entgegen.

Die Label sind **Chips, keine Checkboxen**: eine Zeile je Label mit einem farbigen Punkt links. In den lesenden Betriebsarten fehlt ihnen der Klick-Handler, und sie tragen ein flacheres Glas-Rezept — gesetzte und nicht gesetzte Label bleiben unterscheidbar, aber nichts sieht antippbar aus.

## Schnittstelle im Ueberblick

Der Browser nimmt **zwei gebuendelte Objekte** und **vier Slots** entgegen, keine lose Parameterliste:

```kotlin
SchrittBrowser(
    zustand: SchrittBrowserZustand,
    aktionen: SchrittBrowserAktionen,
    modifier: Modifier = Modifier,
    kopfzeile:    @Composable BoxScope.() -> Unit = {},
    ueberLabels:  @Composable ColumnScope.() -> Unit = {},
    unterLabels:  @Composable ColumnScope.() -> Unit = {},
    bedienkreise: @Composable BoxScope.() -> Unit = {}
)
```

Das Buendel ist Absicht: der Browser wird von drei Screens komponiert, und es haelt deren Signaturen stabil, wenn eine Aktion dazukommt.

**Rein — `SchrittBrowserZustand`:**

| Feld | Inhalt |
|---|---|
| `schritte` | `List<SchrittMitFotos>` in **Anzeige-Reihenfolge**. Ein Schritt bringt seine Fotos mit; es gibt keine zweite Liste. Der Consumer bestimmt vorwaerts oder rueckwaerts, der Browser sortiert nicht um |
| `aktiverIndex` | Index des angezeigten Schritts. Voreinstellung `0` |
| `aktivesFoto` | Index des sichtbaren Fotos im Schritt. Voreinstellung `0` |
| `betriebsart` | `BEARBEITBAR`, `LESEND_MIT_AKTIONEN` oder `NUR_LESEN` |
| `vollbild` | ob die Vollbild-Anzeige offen ist |
| `zeigeWischHinweis` | blendet den Hinweis ein, bis einmal gewischt wurde |
| `zeigeErledigt` | markiert eingebaute Schritte in der Leiste. Nur in der Montage sinnvoll |

**Es gibt kein `-1`-Protokoll.** Beide Indizes sind mit `0` vorbelegt und werden fuer die Darstellung auf den gueltigen Bereich geklemmt. Eine leere Schrittliste fuehrt dazu, dass `aktiverSchritt` schlicht `null` ist — der Consumer muss keinen Sonderwert liefern und der Browser keinen erkennen.

**Raus — `SchrittBrowserAktionen`:**

| Rueckruf | Ausgeloest durch |
|---|---|
| `onSchrittGewaehlt(index)` | Tap auf ein Thumbnail |
| `onFotoGewaehlt(index)` | das Karussell ist auf einem anderen Foto zur Ruhe gekommen |
| `onVollbildOeffnen()` | Tap auf das Foto |
| `onVollbildSchliessen()` | Schliesser oder Zurueck-Geste im Vollbild |
| `onLabelUmgeschaltet(foto, art)` | ein Label-Chip wurde angetippt (nur `BEARBEITBAR`). Uebergeben wird das ganze `SchrittFoto` und die `LabelArt` |

Was der Consumer damit macht (Index fortschreiben, Label persistieren, Kamera starten, Timer stoppen), entscheidet der Consumer. **Der Browser schreibt keinen Index selbst fort.**

**Reset des sichtbaren Fotos beim Schrittwechsel gehoert dem Consumer.** Er setzt `aktivesFoto` beim Wechsel auf `0` zurueck; der Browser zieht den Pager daraufhin nach. Frueher war es umgekehrt beschrieben — das haette zwei Schreiber auf denselben Wert gesetzt und eine Rueckkopplung erzeugt, die sich als sprunghaftes Karussell zeigt.

Details siehe [browser.md](browser.md).

## Abgrenzung

| Verantwortung | Gehoert zu F-006 | Gehoert NICHT zu F-006 |
|---|---|---|
| Thumbnail-Leiste rendern, Fenster berechnen, Ueberlauf anzeigen | Ja | -- |
| Sprung zu einem Schritt per Thumbnail melden | Ja | -- |
| Grenzflags `istErster` / `istLetzter` bereitstellen | Ja | -- |
| Foto-Karussell und Vollbild-Anzeige | Ja | -- |
| Label des sichtbaren Fotos anzeigen (alle Betriebsarten) und Aenderung melden (nur `BEARBEITBAR`) | Ja | -- |
| Kategorie eines Schritts fuer den Farbstreifen ableiten | Ja | -- |
| Leer-Zustand des Karussells bei einem Schritt **ohne Fotos** | Ja | -- |
| Platzhalter-Bild bei **fehlender Foto-Datei** | Ja | -- |
| **Vor/Zurueck rendern** | -- | Consumer (Slot `bedienkreise`), unter Nutzung der Grenzflags |
| Label-Aenderung **persistieren** | -- | Consumer (Repository, Sofort-Save) |
| Index nach Vor/Zurueck oder Sprung fortschreiben | -- | Consumer |
| Sichtbares Foto beim Schrittwechsel zuruecksetzen | -- | Consumer |
| Schritte abhaken (`eingebautBeiMontage`) | -- | Consumer F-004 |
| Kamera starten, Foto aufnehmen und anhaengen | -- | Consumer F-003 |
| Aktion am sichtbaren Foto (z.B. „Wiederholen") | -- | Consumer F-003, vollstaendig — Bedienelement **und** Ausfuehrung |
| Schritt anlegen, abschliessen, Flow beenden | -- | Consumer F-003 / F-004 |
| Abschluss-Screen und Archivieren | -- | Consumer F-004 |
| Anzeige-Reihenfolge der Schritte (vorwaerts / rueckwaerts) | -- | Consumer (liefert die sortierte Liste) |
| Daten aus der DB laden | -- | Consumer (ViewModel + Repository) |
| Zeitmessung pro Schritt | -- | F-005 Zeiterfassung, gesteuert vom Consumer |
| Hinweistext bei Vorgang ganz ohne Schritte | -- | Consumer (Screen-Ebene) |

## Consumer

| Feature | Nutzung | Betriebsart | Beschrieben in |
|---|---|---|---|
| F-003 Demontage | Karussell des aktuellen Schritts, bedienbare Label, eigene Aktionskreise inkl. „Wiederholen" und Vor/Zurueck | `BEARBEITBAR` | F-003 workflow.md / views |
| F-004 Montage | Schritte in Montage-Reihenfolge; Label werden nur gelesen, Aktionskreise „RAUS" / „ZURÜCK" / „SITZT!" gehoeren F-004 | `LESEND_MIT_AKTIONEN` | F-004 montage.md |
| F-001 Archiv-Detailansicht | Durchblaettern der Dokumentation eines archivierten Vorgangs, zwei schlichte Pfeile als Vor/Zurueck | `NUR_LESEN` | F-001 uebersicht.md |

Alle drei Consumer bringen ihre **Vor/Zurueck-Elemente selbst** mit und haengen sie in den Slot `bedienkreise`. Der Thumbnail-Sprung dagegen kommt aus F-006.

Fuer die Randpruefung bietet der Browser `istErster` und `istLetzter` an. **Gebaut ist es anders:** der Browser-Screen prueft mit eigenen Feldern (`istErsterSchritt` / `istLetzterSchritt` auf `BrowserUiState`), die Flags des Browsers haben null Aufrufstellen. Die Doppelung ist bekannt und als #127 notiert.

## Abhaengigkeiten

- **Keine Feature-Abhaengigkeiten.** Die Komponente ist autark und kennt keinen ihrer Consumer. Abhaengigkeitsrichtung: Consumer → F-006.
- **Datenmodell:** Die Entities `Schritt` und `SchrittFoto` gehoeren zum Demontage-Datenmodell (F-003). F-006 nimmt sie als `SchrittMitFotos` entgegen und greift nicht selbst auf die DB zu.
- **Governance:** Sofort-Save, 300ms Debounce, Platzhalter-Bild bei fehlender Datei, Foto-Label und Prioritaetsregel (Ablageort > Uebersicht > Bauteil) sowie die verbindlichen **Touch-Target-Mindestmasse** — alles in [../governance.md](../governance.md). F-006 nennt dafuer keine eigenen Zahlen. Zur Ausnahme fuer Thumbnails siehe [../design-system.md](../design-system.md), K-01.

## Ordner-Inhalt

| Datei | Typ | Beschreibung |
|---|---|---|
| [browser.md](browser.md) | Komponenten-Spec | User Stories, Akzeptanzkriterien, Interface, Nicht-funktionale Anforderungen, Technische Hinweise |

## Entschieden

- [x] **ENTSCHIEDEN:** Der Browser kennt **drei** Betriebsarten (`BEARBEITBAR` / `LESEND_MIT_AKTIONEN` / `NUR_LESEN`). Label werden ausschliesslich in der Demontage gesetzt. Siehe Abschnitt „Betriebsarten".
- [x] **ENTSCHIEDEN:** Die gesetzten Label des sichtbaren Fotos sind **in allen Betriebsarten** sichtbar — in den lesenden als nicht bedienbare Anzeige. F-004 braucht das fuer die Ablageort-Kennzeichnung am Foto (US-004.1). Die frueher offene Frage „Label im nur-lesen-Modus sichtbar?" ist damit mit **Ja** beantwortet.
- [x] **ENTSCHIEDEN:** Der **Thumbnail-Sprung** gehoert F-006. **Vor/Zurueck** gehoert dem Consumer und wird ueber den Slot `bedienkreise` eingehaengt; F-006 liefert dazu die Grenzflags. Einen Sprung-Dialog mit Nummerneingabe gibt es nicht.
- [x] **ENTSCHIEDEN:** Waagerechtes Wischen im Bildbereich wechselt das Foto, nie den Schritt (US-006.4).
- [x] **ENTSCHIEDEN:** Kein `-1`-Protokoll fuer leere Listen. Die Indizes werden geklemmt, `aktiverSchritt` ist bei leerer Liste `null`.

## Offene Fragen

Fuer alle drei Punkte gilt die MVP-Antwort als **bindend**: Sie ist der Stand, gegen den implementiert und getestet wird. Offen ist jeweils nur die spaetere Erweiterung — bis zu einer Entscheidung darueber ist sie **keine Anforderung** und in keinem Feature zu implementieren.

- [ ] **OFFEN:** Soll die Kategorie eines Thumbnails zusaetzlich zur Farbe durch ein Symbol erkennbar sein (Farbfehlsichtigkeit, Sonnenlicht in der Halle)? **MVP (bindend):** nur die in browser.md US-006.3 festgelegte Markierung — ein Farbstreifen am unteren Rand der Kachel. Ein Symbol ist bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
- [ ] **OFFEN:** Braucht die Vollbild-Anzeige Pinch-Zoom? **MVP (bindend): Nein**, die Anzeige aus US-006.5 reicht. Pinch-Zoom ist bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
- [ ] **OFFEN:** Braucht die Leiste bei sehr vielen Schritten (>50) eine Filter- oder Sprungfunktion (z.B. „nur Schritte mit Ablageort")? **MVP (bindend): Nein**, das wandernde Fenster mit Ueberlaufzaehlern aus US-006.1 reicht. Filter oder Sprungfunktion sind bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.

## Aenderungshistorie

| Datum | Aenderung |
|---|---|
| 2026-07-27 | Gegen den gebauten Stand nachgezogen (#109). Korrigiert: Leiste ist senkrecht mit wanderndem Fenster statt waagerecht ueber alle Schritte; Vor/Zurueck gehoert dem Consumer, nicht F-006; kein `FotoAktion`-Parameter; kein `-1`-Protokoll; Betriebsart statt Modus; Label sind Chips, keine Checkboxen; Farbstreifen statt Rahmenfarbe; der Foto-Reset gehoert dem Consumer. Die drei MVP-bindenden Antworten bleiben unveraendert — nur zwei ihrer Begruendungen waren falsch. |
