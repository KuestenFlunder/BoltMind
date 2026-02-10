# Dialog-View

## Zweck

Zeigt drei gleichwertige Optionen nach dem "Ausgebaut"-Tap: "Ablageort fotografieren", "Weiter ohne Ablageort" und "Beenden". Die Auswahl bestimmt den Schritt-Typ (`AUSGEBAUT` oder `AM_FAHRZEUG`) und den weiteren Flow-Verlauf.

## UI-Elemente

- **Dialog mit 3 gleichwertigen Buttons:**
  1. "Ablageort fotografieren"
  2. "Weiter ohne Ablageort"
  3. "Beenden"
- **Alle Optionen gleichwertig dargestellt** (gleiche Button-Groesse, kein Default hervorgehoben)

## Verhalten + DB-Interaktion

### Aus US-003.2 AK 2: Dialog oeffnet sich

- **Given** der Arbeitsphase-Screen wird angezeigt
  **When** der Mechaniker "Ausgebaut" antippt
  **Then** oeffnet sich ein Dialog mit 3 gleichwertigen Optionen:
  1. "Ablageort fotografieren"
  2. "Weiter ohne Ablageort"
  3. "Beenden"
  **And** der "Ausgebaut"-Button ist fuer 300ms nach dem Tap deaktiviert (Debounce)

### Aus US-003.2 AK 3: Ablageort fotografieren

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Ablageort fotografieren" waehlt
  **Then** wird `typ` im aktuellen `Schritt`-Entity auf `AUSGEBAUT` gesetzt
  **And** die Preview-View im Ablageort-Modus oeffnet sich (US-003.3)

### Aus US-003.2 AK 4: Weiter ohne Ablageort

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Weiter ohne Ablageort" waehlt
  **Then** wird `typ` im aktuellen `Schritt`-Entity auf `AM_FAHRZEUG` gesetzt
  **And** `abgeschlossenAm` im aktuellen `Schritt`-Entity wird gesetzt
  **And** die Preview-View oeffnet sich fuer das naechste Bauteil (US-003.1)
  **And** die Schrittnummer wurde inkrementiert

### Aus US-003.2 AK 5: Beenden

- **Given** der Dialog wird angezeigt
  **When** der Mechaniker "Beenden" waehlt
  **Then** wird `typ` im aktuellen `Schritt`-Entity auf `AM_FAHRZEUG` gesetzt
  **And** `abgeschlossenAm` im aktuellen `Schritt`-Entity wird gesetzt
  **And** die Demontage-Ansicht schliesst sich
  **And** die Vorgangs-Uebersicht wird angezeigt (F-001)

### Aus US-003.5 AK 1: Beenden markiert Schritt als abgeschlossen

- **Given** der Dialog nach "Ausgebaut" wird angezeigt
  **When** der Mechaniker "Beenden" waehlt
  **Then** wird der aktuelle Schritt als abgeschlossen markiert (`typ` + `abgeschlossenAm`)
  **And** die Demontage-Ansicht schliesst sich
  **And** die Vorgangs-Uebersicht (F-001) wird angezeigt

## DB-Interaktion

| Auswahl | DB-Operationen |
|---------|---------------|
| "Ablageort fotografieren" | `typ = AUSGEBAUT` sofort in DB schreiben |
| "Weiter ohne Ablageort" | `typ = AM_FAHRZEUG` + `abgeschlossenAm` setzen |
| "Beenden" | `typ = AM_FAHRZEUG` + `abgeschlossenAm` setzen |

## Typische Nutzungsmuster

- **Teil wird ausgebaut und abgelegt** (haeufigster Fall): Mechaniker waehlt "Ablageort fotografieren" -> `typ = AUSGEBAUT`
- **Teil bleibt am Fahrzeug** (z.B. geloester Stecker, zur Seite gelegtes Kabel): Mechaniker waehlt "Weiter ohne Ablageort" -> `typ = AM_FAHRZEUG`

## Nicht-funktionale Anforderungen

- **Debounce:** 300ms fuer alle Dialog-Buttons
- **Gleichwertige Darstellung:** Keine Option darf visuell hervorgehoben sein (gleiche Groesse, gleicher Style)

## Technische Hinweise

- **Dialog-Layout:** Alle 3 Optionen gleichwertig (gleiche Button-Groesse, kein Default)
- **Sofort-Save:** `typ` wird sofort bei Auswahl in DB geschrieben (Sofort-Save Strategie)
