# F-004: Montage-Flow

## Kontext

Der Montage-Flow ist das Gegenstück zum Demontage-Flow (F-003). Der Mechaniker hat ein Fahrzeug demontiert und möchte es nun wieder zusammenbauen. Die App zeigt die Demontage-Schritte in umgekehrter Reihenfolge an, sodass der Mechaniker Schritt für Schritt das Fahrzeug wieder aufbaut.

**Problem:** Nach Tagen oder Wochen erinnert sich der Mechaniker nicht mehr an die Reihenfolge und die Ablageorte der ausgebauten Teile. Er braucht eine visuelle Anleitung für den Zusammenbau.

**Lösung:** Die Demontage-Dokumentation wird rückwärts abgespielt. Pro Schritt sieht der Mechaniker das Bauteil-Foto (wie sieht das Teil montiert aus?), optional das Ablageort-Foto (wo liegt es?) und die Schrittnummer. Erledigte Schritte werden abgehakt.

**Primärer Nutzer:** Mechaniker in der Werkstatt, der ein zuvor demontiertes Fahrzeug wieder zusammenbaut.

**Situation:** Der Mechaniker steht am Fahrzeug, hat die ausgebauten Teile auf nummerierten Ablageplätzen liegen und möchte wissen: Welches Teil kommt als nächstes, und wo liegt es?

### Zusammenspiel mit Schrittnummer (F-003)

Die Schrittnummer aus der Demontage dient im Montage-Flow als Orientierung:
- **Fortschritt:** "Schritt 3 von 15" zeigt, wie weit der Zusammenbau ist
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
  - Bauteil-Foto (groß, Details erkennbar)
  - Ablageort-Foto (falls vorhanden, kleiner als Bauteil-Foto)
  - Schrittnummer (prominent, z.B. "Schritt 15")
  - Fortschrittsanzeige ("Schritt 1 von 15, noch 14")
  - "Eingebaut"-Button

- **Given** ein Schritt hat kein Ablageort-Foto (Teil blieb am Fahrzeug)
  **When** dieser Schritt angezeigt wird
  **Then** wird statt des Ablageort-Fotos der Hinweis "Am Fahrzeug" angezeigt

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

---

### US-004.3: Fortschritt anzeigen

**Als** Mechaniker
**möchte ich** sehen, wie weit der Zusammenbau fortgeschritten ist
**damit** ich den Überblick behalte und abschätzen kann, wie viel noch zu tun ist.

#### Akzeptanzkriterien

- **Given** der Montage-Flow ist geöffnet und 3 von 15 Schritten sind abgehakt
  **When** der aktuelle Schritt angezeigt wird
  **Then** zeigt die Fortschrittsanzeige "Schritt 4 von 15" und einen Fortschrittsbalken (20%)

- **Given** der Montage-Flow ist geöffnet
  **When** ein Schritt abgehakt wird
  **Then** aktualisiert sich der Fortschrittsbalken sofort

- **Given** alle 15 Schritte sind abgehakt
  **When** der letzte Schritt abgehakt wird
  **Then** wird der Abschluss-Screen angezeigt (US-004.6)

---

### US-004.4: Zwischen Schritten navigieren

**Als** Mechaniker
**möchte ich** frei zwischen Schritten blättern können (nicht nur linear)
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
  **When** der Mechaniker "Weiter" antippt
  **Then** passiert nichts (Button ist deaktiviert)

#### UI-Verhalten

- Swipe-Geste links/rechts als Alternative zu Buttons (natürliche Touch-Navigation)
- Große Touch-Targets für "Zurück"/"Weiter" (Werkstatt-Bedingungen)

---

### US-004.5: Zu Schrittnummer springen

**Als** Mechaniker
**möchte ich** direkt zu einer bestimmten Schrittnummer springen können
**damit** ich schnell den Schritt finde, der zu einem bestimmten Ablageort gehört (z.B. "Wo ist das Teil von Ablageort 7?").

#### Akzeptanzkriterien

- **Given** der Montage-Flow ist geöffnet
  **When** der Mechaniker auf die Schrittnummer-Anzeige tippt
  **Then** öffnet sich ein Nummernfeld (Numpad) zur Eingabe einer Schrittnummer

- **Given** das Nummernfeld ist geöffnet
  **When** der Mechaniker "7" eingibt und bestätigt
  **Then** springt die Ansicht direkt zu Schritt 7

- **Given** das Nummernfeld ist geöffnet
  **When** der Mechaniker eine Nummer eingibt, die nicht existiert (z.B. "20" bei 15 Schritten)
  **Then** wird ein Hinweis angezeigt ("Schritt 20 existiert nicht. Letzter Schritt: 15")

---

### US-004.6: Montage abschließen und archivieren

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

---

## UI-Skizzen

### Montage-Schritt (mit Ablageort-Foto)
```
┌─────────────────────────────┐
│  ← Bremsen vorne            │
│    Schritt 3 von 15         │
│    ████████░░░░░░  (20%)    │
├─────────────────────────────┤
│                             │
│   ┌─────────────────────┐   │
│   │                     │   │
│   │ [Foto: Bauteil      │   │
│   │  montiert]          │   │
│   │                     │   │
│   └─────────────────────┘   │
│                             │
│   Ablageort:                │
│   ┌────────────┐            │
│   │[Foto: Kiste]│  Nr. 13   │
│   └────────────┘            │
│                             │
│  ┌─────────────────────┐    │
│  │    ✓ EINGEBAUT       │    │
│  └─────────────────────┘    │
│                             │
│   ◄ Zurück    Weiter ►     │
└─────────────────────────────┘
```

### Montage-Schritt (ohne Ablageort-Foto)
```
┌─────────────────────────────┐
│  ← Bremsen vorne            │
│    Schritt 7 von 15         │
│    ████████████░░  (47%)    │
├─────────────────────────────┤
│                             │
│   ┌─────────────────────┐   │
│   │                     │   │
│   │ [Foto: Kabelbaum-   │   │
│   │  Stecker]           │   │
│   │                     │   │
│   └─────────────────────┘   │
│                             │
│   📍 Am Fahrzeug            │
│                             │
│  ┌─────────────────────┐    │
│  │    ✓ EINGEBAUT       │    │
│  └─────────────────────┘    │
│                             │
│   ◄ Zurück    Weiter ►     │
└─────────────────────────────┘
```

### Alle Schritte erledigt
```
┌─────────────────────────────┐
│  ← Bremsen vorne            │
│    Alle 15 Schritte erledigt│
│    ████████████████ (100%)  │
├─────────────────────────────┤
│                             │
│         ✓                   │
│   Zusammenbau               │
│   abgeschlossen!            │
│                             │
│  ┌─────────────────────┐    │
│  │    ARCHIVIEREN       │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

---

## Technische Hinweise

### Queries

```kotlin
// Schritte in Montage-Reihenfolge (absteigend nach schrittNummer)
@Query("SELECT * FROM schritt WHERE reparaturvorgangId = :vorgangId ORDER BY schrittNummer DESC")
fun beobachteSchritteAbsteigend(vorgangId: Long): Flow<List<Schritt>>

// Fortschritt
@Query("SELECT COUNT(*) FROM schritt WHERE reparaturvorgangId = :vorgangId AND eingebautBeiMontage = 1")
fun zaehleEingebauteSchritte(vorgangId: Long): Flow<Int>
```

### Entity-Feld für Abhaken

Das bestehende `Schritt`-Entity hat bereits das Feld `eingebautBeiMontage: Boolean = false`. Beim Abhaken wird dieses Feld per Update auf `true` gesetzt. Erneutes Antippen setzt es zurück auf `false`.

### Archivierung

```kotlin
// Vorgang archivieren
@Query("UPDATE reparaturvorgang SET status = 'ARCHIVIERT' WHERE id = :vorgangId")
fun archivieren(vorgangId: Long)
```

### Navigation-Logik

- **Montage-Reihenfolge:** Schritte werden nach `schrittNummer DESC` sortiert. Der Schritt mit der höchsten `schrittNummer` (letzter Demontage-Schritt) ist Montage-Schritt 1.
- **Fortschrittsanzeige:** "Schritt X von Y" wobei X = aktuelle Position in der Montage-Liste und Y = Gesamtanzahl Schritte.
- **Schrittnummer-Anzeige:** Die angezeigte Nummer ist die `schrittNummer` aus der Demontage (nicht die Montage-Position), damit sie mit physischen Ablageort-Labels korreliert.
- **Springen zu Schrittnummer:** Suche in der Schritt-Liste nach `schrittNummer = eingabe`.

### Foto-Anzeige

- **Bauteil-Foto (`bauteilFotoPfad`):** Groß dargestellt, nimmt den Hauptbereich ein. Zeigt das Bauteil im montierten Zustand (so wie es wieder eingebaut werden soll).
- **Ablageort-Foto (`ablageortFotoPfad`):** Kleiner dargestellt unterhalb des Bauteil-Fotos. Zeigt den physischen Ablageort (Kiste, Regal, Werkbank). Wird nur angezeigt wenn vorhanden (`!= null`).
- **Fallback:** Wenn `typ == AM_FAHRZEUG` (bzw. `ablageortFotoPfad == null`), wird stattdessen "Am Fahrzeug" angezeigt.
- **Fehlende Dateien:** Wenn die Foto-Datei nicht existiert (z.B. gelöscht), wird ein Platzhalter-Bild angezeigt.

### Sofort-Save

- Jedes Abhaken/Ent-Abhaken wird sofort per `dao.aktualisieren(schritt)` persistiert
- Archivierung wird sofort ausgeführt
- Kein Batch-Save, kein "Speichern"-Button

### Hinweis zu Entity-Änderungen (F-003-kern Alignment)

Die aktuelle Implementierung des `Schritt`-Entity weicht von der neuen F-003-kern Spec ab:

| Aktuelles Feld | Neues Feld (F-003-kern) | Änderung |
|----------------|------------------------|----------|
| `fotoPfad: String` | `bauteilFotoPfad: String?` | Umbenennung + nullable |
| `ablageortNummer: Int` | _(entfällt)_ | Entfällt, Schrittnummer übernimmt |
| _(nicht vorhanden)_ | `ablageortFotoPfad: String?` | Neues optionales Feld |
| `reihenfolge: Int` | `schrittNummer: Int` | Umbenennung (gleiche Semantik) |
| _(nicht vorhanden)_ | `typ: SchrittTyp?` | Neues Enum-Feld (AUSGEBAUT / AM_FAHRZEUG) |
| `tableName = "schritte"` | `tableName = "schritt"` | Singular |

Diese Änderungen müssen im Rahmen der F-003-Implementierung umgesetzt werden. F-004 baut auf dem aktualisierten Entity auf.

**SchrittTyp im Montage-Flow:**
- `AUSGEBAUT` → Ablageort-Foto wird angezeigt, farbliche Kennzeichnung als "abgelegt"
- `AM_FAHRZEUG` → Hinweis "Am Fahrzeug" statt Ablageort-Foto, farbliche Kennzeichnung als "am Fahrzeug"
- Der `typ` ermöglicht eine Schritt-Preview (Icon/Farbe) ohne alle Fotos laden zu müssen
