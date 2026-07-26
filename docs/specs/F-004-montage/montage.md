# F-004: Montage-Flow

## Kontext

Der Montage-Flow ist das Gegenstück zum Demontage-Flow (F-003). Der Mechaniker hat ein Fahrzeug demontiert und möchte es nun wieder zusammenbauen. Die App zeigt die Demontage-Schritte in umgekehrter Reihenfolge an, sodass der Mechaniker Schritt für Schritt das Fahrzeug wieder aufbaut.

**Problem:** Nach Tagen oder Wochen erinnert sich der Mechaniker nicht mehr an die Reihenfolge und die Ablageorte der ausgebauten Teile. Er braucht eine visuelle Anleitung für den Zusammenbau.

**Lösung:** Die Demontage-Dokumentation wird rückwärts abgespielt. Pro Schritt sieht der Mechaniker alle Fotos dieses Schritts im Foto-Karussell (F-006): das Bauteil-Foto (wie sieht das Teil montiert aus?) und — falls vorhanden — ein Foto mit dem Label "Ablageort" (wo liegt es?). Der Schritt-Wechsel (Vor/Zurück und Sprung per Thumbnail) kommt vollständig aus F-006. Erledigte Schritte werden abgehakt. Den Flow verlässt der Mechaniker über die Android-Zurück-Geste — davon getrennt und nie über die Schritt-Navigation (US-004.6).

**Primärer Nutzer:** Mechaniker in der Werkstatt, der ein zuvor demontiertes Fahrzeug wieder zusammenbaut.

**Situation:** Der Mechaniker steht am Fahrzeug, hat die ausgebauten Teile auf nummerierten Ablageplätzen liegen und möchte wissen: Welches Teil kommt als nächstes, und wo liegt es?

### Zusammenspiel mit Schrittnummer (F-003)

Die Schrittnummer aus der Demontage dient im Montage-Flow als Orientierung. **Schrittnummer und Fortschritt sind zwei verschiedene Dinge und werden nie vermischt:**

- **Schrittnummer:** Immer die Demontage-Schrittnummer (z.B. "Schritt 12"). Sie ist die Identität des Schritts und wird in der Montage **nicht umnummeriert**, obwohl die Schritte hier rückwärts durchlaufen werden. Die Schrittnummern aller Schritte erscheinen in der Thumbnail-Leiste (F-006).
- **Fortschritt:** Getrennt davon als "N von M eingebaut" formuliert (z.B. "3 von 15 eingebaut"). Der Fortschritt zählt abgehakte Schritte, er benennt keinen Schritt. Formulierungen wie "Schritt 3 von 15" sind in dieser Spec nicht zulässig, weil sie beides vermischen.
- **Ablageort-Korrelation:** Hat der Mechaniker seine physischen Ablageorte mit Schrittnummern beschriftet, findet er über die Nummer das passende Teil. Genau deshalb bleibt die Nummer über Demontage und Montage hinweg stabil.
- **Schritte ohne Ablageort:** Manche Schritte haben kein Foto mit dem Label "Ablageort" (Teil blieb am Fahrzeug). Diese werden mit dem Hinweis "Am Fahrzeug" angezeigt

---

## User Stories

### US-004.1: Schritte in Montage-Reihenfolge anzeigen

**Als** Mechaniker
**möchte ich** die Demontage-Schritte in umgekehrter Reihenfolge sehen
**damit** ich die Teile in der richtigen Reihenfolge wieder einbauen kann (letztes ausgebautes Teil = erstes einzubauendes Teil).

#### Akzeptanzkriterien

- **Given** ein Reparaturvorgang mit 15 dokumentierten Demontage-Schritten existiert und **kein** Schritt ist abgehakt
  **When** der Mechaniker "Montage starten" wählt (F-001)
  **Then** wird der Montage-Flow geöffnet und zeigt den Schritt mit der höchsten Demontage-Schrittnummer zuerst (Schritt 15)
  **And** der Fortschritt zeigt "0 von 15 eingebaut"

- **Given** ein Reparaturvorgang mit 15 Demontage-Schritten, dessen Zusammenbau bereits begonnen wurde (die Schritte 15, 14 und 13 sind abgehakt)
  **When** der Mechaniker "Montage starten" wählt (F-001)
  **Then** wird der Montage-Flow geöffnet und zeigt den **ersten noch nicht abgehakten Schritt in Montage-Reihenfolge** (Schritt 12) — nicht den Schritt mit der höchsten Demontage-Schrittnummer
  **And** der Fortschritt zeigt "3 von 15 eingebaut"
  **And** es wird beim Öffnen nichts in die DB geschrieben (der Wiedereinstieg ist ein reiner Lesevorgang)

- **Given** ein Reparaturvorgang, bei dem **alle** Schritte abgehakt sind, der aber noch nicht archiviert wurde (der Mechaniker hat den Abschluss-Screen zuvor ohne "Archivieren" verlassen)
  **When** der Mechaniker "Montage starten" wählt (F-001)
  **Then** wird direkt der Abschluss-Screen angezeigt (US-004.5)
  **And** der Vorgang bleibt bis zum Tap auf "Archivieren" im Status `OFFEN`

- **Given** der Montage-Flow ist geöffnet
  **When** der erste Schritt angezeigt wird
  **Then** sind folgende Informationen sichtbar:
  - das Foto-Karussell dieses Schritts mit allen seinen Fotos (F-006)
  - die Schrittnummer (Demontage-Nummer)
  - der Fortschritt "N von M eingebaut"
  - die Label des sichtbaren Fotos (Bauteil / Uebersicht / Ablageort) — sichtbar, aber **nicht änderbar** (F-006, Modus "lesend-mit-Aktionen")
  - die Thumbnail-Leiste über alle Schritte (F-006)
  - "Eingebaut"-Button

- **Given** ein Schritt hat mindestens ein Foto mit dem Label "Ablageort"
  **When** dieses Foto im Karussell sichtbar ist
  **Then** ist am Foto erkennbar, dass es das Label "Ablageort" trägt (Label-Anzeige am sichtbaren Foto, F-006)
  **And** das Thumbnail dieses Schritts ist in der Leiste in der Ablageort-Markierung eingefärbt (F-006 US-006.3)
  **And** die Label-Anzeige ist nicht bedienbar (Label werden ausschließlich in der Demontage gesetzt)

- **Given** ein Schritt hat kein Foto mit dem Label "Ablageort" (Teil blieb am Fahrzeug)
  **When** dieser Schritt angezeigt wird
  **Then** wird der Hinweis "Am Fahrzeug" angezeigt

#### UI-Verhalten

- Foto-Karussell, Vergrößern eines Fotos per Tap, die Label-Anzeige am sichtbaren Foto und die Thumbnail-Leiste sind in F-006 spezifiziert und werden hier nicht wiederholt
- Die farbliche Markierung eines Thumbnails danach, ob der Schritt ein Ablageort-Foto hat, ist Teil von F-006
- F-004 nutzt F-006 im Modus **"lesend-mit-Aktionen"**: Label sind sichtbar, aber nicht änderbar, und es gibt im Browser keine Foto-Aufnahme. Die Schritt-Aktion "Eingebaut" gehört dem Consumer F-004
- Der Button "Zum Abschluss" ist die einzige weitere Schritt-Aktion. Er erscheint nur, wenn kein Schritt mehr offen ist (US-004.5), und fehlt deshalb in der Aufzählung oben

---

### US-004.2: Schritt als eingebaut markieren

**Als** Mechaniker
**möchte ich** erledigte Schritte als "eingebaut" abhaken
**damit** ich den Fortschritt verfolgen und weiß, welche Teile noch fehlen.

#### Regel: nächster offener Schritt

Überall dort, wo diese Spec vom "nächsten offenen Schritt" spricht (US-004.2, US-004.3), gilt dieselbe Regel. Sie wird in dieser Reihenfolge geprüft:

1. Der nächste noch **nicht abgehakte** Schritt in Montage-Reihenfolge **nach** dem gerade abgehakten (nächst-niedrigere Demontage-Schrittnummer; bereits abgehakte Schritte werden dabei übersprungen).
2. Existiert dahinter keiner mehr, der **erste** noch nicht abgehakte Schritt in Montage-Reihenfolge — derselbe Schritt, den auch der Wiedereinstieg liefert (US-004.1). So landet der Mechaniker, der per Thumbnail nach vorn gesprungen ist, anschließend wieder bei seiner ersten Lücke statt in einer Sackgasse.
3. Ist kein Schritt mehr offen, erscheint der Abschluss-Screen (US-004.5).

Beispiel zu Punkt 2: Der Mechaniker ist per Thumbnail auf Schritt 4 gesprungen, während 15 bis 5 noch offen sind, und hakt Schritt 4 ab. Angezeigt wird danach Schritt 3 (Punkt 1). Hakt er weiter bis Schritt 1 ab, greift Punkt 2 und es erscheint Schritt 15.

Die Regel gilt **nur für das automatische Weiterrücken nach dem Abhaken**. Der Button "Weiter" (US-004.4) wechselt dagegen immer zum unmittelbar benachbarten Schritt, auch wenn dieser bereits abgehakt ist — Blättern soll keine Schritte überspringen.

#### Akzeptanzkriterien

- **Given** ein Montage-Schritt wird angezeigt und ist noch nicht abgehakt
  **When** der Mechaniker den "Eingebaut"-Button antippt
  **Then** wird der Schritt als eingebaut markiert (`eingebautBeiMontage = true`)
  **And** die Markierung wird sofort in der DB persistiert
  **And** in derselben Operation wird `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion gesetzt ([../governance.md](../governance.md), Abschnitt "Sofort-Save")
  **And** es wird der nächste offene Schritt nach der Regel oben angezeigt

- **Given** der Montage-Flow zeigt Schritt 4, die Schritte 15 bis 5 sind noch offen und Schritt 3 ist noch nicht abgehakt
  **When** der Mechaniker Schritt 4 abhakt
  **Then** wird Schritt 3 angezeigt (nächster nicht abgehakter Schritt in Montage-Reihenfolge, Regel Punkt 1)

- **Given** der Montage-Flow zeigt Schritt 4, die Schritte 3, 2 und 1 sind bereits abgehakt und die Schritte 15 bis 5 sind noch offen
  **When** der Mechaniker Schritt 4 abhakt
  **Then** wird Schritt 15 angezeigt (erster nicht abgehakter Schritt in Montage-Reihenfolge, Regel Punkt 2)

- **Given** ein Schritt wurde als eingebaut markiert
  **When** der Mechaniker zu diesem Schritt zurücknavigiert
  **Then** ist der "Eingebaut"-Button als bereits erledigt dargestellt (z.B. ausgegraut mit Häkchen)

- **Given** ein bereits abgehakter Schritt wird angezeigt und der Fortschritt steht auf "8 von 15 eingebaut"
  **When** der Mechaniker den "Eingebaut"-Button erneut antippt
  **Then** erscheint ein Warndialog ("Markierung zurücknehmen?")
  **And** nach Bestätigung wird `eingebautBeiMontage = false` gesetzt und sofort in der DB persistiert
  **And** in derselben Operation wird `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion gesetzt ([../governance.md](../governance.md), Abschnitt "Sofort-Save")
  **And** der Fortschritt sinkt um eins ("7 von 15 eingebaut")
  **And** derselbe Schritt bleibt angezeigt (Zurücknehmen springt nie weiter — die Regel "nächster offener Schritt" gilt nur für das Abhaken)
  **And** bei Abbruch bleibt die Markierung gesetzt, der Fortschritt unverändert und es wird nichts geschrieben

---

### US-004.3: Fortschritt anzeigen

**Als** Mechaniker
**möchte ich** sehen, wie weit der Zusammenbau fortgeschritten ist
**damit** ich den Überblick behalte und abschätzen kann, wie viel noch zu tun ist.

#### Akzeptanzkriterien

- **Given** der Montage-Flow ist geöffnet und 3 von 15 Schritten sind abgehakt
  **When** der aktuelle Schritt angezeigt wird
  **Then** ist er in der Thumbnail-Leiste (F-006) hervorgehoben
  **And** wird seine Demontage-Schrittnummer angezeigt (z.B. "Schritt 12")
  **And** wird der Fortschritt "3 von 15 eingebaut" angezeigt

- **Given** der Montage-Flow ist geöffnet und mindestens ein weiterer Schritt ist noch offen
  **When** ein Schritt abgehakt wird
  **Then** rückt die Hervorhebung in der Thumbnail-Leiste auf den nächsten offenen Schritt (Regel in US-004.2)
  **And** der Fortschritt erhöht sich um eins ("4 von 15 eingebaut")

Zwei Fälle gehören bewusst nicht hierher: "der letzte offene Schritt wird abgehakt" besitzt US-004.5 (Abschluss-Screen), "bloßes Blättern ändert den Fortschritt nicht" besitzt US-004.4.

---

### US-004.4: Zwischen Schritten navigieren

**Als** Mechaniker
**möchte ich** frei zwischen Schritten navigieren können
**damit** ich nachschauen kann, was ich bereits eingebaut habe oder was als nächstes kommt.

Die Schritt-Navigation (Vor/Zurück zwischen Schritten und Sprung per Thumbnail) gehört vollständig **F-006** und wird hier nicht selbst spezifiziert. F-004 legt nur fest, welcher Schritt daraus in Montage-Reihenfolge folgt und wie sich der Flow an den Rändern verhält.

**"Zurück" wechselt den Schritt, es verlässt nie den Flow.** Der Ausstieg aus dem Montage-Flow ist ein anderes Bedienelement und in US-004.6 geregelt.

#### Akzeptanzkriterien

- **Given** der Montage-Flow zeigt Schritt 11
  **When** der Mechaniker "Weiter" antippt (Navigation aus F-006)
  **Then** wird der nächste Schritt in Montage-Reihenfolge angezeigt: Schritt 10 (nächst-niedrigere Demontage-Schrittnummer)
  **And** der Fortschritt "N von M eingebaut" bleibt unverändert (bloßes Blättern hakt nichts ab)

- **Given** der Montage-Flow zeigt Schritt 11
  **When** der Mechaniker "Zurück" antippt (Navigation aus F-006)
  **Then** wird Schritt 12 angezeigt (nächst-höhere Demontage-Schrittnummer)

- **Given** der Montage-Flow zeigt den ersten Schritt in Montage-Reihenfolge (Schritt 15 bei 15 Schritten)
  **When** der Mechaniker "Zurück" antippt
  **Then** passiert nichts (Button ist deaktiviert, nicht ausgeblendet — F-006 US-006.10)
  **And** der Montage-Flow wird **nicht** verlassen (dafür gibt es die Android-Zurück-Geste, US-004.6)

- **Given** der Montage-Flow zeigt den letzten Schritt in Montage-Reihenfolge (Schritt 1)
  **When** der Mechaniker "Weiter" antippt
  **Then** passiert nichts (Button ist deaktiviert) — "Weiter" wird am Ende nie zum Abschluss-Button; zum Abschluss-Screen führt allein das Abhaken des letzten offenen Schritts (US-004.5)

- **Given** der Montage-Flow zeigt Schritt 11
  **When** der Mechaniker in der Thumbnail-Leiste (F-006) das Thumbnail von Schritt 4 antippt
  **Then** wird Schritt 4 angezeigt

#### UI-Verhalten

- Schritt-Wechsel ausschließlich über "Zurück"/"Weiter" und die Thumbnail-Leiste — beides aus F-006
- Die horizontale Wischgeste im Bildbereich gehört dem Foto-Karussell und wechselt **nur das Foto innerhalb des Schritts** (F-006 US-006.4), niemals den Schritt
- Es gibt keine Eingabe einer Schrittnummer und keinen Sprung-Dialog
- "Zurück"/"Weiter" sind reine Schritt-Navigation. Sie schließen nichts ab, haken nichts ab und verlassen den Flow nicht
- Touch-Targets und Abstände für "Zurück"/"Weiter" nach [../governance.md](../governance.md), Abschnitt "Touch-Targets"

---

### US-004.5: Montage abschließen und archivieren

**Als** Mechaniker
**möchte ich** nach Abschluss aller Schritte den Vorgang archivieren
**damit** der Vorgang aus der aktiven Liste verschwindet und im Archiv für spätere Referenz verfügbar ist.

Der Abschluss-Screen ist der **einzige** Weg zur Archivierung. Es gibt keinen "Weiter"-Button, der sich am Ende in einen Abschluss-Button verwandelt, und keine Archivierung ohne den Abschluss-Screen. Grund: Archivieren nimmt den Vorgang aus der aktiven Liste — mit Handschuhen braucht das eine ausdrückliche Bestätigung. Erreicht wird der Abschluss-Screen auf drei Wegen: durch das Abhaken des letzten offenen Schritts, beim Wiedereinstieg in einen vollständig abgehakten Vorgang (US-004.1) und über den Button "Zum Abschluss" nach einer Rückkehr in die Schritt-Ansicht.

#### Akzeptanzkriterien

- **Given** 14 von 15 Schritten sind abgehakt
  **When** der letzte offene Schritt abgehakt wird
  **Then** wird der Abschluss-Screen angezeigt mit "Zusammenbau abgeschlossen!" und einem "Archivieren"-Button

- **Given** der Abschluss-Screen wird angezeigt
  **When** der Mechaniker "Archivieren" antippt
  **Then** wird der Reparaturvorgang-Status auf `ARCHIVIERT` gesetzt
  **And** in derselben Operation wird `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt des Archivierens gesetzt ([../governance.md](../governance.md), Abschnitt "Sofort-Save") — das Archivieren schreibt also **nicht** nur den Status
  **And** die Vorgangs-Übersicht (F-001) wird angezeigt
  **And** der Vorgang erscheint im Archiv-Tab
  **And** F-001 zeigt diesen Zeitpunkt als Abschlussdatum des archivierten Vorgangs an (es gibt kein eigenes Archivierungs-Datum)

- **Given** der Abschluss-Screen wird angezeigt, nachdem der Mechaniker Schritt 7 als letzten offenen Schritt abgehakt hat
  **When** er die Android-Zurück-Geste ausführt (statt "Archivieren")
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

Der Ausstieg aus dem Flow ohne Archivierung gehört US-004.6.

---

### US-004.6: Den Montage-Flow verlassen

**Als** Mechaniker
**möchte ich** die Montage jederzeit unterbrechen können
**damit** ich zwischendurch etwas anderes erledigen kann, ohne meinen Fortschritt zu verlieren.

**Flow-Ausstieg und Schritt-Navigation sind zwei verschiedene Dinge und tragen deshalb verschiedene Namen:**

| Bedienelement | Wirkung | Eigentümer |
| --- | --- | --- |
| Button "Zurück" / "Weiter" | wechselt den angezeigten Schritt innerhalb des Flows, an den Rändern deaktiviert (US-004.4) | F-006 |
| Android-Zurück-Geste (bzw. -Taste) | verlässt den Montage-Flow und führt zur Vorgangs-Übersicht (F-001) | F-004 |

Es gibt in der Montage **keinen Button, der den Flow verlässt**. Der Button "Zurück" ist ausschließlich Schritt-Navigation und darf nie so verstanden werden. Anders als die Demontage (F-003 US-003.6: "Beenden" ist der einzige Ausstieg, Back bewirkt nichts) braucht die Montage keinen eigenen Ausstiegs-Button, weil sie keinen unfertigen Zustand hinterlassen kann: Jedes Häkchen steht durch den Sofort-Save bereits in der DB, und es gibt keinen offenen Schritt, der abgeschlossen werden müsste. Deshalb verlässt hier die Android-Zurück-Geste den Flow — das Standardverhalten von Android, ohne Datenverlust.

#### Akzeptanzkriterien

- **Given** der Montage-Flow zeigt einen Schritt und 12 von 15 Schritten sind abgehakt
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** wird der Montage-Flow verlassen und die Vorgangs-Übersicht (F-001) angezeigt
  **And** alle 12 Häkchen bleiben gesetzt (beim Verlassen wird weder geschrieben noch etwas verworfen)
  **And** der Vorgang bleibt im Status `OFFEN`, erscheint weiter in der Liste der offenen Vorgänge und wird **nicht** archiviert

- **Given** der Mechaniker hat den Montage-Flow bei 12 von 15 abgehakten Schritten verlassen
  **When** er den Vorgang erneut öffnet und "Montage starten" wählt (F-001)
  **Then** wird der erste noch nicht abgehakte Schritt in Montage-Reihenfolge angezeigt
  **And** der Fortschritt zeigt "12 von 15 eingebaut" (Wiedereinstieg, US-004.1)

- **Given** der Montage-Flow zeigt den ersten Schritt in Montage-Reihenfolge (Schritt 15), an dem der Button "Zurück" deaktiviert ist
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** wird der Flow trotzdem verlassen und die Vorgangs-Übersicht angezeigt (die Geste ist nicht der deaktivierte Button)

- **Given** die Vollbild-Anzeige eines Fotos (F-006) ist geöffnet
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** schließt sich nur die Vollbild-Anzeige
  **And** die Schritt-Ansicht bleibt sichtbar und der Montage-Flow aktiv

- **Given** der Warndialog "Markierung zurücknehmen?" (US-004.2) ist geöffnet
  **When** der Mechaniker die Android-Zurück-Geste ausführt
  **Then** schließt sich nur der Dialog wie bei "Abbrechen" (die Markierung bleibt gesetzt)
  **And** der Montage-Flow bleibt aktiv

---

## Nicht-funktionale Anforderungen

**Bedienbarkeit (Quality Goal #1):**

- Touch-Targets und Abstände für "Eingebaut"-Button und Navigation nach [../governance.md](../governance.md), Abschnitt "Touch-Targets" (Handschuhe, dreckige Hände)
- Abhaken mit einem einzigen Tap. Nur das **Zurücknehmen** einer Markierung braucht zusätzlich den Warndialog aus US-004.2
- Fotos im Karussell groß genug um Details zu erkennen (z.B. Schrauben-Positionen)
- Schrittnummer (Demontage-Nummer) immer sichtbar und groß — sie stellt die Korrelation zum physischen Ablageort her
- Der Fortschritt "N von M eingebaut" steht neben der Schrittnummer und ersetzt sie nicht

**Performance (Quality Goal #3):**

- Fotos werden skaliert geladen (nicht Full-Size in den Speicher)
- Wechsel zwischen Schritten ohne spürbare Verzögerung
- Die Wischgeste im Foto-Karussell (F-006) reagiert sofort

**Zuverlässigkeit (Quality Goal #2):**

- Jedes Abhaken wird sofort in der DB persistiert (Sofort-Save)
- App-Unterbrechung und Verlassen des Flows (US-004.6) verlieren keinen Fortschritt
- Bei erneutem Öffnen: Erster nicht abgehakter Schritt; ist keiner mehr offen, der Abschluss-Screen (US-004.1)

**Fehlerbehandlung:**

- Fehlende Foto-Datei zu einem vorhandenen Foto-Datensatz (z.B. nach Backup/Restore): Platzhalter-Bild (App-Icon) statt Crash — Darstellung in **F-006 US-006.8**
- Schritt ohne Foto mit Label "Ablageort": Hinweis "Am Fahrzeug" (kein Fehlerfall)
- Schritt ganz ohne Fotos: Karussell im Leer-Zustand, Thumbnail mit Platzhalter-Bild — **F-006 US-006.9** (Karussell) und **US-006.8** (Thumbnail). Leer-Zustand und Platzhalter-Bild sind zwei verschiedene Fälle und werden nicht vermischt; F-004 beschreibt keinen von beiden selbst
- Vorgang ohne Schritte: Hinweis "Keine Demontage-Schritte vorhanden. Zuerst demontieren."

---

## Technische Hinweise

- Datenmodell: Ein Schritt hat N Fotos in der Room-Entity `SchrittFoto` (Tabelle `schritt_foto`, FK auf `schritt.id`, `onDelete = CASCADE`). Geladen wird `Schritt` mit seinen Fotos per Room-`@Relation`.
- Labels sind drei unabhängige Boolean-Flags am Foto: `istBauteil` (Default `true`), `istUebersicht`, `istAblageort`. Mehrere Flags gleichzeitig sind erlaubt.
- Ein `SchrittTyp`-Feld gibt es nicht mehr. Ob ein Schritt einen Ablageort hat, wird abgeleitet: `schritt.fotos.any { it.istAblageort }`.
- Wo genau eine Kategorie nötig ist (Einfärbung, Filter), gilt die Priorität `Ablageort > Uebersicht > Bauteil`.
- Query Montage-Reihenfolge: Schritte eines Vorgangs `ORDER BY schrittNummer DESC` (höchste Demontage-Schrittnummer zuerst). Die Schrittnummer wird dabei **nicht** neu vergeben — nur die Reihenfolge der Anzeige dreht sich um.
- Query Fotos eines Schritts: `ORDER BY reihenfolge ASC`; das erste Foto ist das Thumbnail des Schritts in der Leiste (F-006).
- Query Fortschritt: `SELECT COUNT(*) FROM schritt WHERE reparaturvorgangId = :id AND eingebautBeiMontage = 1` (= N) gegen die Gesamtzahl der Schritte des Vorgangs (= M). Daraus wird "N von M eingebaut" gebildet; die Schrittnummer wird dafür nie verwendet.
- Query Wiedereinstieg: erster Schritt in Montage-Reihenfolge mit `eingebautBeiMontage = 0`. Liefert die Query **kein** Ergebnis, sind alle Schritte abgehakt und es wird direkt der Abschluss-Screen geöffnet (US-004.1). Dieselbe Query liefert auch Punkt 2 der Regel "nächster offener Schritt" (US-004.2).
- Abhaken: `UPDATE schritt SET eingebautBeiMontage = :wert WHERE id = :id` als suspend-Funktion, Sofort-Save (Governance). Beim Zurücknehmen wird erst nach Bestätigung des Warndialogs geschrieben (US-004.2). Der Fortschritt wird nie hochgezählt, sondern nach jedem Schreibvorgang aus der Fortschritts-Query neu gelesen — deshalb sinkt er beim Zurücknehmen automatisch.
- `aktualisiertAm`: Abhaken, Zurücknehmen und Archivieren setzen in **derselben** Operation `reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion — verbindliche Regel aus [../governance.md](../governance.md), Abschnitt "Sofort-Save". Praktisch heißt das: Schritt-Update und Vorgangs-Update laufen zusammen in einer Room-`@Transaction`-Funktion des Repositories, damit kein Zwischenzustand entstehen kann.
- Archivierung: `Reparaturvorgang.status = ARCHIVIERT` **und** `aktualisiertAm = jetzt` in einer Transaktion, danach Navigation zur Übersicht (F-001). Es wird also nicht nur der Status geschrieben. Geschrieben wird ausschließlich beim Tap auf "Archivieren" im Abschluss-Screen, nicht beim Abhaken des letzten Schritts und nicht beim Verlassen des Flows (US-004.6). Ein eigenes Archivierungs-Datum gibt es nicht: F-001 nutzt dieses `aktualisiertAm` als Abschlussdatum. Da archivierte Vorgänge nur lesend geöffnet werden, ändert es sich danach nicht mehr.
- Flow-Ausstieg (US-004.6): Die Montage-Schritt-Ansicht braucht **keinen** `BackHandler` — Back ist gewöhnliche Navigation (`popBackStack`) zurück zur Übersicht. Die F-006-Vollbild-Anzeige und der Warndialog sind Overlays innerhalb der Ansicht und konsumieren Back zuerst. Beim Verlassen wird nichts geschrieben; der Fortschritt steht bereits durch den Sofort-Save des Abhakens in der DB. Das ist die bewusste Abweichung von F-003, wo ein `BackHandler` den Flow festhält, weil dort ein offener Schritt zurückbleiben könnte.
- Abschluss-Screen: eigenes Navigations-Ziel auf dem Back-Stack. Back darauf ist `popBackStack` zurück zur Schritt-Ansicht und schreibt nichts (US-004.5). Der zuletzt angezeigte Schritt bleibt im ViewModel erhalten; wurde der Abschluss-Screen ohne vorherige Schritt-Ansicht geöffnet (Wiedereinstieg), ist das Ziel der erste Schritt in Montage-Reihenfolge.
- Sichtbarkeit von "Zum Abschluss": abgeleiteter State aus der Fortschritts-Query (`N == M`), kein eigenes Feld. Der Button ist eine Schritt-Aktion des Consumers neben "Eingebaut", nicht Teil von F-006.
- Foto-Karussell, Thumbnail-Leiste, Vollbild-Ansicht, Label-Anzeige am sichtbaren Foto sowie die Vor/Zurück-Navigation zwischen Schritten kommen aus dem gemeinsamen Modul F-006. F-004 nutzt es im Modus **"lesend-mit-Aktionen"**: Label sichtbar, aber nicht änderbar, keine Foto-Aufnahme, Schritt-Aktionen ("Eingebaut", "Zum Abschluss") beim Consumer.
- F-006 meldet Navigation und Foto-Wechsel nur als Callbacks; F-004 hält `aktuellerIndex` und `sichtbaresFotoIndex` im ViewModel und schreibt sie fort.
- Fotos skaliert laden (kein Full-Size), Thumbnails in Thumbnail-Größe dekodieren.

---

## Offene Fragen

Keine offenen Fragen.

**Entschieden:** F-004 nutzt F-006 im Modus **"lesend-mit-Aktionen"**. Die Label eines Fotos sind während der Montage sichtbar, aber nicht änderbar; Fotos werden in der Montage weder aufgenommen noch gelöscht. Label werden ausschließlich in der Demontage (F-003) gesetzt. Die einzige datenverändernde Aktion der Montage ist das Abhaken (`eingebautBeiMontage`) und die abschließende Archivierung.

**Entschieden:** Der Name "Zurück" gehört in F-004 ausschließlich der Schritt-Navigation aus F-006. Der Flow-Ausstieg ist die **Android-Zurück-Geste** und hat bewusst keinen gleichnamigen Button (US-004.6). Er führt zur Vorgangs-Übersicht (F-001), lässt den Fortschritt unverändert stehen und archiviert nicht — archiviert wird ausschließlich über den "Archivieren"-Button des Abschluss-Screens (US-004.5).
