# Workflow: Demontage-Flow

## States (= Views/Phasen)

| State | View | Beschreibung |
|-------|------|-------------|
| `PREVIEW_BAUTEIL` | Preview-View (Bauteil-Modus) | Schrittnummer + "Foto aufnehmen"-Button, nach Aufnahme: Foto-Vorschau mit Bestaetigen/Wiederholen |
| `ARBEITSPHASE` | Arbeitsphase-View | Schrittnummer + Bauteil-Foto + "Ausgebaut"-Button |
| `DIALOG` | Dialog-View | 3 Optionen: Ablageort / Weiter / Beenden |
| `PREVIEW_ABLAGEORT` | Preview-View (Ablageort-Modus) | Ablageort-Hinweis + "Foto aufnehmen"-Button, nach Aufnahme: Banner "Ist das der Ablageort?" + Bestaetigen/Wiederholen |

**Hinweis:** Die System-Kamera ist kein eigener State. Sie wird per `ActivityResultContracts.TakePicture()` Intent aus der Preview-View aufgerufen und kehrt nach Aufnahme zur Preview zurueck.

## Transitions

| Von | Event | Nach | Bedingung | DB-Aktion |
|-----|-------|------|-----------|-----------|
| -- (Entry) | Flow starten | `PREVIEW_BAUTEIL` | Reparaturvorgang ist OFFEN | `Schritt` anlegen: `schrittNummer`, `gestartetAm` |
| `PREVIEW_BAUTEIL` | "Foto aufnehmen" | System-Kamera Intent | -- | -- |
| System-Kamera | Foto aufgenommen | `PREVIEW_BAUTEIL` (Vorschau-Zustand) | -- | Foto in `photos/temp/` speichern |
| System-Kamera | Abgebrochen | `PREVIEW_BAUTEIL` (Initial-Zustand) | -- | -- |
| `PREVIEW_BAUTEIL` | "Wiederholen" | System-Kamera Intent | -- | Temp-Datei loeschen |
| `PREVIEW_BAUTEIL` | "Bestaetigen" | `ARBEITSPHASE` | -- | Foto nach `photos/` verschieben, `bauteilFotoPfad` setzen |
| `ARBEITSPHASE` | "Ausgebaut"-Tap | `DIALOG` | Debounce 300ms | -- |
| `DIALOG` | "Ablageort fotografieren" | `PREVIEW_ABLAGEORT` | -- | `typ = AUSGEBAUT` setzen |
| `DIALOG` | "Weiter ohne Ablageort" | `PREVIEW_BAUTEIL` | -- | `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, neuen `Schritt` anlegen (N+1) |
| `DIALOG` | "Beenden" | Uebersicht (F-001) | -- | `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen |
| `PREVIEW_ABLAGEORT` | "Foto aufnehmen" | System-Kamera Intent | -- | -- |
| System-Kamera (Ablageort) | Foto aufgenommen | `PREVIEW_ABLAGEORT` (Vorschau-Zustand) | -- | Foto in `photos/temp/` speichern |
| System-Kamera (Ablageort) | Abgebrochen | `PREVIEW_ABLAGEORT` (Initial-Zustand) | -- | -- |
| `PREVIEW_ABLAGEORT` | "Wiederholen" | System-Kamera Intent | -- | Temp-Datei loeschen |
| `PREVIEW_ABLAGEORT` | "Bestaetigen" | `PREVIEW_BAUTEIL` | -- | Foto nach `photos/` verschieben, `ablageortFotoPfad` setzen, `abgeschlossenAm` setzen, neuen `Schritt` anlegen (N+1) |

## Navigations-Diagramm

```
Preview (Schritt N, Initial-Zustand) -- zeigt Schrittnummer, "Foto aufnehmen"-Button
  -> [Foto aufnehmen] -> System-Kamera Intent
    -> [Foto aufgenommen] -> Preview (Schritt N, Vorschau-Zustand)
      -> [Bestaetigen] -> Arbeitsphase-Screen (Schrittnummer gross + Bauteil-Foto)
        -> [Ausgebaut] -> Dialog
          -> [Ablageort fotografieren] -> typ=AUSGEBAUT -> Preview (Ablageort-Modus, Initial-Zustand)
            -> [Foto aufnehmen] -> System-Kamera Intent
              -> [Foto aufgenommen] -> Preview (Ablageort-Modus, Vorschau-Zustand)
                -> [Bestaetigen] -> Preview (Schritt N+1)
                -> [Wiederholen] -> System-Kamera erneut (bleibt in Preview Ablageort)
          -> [Weiter ohne Ablageort] -> typ=AM_FAHRZEUG -> Preview (Schritt N+1)
          -> [Beenden] -> typ=AM_FAHRZEUG -> Uebersicht (F-001)
      -> [Wiederholen] -> System-Kamera erneut (bleibt in Preview Bauteil)
    -> [Abgebrochen] -> Preview (Schritt N, Initial-Zustand)

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
  **When** die Preview-View fuer den naechsten Schritt oeffnet
  **Then** zeigt der Schrittzaehler die um 1 erhoehte Nummer

### Aus US-003.4 AK 5: Inkrementierung ohne Ablageort

- **Given** der Mechaniker hat "Weiter ohne Ablageort" gewaehlt (kein Ablageort-Foto)
  **When** die Preview-View fuer den naechsten Schritt oeffnet
  **Then** ist die Schrittnummer trotzdem inkrementiert

## Timestamp-Semantik

| Feld | Wird gesetzt wenn... | Bedeutung |
|------|---------------------|-----------|
| `gestartetAm` | Preview-View fuer diesen Schritt oeffnet sich | Beginn der Arbeit am Schritt |
| `abgeschlossenAm` | Dialog-Auswahl getroffen ("Weiter"/"Beenden") ODER Ablageort-Foto bestaetigt | Schritt vollstaendig dokumentiert |

**Sonderfall:** Wenn der Mechaniker die App nach Foto-Bestaetigung aber vor Dialog-Auswahl schliesst, ist `abgeschlossenAm = null` und `typ = null`. Beim Fortsetzen wird der Arbeitsphase-Screen fuer diesen Schritt erneut angezeigt.

## Foto-Flow Logik

1. **Schritt anlegen:** Preview-View oeffnet sich -> `Schritt`-Entity in DB anlegen mit `schrittNummer` und `gestartetAm`, `bauteilFotoPfad = null`, `typ = null`
2. **Foto aufnehmen:** "Foto aufnehmen"-Button -> System-Kamera Intent -> Foto in `photos/temp/`
3. **Vorschau anzeigen:** Rueckkehr von System-Kamera -> Bild in Preview-View im Vorschau-Zustand darstellen
4. **Wiederholen:** Temporaere Datei loeschen, System-Kamera erneut per Intent oeffnen
5. **Bestaetigen:**
   - Datei von `photos/temp/` nach `photos/` verschieben (rename)
   - `bauteilFotoPfad` im `Schritt`-Entity per Update setzen
   - Weiter zu Arbeitsphase-Screen
6. **Dialog-Auswahl:** `typ` und `abgeschlossenAm` setzen, dann:
   - "Ablageort fotografieren" -> `typ = AUSGEBAUT`, Preview-View im Ablageort-Modus
   - "Weiter ohne Ablageort" -> `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, naechsten Schritt starten
   - "Beenden" -> `typ = AM_FAHRZEUG`, `abgeschlossenAm` setzen, zurueck zur Uebersicht

## Sofort-Save Strategie

- **Schritt-Entity:** Wird beim Preview-View-Oeffnen sofort in DB angelegt (mit `gestartetAm`, ohne `bauteilFotoPfad`, `typ = null`)
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
  - **Falls ja:** Preview-View oeffnet sich fuer Schritt 6
  - **Falls nein:** Arbeitsphase-Screen fuer Schritt 5 wird angezeigt (Schritt fortsetzen)

### Aus US-003.5 AK 2: Alle Schritte sichtbar nach Beenden

- **Given** der Mechaniker hat 5 Schritte dokumentiert und die Demontage beendet
  **When** die Vorgangs-Uebersicht angezeigt wird
  **Then** sind alle 5 Schritte mit Fotos im Vorgang sichtbar

### Aus US-003.5 AK 3: Fortsetzung mit korrekter Nummer

- **Given** die Demontage wurde beendet und die Uebersicht zeigt 5 Schritte
  **When** der Mechaniker den Vorgang erneut antippt und "Demontage fortsetzen" waehlt
  **Then** oeffnet sich die Preview-View fuer Schritt 6
  **And** die Schrittnummer zeigt "Schritt 6"

## App-Unterbrechungs-Verhalten

| Unterbrechung bei... | Persistierter Zustand | Verhalten beim Fortsetzen |
|----------------------|----------------------|--------------------------|
| Preview im Initial-Zustand, kein Foto | `Schritt` in DB (ohne Foto, `typ = null`) | Preview-View oeffnet sich erneut fuer diesen Schritt (Initial-Zustand) |
| System-Kamera aktiv | `Schritt` in DB (ohne Foto, `typ = null`) | Preview-View oeffnet sich erneut fuer diesen Schritt (Initial-Zustand) |
| Preview im Vorschau-Zustand | `Schritt` in DB (ohne Foto, `typ = null`), temp. Datei | Temp-Datei cleanup, Preview-View oeffnet sich erneut (Initial-Zustand) |
| Arbeitsphase-Screen | `Schritt` in DB (mit Foto, `typ = null`, ohne `abgeschlossenAm`) | Arbeitsphase-Screen wird angezeigt |
| Dialog offen | `Schritt` in DB (mit Foto, `typ = null`, ohne `abgeschlossenAm`) | Arbeitsphase-Screen wird angezeigt |
| Ablageort-Preview (Initial oder System-Kamera) | `Schritt` in DB (mit Bauteil-Foto, `typ = AUSGEBAUT`, ohne Ablageort, ohne `abgeschlossenAm`) | Arbeitsphase-Screen wird angezeigt |

## Back-Navigation (US-003.6)

### AK 1: Back-Taste blockiert

- **Given** der Demontage-Flow ist aktiv (Preview-View, Arbeitsphase-Screen oder Dialog)
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
