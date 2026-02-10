# Workflow: Demontage-Flow

## States (= Views/Phasen)

| State | View | Beschreibung |
|-------|------|-------------|
| `KAMERA_BAUTEIL` | Kamera-View (Bauteil-Modus) | Vollbild-Kameravorschau fuer Bauteil-Foto |
| `PREVIEW_BAUTEIL` | Preview-View (Bauteil-Modus) | Bauteil-Foto Vorschau mit Bestaetigen/Wiederholen |
| `AUSGEBAUT` | Ausgebaut-View | Schrittnummer + Bauteil-Foto + "Ausgebaut"-Button |
| `DIALOG` | Dialog-View | 3 Optionen: Ablageort / Weiter / Beenden |
| `KAMERA_ABLAGEORT` | Kamera-View (Ablageort-Modus) | Kameravorschau mit Ablageort-Banner |
| `PREVIEW_ABLAGEORT` | Preview-View (Ablageort-Modus) | Ablageort-Foto Vorschau mit Bestaetigen/Wiederholen |

## Transitions

| Von | Event | Nach | Bedingung | DB-Aktion |
|-----|-------|------|-----------|-----------|
| -- (Entry) | Flow starten | `KAMERA_BAUTEIL` | Reparaturvorgang ist OFFEN | `Schritt` anlegen: `schrittNummer`, `gestartetAm` |
| `KAMERA_BAUTEIL` | Ausloeser-Tap | `PREVIEW_BAUTEIL` | Debounce 300ms | Foto in `photos/temp/` speichern |
| `PREVIEW_BAUTEIL` | "Wiederholen" | `KAMERA_BAUTEIL` | -- | Temp-Datei loeschen |
| `PREVIEW_BAUTEIL` | "Bestaetigen" | `AUSGEBAUT` | -- | Foto nach `photos/` verschieben, `bauteilFotoPfad` setzen |
| `AUSGEBAUT` | "Ausgebaut"-Tap | `DIALOG` | Debounce 300ms | -- |
| `DIALOG` | "Ablageort fotografieren" | `KAMERA_ABLAGEORT` | -- | `typ = AUSGEBAUT` setzen |
| `DIALOG` | "Weiter ohne Ablageort" | `KAMERA_BAUTEIL` | -- | `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, neuen `Schritt` anlegen (N+1) |
| `DIALOG` | "Beenden" | Uebersicht (F-001) | -- | `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen |
| `KAMERA_ABLAGEORT` | Ausloeser-Tap | `PREVIEW_ABLAGEORT` | Debounce 300ms | Foto in `photos/temp/` speichern |
| `PREVIEW_ABLAGEORT` | "Wiederholen" | `KAMERA_ABLAGEORT` | -- | Temp-Datei loeschen |
| `PREVIEW_ABLAGEORT` | "Bestaetigen" | `KAMERA_BAUTEIL` | -- | Foto nach `photos/` verschieben, `ablageortFotoPfad` setzen, `abgeschlossenAm` setzen, neuen `Schritt` anlegen (N+1) |

## Navigations-Diagramm

```
Kamera (Schritt N)
  -> Preview
    -> [Bestaetigen] -> Ausgebaut-Screen (Schrittnummer gross + Bauteil-Foto)
      -> [Ausgebaut] -> Dialog
        -> [Ablageort fotografieren] -> typ=AUSGEBAUT -> Kamera (Ablageort) -> Preview
          -> [Bestaetigen] -> Kamera (Schritt N+1)
        -> [Weiter ohne Ablageort] -> typ=AM_FAHRZEUG -> Kamera (Schritt N+1)
        -> [Beenden] -> typ=AM_FAHRZEUG -> Uebersicht (F-001)
    -> [Wiederholen] -> Kamera (Schritt N, erneut)

Back-Taste: Blockiert im gesamten Flow (-> "Beenden" ueber Dialog)
```

## Schrittnummer-Logik

- `schrittNummer` wird beim Anlegen des `Schritt`-Entity gesetzt (= letzte Nummer + 1)
- Wird von der DB geladen beim Fortsetzen (letzte `schrittNummer` + 1, bzw. aktuelle Nummer bei unterbrochenem Schritt)
- Inkrementiert unabhaengig davon ob Ablageort-Foto gemacht wurde
- Keine manuelle Eingabe moeglich, rein auto-increment
- Keine Obergrenze

### Inkrementierung

Die Schrittnummer wird inkrementiert wenn:
1. "Weiter ohne Ablageort" gewaehlt wird -> neuer Schritt N+1
2. Ablageort-Foto bestaetigt wird -> neuer Schritt N+1

Die Schrittnummer wird **nicht** inkrementiert wenn:
- "Beenden" gewaehlt wird (kein neuer Schritt)

### Aus US-003.4 AK 4: Schrittnummer nach Abschluss

- **Given** der Mechaniker hat einen Schritt abgeschlossen (Dialog-Auswahl getroffen)
  **When** die Kamera fuer den naechsten Schritt oeffnet
  **Then** zeigt der Schrittzaehler die um 1 erhoehte Nummer

### Aus US-003.4 AK 5: Inkrementierung ohne Ablageort

- **Given** der Mechaniker hat "Weiter ohne Ablageort" gewaehlt (kein Ablageort-Foto)
  **When** die Kamera fuer den naechsten Schritt oeffnet
  **Then** ist die Schrittnummer trotzdem inkrementiert

## Timestamp-Semantik

| Feld | Wird gesetzt wenn... | Bedeutung |
|------|---------------------|-----------|
| `gestartetAm` | Kamera fuer diesen Schritt oeffnet sich | Beginn der Arbeit am Schritt |
| `abgeschlossenAm` | Dialog-Auswahl getroffen ("Weiter"/"Beenden") ODER Ablageort-Foto bestaetigt | Schritt vollstaendig dokumentiert |

**Sonderfall:** Wenn der Mechaniker die App nach Foto-Bestaetigung aber vor Dialog-Auswahl schliesst, ist `abgeschlossenAm = null` und `typ = null`. Beim Fortsetzen wird der Ausgebaut-Screen fuer diesen Schritt erneut angezeigt.

## Foto-Flow Logik

1. **Schritt anlegen:** Kamera oeffnet sich -> `Schritt`-Entity in DB anlegen mit `schrittNummer` und `gestartetAm`, `bauteilFotoPfad = null`, `typ = null`
2. **Foto aufnehmen:** Kamera -> `takePicture()` -> temporaere Datei in `photos/temp/`
3. **Preview anzeigen:** Bild laden, in Preview-Composable darstellen
4. **Wiederholen:** Temporaere Datei loeschen, zurueck zu Schritt 2
5. **Bestaetigen:**
   - Datei von `photos/temp/` nach `photos/` verschieben (rename)
   - `bauteilFotoPfad` im `Schritt`-Entity per Update setzen
   - Weiter zu Ausgebaut-Screen
6. **Dialog-Auswahl:** `typ` und `abgeschlossenAm` setzen, dann:
   - "Ablageort fotografieren" -> `typ = AUSGEBAUT`, Kamera im Ablageort-Modus
   - "Weiter ohne Ablageort" -> `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, naechsten Schritt starten
   - "Beenden" -> `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, zurueck zur Uebersicht

## Sofort-Save Strategie

- **Schritt-Entity:** Wird beim Kamera-Oeffnen sofort in DB angelegt (mit `gestartetAm`, ohne `bauteilFotoPfad`, `typ = null`)
- **Bauteil-Foto:** Wird beim Bestaetigen von `photos/temp/` nach `photos/` verschoben und Pfad in DB aktualisiert
- **Ablageort-Foto:** Wird beim Bestaetigen von `photos/temp/` nach `photos/` verschoben und Pfad in DB aktualisiert
- **Typ:** Wird bei Dialog-Auswahl sofort in DB geschrieben (`AUSGEBAUT` oder `AM_FAHRZEUG`)
- **Abschluss:** `abgeschlossenAm` wird bei Dialog-Auswahl bzw. nach Ablageort-Foto sofort in DB geschrieben
- **Unterbrechung:** Schritt ohne `abgeschlossenAm` wird beim naechsten Start erkannt und fortgesetzt
- **Orphaned Schritte:** Unterbrochene Schritte bleiben in der DB und werden beim Fortsetzen weiterbearbeitet (kein Loeschen)
- **Orphaned Fotos:** Temporaere Dateien in `photos/temp/` werden beim App-Start aufgeraeumt

## Entry-Bedingungen

Der Demontage-Flow wird gestartet:
- Aus der Vorgangs-Uebersicht (F-001): Mechaniker tippt auf Vorgang und waehlt "Demontage starten" / "Demontage fortsetzen"
- Direkt nach Vorgang-Anlage (F-002): Automatischer Uebergang in den Demontage-Flow

### Aus US-003.4 AK 6: Fortsetzung nach Unterbrechung

- **Given** der Demontage-Flow wurde bei Schritt 5 unterbrochen (App geschlossen)
  **When** der Mechaniker den Vorgang erneut oeffnet und die Demontage fortsetzt
  **Then** prueft die App, ob Schritt 5 abgeschlossen ist (`abgeschlossenAm` vorhanden):
  - **Falls ja:** Kamera oeffnet sich fuer Schritt 6
  - **Falls nein:** Ausgebaut-Screen fuer Schritt 5 wird angezeigt (Schritt fortsetzen)

### Aus US-003.5 AK 2: Alle Schritte sichtbar nach Beenden

- **Given** der Mechaniker hat 5 Schritte dokumentiert und die Demontage beendet
  **When** die Vorgangs-Uebersicht angezeigt wird
  **Then** sind alle 5 Schritte mit Fotos im Vorgang sichtbar

### Aus US-003.5 AK 3: Fortsetzung mit korrekter Nummer

- **Given** die Demontage wurde beendet und die Uebersicht zeigt 5 Schritte
  **When** der Mechaniker den Vorgang erneut antippt und "Demontage fortsetzen" waehlt
  **Then** oeffnet sich die Kamera fuer Schritt 6
  **And** die Schrittnummer zeigt "Schritt 6"

## App-Unterbrechungs-Verhalten

| Unterbrechung bei... | Persistierter Zustand | Verhalten beim Fortsetzen |
|----------------------|----------------------|--------------------------|
| Kamera offen, kein Foto | `Schritt` in DB (ohne Foto, `typ = null`) | Kamera oeffnet sich erneut fuer diesen Schritt |
| Preview angezeigt | `Schritt` in DB (ohne Foto, `typ = null`), temp. Datei | Temp-Datei cleanup, Kamera oeffnet sich erneut |
| Ausgebaut-Screen | `Schritt` in DB (mit Foto, `typ = null`, ohne `abgeschlossenAm`) | Ausgebaut-Screen wird angezeigt |
| Dialog offen | `Schritt` in DB (mit Foto, `typ = null`, ohne `abgeschlossenAm`) | Ausgebaut-Screen wird angezeigt |
| Ablageort-Kamera | `Schritt` in DB (mit Bauteil-Foto, `typ = AUSGEBAUT`, ohne Ablageort, ohne `abgeschlossenAm`) | Ausgebaut-Screen wird angezeigt |

## Back-Navigation (US-003.6)

### AK 1: Back-Taste blockiert

- **Given** der Demontage-Flow ist aktiv (Kamera, Preview, Ausgebaut-Screen oder Dialog)
  **When** der Mechaniker die Android-Zurueck-Taste drueckt
  **Then** passiert nichts (Back-Geste wird abgefangen und ignoriert)

### AK 2: Regulaerer Weg ueber Dialog

- **Given** der Demontage-Flow ist aktiv
  **When** der Mechaniker die Demontage verlassen moechte
  **Then** muss er den regulaeren Weg ueber den Dialog -> "Beenden" nehmen

**Hinweis:** Die Back-Blockierung gilt fuer den gesamten Demontage-Flow. Der einzige Weg zurueck zur Uebersicht ist ueber "Beenden" im Dialog (US-003.2).

### Technisches Detail

- Navigation ueber `NavController` (Jetpack Navigation)
- State Hoisting: ViewModel haelt State, Screens sind stateless
- `BackHandler` in allen Demontage-Screens: `onBack = { /* nichts */ }`
