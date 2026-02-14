# F-004: Montage-Flow

## Kontext

Der Montage-Flow ist das Gegenstück zum Demontage-Flow (F-003). Der Mechaniker hat ein Fahrzeug demontiert und möchte es nun wieder zusammenbauen. Die App zeigt die Demontage-Schritte in umgekehrter Reihenfolge an, sodass der Mechaniker Schritt für Schritt das Fahrzeug wieder aufbaut.

**Problem:** Nach Tagen oder Wochen erinnert sich der Mechaniker nicht mehr an die Reihenfolge und die Ablageorte der ausgebauten Teile. Er braucht eine visuelle Anleitung für den Zusammenbau.

**Lösung:** Die Demontage-Dokumentation wird rückwärts abgespielt. Pro Schritt sieht der Mechaniker das Bauteil-Foto (wie sieht das Teil montiert aus?), optional das Ablageort-Foto (wo liegt es?) und die Schrittnummer in einer fortlaufenden reihe von Kreisen. Erledigte Schritte werden abgehakt. Je nachdem ob der Schritt einen Ablageort hat, wird der Kreis mit der Schrittnummer farblich markiert.

**Primärer Nutzer:** Mechaniker in der Werkstatt, der ein zuvor demontiertes Fahrzeug wieder zusammenbaut.

**Situation:** Der Mechaniker steht am Fahrzeug, hat die ausgebauten Teile auf nummerierten Ablageplätzen liegen und möchte wissen: Welches Teil kommt als nächstes, und wo liegt es?

### Zusammenspiel mit Schrittnummer (F-003)

Die Schrittnummer aus der Demontage dient im Montage-Flow als Orientierung:

- **Fortschritt:** Der jeweilige Schritt wird Mittig angezeigt, wobei die Schrittnummern in einer Fortlaufenden Reihe von Kreisen angezeigt werden.
- **Ablageort-Korrelation:** Hat der Mechaniker seine physischen Ablageorte mit Schrittnummern beschriftet, findet er über die Nummer das passende Teil
- **Schritte ohne Ablageort:** Manche Schritte haben kein Ablageort-Foto (Teil blieb am Fahrzeug). Diese werden mit dem Hinweis "Am Fahrzeug" angezeigt

---

## User Stories

### US-004.1: Schritte in Montage-Reihenfolge anzeigen

**Als** Mechaniker
**möchte ich** die Demontage-Schritte in umgekehrter Reihenfolge sehen
**damit** ich die Teile in der richtigen Reihenfolge wieder einbauen kann (letztes ausgebautes Teil = erstes einzubauendes Teil).

#### Akzeptanzkriterien

- **Given** ein Reparaturvorgang mit 15 dokumentierten Demontage-Schritten existiert
  **When** der Mechaniker "Montage starten" wählt (F-001)
  **Then** wird der Montage-Flow geöffnet und zeigt den letzten Demontage-Schritt zuerst (Schritt 15)

- **Given** der Montage-Flow ist geöffnet
  **When** der erste Schritt angezeigt wird
  **Then** sind folgende Informationen sichtbar:
  - Bauteil-Foto und Ablageort Foto untereinander
  - die Fotos können mit je einem Touch vergrößert werden
  - Schrittnummer in fortlauf Kette von Kreisen, die mit den Schritten weiter wandert (animier)
  - "Eingebaut"-Button

- **Given** ein Schritt hat kein Ablageort-Foto (Teil blieb am Fahrzeug)
  **When** dieser Schritt angezeigt wird
  **Then** wird statt des Ablageort-Fotos der Hinweis "Am Fahrzeug" angezeigt
  **AND** wird der Kreis mit der Schrittnummer in einer anderen Farbe dargestellt

- **Given** ein Schritt hat ein Ablageort-Foto
  **When** dieser Schritt angezeigt wird
  **Then** wird das Ablageort-Foto unterhalb des Bauteil-Fotos angezeigt mit dem Label "Ablageort"

---

### US-004.2: Schritt als eingebaut markieren

**Als** Mechaniker
**möchte ich** erledigte Schritte als "eingebaut" abhaken
**damit** ich den Fortschritt verfolgen und weiß, welche Teile noch fehlen.

#### Akzeptanzkriterien

- **Given** ein Montage-Schritt wird angezeigt und ist noch nicht abgehakt
  **When** der Mechaniker den "Eingebaut"-Button antippt
  **Then** wird der Schritt als eingebaut markiert (`eingebautBeiMontage = true`)
  **And** die Markierung wird sofort in der DB persistiert
  **And** der nächste Schritt wird automatisch angezeigt

- **Given** ein Schritt wurde als eingebaut markiert
  **When** der Mechaniker zu diesem Schritt zurücknavigiert
  **Then** ist der "Eingebaut"-Button als bereits erledigt dargestellt (z.B. ausgegraut mit Häkchen)

- **Given** ein bereits abgehakter Schritt wird angezeigt
  **When** der Mechaniker den "Eingebaut"-Button erneut antippt
  **Then** wird die Markierung rückgängig gemacht (`eingebautBeiMontage = false`)
  **And** die Änderung wird sofort in der DB persistiert

- **Given** ein bereits abgehakter Schritt wird angezeigt
  **When** der Mechaniker den "Eingebaut"-Button erneut antippt
  **Then** erscheint ein Dialog mit einer Warnung, dass die Markierung rückgängig gemacht wird
  **And** die Änderung wird nach bestätigung sofort in der DB persistiert

---

### US-004.3: Fortschritt anzeigen

**Als** Mechaniker
**möchte ich** sehen, wie weit der Zusammenbau fortgeschritten ist
**damit** ich den Überblick behalte und abschätzen kann, wie viel noch zu tun ist.

#### Akzeptanzkriterien

- **Given** der Montage-Flow ist geöffnet die Schritte erscheinen wie an einer Perlenkette als header
  **When** der aktuelle Schritt angezeigt wird
  **Then** wird der Kreis mit der Schrittzahl in der Mitte angezeigt. Links stehen schon abgeschlossene, rechts noch kommende Schritte.

- **Given** der Montage-Flow ist geöffnet
  **When** ein Schritt abgehakt wird
  **Then** läuft die Kette animiert weiter

- **Given** alle 15 Schritte sind abgehakt
  **When** der letzte Schritt abgehakt wird
  **Then** wird der Abschluss-Screen angezeigt (US-004.6)

---

### US-004.4: Zwischen Schritten navigieren

**Als** Mechaniker
**möchte ich** frei zwischen Schritten navigieren können
**damit** ich nachschauen kann, was ich bereits eingebaut habe oder was als nächstes kommt.

#### Akzeptanzkriterien

- **Given** der Montage-Flow zeigt Schritt 5 von 15 an
  **When** der Mechaniker "Weiter" antippt
  **Then** wird Schritt 6 angezeigt (nächster einzubauender Schritt = nächst-niedrigere Demontage-Schrittnummer)

- **Given** der Montage-Flow zeigt Schritt 5 von 15 an
  **When** der Mechaniker "Zurück" antippt
  **Then** wird Schritt 4 angezeigt (vorheriger Schritt)

- **Given** der Montage-Flow zeigt den ersten Schritt an (Schritt 1 von 15)
  **When** der Mechaniker "Zurück" antippt
  **Then** passiert nichts (Button ist deaktiviert)

- **Given** der Montage-Flow zeigt den letzten Schritt an (Schritt 15 von 15)
  **When** die Montage abgeschlossen ist
  **Then** wird der Weiter button zu einem Abschlussbutton

- **Given** der Mechaniker den Abschlussbutton antippt
  **When** die Montage abgeschlossen ist
  **Then** wird er zur Übersicht navigiert und der abgschlossene Vorgang unter Archiv abgelegt

#### UI-Verhalten

- Swipe-Geste links/rechts als Alternative zu Buttons (natürliche Touch-Navigation)
- Große Touch-Targets für "Zurück"/"Weiter" (Werkstatt-Bedingungen)

---

### US-004.5: Zu Schrittnummer springen

**Als** Mechaniker
**möchte ich** nach Abschluss aller Schritte den Vorgang archivieren
**damit** der Vorgang aus der aktiven Liste verschwindet und im Archiv für spätere Referenz verfügbar ist.

#### Akzeptanzkriterien

- **Given** alle Schritte sind als eingebaut markiert
  **When** der letzte Schritt abgehakt wird
  **Then** wird der Abschluss-Screen angezeigt mit "Zusammenbau abgeschlossen!" und einem "Archivieren"-Button

- **Given** der Abschluss-Screen wird angezeigt
  **When** der Mechaniker "Archivieren" antippt
  **Then** wird der Reparaturvorgang-Status auf `ARCHIVIERT` gesetzt
  **And** die Vorgangs-Übersicht (F-001) wird angezeigt
  **And** der Vorgang erscheint im Archiv-Tab

- **Given** nicht alle Schritte sind abgehakt (z.B. 12 von 15)
  **When** der Mechaniker den Montage-Flow über den Zurück-Button verlässt
  **Then** bleibt der Fortschritt erhalten (alle Häkchen bleiben gesetzt)
  **And** bei erneutem Öffnen wird der erste nicht-abgehakte Schritt angezeigt

- **Given** der Abschluss-Screen wird angezeigt
  **When** der Mechaniker den Zurück-Button drückt (statt "Archivieren")
  **Then** kehrt er zum letzten Schritt zurück
  **And** der Vorgang wird **nicht** archiviert (Mechaniker kann nochmal prüfen)

---

## Nicht-funktionale Anforderungen

**Bedienbarkeit (Quality Goal #1):**

- Große Touch-Targets für "Eingebaut"-Button und Navigation (Handschuhe, dreckige Hände)
- Abhaken mit einem einzigen Tap
- Bauteil-Foto groß genug um Details zu erkennen (z.B. Schrauben-Positionen)
- Schrittnummer immer sichtbar und groß (Korrelation mit physischem Ablageort)

**Performance (Quality Goal #3):**

- Fotos werden skaliert geladen (nicht Full-Size in den Speicher)
- Wechsel zwischen Schritten ohne spürbare Verzögerung
- Swipe-Geste reagiert sofort

**Zuverlässigkeit (Quality Goal #2):**

- Jedes Abhaken wird sofort in der DB persistiert (Sofort-Save)
- App-Unterbrechung verliert keinen Fortschritt
- Bei erneutem Öffnen: Erster nicht-abgehakter Schritt wird angezeigt

**Fehlerbehandlung:**

- Fehlende Foto-Dateien (z.B. nach Backup/Restore): Platzhalter-Bild statt Crash
- Vorgang ohne Schritte: Hinweis "Keine Demontage-Schritte vorhanden. Zuerst demontieren."
