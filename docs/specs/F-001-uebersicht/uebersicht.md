# F-001: Vorgangs-Übersicht

## Kontext

Die Vorgangs-Übersicht ist der erste Arbeitsbildschirm der App — sie erscheint direkt nach dem Splash. Der Mechaniker hat typischerweise 1-3 offene Reparaturvorgänge gleichzeitig. Sein primäres Ziel beim Öffnen der App ist ein schneller Überblick: Was ist offen, wo mache ich weiter?

Das Archiv ist strategisch wichtig: Die erfassten Zeitdaten (F-005) werden langfristig genutzt, um Arbeitsdauern zu analysieren und Angebote besser kalkulieren zu können. Deshalb trägt jede Archivkarte die gemessene Dauer.

Wörtliche UI-Texte sind in diesem Dokument als `` `key` `` = "Text" notiert; die vollständige Liste steht unter [Wörtliche UI-Texte](#wörtliche-ui-texte). Ein UI-Test darf gegen genau diese Texte prüfen.

## User Stories

### US-001.1: Offene Vorgänge anzeigen

**Als** Mechaniker
**möchte ich** beim Öffnen der App sofort alle offenen Reparaturvorgänge sehen
**damit** ich weiß, welche Aufträge aktuell in Arbeit sind und direkt weiterarbeiten kann.

#### Akzeptanzkriterien

- **Given** die App ist geöffnet und es existieren offene Vorgänge
  **When** der Tab `uebersicht_tab_offen` = "OFFEN" aktiv ist
  **Then** werden alle offenen Vorgänge als Liste angezeigt, sortiert nach letzter Bearbeitung (neueste oben, Quelle `aktualisiertAm`)

- **Given** ein offener Vorgang wird in der Liste angezeigt
  **When** seine Karte gezeichnet wird
  **Then** sind sichtbar: Fahrzeugfoto (Thumbnail), Auftragsnummer mit vorangestelltem `#`, die Beschreibung des Auftrags, ein Chip `uebersicht_teile` = "%1$d TEILE" mit der Anzahl der Schritte und das Datum (Quelle `erstelltAm`)

- **Given** ein offener Vorgang hat keine Beschreibung
  **When** seine Karte gezeichnet wird
  **Then** steht an ihrer Stelle genau der Text `uebersicht_ohne_beschreibung` = "Ohne Beschreibung"

- **Given** ein Vorgang wurde vor weniger als 60 Sekunden zuletzt angefasst
  **When** sein Datum dargestellt wird
  **Then** steht dort genau der Text "Gerade eben" — auch dann, wenn der Zeitpunkt kalendarisch schon auf gestern fällt

- **Given** ein Vorgang stammt vom heutigen Kalendertag und ist älter als 60 Sekunden
  **When** sein Datum dargestellt wird
  **Then** steht dort "Heute, hh:mm" mit zweistelliger Stunde und Minute (Beispiel: "Heute, 08:12")

- **Given** ein Vorgang stammt vom Vortag
  **When** sein Datum dargestellt wird
  **Then** steht dort "Gestern, hh:mm" (Beispiel: "Gestern, 15:40")

- **Given** ein Vorgang ist älter als der Vortag (z.B. 01.05.2024)
  **When** sein Datum dargestellt wird
  **Then** steht dort das Kalenderdatum im Format `TT.MM.JJJJ` (im Beispiel "01.05.2024"), **ohne** Uhrzeit und **ohne** Monatsnamen
  > Die Uhrzeit auf den beiden Wortstufen trennt mehrere Aufträge desselben Tages ("Heute, 08:12" gegen "Heute, 14:40"). Ein Monatsname ohne Jahr ("12. Juli") wäre im Langzeitarchiv mehrdeutig und ist deshalb ausgeschlossen (design-system.md, Abschnitt "Bewusste Abweichungen vom Prototyp", K-09).

- **Given** verglichen werden zwei Zeitpunkte
  **When** die Stufe "Heute" oder "Gestern" bestimmt wird
  **Then** entscheidet der **Kalendertag in der lokalen Zeitzone**, nicht ein 24-Stunden-Abstand

- **Given** die Tableiste ist sichtbar
  **When** sie gezeichnet wird
  **Then** trägt jeder Tab die Anzahl der Vorgänge in seiner Liste, und der aktive Tab ist orange hervorgehoben

- **Given** es existieren keine offenen Vorgänge
  **When** der Tab "OFFEN" aktiv ist
  **Then** erscheint der Leerzustand mit `uebersicht_leer_titel_offen` = "NICHTS OFFEN" und `uebersicht_leer_text_offen` = "Tipp auf NEUER AUFTRAG und leg los."

#### UI-Verhalten

- Vorgangskarte: Fahrzeugfoto (96dp) links, Auftragsnummer, Beschreibung, Chip und Datum rechts, Löschen-Knopf am rechten Rand (US-001.4)
- Glaskarte über dem Mesh-Hintergrund (design-system.md, Abschnitt "Glas", Rezept GD)
- Liste lädt sofort ohne Spinner (Quality Goal #3)
- Karten sind groß genug für Bedienung mit Handschuhen/öligen Händen (Quality Goal #1)

---

### US-001.2: Vorgang für Weiterarbeit öffnen

**Als** Mechaniker
**möchte ich** einen Vorgang antippen und wählen, ob ich demontieren oder montieren will
**damit** ich direkt im richtigen Modus weiterarbeiten kann.

#### Akzeptanzkriterien

- **Given** ein offener Vorgang **ohne** Schritte existiert
  **When** der Mechaniker den Vorgang antippt
  **Then** öffnet sich **ohne** Auswahl-Sheet direkt der Demontage-Flow (F-003)
  > "Montage starten" führt bei null Schritten garantiert in eine Sackgasse — es gibt nichts einzubauen. Eine Auswahl mit nur einer sinnvollen Antwort ist keine Auswahl (design-system.md, Abschnitt "Bewusste Abweichungen vom Prototyp", K-10). Der Fall ist ein Sicherheitsnetz: F-002 legt zusammen mit dem Vorgang immer Schritt 1 an (F-002 anlegen.md), und Schritte lassen sich nicht löschen.

- **Given** ein offener Vorgang mit mindestens einem Schritt existiert
  **When** der Mechaniker den Vorgang antippt
  **Then** erscheint ein Auswahl-Sheet mit Fahrzeugfoto, "#Auftragsnummer" als Titel, der Beschreibung als Text und den beiden Aktionen `uebersicht_weiter_demontieren` = "WEITER DEMONTIEREN" (primär) und `uebersicht_montage_starten` = "MONTAGE STARTEN"

- **Given** das Auswahl-Sheet ist sichtbar
  **When** der Mechaniker "WEITER DEMONTIEREN" wählt
  **Then** öffnet sich der Demontage-Flow (F-003) für diesen Vorgang

- **Given** das Auswahl-Sheet ist sichtbar
  **When** der Mechaniker "MONTAGE STARTEN" wählt
  **Then** öffnet sich der Montage-Flow (F-004) für diesen Vorgang

- **Given** das Auswahl-Sheet ist sichtbar
  **When** der Mechaniker neben das Sheet tippt
  **Then** schließt das Sheet, ohne zu navigieren

#### UI-Verhalten

- Bottom-Sheet mit zwei großformatigen Aktionen (design-system.md, Rezept GS)
- **Verbindlicher Wortlaut:** Die beiden Aktionen heißen "Weiter demontieren" und "Montage starten" — unabhängig davon, ob der Vorgang 1 oder 40 Schritte hat. Der Dialog gehört F-001; andere Specs (F-003, F-004) verweisen darauf, statt eigene Bezeichnungen wie "Demontage starten" oder "Demontage fortsetzen" einzuführen
- **Großschreibung ist Darstellung, nicht Wortlaut.** Die Ressourcen halten sie versal ("WEITER DEMONTIEREN"), weil der Entwurf sie versal setzt. Ein UI-Test darf **case-insensitiv** gegen "weiter demontieren" bzw. "montage starten" prüfen; eine spätere Änderung der Schreibweise darf ihn nicht brechen

---

### US-001.3: Neuen Vorgang starten

**Als** Mechaniker
**möchte ich** über einen gut sichtbaren Knopf einen neuen Reparaturvorgang anlegen können
**damit** ich schnell mit einem neuen Auftrag beginnen kann.

#### Akzeptanzkriterien

- **Given** der Mechaniker ist im Tab "OFFEN"
  **When** er den Knopf `uebersicht_fab_plus` = "+" / `uebersicht_fab` = "NEUER AUFTRAG" antippt
  **Then** wird der Anlage-Flow (F-002) gestartet

- **Given** der Mechaniker ist im Tab "ARCHIV"
  **When** der Screen gezeichnet wird
  **Then** ist der Knopf "NEUER AUFTRAG" **nicht** sichtbar — im Archiv wird nichts angelegt

#### UI-Verhalten

- Orange abgesetzter Knopf unten rechts (design-system.md, Rezept GO-F), 74dp hoch, über der Liste schwebend und beim Scrollen sichtbar bleibend
- Ein abdunkelnder Verlauf am unteren Listenrand hält die Karten optisch vom Knopf frei

---

### US-001.4: Vorgang löschen

**Als** Mechaniker
**möchte ich** einen Vorgang löschen können
**damit** ich fehlerhafte oder nicht mehr benötigte Vorgänge entfernen kann.

#### Akzeptanzkriterien

- **Given** ein Vorgang wird in der Liste angezeigt (offen oder archiviert)
  **When** der Mechaniker den Löschen-Knopf am rechten Rand seiner Karte antippt
  **Then** erscheint ein Bestätigungs-Sheet mit `uebersicht_loeschen_titel` = "WIRKLICH WEG?" und `uebersicht_loeschen_frage` = "Vorgang und alle Fotos unwiderruflich löschen?"

- **Given** das Bestätigungs-Sheet ist sichtbar
  **When** der Mechaniker `uebersicht_loeschen` = "LÖSCHEN" wählt
  **Then** verschwindet der Vorgang aus der Liste, und mit ihm werden alle zugehörigen Schritte und `schritt_foto`-Einträge kaskadierend gelöscht
  **And** die zugehörigen Fotodateien werden beim nächsten App-Start entfernt, weil auf sie keine Datenbankzeile mehr verweist ([../governance.md](../governance.md), Cleanup-Regel)

- **Given** das Bestätigungs-Sheet ist sichtbar
  **When** der Mechaniker `uebersicht_loeschen_abbrechen` = "ABBRECHEN" wählt oder neben das Sheet tippt
  **Then** bleibt der Vorgang unverändert erhalten

#### UI-Verhalten

- Bestätigungs-Sheet in derselben Optik wie das Auswahl-Sheet, die Lösch-Aktion in der Gefahr-Variante (rot)
- Der Löschen-Knopf ist ein eigenes Bedienelement von 56dp Kantenlänge auf der Karte; ein Tap darauf öffnet **nicht** den Vorgang
- **[OFFEN]** Der Knopf trägt derzeit die Glyphe "›", die im Entwurf "öffnen" bedeutet und auf der Karte für den Vorgangs-Tap steht. Zwei gegenläufige Bedeutungen für dasselbe Zeichen sind mit Handschuhen ein Fehlgriffrisiko. Zu entscheiden: eigenes Löschen-Zeichen, oder das Löschen zurück auf eine Wischgeste legen

---

### US-001.5: Archivierte Vorgänge einsehen

**Als** Mechaniker
**möchte ich** abgeschlossene Reparaturvorgänge im Archiv einsehen können
**damit** ich bei Reklamationen nachschauen und Arbeitsdauern für die Kalkulation analysieren kann.

#### Die Archivliste

- **Given** der Mechaniker ist im Tab "OFFEN"
  **When** er auf den Tab `uebersicht_tab_archiv` = "ARCHIV" wechselt
  **Then** werden alle archivierten Vorgänge angezeigt, sortiert nach letzter Bearbeitung (neueste oben) — dieselbe Sortierung wie im Tab "OFFEN"

- **Given** ein archivierter Vorgang wird in der Liste angezeigt
  **When** seine Karte gezeichnet wird
  **Then** sind dieselben Angaben sichtbar wie bei einem offenen Vorgang, nur zeigt das Datum das **Abschlussdatum** (Quelle `aktualisiertAm`, siehe Technische Hinweise)

- **Given** ein archivierter Vorgang hat gemessene Zeit
  **When** seine Karte gezeichnet wird
  **Then** hängt hinter dem Abschlussdatum, getrennt durch " · ", die gemessene Gesamtdauer des Vorgangs (Beispiel: "12.05.2026 · 1 h 26 min")

- **Given** die gemessene Gesamtdauer wird dargestellt
  **When** sie unter einer Stunde liegt
  **Then** erscheint sie auf volle Minuten gerundet als "43 min", mindestens aber als "1 min"
  **And** ab einer Stunde als "1 h 26 min" mit zweistelliger Minutenzahl

- **Given** zu einem archivierten Vorgang liegt keine Messung vor
  **When** seine Karte gezeichnet wird
  **Then** steht dort nur das Abschlussdatum — kein Trenner, kein "0 min"

- **Given** es existieren keine archivierten Vorgänge
  **When** der Tab "ARCHIV" aktiv ist
  **Then** erscheint der Leerzustand mit `uebersicht_leer_titel_archiv` = "ARCHIV IST LEER" und `uebersicht_leer_text_archiv` = "Fertige Vorgänge landen hier."

#### Die Archiv-Detailansicht

Die Detailansicht ist der Schritt-Browser (F-006) im Modus **ARCHIV**, der auf die F-006-Betriebsart **nur-lesen** abbildet (F-006 README, Abschnitt "Modi"). F-001 liefert Daten und Chrome, F-006 die Navigation und die Foto-Anzeige.

- **Given** ein archivierter Vorgang wird angezeigt
  **When** der Mechaniker den Vorgang antippt
  **Then** öffnet sich **ohne** Zwischen-Sheet die Archiv-Detailansicht, beginnend beim Schritt mit der niedrigsten `schrittNummer`

- **Given** die Archiv-Detailansicht wird geöffnet
  **When** F-001 die Schrittliste an F-006 übergibt
  **Then** sind die Schritte **aufsteigend nach `schrittNummer`** sortiert (Demontage-Reihenfolge)
  > F-006 sortiert nicht selbst, sondern übernimmt die Reihenfolge des Consumers unverändert (F-006 README, Abschnitt "Schnittstelle im Ueberblick"). F-001 wählt aufsteigend, weil im Archiv nachvollzogen werden soll, *wie zerlegt wurde* — die Demontage-Reihenfolge ist die Erzählreihenfolge der Dokumentation. F-003 übergibt aus demselben Grund aufsteigend; nur F-004 dreht die Reihenfolge um, weil rückwärts montiert wird.

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** sie gezeichnet wird
  **Then** zeigt die Kopfzeile die **Schrittnummer** des betrachteten Schritts groß, darunter `browser_modus_archiv` = "ARCHIV", die Auftragsnummer mit `#` und eine Zeitzeile aus der Dauer dieses Schritts und der Gesamtdauer des Vorgangs, verbunden mit " · Σ " (Beispiel: "04:12 · Σ 1 h 26 min")

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** sie gezeichnet wird
  **Then** ist die Plakette `archiv_nur_lesen` = "ARCHIV · NUR LESEN" sichtbar
  **And** es gibt **keine** Timer-Kapsel — im Archiv wird nichts mehr gemessen

- **Given** die Archiv-Detailansicht zeigt einen Schritt
  **When** der Mechaniker die Vor-/Zurück-Bedienung des Schritt-Browsers nutzt
  **Then** wird der vorherige bzw. nächste Schritt mit Schrittnummer und seinen Fotos angezeigt
  **And** am ersten Schritt ist "Zurück", am letzten "Weiter" **sichtbar deaktiviert** statt still wirkungslos (design-system.md, K-05)
  > Die Schritt-Navigation gehört vollständig F-006 (F-006 browser.md, US-006.10). F-001 stellt dafür keine eigene Bedienung bereit.

- **Given** die Archiv-Detailansicht zeigt einen Schritt
  **When** der Mechaniker in der Thumbnail-Leiste das Thumbnail eines anderen Schritts antippt
  **Then** springt die Ansicht direkt zu diesem Schritt

- **Given** ein Schritt mit mehreren Fotos wird angezeigt
  **When** der Mechaniker im Bildkarussell horizontal wischt
  **Then** werden die Fotos **dieses** Schritts der Reihe nach angezeigt — der Schritt wechselt dabei nie

- **Given** ein Foto wird im Bildkarussell angezeigt
  **When** der Mechaniker das Foto antippt
  **Then** wird es im Vollbild angezeigt, und die Android-Zurück-Geste schließt das Vollbild, ohne die Detailansicht zu verlassen

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** ein Schritt mit seinen Fotos angezeigt wird
  **Then** wird keine bearbeitende Aktion angeboten (kein Foto aufnehmen, kein Wiederholen, keine Label-Änderung, kein Anlegen oder Abschließen von Schritten) und auch keine Flow-Aktion (kein "Eingebaut", kein "Feierabend") — die gesetzten Foto-Label sind sichtbar, aber nicht änderbar

- **Given** die Archiv-Detailansicht ist geöffnet
  **When** der Mechaniker das Schließen-Kreuz "✕" oben rechts antippt oder die Android-Zurück-Geste ausführt
  **Then** wird zur Übersicht in den Tab "ARCHIV" zurückgekehrt, ohne dass Daten verändert wurden

#### UI-Verhalten

- **Namensabgrenzung:** Die Beschriftungen "Zurück" und "Weiter" gehören ausschließlich der Schritt-Navigation aus F-006 im Inhaltsbereich. Der Ausstieg aus der Detailansicht läuft über das Schließen-Kreuz und die Android-Zurück-Geste; einen Knopf mit der Beschriftung "Zurück", der die Ansicht verlässt, gibt es nicht
- Das Schließen-Kreuz ist der **einzige** sichtbare Ausstieg — anders als in der Montage gibt es daneben keinen "RAUS"-Knopf (design-system.md, K-06)

---

## Nicht-funktionale Anforderungen

- **Bedienbarkeit** (Quality Goal #1): Große Karten und Knöpfe; die verbindlichen Mindestmaße für Touch-Targets und Abstände stehen in [../governance.md](../governance.md), Abschnitt "Touch-Targets". Tabs, Löschen-Knopf und alle Sheet-Aktionen halten mindestens 56dp
- **Performance** (Quality Goal #3): Liste lädt sofort beim Öffnen, kein Spinner. Schrittzahl und Gesamtdauer kommen in **einer** Abfrage mit der Vorgangsliste, nicht in einer Abfrage je Karte
- **Zuverlässigkeit** (Quality Goal #2): Löschung ist kaskadierend. Die Dateien räumt die Cleanup-Regel beim App-Start ab; sie löscht ausschließlich Dateien ohne Datenbankverweis und **gar nichts**, wenn der Datenbestand nicht lesbar ist — ein einzelner Lesefehler darf nie den Fotobestand abräumen

## Technische Hinweise

- Room: `Reparaturvorgang` mit Status-Feld (`OFFEN`, `ARCHIVIERT`); Liste als `LazyColumn`
- Navigation: zu F-002 sowie zum Schritt-Browser in den Modi `DEMONTAGE`, `MONTAGE` und `ARCHIV`. Die Archiv-Detailansicht ist kein eigener Screen, sondern derselbe Browser-Screen im Modus `ARCHIV`
- Thumbnail-Loading: Foto aus dem Filesystem laden, skaliert auf Kartengröße (kein Full-Size)
- **Sortierung:** `ORDER BY aktualisiertAm DESC` für offene **und** archivierte Vorgänge. Dass `aktualisiertAm` tatsächlich "zuletzt angefasst" abbildet, garantiert die projektweite Invariante in [../governance.md](../governance.md), Abschnitt "Sofort-Save Strategie / Invariante `aktualisiertAm`": jede erzeugende, ändernde oder löschende Aktion an einem Vorgang oder seinen Schritten und Fotos schreibt das Feld in derselben Operation mit — einschließlich des Archivierens (F-004)
- **Datumsformat**, ein einziger Formatierer für beide Listen, vier Stufen:

  | Bedingung | Ausgabe |
  |---|---|
  | jünger als 60 Sekunden | "Gerade eben" |
  | heutiger Kalendertag | "Heute, HH:mm" |
  | Vortag | "Gestern, HH:mm" |
  | älter | "dd.MM.yyyy" |

  Die Frische-Stufe gewinnt gegen die Tagesgrenze: 40 Sekunden vor Mitternacht angelegt heißt kurz nach Mitternacht "Gerade eben", nicht "Gestern, 23:59". Ein Zeitpunkt in der Zukunft (zurückgestellte Uhr) fällt ebenfalls auf "Gerade eben" — auf keiner Karte steht ein Datum aus der Zukunft
- **Wortlaute des Datums:** Alle vier Formen sind verbindlich und gehören nach `strings.xml`, damit UI-Tests dagegen prüfen können.
  **[OFFEN]** Der gebaute Formatierer hält sie stattdessen als Konstanten in `feature/uebersicht/DatumFormat.kt`, damit er ohne Android auf der JVM testbar bleibt. Zu entscheiden: Ressourcen ins ViewModel injizieren, oder die Konstanten als Prüfquelle festschreiben und den UI-Test gegen `DatumFormat` statt gegen `R.string` binden
- **Bewusste Abweichung (Anzeige vs. Sortierung):** Die Karte im Tab "OFFEN" zeigt `erstelltAm`, sortiert wird nach `aktualisiertAm`. Nach dem sichtbaren Datum kann die Liste dadurch unsortiert wirken. Das ist gewollt: Das Anlagedatum identifiziert den Auftrag wiedererkennbar ("der Wagen von gestern"), während die Reihenfolge abbilden soll, woran zuletzt gearbeitet wurde. Bei den typischen 1-3 offenen Vorgängen ist der Effekt vernachlässigbar. Im Tab "ARCHIV" tritt er nicht auf, weil Anzeige und Sortierung dort dieselbe Quelle nutzen
- **Einschränkung (Abschlussdatum):** Ein eigener Archivierungs-Zeitstempel (`archiviertAm` o.ä.) existiert **nicht**. Das im Archiv angezeigte Abschlussdatum ist `aktualisiertAm` **zum Zeitpunkt des Archivierens** — F-004 schreibt das Feld beim Archivieren mit. Danach ändert sich der Wert nicht mehr, weil archivierte Vorgänge ausschließlich lesend geöffnet werden: es existiert keine Aktion, die `aktualisiertAm` erneut setzen könnte
- **Gesamtdauer:** Summe über die `zeit_messung`-Einträge aller Schritte des Vorgangs, beide Referenztypen (`DEMONTAGE_SCHRITT` und `MONTAGE_SCHRITT`, F-005 service.md). **Nicht** aus `Schritt.gestartetAm`/`abgeschlossenAm` — das sind Workflow-Timestamps und ausdrücklich keine Zeitmessung (Governance: keine Dual-Purpose-Felder). Die Richtung ist erlaubt: der Consumer F-001 kennt die Service-Tabelle, nicht umgekehrt
- Die Dauer eines archivierten Vorgangs ist stabil: beim Verlassen des Montage-Flows und beim Archivieren werden alle offenen Messungen gestoppt (F-005). Eine noch laufende Messung würde bis "jetzt" gerechnet — das kann im Archiv nicht mehr auftreten
- **Duplikat beachten:** Die Kurzform der Dauer ("1 h 26 min" / "43 min") wird an zwei Stellen gebraucht — auf der Archivkarte und in der Zeitzeile der Detailansicht. Beide Implementierungen müssen für dieselbe Eingabe dasselbe liefern; ein Test hält das fest
- **[OFFEN]** Ein archivierter Vorgang **ohne** Schritte ist derzeit nicht darstellbar: es gibt keinen Hinweistext für diesen Fall (F-006 überlässt ihn ausdrücklich dem Consumer, F-006 README, Abschnitt "Abgrenzung"). Der Fall ist momentan unerreichbar, weil F-002 immer Schritt 1 anlegt und Schritte nicht löschbar sind. Zu entscheiden: Hinweistext nachziehen, oder die Unerreichbarkeit als Invariante festschreiben und den Fall streichen

### Wörtliche UI-Texte

Alle Texte liegen in `res/values/strings_uebersicht.xml`, soweit nicht anders vermerkt. Die versale Schreibweise ist Darstellung; UI-Tests dürfen case-insensitiv prüfen.

| Key | Text |
|---|---|
| `uebersicht_logo` / `uebersicht_wortmarke` | "B" / "BOLTMIND" |
| `uebersicht_tab_offen` / `uebersicht_tab_archiv` | "OFFEN" / "ARCHIV" |
| `uebersicht_teile` | "%1$d TEILE" |
| `uebersicht_ohne_beschreibung` | "Ohne Beschreibung" |
| `uebersicht_leer_titel_offen` | "NICHTS OFFEN" |
| `uebersicht_leer_text_offen` | "Tipp auf NEUER AUFTRAG und leg los." |
| `uebersicht_leer_titel_archiv` | "ARCHIV IST LEER" |
| `uebersicht_leer_text_archiv` | "Fertige Vorgänge landen hier." |
| `uebersicht_fab_plus` / `uebersicht_fab` | "+" / "NEUER AUFTRAG" |
| `uebersicht_weiter_demontieren` | "WEITER DEMONTIEREN" |
| `uebersicht_montage_starten` | "MONTAGE STARTEN" |
| `uebersicht_loeschen_titel` | "WIRKLICH WEG?" |
| `uebersicht_loeschen_frage` | "Vorgang und alle Fotos unwiderruflich löschen?" |
| `uebersicht_loeschen_abbrechen` / `uebersicht_loeschen` | "ABBRECHEN" / "LÖSCHEN" |
| `uebersicht_motivation` | "You\ncan\ndo it." (Dekoration, nicht bedienbar) |
| `browser_modus_archiv` | "ARCHIV" (Detailansicht, `strings_browser_aktionen.xml`) |
| `archiv_nur_lesen` | "ARCHIV · NUR LESEN" (Detailansicht, `strings_browser_aktionen.xml`) |

## UI-Skizze

### Übersicht (Tab OFFEN)
```
┌─────────────────────────────┐
│  [B] BOLTMIND               │
├──────────────┬──────────────┤
│  OFFEN    2  │  ARCHIV   1  │   ← aktiver Tab orange
├──────────────┴──────────────┤
│ ┌─────────────────────────┐ │
│ │[Foto] #2026-0815        │ │
│ │       Bremsen vorne …   │ │
│ │       [12 TEILE] Heute, │ │
│ │                  08:12  │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │[Foto] #2026-0793        │ │
│ │       Kupplung raus     │ │
│ │       [8 TEILE] Gestern,│ │
│ │                  15:40  │ │
│ └─────────────────────────┘ │
│ You                         │
│ can        [ + NEUER      ] │
│ do it.     [   AUFTRAG    ] │
└─────────────────────────────┘
```

### Archivkarte
```
┌─────────────────────────────┐
│[Foto] #2026-0651            │
│       Zahnriemen erneuert   │
│       [9 TEILE]             │
│       12.05.2026 · 1 h 26 min│
└─────────────────────────────┘
```

### Auswahl-Sheet
```
┌─────────────────────────────┐
│            ▬▬▬              │
│  [Foto]  #2026-0815         │
│          Bremsen vorne …    │
│  ┌───────────────────────┐  │
│  │  WEITER DEMONTIEREN   │  │  ← primär
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │  MONTAGE STARTEN      │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

### Leerzustand
```
┌─────────────────────────────┐
│  [B] BOLTMIND               │
├──────────────┬──────────────┤
│  OFFEN    0  │  ARCHIV   0  │
├──────────────┴──────────────┤
│                             │
│           ┌───┐             │
│           │ 0 │             │
│           └───┘             │
│        NICHTS OFFEN         │
│  Tipp auf NEUER AUFTRAG     │
│       und leg los.          │
└─────────────────────────────┘
```

## Änderungshistorie

| Datum | Änderung |
|---|---|
| 2026-07-27 | Datumsformat auf vier Stufen erweitert ("Gerade eben", Uhrzeit auf den Wortstufen, Monatsname gestrichen) — design-system.md K-09 |
| 2026-07-27 | Gemessene Gesamtdauer auf der Archivkarte, angehängt mit " · "; Quelle `zeit_messung` (F-005) |
| 2026-07-27 | Offener Vorgang ohne Schritte öffnet ohne Auswahl-Sheet direkt die Demontage — design-system.md K-10 |
| 2026-07-27 | Archiv-Detailansicht als Schritt-Browser im Modus ARCHIV beschrieben: Kopfzeile, Plakette, deaktivierte Ränder, Ausstieg über "✕" statt TopBar-Pfeil |
| 2026-07-27 | Sheet-Wortlaute als case-insensitiv prüfbar festgehalten; wörtliche UI-Texte in einer Tabelle gebündelt |
| 2026-07-27 | Löschen über einen Knopf auf der Karte statt über eine Wischgeste; Bestätigung als Bottom-Sheet |
| 2026-07-27 | Tab-Zähler, Beschreibungszeile auf der Karte, neue Leerzustands-Texte und "NEUER AUFTRAG" nur im Tab OFFEN aufgenommen |
</content>
</invoke>
