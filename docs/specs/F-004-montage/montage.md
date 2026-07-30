# F-004: Montage-Flow

## Kontext

Der Montage-Flow ist das Gegenstück zum Demontage-Flow (F-003). Der Mechaniker hat ein Fahrzeug demontiert und möchte es nun wieder zusammenbauen. Die App zeigt die Demontage-Schritte in umgekehrter Reihenfolge an, sodass der Mechaniker Schritt für Schritt das Fahrzeug wieder aufbaut.

**Problem:** Nach Tagen oder Wochen erinnert sich der Mechaniker nicht mehr an die Reihenfolge und die Ablageorte der ausgebauten Teile. Er braucht eine visuelle Anleitung für den Zusammenbau.

**Lösung:** Die Demontage-Dokumentation wird rückwärts abgespielt. Pro Schritt sieht der Mechaniker alle Fotos dieses Schritts im Foto-Karussell (F-006): das Bauteil-Foto (wie sieht das Teil montiert aus?) und — falls vorhanden — ein Foto mit dem Label "Ablageort" (wo liegt es?). Erledigte Schritte werden abgehakt; danach rückt die Ansicht von selbst zum nächsten noch nicht eingebauten Teil. Ist keines mehr offen, erscheint der Abschluss-Screen. Den Flow verlässt der Mechaniker über den Rundbutton „RAUS" mit Rückfrage — das ist der einzige Ausstieg (US-004.6).

**Primärer Nutzer:** Mechaniker in der Werkstatt, der ein zuvor demontiertes Fahrzeug wieder zusammenbaut.

**Situation:** Der Mechaniker steht am Fahrzeug, hat die ausgebauten Teile auf nummerierten Ablageplätzen liegen und möchte wissen: Welches Teil kommt als nächstes, und wo liegt es?

### Zusammenspiel mit Schrittnummer (F-003)

Die Schrittnummer aus der Demontage dient im Montage-Flow als Orientierung. **Schrittnummer und Fortschritt sind zwei verschiedene Dinge und werden nie vermischt:**

- **Schrittnummer:** Immer die Demontage-Schrittnummer (z.B. "Schritt 12"). Sie ist die Identität des Schritts und wird in der Montage **nicht umnummeriert**, obwohl die Schritte hier rückwärts durchlaufen werden. Sie steht zweistellig und groß in der Kopfzeile und auf jedem Thumbnail (F-006).
- **Fortschritt:** Getrennt davon als "**n VON m DRIN**" plus grünem Balken (US-004.3). Der Fortschritt zählt abgehakte Schritte, er benennt keinen Schritt. Formulierungen wie "Schritt 3 von 15" sind nicht zulässig, weil sie beides vermischen.
- **Ablageort-Korrelation:** Hat der Mechaniker seine physischen Ablageorte mit Schrittnummern beschriftet, findet er über die Nummer das passende Teil. Genau deshalb bleibt die Nummer über Demontage und Montage hinweg stabil.
- **Schritte ohne Ablageort:** Manche Schritte haben kein Foto mit dem Label "Ablageort" (Teil blieb am Fahrzeug). Diese tragen den Hinweis "AM FAHRZEUG GEBLIEBEN".

### Wörtliche UI-Texte

Diese Texte sind verbindlich und werden von UI-Tests wörtlich geprüft. Sie liegen in `res/values/` und stammen aus dem Design-Prototyp (siehe [../design-system.md](../design-system.md), Abschnitt "Bewusste Abweichungen vom Prototyp", K-08: Werkstatt-Wortlaute gehen den Glossar-Begriffen vor).

| Ort | Wörtlicher Text | String-Ressource |
| --- | --- | --- |
| Kopfzeile, Modus-Wort | `EINBAU` | `browser_modus_montage` |
| Fortschritt | `%1$d VON %2$d DRIN` — z.B. `3 VON 15 DRIN` | `montage_fortschritt` |
| Rundbutton, Schritt noch nicht eingebaut | `SITZT!` | `montage_sitzt` |
| Rundbutton, Schritt bereits eingebaut | `DRIN` | `montage_drin` |
| Rundbutton, Ausstieg | `RAUS` | `montage_raus` |
| Rundbutton, Schritt-Navigation | `ZURÜCK` (mit Glyphe `↑`) | `browser_zurueck` |
| Rundbutton, Weg zum Abschluss (nur wenn alles drin ist) | `ABSCHLUSS` | `montage_zum_abschluss` |
| Hinweis ohne Ablageort-Foto | `AM FAHRZEUG GEBLIEBEN` | `montage_am_fahrzeug` |
| Rückfrage Ausstieg, Titel | `FEIERABEND?` | `sheet_feierabend_titel` |
| Rückfrage Ausstieg, Aktionen | `WEITER ARBEITEN` / `JA, FEIERABEND` | `sheet_weiter_arbeiten`, `sheet_ja_feierabend` |
| Rückfrage Häkchen, Titel | `DOCH NICHT DRIN?` | `sheet_haekchen_titel` |
| Rückfrage Häkchen, Text | `Häkchen für diesen Schritt zurücknehmen?` | `sheet_haekchen_text` |
| Rückfrage Häkchen, Aktionen | `ABBRECHEN` / `HÄKCHEN WEG` | `sheet_abbrechen`, `sheet_haekchen_weg` |
| Abschluss-Screen, Titel | `ALLES WIEDER DRAN` | `abschluss_titel` |
| Abschluss-Screen, Kennzahlen | `TEILE` / `GEMESSEN` | `abschluss_teile`, `abschluss_gemessen` |
| Abschluss-Screen, Aktion | `AB INS ARCHIV` | `abschluss_archivieren` |

**[OFFEN]** Der Fließtext der Ausstiegs-Rückfrage. Der Prototyp führt für die Montage eine eigene Zeile („Deine Häkchen bleiben gesetzt. Beim nächsten Mal geht es beim ersten offenen Teil weiter."), gebaut ist ein für Demontage und Montage gemeinsamer Text (`sheet_feierabend_text`). Titel und Aktionen sind davon nicht betroffen.

### Ankerpunkt F-005

Die Montage misst ihre Zeit selbst, mit `referenzTyp = "MONTAGE_SCHRITT"` (US-004.7). Start- und Stopp-Trigger stehen in [../F-005-zeiterfassung/service.md](../F-005-zeiterfassung/service.md), Abschnitt "Consumer-Trigger"; die Anzeige gehört dieser Spec.

---

## User Stories

### US-004.1: Schritte in Montage-Reihenfolge anzeigen

**Als** Mechaniker
**möchte ich** die Demontage-Schritte in umgekehrter Reihenfolge sehen
**damit** ich die Teile in der richtigen Reihenfolge wieder einbauen kann (letztes ausgebautes Teil = erstes einzubauendes Teil).

#### Akzeptanzkriterien

- **Given** ein Reparaturvorgang mit 15 dokumentierten Demontage-Schritten existiert und **kein** Schritt ist abgehakt
  **When** der Mechaniker "MONTAGE STARTEN" wählt (F-001)
  **Then** wird der Montage-Flow geöffnet und zeigt den Schritt mit der höchsten Demontage-Schrittnummer zuerst (Schritt 15)
  **And** der Fortschritt zeigt "0 VON 15 DRIN"

- **Given** ein Reparaturvorgang mit 15 Demontage-Schritten, dessen Zusammenbau bereits begonnen wurde (die Schritte 15, 14 und 13 sind abgehakt)
  **When** der Mechaniker "MONTAGE STARTEN" wählt (F-001)
  **Then** wird der Montage-Flow geöffnet und zeigt den **ersten noch nicht abgehakten Schritt in Montage-Reihenfolge** (Schritt 12) — nicht den Schritt mit der höchsten Demontage-Schrittnummer
  **And** der Fortschritt zeigt "3 VON 15 DRIN"
  **And** es wird beim Öffnen nichts in die DB geschrieben (der Wiedereinstieg ist ein reiner Lesevorgang)

- **Given** ein Reparaturvorgang, bei dem **alle** Schritte abgehakt sind, der aber noch nicht archiviert wurde (der Mechaniker hat den Abschluss-Screen zuvor ohne "AB INS ARCHIV" verlassen)
  **When** der Mechaniker "MONTAGE STARTEN" wählt (F-001)
  **Then** wird direkt der Abschluss-Screen angezeigt (US-004.5)
  **And** der Vorgang bleibt bis zum Tap auf "AB INS ARCHIV" im Status `OFFEN`

- **Given** der Montage-Flow ist geöffnet
  **When** der erste Schritt angezeigt wird
  **Then** sind folgende Informationen sichtbar:
  - das Foto-Karussell dieses Schritts mit allen seinen Fotos (F-006)
  - in der Kopfzeile die zweistellige Demontage-Schrittnummer, das Modus-Wort "EINBAU", die Auftragsnummer mit vorangestelltem `#` und die Beschreibung des Vorgangs
  - der Fortschritt "n VON m DRIN" mit Balken (US-004.3)
  - die Label des sichtbaren Fotos (Bauteil / Übersicht / Ablageort) — sichtbar, aber **nicht änderbar** (F-006, Modus "lesend-mit-Aktionen")
  - die Thumbnail-Leiste über alle Schritte (F-006)
  - die Timer-Kapsel des Schritts (US-004.7)
  - die Rundbuttons "RAUS", "ZURÜCK" und "SITZT!" bzw. "DRIN"

- **Given** ein Schritt hat mindestens ein Foto mit dem Label "Ablageort"
  **When** dieses Foto im Karussell sichtbar ist
  **Then** ist am Foto erkennbar, dass es das Label "Ablageort" trägt (Label-Anzeige am sichtbaren Foto, F-006)
  **And** das Thumbnail dieses Schritts ist in der Leiste in der Ablageort-Markierung eingefärbt (F-006 US-006.3)
  **And** die Label-Anzeige ist nicht bedienbar (Label werden ausschließlich in der Demontage gesetzt)

- **Given** ein Schritt hat kein Foto mit dem Label "Ablageort" (Teil blieb am Fahrzeug)
  **When** dieser Schritt angezeigt wird
  **Then** wird unter den Label-Pillen der Hinweis "AM FAHRZEUG GEBLIEBEN" angezeigt
  **And** der Hinweis ist reine Anzeige und nicht bedienbar

#### UI-Verhalten

- Foto-Karussell, Vergrößern eines Fotos per Tap, die Label-Anzeige am sichtbaren Foto und die Thumbnail-Leiste sind in F-006 spezifiziert und werden hier nicht wiederholt
- Die farbliche Markierung eines Thumbnails danach, ob der Schritt ein Ablageort-Foto hat, ist Teil von F-006
- Ein bereits abgehakter Schritt trägt in der Thumbnail-Leiste ein grünes Erledigt-Häkchen (F-006). Nur die Montage zeigt es
- F-004 nutzt F-006 im Modus **"lesend-mit-Aktionen"**: Label sind sichtbar, aber nicht änderbar, und es gibt im Browser keine Foto-Aufnahme und keine Aktion am sichtbaren Foto. Die Schritt-Aktionen gehören dem Consumer F-004

---

### US-004.2: Schritt als eingebaut markieren

**Als** Mechaniker
**möchte ich** erledigte Schritte als "eingebaut" abhaken
**damit** ich den Fortschritt verfolgen und weiß, welche Teile noch fehlen.

Der große Rundbutton unten rechts trägt zwei Zustände und ist derselbe Platz:

| Zustand des Schritts | Beschriftung | Fläche | Wirkung |
| --- | --- | --- | --- |
| noch nicht eingebaut | `SITZT!` | grün gefüllt (Aktionsfläche) | hakt ab (diese Story) |
| bereits eingebaut | `DRIN` | grün, flach und ohne Verlauf (Zustandsfläche) | öffnet die Rückfrage zum Zurücknehmen |

Die flache Fläche ist die verbindliche Unterscheidung: "DRIN" meldet einen Zustand und lädt nicht zum Tippen ein ([../design-system.md](../design-system.md), Abschnitt "Glas", Rezepte `GG-V` und `GG-Z`).

#### Regel: nächster offener Schritt

Überall dort, wo diese Spec vom "nächsten offenen Schritt" spricht, gilt dieselbe Regel. Sie wird in dieser Reihenfolge geprüft:

1. Der nächste noch **nicht abgehakte** Schritt in Montage-Reihenfolge **nach** dem gerade abgehakten (nächst-niedrigere Demontage-Schrittnummer; bereits abgehakte Schritte werden dabei übersprungen).
2. Existiert dahinter keiner mehr, der **erste** noch nicht abgehakte Schritt in Montage-Reihenfolge — derselbe Schritt, den auch der Wiedereinstieg liefert (US-004.1). So landet der Mechaniker, der per Thumbnail nach vorn gesprungen ist, anschließend wieder bei seiner ersten Lücke statt in einer Sackgasse.
3. Ist kein Schritt mehr offen, erscheint der Abschluss-Screen (US-004.5).

Beispiel zu Punkt 2: Der Mechaniker ist per Thumbnail auf Schritt 4 gesprungen, während 15 bis 5 noch offen sind, und hakt Schritt 4 ab. Angezeigt wird danach Schritt 3 (Punkt 1). Hakt er weiter bis Schritt 1 ab, greift Punkt 2 und es erscheint Schritt 15.

Die Regel gilt **nur für das automatische Weiterrücken nach dem Abhaken**. "ZURÜCK" und der Thumbnail-Sprung (US-004.4) überspringen nie einen Schritt, auch keinen abgehakten.

#### Akzeptanzkriterien

- **Given** ein Montage-Schritt wird angezeigt und ist noch nicht abgehakt
  **When** der Mechaniker "SITZT!" antippt
  **Then** wird der Schritt als eingebaut markiert (`eingebautBeiMontage = true`)
  **And** die Markierung wird sofort in der DB persistiert
  **And** in derselben Operation wird `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion gesetzt ([../governance.md](../governance.md), Abschnitt "Sofort-Save")
  **And** eine laufende Zeitmessung dieses Schritts wird gestoppt (US-004.7)
  **And** die Ansicht springt danach von selbst auf den nächsten offenen Schritt nach der Regel oben

- **Given** der Montage-Flow zeigt Schritt 4, die Schritte 15 bis 5 sind noch offen und Schritt 3 ist noch nicht abgehakt
  **When** der Mechaniker Schritt 4 abhakt
  **Then** wird Schritt 3 angezeigt (nächster nicht abgehakter Schritt in Montage-Reihenfolge, Regel Punkt 1)

- **Given** der Montage-Flow zeigt Schritt 4, die Schritte 3, 2 und 1 sind bereits abgehakt und die Schritte 15 bis 5 sind noch offen
  **When** der Mechaniker Schritt 4 abhakt
  **Then** wird Schritt 15 angezeigt (erster nicht abgehakter Schritt in Montage-Reihenfolge, Regel Punkt 2)

- **Given** ein Schritt wurde als eingebaut markiert
  **When** der Mechaniker zu diesem Schritt zurücknavigiert
  **Then** trägt der große Rundbutton die Beschriftung "DRIN" auf der flachen Zustandsfläche
  **And** das Thumbnail dieses Schritts trägt in der Leiste das Erledigt-Häkchen

- **Given** ein bereits abgehakter Schritt wird angezeigt und der Fortschritt steht auf "8 VON 15 DRIN"
  **When** der Mechaniker "DRIN" antippt
  **Then** erscheint die Rückfrage mit dem Titel "DOCH NICHT DRIN?", dem Text "Häkchen für diesen Schritt zurücknehmen?" und den Aktionen "ABBRECHEN" und "HÄKCHEN WEG"
  **And** solange die Rückfrage offen ist, wurde nichts geschrieben

- **Given** die Rückfrage "DOCH NICHT DRIN?" ist offen
  **When** der Mechaniker "HÄKCHEN WEG" antippt
  **Then** wird `eingebautBeiMontage = false` gesetzt und sofort in der DB persistiert
  **And** in derselben Operation wird `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion gesetzt ([../governance.md](../governance.md), Abschnitt "Sofort-Save")
  **And** der Fortschritt sinkt um eins ("7 VON 15 DRIN") und der Balken schrumpft entsprechend
  **And** derselbe Schritt bleibt angezeigt (Zurücknehmen springt nie weiter — die Regel "nächster offener Schritt" gilt nur für das Abhaken)

- **Given** die Rückfrage "DOCH NICHT DRIN?" ist offen
  **When** der Mechaniker "ABBRECHEN" antippt oder neben die Rückfrage tippt
  **Then** bleibt die Markierung gesetzt, der Fortschritt unverändert und es wird nichts geschrieben

---

### US-004.3: Fortschritt anzeigen

**Als** Mechaniker
**möchte ich** sehen, wie weit der Zusammenbau fortgeschritten ist
**damit** ich den Überblick behalte und abschätzen kann, wie viel noch zu tun ist.

Der Fortschritt besteht aus zwei Teilen, die immer gemeinsam auftreten: der Zeile "n VON m DRIN" und darunter einem waagerechten Balken, dessen grüner Anteil `n / m` beträgt. Er steht links unten neben den Bedienkreisen und ist reine Anzeige.

#### Akzeptanzkriterien

- **Given** der Montage-Flow ist geöffnet und 3 von 15 Schritten sind abgehakt
  **When** der aktuelle Schritt angezeigt wird
  **Then** ist er in der Thumbnail-Leiste (F-006) hervorgehoben
  **And** wird seine Demontage-Schrittnummer zweistellig in der Kopfzeile angezeigt (z.B. "12")
  **And** wird der Fortschritt "3 VON 15 DRIN" angezeigt
  **And** ist der Balken zu einem Fünftel grün gefüllt

- **Given** der Montage-Flow ist geöffnet und mindestens ein weiterer Schritt ist noch offen
  **When** ein Schritt abgehakt wird
  **Then** rückt die Hervorhebung in der Thumbnail-Leiste auf den nächsten offenen Schritt (Regel in US-004.2)
  **And** der Fortschritt erhöht sich um eins ("4 VON 15 DRIN")
  **And** der Balken wächst animiert auf den neuen Anteil

- **Given** ein Vorgang hat keine Schritte
  **When** der Montage-Flow geöffnet wird
  **Then** bleibt der Balken leer und es wird nicht durch null geteilt

Zwei Fälle gehören bewusst nicht hierher: "der letzte offene Schritt wird abgehakt" besitzt US-004.5 (Abschluss-Screen), "bloßes Blättern ändert den Fortschritt nicht" besitzt US-004.4.

---

### US-004.4: Zwischen Schritten navigieren

**Als** Mechaniker
**möchte ich** frei zwischen Schritten navigieren können
**damit** ich nachschauen kann, was ich bereits eingebaut habe oder was als nächstes kommt.

Die Schritt-Navigation gehört **F-006** und wird hier nicht selbst spezifiziert. F-004 legt nur fest, welchen Satz an Bedienelementen es andockt und welcher Schritt in Montage-Reihenfolge folgt.

**Die Montage dockt nur "ZURÜCK" an.** Einen Vorwärts-Rundbutton gibt es hier nicht: vorwärts kommt der Mechaniker über "SITZT!" (mit Abhaken) oder über die Thumbnail-Leiste (ohne Abhaken). Dass der Button-Satz je Modus verschieden ist, ist entschieden ([../design-system.md](../design-system.md), Abschnitt "Bewusste Abweichungen vom Prototyp", K-05); die Rand-Regel aus F-006 gilt trotzdem — an den Enden ist ein Element sichtbar deaktiviert, nie still klemmend.

**"ZURÜCK" wechselt den Schritt, es verlässt nie den Flow.** Der Ausstieg ist "RAUS" und in US-004.6 geregelt.

#### Akzeptanzkriterien

- **Given** der Montage-Flow zeigt Schritt 11
  **When** der Mechaniker "ZURÜCK" antippt
  **Then** wird Schritt 12 angezeigt (nächst-höhere Demontage-Schrittnummer, also der vorherige Schritt in Montage-Reihenfolge)
  **And** der Fortschritt "n VON m DRIN" bleibt unverändert (bloßes Blättern hakt nichts ab)
  **And** das Karussell zeigt das erste Foto des neuen Schritts (F-006 US-006.10)

- **Given** der Montage-Flow zeigt den ersten Schritt in Montage-Reihenfolge (Schritt 15 bei 15 Schritten)
  **When** die Ansicht dargestellt wird
  **Then** ist "ZURÜCK" sichtbar, aber deaktiviert
  **And** ein Tap darauf bewirkt nichts und verlässt den Flow **nicht**

- **Given** der Montage-Flow zeigt Schritt 11
  **When** der Mechaniker in der Thumbnail-Leiste (F-006) das Thumbnail von Schritt 4 antippt
  **Then** wird Schritt 4 angezeigt
  **And** der Fortschritt bleibt unverändert

- **Given** der Montage-Flow zeigt den letzten Schritt in Montage-Reihenfolge (Schritt 1) und dieser ist noch offen
  **When** die Ansicht dargestellt wird
  **Then** gibt es kein Bedienelement, das weiterblättert — der Weg nach vorn ist "SITZT!", und das führt bei keinem offenen Schritt mehr zum Abschluss-Screen (US-004.5)

#### UI-Verhalten

- Schritt-Wechsel ausschließlich über "ZURÜCK", die Thumbnail-Leiste und das automatische Weiterrücken nach "SITZT!"
- Die horizontale Wischgeste im Bildbereich gehört dem Foto-Karussell und wechselt **nur das Foto innerhalb des Schritts** (F-006 US-006.4), niemals den Schritt
- Es gibt keine Eingabe einer Schrittnummer und keinen Sprung-Dialog
- "ZURÜCK" schließt nichts ab, hakt nichts ab und verlässt den Flow nicht
- Touch-Targets und Abstände nach [../governance.md](../governance.md), Abschnitt "Touch-Targets"

---

### US-004.5: Montage abschließen und archivieren

**Als** Mechaniker
**möchte ich** nach Abschluss aller Schritte den Vorgang archivieren
**damit** der Vorgang aus der aktiven Liste verschwindet und im Archiv für spätere Referenz verfügbar ist.

Der Abschluss-Screen ist der **einzige** Weg zur Archivierung. Kein Bedienelement der Schritt-Ansicht verwandelt sich am Ende in einen Abschluss-Button, und es gibt keine Archivierung ohne den Abschluss-Screen. Grund: Archivieren nimmt den Vorgang aus der aktiven Liste — mit Handschuhen braucht das eine ausdrückliche Bestätigung. Erreicht wird der Abschluss-Screen auf drei Wegen: durch das Abhaken des letzten offenen Schritts, beim Wiedereinstieg in einen vollständig abgehakten Vorgang (US-004.1) und über den Button "Zum Abschluss" nach einer Rückkehr in die Schritt-Ansicht.

#### Aufbau des Abschluss-Screens

Von oben nach unten: ein grün gestempeltes Häkchen, der Titel "ALLES WIEDER DRAN", darunter Auftragsnummer und Beschreibung als eine Zeile im Format `#{Auftragsnummer} · {Beschreibung}` (fehlt die Beschreibung, bleibt `#{Auftragsnummer}` allein stehen), darunter zwei Kennzahlen nebeneinander mit senkrechtem Trennstrich — links die Zahl der Schritte unter der Beschriftung "TEILE", rechts die gemessene Gesamtzeit unter der Beschriftung "GEMESSEN" — und am unteren Rand der Primärbutton "AB INS ARCHIV".

Die Kennzahl "GEMESSEN" ist die Summe aller Zeitmessungen des Vorgangs über beide `referenzTyp`-Werte, ausgeschrieben (z.B. `49 min 55 s`). Existiert keine Messung, steht dort `0 min 00 s`.

#### Akzeptanzkriterien

- **Given** 14 von 15 Schritten sind abgehakt
  **When** der letzte offene Schritt mit "SITZT!" abgehakt wird
  **Then** wird der Abschluss-Screen mit dem Titel "ALLES WIEDER DRAN" und dem Button "AB INS ARCHIV" angezeigt
  **And** alle noch laufenden Zeitmessungen des Vorgangs sind gestoppt (US-004.7)

- **Given** der Abschluss-Screen wird für einen Vorgang mit 15 Schritten und 49 Minuten 55 Sekunden gemessener Zeit angezeigt
  **When** die Kennzahlen dargestellt werden
  **Then** steht unter "TEILE" die Zahl `15` und unter "GEMESSEN" der Text `49 min 55 s`

- **Given** der Abschluss-Screen wird angezeigt
  **When** der Mechaniker "AB INS ARCHIV" antippt
  **Then** wird der Reparaturvorgang-Status auf `ARCHIVIERT` gesetzt
  **And** in derselben Operation wird `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt des Archivierens gesetzt ([../governance.md](../governance.md), Abschnitt "Sofort-Save") — das Archivieren schreibt also **nicht** nur den Status
  **And** die Vorgangs-Übersicht (F-001) wird angezeigt
  **And** der Vorgang erscheint im Archiv-Tab
  **And** F-001 zeigt diesen Zeitpunkt als Abschlussdatum des archivierten Vorgangs an (es gibt kein eigenes Archivierungs-Datum)

- **Given** der Abschluss-Screen wird angezeigt, nachdem der Mechaniker Schritt 7 als letzten offenen Schritt abgehakt hat
  **When** er die Android-Zurück-Geste ausführt (statt "AB INS ARCHIV")
  **Then** kehrt er zu **dem Schritt zurück, der vor dem Abschluss-Screen angezeigt wurde** — hier Schritt 7, nicht der letzte Schritt in Montage-Reihenfolge
  **And** der Vorgang wird **nicht** archiviert (Mechaniker kann nochmal prüfen)
  **And** alle Häkchen bleiben gesetzt, es wird nichts geschrieben

- **Given** der Abschluss-Screen wurde beim Wiedereinstieg direkt angezeigt (alle Schritte waren bereits abgehakt, US-004.1) — es gibt also keinen zuvor angezeigten Schritt
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** wird die Schritt-Ansicht mit dem ersten Schritt in Montage-Reihenfolge (Schritt 15) angezeigt, damit er die Dokumentation durchsehen kann
  **And** der Vorgang wird **nicht** archiviert

- **Given** die Schritt-Ansicht wird angezeigt und **kein** Schritt ist mehr offen (der Mechaniker ist vom Abschluss-Screen zurückgekehrt, um noch etwas nachzusehen)
  **When** die Schritt-Ansicht angezeigt wird
  **Then** erscheint zusätzlich der Button "Zum Abschluss"
  **And** ein Tap darauf zeigt den Abschluss-Screen wieder an, ohne etwas zu schreiben
  **And** nimmt der Mechaniker eine Markierung zurück (US-004.2), verschwindet der Button wieder, weil wieder ein Schritt offen ist

Ohne diesen Button wäre die Rückkehr vom Abschluss-Screen eine Sackgasse: Es gibt dann keinen offenen Schritt mehr, dessen Abhaken den Abschluss-Screen erneut auslösen könnte. Der Button navigiert nur — archiviert wird weiterhin ausschließlich auf dem Abschluss-Screen.

**Beschriftung und Platz** (entschieden am 2026-07-27, vorher `[OFFEN]`): ein mittlerer, orange gefüllter Rundkreis mit der Beschriftung **„ABSCHLUSS"**, rechts neben „ZURÜCK" und links vom großen Kreis. Der Prototyp kennt den Fall nicht — dort ist der Fertig-Screen eine Sackgasse ohne Rückweg.

Drei Festlegungen dazu:

- **Orange, nicht grün.** Orange ist im Entwurf durchgehend die Aktion, die nach vorne führt („NÄCHSTES", „LOS GEHT'S", „AB INS ARCHIV"). Grün gehört dem Einbau-Zustand.
- **Zusätzlich statt anstelle.** Der große Kreis bleibt „DRIN", denn er trägt das Zurücknehmen des Häkchens. Würde er zum Abschluss-Knopf, wäre die Korrektur genau in dem Zustand unerreichbar, in dem sie am ehesten gebraucht wird.
- **Nur in der Montage.** Demontage und Archiv kennen den Knopf nicht.

Der Zustand ist aus den Daten abgeleitet (`alleEingebaut`), nicht gespeichert: Sobald eine Markierung zurückgenommen wird, verschwindet der Knopf von selbst.

Der Ausstieg aus dem Flow ohne Archivierung gehört US-004.6.

---

### US-004.6: Den Montage-Flow verlassen

**Als** Mechaniker
**möchte ich** die Montage jederzeit unterbrechen können
**damit** ich zwischendurch etwas anderes erledigen kann, ohne meinen Fortschritt zu verlieren.

**Es gibt genau einen Ausstieg: den kleinen Rundbutton "RAUS".** Er fragt vor dem Verlassen zurück. Ein "✕" trägt die Montage nicht — das gehört allein dem Archiv ([../design-system.md](../design-system.md), Abschnitt "Bewusste Abweichungen vom Prototyp", K-06). Zwei Ausstiege nebeneinander, von denen einer zurückfragt und der andere nicht, wären mit Handschuhen ein Bedienfehler.

| Bedienelement | Wirkung | Eigentümer |
| --- | --- | --- |
| Rundbutton "ZURÜCK" | wechselt den angezeigten Schritt innerhalb des Flows, am ersten Schritt deaktiviert (US-004.4) | F-006 |
| Rundbutton "RAUS" | fragt zurück und verlässt danach den Montage-Flow zur Vorgangs-Übersicht (F-001) | F-004 |

Der Flow kann keinen unfertigen Zustand hinterlassen: Jedes Häkchen steht durch den Sofort-Save bereits in der DB, und es gibt keinen offenen Schritt, der abgeschlossen werden müsste. Die Rückfrage dient deshalb nicht der Datensicherheit, sondern verhindert den Fehlgriff — "RAUS" sitzt in Reichweite der Daumen, die eigentlich "SITZT!" treffen wollten.

#### Akzeptanzkriterien

- **Given** der Montage-Flow zeigt einen Schritt und 12 von 15 Schritten sind abgehakt
  **When** der Mechaniker "RAUS" antippt
  **Then** erscheint die Rückfrage mit dem Titel "FEIERABEND?" und den Aktionen "WEITER ARBEITEN" und "JA, FEIERABEND"
  **And** es wurde noch nichts geschrieben und der Flow ist noch aktiv

- **Given** die Rückfrage "FEIERABEND?" ist offen
  **When** der Mechaniker "WEITER ARBEITEN" antippt oder neben die Rückfrage tippt
  **Then** schließt sich nur die Rückfrage
  **And** derselbe Schritt bleibt angezeigt und der Flow aktiv

- **Given** die Rückfrage "FEIERABEND?" ist offen und 12 von 15 Schritten sind abgehakt
  **When** der Mechaniker "JA, FEIERABEND" antippt
  **Then** wird der Montage-Flow verlassen und die Vorgangs-Übersicht (F-001) im Tab der offenen Vorgänge angezeigt
  **And** eine noch laufende Zeitmessung wird gestoppt (US-004.7)
  **And** alle 12 Häkchen bleiben gesetzt (am Fortschritt wird beim Verlassen nichts geschrieben und nichts verworfen)
  **And** der Vorgang bleibt im Status `OFFEN`, erscheint weiter in der Liste der offenen Vorgänge und wird **nicht** archiviert

- **Given** der Mechaniker hat den Montage-Flow bei 12 von 15 abgehakten Schritten verlassen
  **When** er den Vorgang erneut öffnet und "MONTAGE STARTEN" wählt (F-001)
  **Then** wird der erste noch nicht abgehakte Schritt in Montage-Reihenfolge angezeigt
  **And** der Fortschritt zeigt "12 VON 15 DRIN" (Wiedereinstieg, US-004.1)

- **Given** der Montage-Flow ist geöffnet
  **When** die Kopfzeile dargestellt wird
  **Then** ist dort **kein** "✕" sichtbar (das gibt es nur im Archiv) und **kein** "FEIERABEND"-Chip (den gibt es nur in der Demontage)

- **Given** die Vollbild-Anzeige eines Fotos (F-006) ist geöffnet
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** schließt sich nur die Vollbild-Anzeige
  **And** die Schritt-Ansicht bleibt sichtbar und der Montage-Flow aktiv

- **Given** eine Rückfrage ("FEIERABEND?" oder "DOCH NICHT DRIN?") ist geöffnet
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** schließt sich nur die Rückfrage wie bei "WEITER ARBEITEN" bzw. "ABBRECHEN"
  **And** der Montage-Flow bleibt aktiv

**[OFFEN]** Was die Android-Zurück-Geste in der Schritt-Ansicht selbst tut, wenn kein Overlay offen ist. Sie kann den Flow wie "RAUS" verlassen — dann muss auch sie zurückfragen und die laufende Messung stoppen — oder wirkungslos bleiben, damit "RAUS" der einzige Weg ist. Weder Prototyp (er kennt keine Android-Geste) noch Design-System entscheiden das.

---

### US-004.7: Die Einbauzeit messen

**Als** Mechaniker
**möchte ich** sehen und steuern, wie lange ich an einem Teil einbaue
**damit** die gemessene Zeit zum Vorgang passt, auch wenn ich zwischendurch etwas anderes tue.

Die Montage misst wie die Demontage, aber getrennt: `referenzTyp = "MONTAGE_SCHRITT"`, `referenzId = schritt.id`. Die Trigger stehen in [../F-005-zeiterfassung/service.md](../F-005-zeiterfassung/service.md), Abschnitt "Consumer-Trigger". Das Modell trägt Pausen von selbst — jedes Start/Stopp-Paar ist eine eigene Zeile, die Zeit eines Schritts ist ihre Summe.

Angezeigt wird das in der **Timer-Kapsel** oben links: links ein quadratischer Schalter mit der Glyphe `▶` bzw. `❚❚`, daneben die Beschriftung "STEHT" bzw. "LÄUFT" und darunter die aufgelaufene Zeit des betrachteten Schritts als `mm:ss`. Ab einer Stunde zählen die Minuten weiter (`61:01`), es wird nicht auf Stunden umgebrochen.

#### Akzeptanzkriterien

- **Given** der Montage-Flow zeigt einen Schritt, für den bereits 4 Minuten 12 Sekunden gemessen wurden, und es läuft keine Messung
  **When** die Ansicht dargestellt wird
  **Then** zeigt die Timer-Kapsel die Beschriftung "STEHT", die Glyphe `▶` und die Zeit `04:12`

- **Given** ein Montage-Schritt wird zum betrachteten Schritt, weil der Flow geöffnet wird oder weil die Ansicht nach "SITZT!" weiterrückt
  **When** dieser Schritt angezeigt wird
  **Then** startet für ihn eine Zeitmessung mit `referenzTyp = "MONTAGE_SCHRITT"` und der ID dieses Schritts
  **And** die Timer-Kapsel zeigt "LÄUFT"

- **Given** die Timer-Kapsel zeigt "STEHT"
  **When** der Mechaniker den Schalter antippt
  **Then** wird eine Zeitmessung mit `referenzTyp = "MONTAGE_SCHRITT"` und der ID des betrachteten Schritts gestartet
  **And** die Kapsel zeigt "LÄUFT", die Glyphe `❚❚` und die Fläche in der Leitfarbe
  **And** die angezeigte Zeit läuft im Sekundentakt weiter

- **Given** die Timer-Kapsel zeigt "LÄUFT"
  **When** der Mechaniker den Schalter antippt
  **Then** wird die laufende Messung dieses Schritts gestoppt
  **And** die Kapsel zeigt wieder "STEHT" und die Glyphe `▶`
  **And** die bereits gemessene Zeit bleibt stehen und geht nicht verloren

- **Given** die Timer-Kapsel zeigt "STEHT" für einen Schritt, der zuvor schon einmal gemessen wurde
  **When** der Mechaniker den Schalter erneut auf "LÄUFT" stellt
  **Then** entsteht eine **zweite** Messung für denselben Schritt
  **And** die angezeigte Zeit setzt bei der Summe der bisherigen Messungen auf, nicht bei `00:00`

- **Given** eine Messung des betrachteten Schritts läuft
  **When** der Mechaniker "SITZT!" antippt
  **Then** wird diese Messung gestoppt, bevor die Ansicht zum nächsten offenen Schritt weiterrückt (US-004.2)

- **Given** eine Messung läuft
  **When** der Mechaniker den Flow über "RAUS" → "JA, FEIERABEND" verlässt oder auf dem Abschluss-Screen archiviert
  **Then** ist danach keine Messung dieses Vorgangs mehr offen

- **Given** der Montage-Flow ist geöffnet
  **When** die Ansicht dargestellt wird
  **Then** ist die Timer-Kapsel sichtbar (anders als im Archiv, wo die Zeit stattdessen in der Kopfzeile steht — F-001)

**[OFFEN]** Was mit einer laufenden Messung geschieht, wenn der Mechaniker den betrachteten Schritt **von Hand** wechselt (Thumbnail-Sprung oder "ZURÜCK"). Der Prototyp lässt sie auf dem verlassenen Schritt weiterlaufen, während die Kapsel für den neuen Schritt "STEHT" zeigt; ob sie stattdessen stoppen oder mitwandern soll, ist nicht entschieden. Dieselbe Lücke steht in [../F-005-zeiterfassung/service.md](../F-005-zeiterfassung/service.md), Abschnitt "Consumer-Trigger".

---

## Nicht-funktionale Anforderungen

**Bedienbarkeit (Quality Goal #1):**

- Touch-Targets und Abstände nach [../governance.md](../governance.md), Abschnitt "Touch-Targets" (Handschuhe, dreckige Hände). Die Rundbuttons der Montage erfüllen das mit Abstand; der Timer-Schalter und die Label-Pillen sind auf das Mindestmaß angehoben ([../design-system.md](../design-system.md), Abschnitt "Maße und Trefferflächen")
- Abhaken mit einem einzigen Tap. Nur das **Zurücknehmen** einer Markierung und der **Ausstieg** brauchen eine Rückfrage
- Fotos im Karussell groß genug um Details zu erkennen (z.B. Schrauben-Positionen)
- Schrittnummer (Demontage-Nummer) immer sichtbar und groß — sie stellt die Korrelation zum physischen Ablageort her
- Der Fortschritt "n VON m DRIN" steht neben der Schrittnummer und ersetzt sie nicht

**Performance (Quality Goal #3):**

- Fotos werden skaliert geladen (nicht Full-Size in den Speicher)
- Wechsel zwischen Schritten ohne spürbare Verzögerung
- Die Wischgeste im Foto-Karussell (F-006) reagiert sofort
- Der Sekundentakt der Timer-Kapsel läuft nur, solange eine Messung offen ist

**Zuverlässigkeit (Quality Goal #2):**

- Jedes Abhaken wird sofort in der DB persistiert (Sofort-Save)
- App-Unterbrechung und Verlassen des Flows (US-004.6) verlieren keinen Fortschritt
- Bei erneutem Öffnen: Erster nicht abgehakter Schritt; ist keiner mehr offen, der Abschluss-Screen (US-004.1)

**Fehlerbehandlung:**

- Fehlende Foto-Datei zu einem vorhandenen Foto-Datensatz (z.B. nach Backup/Restore): Platzhalter-Bild (App-Icon) statt Crash — Darstellung in **F-006 US-006.8**
- Schritt ohne Foto mit Label "Ablageort": Hinweis "AM FAHRZEUG GEBLIEBEN" (kein Fehlerfall)
- Schritt ganz ohne Fotos: Karussell im Leer-Zustand, Thumbnail mit Platzhalter-Bild — **F-006 US-006.9** (Karussell) und **US-006.8** (Thumbnail). Leer-Zustand und Platzhalter-Bild sind zwei verschiedene Fälle und werden nicht vermischt; F-004 beschreibt keinen von beiden selbst. Der Hinweis "AM FAHRZEUG GEBLIEBEN" erscheint auch hier, weil er allein am fehlenden Ablageort-Foto hängt
- Vorgang ohne Schritte: Hinweis "Keine Demontage-Schritte vorhanden. Zuerst demontieren."

---

## Technische Hinweise

- Datenmodell: Ein Schritt hat N Fotos in der Room-Entity `SchrittFoto` (Tabelle `schritt_foto`, FK auf `schritt.id`, `onDelete = CASCADE`). Geladen wird `Schritt` mit seinen Fotos per Room-`@Relation`.
- Labels sind drei unabhängige Boolean-Flags am Foto: `istBauteil` (Default `true`), `istUebersicht`, `istAblageort`. Mehrere Flags gleichzeitig sind erlaubt.
- Ein `SchrittTyp`-Feld gibt es nicht mehr. Ob ein Schritt einen Ablageort hat, wird abgeleitet: `schritt.fotos.any { it.istAblageort }`.
- Wo genau eine Kategorie nötig ist (Einfärbung, Filter), gilt die Priorität `Ablageort > Uebersicht > Bauteil`.
- Query Montage-Reihenfolge: Schritte eines Vorgangs `ORDER BY schrittNummer DESC` (höchste Demontage-Schrittnummer zuerst). Die Schrittnummer wird dabei **nicht** neu vergeben — nur die Reihenfolge der Anzeige dreht sich um.
- Query Fotos eines Schritts: `ORDER BY reihenfolge ASC`; das erste Foto ist das Thumbnail des Schritts in der Leiste (F-006).
- Query Fortschritt: `SELECT COUNT(*) FROM schritt WHERE reparaturvorgangId = :id AND eingebautBeiMontage = 1` (= n) gegen die Gesamtzahl der Schritte des Vorgangs (= m). Daraus werden Text und Balkenanteil gebildet; die Schrittnummer wird dafür nie verwendet. Bei `m = 0` ist der Anteil `0`, nicht undefiniert.
- Query Wiedereinstieg: erster Schritt in Montage-Reihenfolge mit `eingebautBeiMontage = 0`. Liefert die Query **kein** Ergebnis, sind alle Schritte abgehakt und es wird direkt der Abschluss-Screen geöffnet (US-004.1). Dieselbe Query liefert auch Punkt 2 der Regel "nächster offener Schritt" (US-004.2).
- Abhaken: `UPDATE schritt SET eingebautBeiMontage = :wert WHERE id = :id` als suspend-Funktion, Sofort-Save (Governance). Beim Zurücknehmen wird erst nach Bestätigung der Rückfrage geschrieben (US-004.2). Der Fortschritt wird nie hochgezählt, sondern nach jedem Schreibvorgang aus der Fortschritts-Query neu gelesen — deshalb sinkt er beim Zurücknehmen automatisch.
- Weiterrücken nach dem Abhaken: Punkt 1 der Regel sucht ab **hinter** dem gerade abgehakten Schritt in der Anzeige-Reihenfolge (`indexOfFirst { index > aktuell && !eingebaut }`), erst Punkt 2 fällt auf den ersten offenen Schritt der ganzen Liste zurück. Beide Suchen dürfen den gerade abgehakten Schritt nicht mehr als offen sehen, auch wenn der Room-Flow den neuen Stand noch nicht nachgeliefert hat.
- `aktualisiertAm`: Abhaken, Zurücknehmen und Archivieren setzen in **derselben** Operation `reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion — verbindliche Regel aus [../governance.md](../governance.md), Abschnitt "Sofort-Save". Praktisch heißt das: Schritt-Update und Vorgangs-Update laufen zusammen in einer Room-`@Transaction`-Funktion des Repositories, damit kein Zwischenzustand entstehen kann.
- Archivierung: `Reparaturvorgang.status = ARCHIVIERT` **und** `aktualisiertAm = jetzt` in einer Transaktion, danach Navigation zur Übersicht (F-001). Geschrieben wird ausschließlich beim Tap auf "AB INS ARCHIV" im Abschluss-Screen, nicht beim Abhaken des letzten Schritts und nicht beim Verlassen des Flows (US-004.6). Ein eigenes Archivierungs-Datum gibt es nicht: F-001 nutzt dieses `aktualisiertAm` als Abschlussdatum. Da archivierte Vorgänge nur lesend geöffnet werden, ändert es sich danach nicht mehr.
- Nach dem Archivieren verschwindet der ganze Montage-Pfad vom Navigations-Stapel (`popBackStack` bis zur Übersicht), damit die Zurück-Geste nicht in den Abschluss-Screen eines bereits archivierten Vorgangs führt.
- Abschluss-Screen: eigenes Navigations-Ziel auf dem Back-Stack. Back darauf ist `popBackStack` zurück zur Schritt-Ansicht und schreibt nichts (US-004.5). Der zuletzt angezeigte Schritt bleibt im ViewModel erhalten; wurde der Abschluss-Screen ohne vorherige Schritt-Ansicht geöffnet (Wiedereinstieg), ist das Ziel der erste Schritt in Montage-Reihenfolge.
- Kennzahl "GEMESSEN": Summe über alle Schritte des Vorgangs und über beide `referenzTyp`-Werte, gebildet aus `gesamtdauer(schrittId, typ)` (F-005). Formatierung ausgeschrieben (`m min ss s`, ab einer Stunde `h h mm min`).
- Sichtbarkeit von "Zum Abschluss": abgeleiteter State aus der Fortschritts-Query (`n == m`), kein eigenes Feld. Der Button ist eine Schritt-Aktion des Consumers neben "SITZT!"/"DRIN", nicht Teil von F-006.
- Foto-Karussell, Thumbnail-Leiste, Vollbild-Ansicht, Label-Anzeige am sichtbaren Foto sowie die Schritt-Navigation kommen aus dem gemeinsamen Modul F-006. F-004 nutzt es im Modus **"lesend-mit-Aktionen"**: Label sichtbar, aber nicht änderbar, keine Foto-Aufnahme, keine Aktion am sichtbaren Foto, Schritt-Aktionen beim Consumer.
- F-006 meldet Navigation und Foto-Wechsel nur als Callbacks; F-004 hält `aktuellerIndex` und `sichtbaresFotoIndex` im ViewModel und schreibt sie fort.
- Fotos skaliert laden (kein Full-Size), Thumbnails in Thumbnail-Größe dekodieren.

---

## Offene Fragen

Drei Punkte sind mit `[OFFEN]` markiert und zu klären:

- Fließtext der Ausstiegs-Rückfrage (Abschnitt "Wörtliche UI-Texte")
- Wirkung der Android-Zurück-Geste in der Schritt-Ansicht (US-004.6)
- Verhalten einer laufenden Zeitmessung beim manuellen Schrittwechsel (US-004.7)

**Erledigt:** Beschriftung und Platz des Buttons "Zum Abschluss" (US-004.5) — am 2026-07-27 entschieden, siehe dort.

**Entschieden:** F-004 nutzt F-006 im Modus **"lesend-mit-Aktionen"**. Die Label eines Fotos sind während der Montage sichtbar, aber nicht änderbar; Fotos werden in der Montage weder aufgenommen noch gelöscht. Label werden ausschließlich in der Demontage (F-003) gesetzt. Die datenverändernden Aktionen der Montage sind das Abhaken (`eingebautBeiMontage`), die Zeitmessung (F-005) und die abschließende Archivierung.

**Entschieden:** Der Name "ZURÜCK" gehört in F-004 ausschließlich der Schritt-Navigation aus F-006. Der Flow-Ausstieg heißt "RAUS", fragt zurück und ist das einzige Bedienelement, das den Flow verlässt (US-004.6). Er führt zur Vorgangs-Übersicht (F-001), lässt den Fortschritt unverändert stehen und archiviert nicht — archiviert wird ausschließlich über "AB INS ARCHIV" auf dem Abschluss-Screen (US-004.5).

---

## Änderungshistorie

| Datum | Änderung |
| --- | --- |
| 2026-07-27 | Ausstieg auf den Rundbutton "RAUS" mit Rückfrage vereinheitlicht; "✕" gehört allein dem Archiv (design-system.md, K-06). US-004.6 neu gefasst. |
| 2026-07-27 | Fortschritt als "n VON m DRIN" plus grünem Balken festgelegt (US-004.3). |
| 2026-07-27 | Wörtliche UI-Texte "SITZT!", "DRIN", "AM FAHRZEUG GEBLIEBEN", "EINBAU" sowie die Rückfrage-Wortlaute aufgenommen; "DRIN" als flache Zustandsfläche von der Aktionsfläche "SITZT!" unterschieden. |
| 2026-07-27 | Abschluss-Screen mit "ALLES WIEDER DRAN", Auftragsnummer und Beschreibung, den Kennzahlen "TEILE"/"GEMESSEN" und "AB INS ARCHIV" beschrieben (US-004.5). |
| 2026-07-27 | Montage dockt nur "ZURÜCK" an; einen Vorwärts-Rundbutton gibt es nicht (design-system.md, K-05). US-004.4 neu gefasst. |
| 2026-07-27 | US-004.7 "Die Einbauzeit messen" ergänzt: Timer-Kapsel mit Schalter, `referenzTyp = "MONTAGE_SCHRITT"` (design-system.md, K-03 und K-04). |
