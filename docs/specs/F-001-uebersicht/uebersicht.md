# F-001: Vorgangs-Übersicht

## Kontext

Die Vorgangs-Übersicht ist der Startscreen der App. Der Mechaniker hat typischerweise 1-3 offene Reparaturvorgänge gleichzeitig. Sein primäres Ziel beim Öffnen der App ist ein schneller Überblick: Was ist offen, wo mache ich weiter?

Das Archiv ist strategisch wichtig: Die erfassten Zeitdaten (F-005) sollen langfristig genutzt werden, um Arbeitsdauern zu analysieren und Angebote besser kalkulieren zu können.

## User Stories

### US-001.1: Offene Vorgänge anzeigen

**Als** Mechaniker
**möchte ich** beim Öffnen der App sofort alle offenen Reparaturvorgänge sehen
**damit** ich weiß, welche Aufträge aktuell in Arbeit sind und direkt weiterarbeiten kann.

#### Akzeptanzkriterien

- **Given** die App wird geöffnet und es existieren offene Vorgänge
  **When** der Startscreen geladen ist
  **Then** werden alle offenen Vorgänge als Liste angezeigt, sortiert nach letzter Bearbeitung (neueste oben)

- **Given** ein offener Vorgang existiert
  **When** der Vorgang in der Liste angezeigt wird
  **Then** sind folgende Informationen sichtbar: Fahrzeugfoto (Thumbnail), Auftragsnummer, Anzahl Schritte, Erstellungsdatum (Quelle: `erstelltAm`)

- **Given** ein Vorgang wurde am heutigen Tag angelegt
  **When** seine Karte angezeigt wird
  **Then** steht als Datum genau der Text "Heute"

- **Given** ein Vorgang wurde am Vortag angelegt
  **When** seine Karte angezeigt wird
  **Then** steht als Datum genau der Text "Gestern"

- **Given** ein Vorgang wurde vor mehr als einem Tag angelegt (z.B. am 01.05.2024)
  **When** seine Karte angezeigt wird
  **Then** steht als Datum das Kalenderdatum im Format `TT.MM.JJJJ` (im Beispiel "01.05.2024") und **kein** relativer Text
  > Dieses Datumsformat ("Heute" / "Gestern" / `TT.MM.JJJJ`) gilt für **jedes** Datum in F-001 — auch für das Abschlussdatum im Archiv (US-001.5).

- **Given** die App wird geöffnet und es existieren keine offenen Vorgänge
  **When** der Startscreen geladen ist
  **Then** wird ein Hinweis angezeigt, dass keine Vorgänge vorhanden sind (z.B. "Noch keine Vorgänge. Tippe auf + um zu starten.")

#### UI-Verhalten
- Vorgangskarte: Thumbnail links, Auftragsnummer + Metadaten rechts
- Liste lädt sofort ohne Spinner (Quality Goal #3)
- Karten sind groß genug für Bedienung mit Handschuhen/öligen Händen (Quality Goal #1)

---

### US-001.2: Vorgang für Weiterarbeit öffnen

**Als** Mechaniker
**möchte ich** einen Vorgang antippen und wählen ob ich demontieren oder montieren will
**damit** ich direkt im richtigen Modus weiterarbeiten kann.

#### Akzeptanzkriterien

- **Given** ein offener Vorgang mit 0 Schritten existiert (frisch angelegt)
  **When** der Mechaniker den Vorgang antippt
  **Then** öffnet sich direkt der Demontage-Flow (F-003)

- **Given** ein offener Vorgang mit mindestens 1 Schritt existiert
  **When** der Mechaniker den Vorgang antippt
  **Then** erscheint ein Auswahl-Dialog mit "Weiter demontieren" und "Montage starten"

- **Given** der Auswahl-Dialog ist sichtbar
  **When** der Mechaniker "Weiter demontieren" wählt
  **Then** öffnet sich der Demontage-Flow (F-003) für diesen Vorgang

- **Given** der Auswahl-Dialog ist sichtbar
  **When** der Mechaniker "Montage starten" wählt
  **Then** öffnet sich der Montage-Flow (F-004) für diesen Vorgang

#### UI-Verhalten
- Auswahl-Dialog: BottomSheet oder Dialog mit zwei großen Buttons
- Fahrzeugfoto und Auftragsnummer im Dialog-Header zur Bestätigung
- **Verbindlicher Wortlaut:** Die beiden Buttons heißen exakt "Weiter demontieren" und "Montage starten" — unabhängig davon, ob der Vorgang 1 oder 40 Schritte hat. Der Dialog gehört F-001; andere Specs (F-003, F-004) verweisen darauf, statt eigene Bezeichnungen wie "Demontage starten" oder "Demontage fortsetzen" einzuführen. Ein Compose-UI-Test darf gegen genau diese beiden Texte prüfen (`weiter_demontieren`, `montage_starten` in `strings.xml`)

---

### US-001.3: Neuen Vorgang starten

**Als** Mechaniker
**möchte ich** über einen gut sichtbaren Button einen neuen Reparaturvorgang anlegen können
**damit** ich schnell mit einem neuen Auftrag beginnen kann.

#### Akzeptanzkriterien

- **Given** der Mechaniker ist auf dem Startscreen
  **When** er auf den "+"-Button tippt
  **Then** wird der Anlage-Flow (F-002) gestartet

#### UI-Verhalten
- FAB (Floating Action Button) oder prominenter "+"-Button
- Immer sichtbar, auch wenn die Liste scrollbar ist

---

### US-001.4: Vorgang löschen

**Als** Mechaniker
**möchte ich** einen Vorgang per Wischgeste löschen können
**damit** ich fehlerhafte oder nicht mehr benötigte Vorgänge entfernen kann.

#### Akzeptanzkriterien

- **Given** ein Vorgang wird in der Liste angezeigt (offen oder archiviert)
  **When** der Mechaniker die Karte nach links wischt
  **Then** wird ein Löschen-Button sichtbar

- **Given** der Löschen-Button ist sichtbar
  **When** der Mechaniker auf "Löschen" tippt
  **Then** erscheint ein Bestätigungsdialog ("Vorgang und alle Fotos unwiderruflich löschen?")

- **Given** der Bestätigungsdialog ist sichtbar
  **When** der Mechaniker "Löschen" bestätigt
  **Then** werden der Vorgang, alle zugehörigen Schritte und alle Fotos gelöscht

- **Given** der Bestätigungsdialog ist sichtbar
  **When** der Mechaniker "Abbrechen" wählt
  **Then** bleibt der Vorgang erhalten und die Swipe-Aktion wird zurückgesetzt

#### UI-Verhalten
- Swipe-to-Delete (Material-Pattern)
- Bestätigungsdialog mit rotem "Löschen"-Button (versehentliches Löschen verhindern)

---

### US-001.5: Archivierte Vorgänge einsehen

**Als** Mechaniker
**möchte ich** abgeschlossene Reparaturvorgänge im Archiv einsehen können
**damit** ich bei Reklamationen nachschauen und Arbeitsdauern für die Kalkulation analysieren kann.

#### Akzeptanzkriterien

- **Given** der Mechaniker ist auf dem Startscreen im Tab "Offen"
  **When** er auf den Tab "Archiv" wechselt
  **Then** werden alle archivierten Vorgänge angezeigt, sortiert nach letzter Bearbeitung (neueste oben) — dieselbe Sortierung wie im Tab "Offen"

- **Given** archivierte Vorgänge existieren
  **When** ein archivierter Vorgang in der Liste angezeigt wird
  **Then** sind folgende Informationen sichtbar: Fahrzeugfoto (Thumbnail), Auftragsnummer, Anzahl Schritte, Abschlussdatum (Quelle: `aktualisiertAm`, siehe Technische Hinweise)

- **Given** ein archivierter Vorgang wird in der Liste angezeigt
  **When** sein Abschlussdatum dargestellt wird
  **Then** gilt dasselbe Datumsformat wie im Tab "Offen": "Heute", "Gestern" oder `TT.MM.JJJJ` (US-001.1)

- **[F-005-abhängig]** **Given** archivierte Vorgänge existieren
  **When** ein archivierter Vorgang in der Liste angezeigt wird
  **Then** wird zusätzlich die Gesamtdauer des Vorgangs angezeigt
  > Setzt F-005 (Zeiterfassung) voraus. F-005 ist nicht implementiert; solange die Tabelle `zeit_messung` keine Daten liefert, entfällt die Dauer-Anzeige ersatzlos. Es wird **keine** Ersatzquelle aus Schritt-Zeitstempeln verwendet (siehe Technische Hinweise).

- **Given** keine archivierten Vorgänge existieren
  **When** der Mechaniker den Archiv-Tab öffnet
  **Then** wird ein Hinweis angezeigt (z.B. "Noch keine abgeschlossenen Vorgänge.")

- **Given** ein archivierter Vorgang wird angezeigt
  **When** der Mechaniker den Vorgang antippt
  **Then** öffnet sich die Archiv-Detailansicht mit dem Schritt-Browser (F-006) im Modus **nur-lesen**, beginnend beim ersten Schritt — hat der Vorgang keine Schritte, gilt stattdessen das Kriterium weiter unten

- **Given** die Archiv-Detailansicht wird geöffnet
  **When** F-001 die Schrittliste an den Schritt-Browser (F-006) übergibt
  **Then** sind die Schritte **aufsteigend nach `schrittNummer`** sortiert (Demontage-Reihenfolge)
  **And** "beginnend beim ersten Schritt" bedeutet damit: beim Schritt mit der niedrigsten `schrittNummer`
  > F-006 sortiert nicht selbst, sondern verlangt vom Consumer eine bereits sortierte Liste und übernimmt deren Reihenfolge unverändert (F-006 browser.md, Interface). F-001 wählt **aufsteigend**, weil im Archiv nachvollzogen werden soll, *wie zerlegt wurde* — die Demontage-Reihenfolge ist die Erzählreihenfolge der Dokumentation. F-003 übergibt aus demselben Grund aufsteigend; nur F-004 dreht die Reihenfolge um, weil montiert rückwärts wird.

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** sie geladen ist
  **Then** sind Auftragsnummer und Fahrzeugfoto des Vorgangs sichtbar

- **Given** ein archivierter Vorgang enthält überhaupt keine Schritte
  **When** der Mechaniker ihn antippt und die Archiv-Detailansicht geladen ist
  **Then** erscheint oberhalb des Schritt-Browsers der Hinweistext "Keine Demontage-Schritte dokumentiert."
  **And** die Thumbnail-Leiste bleibt leer, der Karussell-Bereich zeigt den Leer-Zustand und beide Vor-/Zurück-Bedienelemente sind deaktiviert (F-006 US-006.9)
  > Der Hinweistext liegt bewusst auf Screen-Ebene: F-006 überlässt ihn ausdrücklich dem Consumer (F-006 README, Abgrenzung). Der Text nennt keine Handlungsaufforderung wie "Zuerst demontieren" — der Vorgang ist archiviert und wird nicht mehr bearbeitet.

- **[F-005-abhängig]** **Given** die Archiv-Detailansicht ist geöffnet
  **When** sie geladen ist
  **Then** wird zusätzlich die Gesamtdauer des Vorgangs angezeigt
  > Setzt F-005 voraus. Ohne F-005 entfällt die Dauer-Anzeige.

- **Given** die Archiv-Detailansicht zeigt einen Schritt
  **When** der Mechaniker die Vor-/Zurück-Bedienung des Schritt-Browsers (F-006) nutzt
  **Then** wird der vorherige bzw. nächste Schritt mit Schrittnummer und seinen Fotos angezeigt
  > Die Schritt-Navigation (vor/zurück und Thumbnail-Sprung) gehört vollständig zu F-006. F-001 stellt dafür keine eigene Bedienung bereit.

- **[F-005-abhängig]** **Given** die Archiv-Detailansicht zeigt einen Schritt
  **When** der Schritt angezeigt wird
  **Then** wird die Dauer dieses Schritts oberhalb des Schritt-Browsers angezeigt (Consumer-Chrome, nicht Teil von F-006)
  > Setzt F-005 voraus. Quelle ist ausschließlich die Tabelle `zeit_messung`; ohne F-005 entfällt die Dauer-Anzeige.

- **Given** die Archiv-Detailansicht zeigt einen Schritt
  **When** der Mechaniker in der Thumbnail-Leiste das Thumbnail eines anderen Schritts antippt
  **Then** springt die Ansicht direkt zu diesem Schritt

- **Given** ein Schritt mit mehreren Fotos wird angezeigt
  **When** der Mechaniker im Bildkarussell horizontal wischt
  **Then** werden die Fotos dieses Schritts der Reihe nach angezeigt

- **Given** ein Foto wird im Bildkarussell angezeigt
  **When** der Mechaniker das Foto antippt
  **Then** wird es im Vollbild angezeigt

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** ein Schritt mit seinen Fotos angezeigt wird
  **Then** wird keine bearbeitende Aktion angeboten (kein Foto aufnehmen, keine Label-Änderung, kein Anlegen oder Löschen von Schritten) und auch keine Flow-Aktion (kein "Eingebaut", kein "Beenden") — das entspricht dem F-006-Modus **nur-lesen**; die gesetzten Label sind sichtbar, aber nicht änderbar

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** wird zur Übersicht in den Tab "Archiv" zurückgekehrt, ohne dass Daten verändert wurden

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** der Mechaniker den Zurück-Pfeil in der TopBar antippt
  **Then** wird zur Übersicht in den Tab "Archiv" zurückgekehrt, ohne dass Daten verändert wurden
  > **Namensabgrenzung (wie in F-004 entschieden):** Die Beschriftungen "Zurück" und "Weiter" gehören ausschließlich der Schritt-Navigation aus F-006 im Inhaltsbereich. Der Ausstieg aus der Archiv-Detailansicht läuft ausschließlich über die Android-Zurück-Geste und den Zurück-Pfeil in der TopBar; einen Button mit der Beschriftung "Zurück", der die Ansicht verlässt, gibt es nicht. Ein Tap auf "Zurück" im Inhaltsbereich wechselt immer nur den betrachteten Schritt.

## Nicht-funktionale Anforderungen

- **Bedienbarkeit** (Quality Goal #1): Große Karten, gut treffbar mit Handschuhen/öligen Händen — die verbindlichen Mindestmaße für Touch-Targets und Abstände stehen in [../governance.md](../governance.md), Abschnitt "Touch-Targets". Swipe-Geste großzügig tolerant.
- **Performance** (Quality Goal #3): Liste lädt sofort beim App-Start, kein Spinner.
- **Zuverlässigkeit** (Quality Goal #2): Löschung ist kaskadierend und vollständig (keine verwaisten Fotos).

## Technische Hinweise

- Room-Datenbank: `Reparaturvorgang` Entity mit Status-Feld (`OFFEN`, `ARCHIVIERT`)
- Compose: `LazyColumn` für die Liste, `SwipeToDismiss` für Löschen
- Navigation: Compose Navigation zu F-002, F-003, F-004 sowie zur Archiv-Detailansicht
- Auswahl-Dialog: `ModalBottomSheet` oder `AlertDialog` mit zwei Buttons
- Thumbnail-Loading: Foto aus Filesystem laden, skaliert auf Karten-Größe (kein Full-Size laden)
- Sortierung: `ORDER BY aktualisiertAm DESC` — für offene **und** archivierte Vorgänge. Die Entity `Reparaturvorgang` hat die Zeitstempel `erstelltAm` und `aktualisiertAm` (deutsche Feldnamen, Governance/DDD). Beide Listen führen die Sortierung als Akzeptanzkriterium (US-001.1 bzw. US-001.5). Dass `aktualisiertAm` tatsächlich "zuletzt angefasst" abbildet, garantiert die projektweite Invariante in [../governance.md](../governance.md), Abschnitt "Sofort-Save Strategie / Invariante `aktualisiertAm`": jede erzeugende, ändernde oder löschende Aktion an einem Vorgang oder seinen Schritten und Fotos schreibt das Feld in derselben Operation mit — einschließlich des Archivierens (F-004)
- Datumsformat (beide Listen und die Archiv-Detailansicht, ein einziger Formatierer): heutiger Kalendertag → "Heute", Vortag → "Gestern", älter → `TT.MM.JJJJ` (`DateTimeFormatter.ofPattern("dd.MM.yyyy")`). Verglichen werden **Kalendertage in der lokalen Zeitzone**, nicht 24-Stunden-Abstände. "Heute" und "Gestern" liegen als Strings in `strings.xml`, damit UI-Tests gegen feste Texte prüfen können
- **Bewusste Abweichung (Anzeige vs. Sortierung):** Die Karte im Tab "Offen" zeigt `erstelltAm`, sortiert wird aber nach `aktualisiertAm`. Nach dem sichtbaren Datum kann die Liste dadurch unsortiert wirken. Das ist so gewollt: Das Anlagedatum identifiziert den Auftrag wiedererkennbar ("der Wagen von gestern"), während die Reihenfolge abbilden soll, woran zuletzt gearbeitet wurde. Bei den typischen 1-3 offenen Vorgängen ist der Effekt vernachlässigbar. Im Tab "Archiv" tritt er nicht auf, weil Anzeige und Sortierung dort dieselbe Quelle (`aktualisiertAm`) nutzen
- Auswahl-Dialog Beschriftungen (verbindlich, US-001.2): `weiter_demontieren` = "Weiter demontieren", `montage_starten` = "Montage starten"
- **Einschränkung (Abschlussdatum):** Ein eigener Archivierungs-Zeitstempel (`archiviertAm` o.ä.) existiert **nicht**. Das im Archiv angezeigte Abschlussdatum ist `aktualisiertAm` **zum Zeitpunkt des Archivierens** — F-004 schreibt das Feld beim Archivieren mit (governance.md, Invariante `aktualisiertAm`). Danach ändert sich der Wert nicht mehr, weil archivierte Vorgänge ausschließlich **lesend** geöffnet werden (Modus nur-lesen, US-001.5): es existiert keine Aktion, die `aktualisiertAm` erneut setzen könnte. Ein dediziertes Feld ist deshalb nicht erforderlich
- Gesamtdauer (Archiv-Karten und Archiv-Detailansicht): Summe der `ZeitMessung`-Einträge der Schritte des Vorgangs aus der Tabelle `zeit_messung` (F-005). **Nicht** aus `Schritt.gestartetAm`/`abgeschlossenAm` — das sind Workflow-Timestamps und ausdrücklich keine Zeitmessung (F-003 workflow.md; Governance: keine Dual-Purpose-Felder). F-005 ist nicht implementiert: solange keine `zeit_messung`-Daten vorliegen, wird **keine** Dauer angezeigt (weder Gesamtdauer noch Dauer je Schritt)
- Archiv-Detailansicht bei einem Vorgang ohne Schritte: Der Hinweistext "Keine Demontage-Schritte dokumentiert." wird vom Screen gerendert, nicht vom Browser (F-006 überlässt ihn dem Consumer). Der Browser wird trotzdem eingebunden — mit leerer Schrittliste und `aktuellerIndex = -1` / `sichtbaresFotoIndex = -1` (F-006 browser.md, Interface-Skizze) — und zeigt seinen Leer-Zustand. Der Hinweistext liegt in `strings.xml`
- Archiv-Detailansicht: Bindet den Schritt-Browser aus F-006 im Modus **nur-lesen** ein (Thumbnail-Leiste, Bildkarussell, Vollbild, Vor/Zurück zwischen Schritten). Die Schritt-Navigation und die Karussell-Logik gehören vollständig F-006; F-001 implementiert beides nicht selbst, sondern reagiert nur auf die Callbacks des Browsers
- Anzeige-Reihenfolge der Schritte (Eingabe an F-006): Query `ORDER BY schrittNummer ASC` — die Archiv-Detailansicht zeigt die Schritte in Demontage-Reihenfolge (US-001.5). F-006 übernimmt die Reihenfolge unverändert; "Weiter" führt damit zur nächsthöheren Schrittnummer, der Startindex 0 ist der Schritt mit der niedrigsten `schrittNummer`. Unterschied zu F-004, das dieselbe Liste für die Montage absteigend übergibt
- Verlassen der Archiv-Detailansicht: Android-Zurück-Geste und Zurück-Pfeil in der TopBar (Navigation-Icon der `TopAppBar`). Die Wörter "Zurück"/"Weiter" als Button-Beschriftung sind für die F-006-Schritt-Navigation im Inhaltsbereich reserviert und dürfen im Screen-Chrome nicht vergeben werden
- Modus nur-lesen: Der Schritt-Browser wird ohne bearbeitende Callbacks eingebunden. Label sind sichtbar, aber nicht änderbar (Label werden ausschließlich in der Demontage gesetzt); die Daten werden ausschließlich lesend geladen

## UI-Skizze

### Startscreen (Offene Vorgänge)
```
┌─────────────────────────────┐
│  BoltMind                   │
├──────────┬──────────────────┤
│  Offen   │   Archiv         │
├──────────┴──────────────────┤
│                             │
│  ┌──────┬──────────────┐    │
│  │[Foto]│ #2024-0815    │    │
│  │      │ 12 Schritte   │    │
│  │      │ Heute         │    │
│  └──────┴──────────────┘    │
│                             │
│  ┌──────┬──────────────┐    │
│  │[Foto]│ #2024-0712    │    │
│  │      │ 8 Schritte    │    │
│  │      │ Gestern       │    │
│  └──────┴──────────────┘    │
│                             │
│                        [+]  │
└─────────────────────────────┘
```

### Auswahl-Dialog
```
┌─────────────────────────────┐
│                             │
│  [Foto] · #2024-0815        │
│                             │
│  ┌─────────────────────┐    │
│  │  Weiter demontieren  │    │
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │  Montage starten     │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

### Leerer Zustand
```
┌─────────────────────────────┐
│  BoltMind                   │
├──────────┬──────────────────┤
│  Offen   │   Archiv         │
├──────────┴──────────────────┤
│                             │
│                             │
│     Noch keine Vorgänge.    │
│     Tippe auf + um zu       │
│     starten.                │
│                             │
│                             │
│                        [+]  │
└─────────────────────────────┘
```
