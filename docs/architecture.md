# BoltMind - Architecture Overview

## 1. Einführung

### Vision

BoltMind ist eine Android-App für Kfz-Mechaniker in Autowerkstätten. Sie unterstützt beim systematischen Auseinander- und Zusammenbau von Fahrzeugkomponenten im Rahmen von Reparaturen.

### Kernidee

- Beim **Auseinanderbau** erstellt der Mechaniker eine Foto-Historie mit Teilreferenzen - Schritt für Schritt wird dokumentiert, welches Teil wo war und wo es abgelegt wird.
- Beim **Zusammenbau** wird die Historie rückwärts abgespielt, sodass nichts vergessen wird und jedes Teil wiedergefunden werden kann.
- Die App verwaltet **Ablageorte** für ausgebaute Teile, damit diese beim Zusammenbau schnell lokalisiert werden können.
- Im Hintergrund wird die **Zeiterfassung** pro Schritt mitgeloggt, um später Arbeitsprozesse analysieren und besser timen zu können.

### Zielgruppe

Kfz-Mechaniker in Autowerkstätten, die Reparaturen mit vielen Einzelteilen durchführen und eine visuelle Dokumentation des Demontageprozesses benötigen. Jeder Mechaniker nutzt sein eigenes Werkstatt-Handy.

## 2. Domäne

### Zentrale Begriffe

| Begriff | Beschreibung |
|---------|-------------|
| **Reparaturvorgang** | Ein Reparaturauftrag an einem Fahrzeug. Enthält Fahrzeug-Bezeichnung, Auftragsnummer, Beschreibung. Mehrere Vorgänge können gleichzeitig offen sein. |
| **Schritt** | Ein einzelner Demontage-Schritt: Foto (Zustand vor Ausbau) + Ablageort. Die Granularität bestimmt der Mechaniker selbst. Jeder Schritt hat einen Zeitstempel. |
| **Ablageort** | Physischer Ort (Werkbank, Tisch etc.) mit Nummer oder QR-Code. Wird vom Mechaniker selbst eingerichtet. Ist fest mit dem Schritt verknüpft. |
| **Historie** | Chronologische Abfolge aller Schritte eines Reparaturvorgangs. Kann vorwärts (Demontage) und rückwärts (Montage) durchlaufen werden. |
| **Archiv** | Abgeschlossene Reparaturvorgänge werden archiviert und bleiben einsehbar. |

### Demontage-Flow (Auseinanderbau)

```
Mechaniker startet neuen Reparaturvorgang
  → Initialdialog: Fahrzeug, Auftragsnummer, Beschreibung
  → Schritt-Schleife:
      1. Foto der Baugruppe (eingebauter Zustand)
      2. Mechaniker baut Teil aus (keine App-Interaktion)
      3. Mechaniker trägt Ablageort ein (MVP: Nummer, Final: QR-Scan)
      4. Eingabe des Ablageorts öffnet automatisch den nächsten Schritt
  → Zeiterfassung läuft pro Schritt im Hintergrund mit
```

### Montage-Flow (Zusammenbau)

```
Mechaniker öffnet Reparaturvorgang im Montage-Modus
  → Historie wird rückwärts angezeigt (letzter Schritt zuerst)
  → Pro Schritt: Foto + zugehöriger Ablageort sichtbar
  → Mechaniker hakt erledigte Schritte ab
  → Fortschrittsanzeige zeigt verbleibende Schritte
```

### MVP vs. Final

| Aspekt | MVP | Final |
|--------|-----|-------|
| Ablageort-Eingabe | Nummer (Freitext/Vergabe durch Mechaniker) | QR-Code-Sticker scannen |
| Ablageort-Registrierung | Mechaniker vergibt Nummern manuell | QR-Sticker auf Werkbank kleben und per Foto registrieren |
| Datenhaltung | Lokal auf dem Gerät | Lokal + Sharing zwischen Mechanikern |
| Notizen pro Schritt | Nur Foto + Ablageort | Ggf. Text-/Sprachnotiz |
| Zeiterfassung | Sichtbar, pro Schritt | Analyse-Dashboard |

### Entschiedene Fragen

| Frage | Entscheidung |
|-------|-------------|
| Vorlagen für wiederkehrende Abläufe? | Nicht im Scope - Reparaturen sind zu individuell (mechaniker- und fahrzeugspezifisch) |
| Anbindung an Werkstatt-Software? | MVP: Standalone. Integration eventuell später |
| Sharing-Mechanismus? | MVP: keins. Probephase: Bluetooth. Vermarktung: Cloud (Firebase o.ä.) |
| Datensicherung bei Geräteverlust? | MVP: Kein Backup, Risiko akzeptiert |

## 3. Quality Goals

| Priorität | Qualitätsziel | Motivation |
|-----------|--------------|------------|
| 1 | **Bedienbarkeit unter Werkstatt-Bedingungen** | Mechaniker haben dreckige/ölige Hände, wenig Zeit, arbeiten im Stehen. Die App muss mit minimalen Interaktionen bedienbar sein - kein langes Tippen, große Buttons, schnelle Kamera. |
| 2 | **Zuverlässigkeit** | Fotos und Ablageort-Zuordnungen dürfen während eines laufenden Reparaturvorgangs niemals verloren gehen. Ein verlorener Schritt kann dazu führen, dass Teile nicht wiedergefunden werden. |
| 3 | **Performance** | Kamera muss sofort auslösen, Schrittübergänge ohne Wartezeit. Die App darf den Arbeitsfluss des Mechanikers nicht bremsen - jede Sekunde Verzögerung stört den Reparaturprozess. |

## 4. Cross-Cutting Concerns

### Persistenz

- **Metadaten** (Reparaturvorgang, Schritte, Ablageorte, Zeitstempel): Room-Datenbank
- **Fotos**: Filesystem (App-interner Speicher), Referenz in der DB
- **Speicherstrategie**: Sofort-Persistierung - jedes Foto und jeder Ablageort wird unmittelbar gespeichert, nicht erst am Schrittende. Kein Datenverlust bei App-Crash, Anruf oder Unterbrechung.

### Foto-Qualität & Speicherplatz

- Mittlere Qualität: Komprimiert, aber Details erkennbar (ca. 2-3 MB pro Foto)
- Ausreichend für Baugruppen-Erkennung und Schrauben-Identifikation
- Kompression beim Speichern, nicht bei der Vorschau

### App-Lifecycle

- Werkstatt-Umgebung: Unterbrechungen (Anrufe, Kollegen, Handy weglegen) sind der Normalfall
- Sofort-Save garantiert, dass kein halbfertiger Schritt verloren geht
- App-Neustart setzt nahtlos am letzten Stand fort

### Datenlöschung

- Mechaniker kann Reparaturvorgänge händisch löschen (offen und archiviert)
- Kaskadierend: Alle zugehörigen Schritte, Fotos und Metadaten werden mitgelöscht
- Bestätigungsdialog vor Löschung (Werkstatt-Bedingungen: kein versehentliches Löschen)

### Permission-Handling

- Kamera-Permission: Erforderlich für Kernfunktion, freundliche Erklärung bei Verweigerung
- Speicher-Permission: READ_MEDIA_IMAGES (API 33+), READ_EXTERNAL_STORAGE (Fallback < API 33)
- Keine App-Nutzung ohne Kamera-Berechtigung möglich (Kern-Feature)

## 5. Randbedingungen

### Technisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Plattform | Android (Kotlin, Jetpack Compose) |
| Min SDK | API 26 (Android 8.0) |
| Kamera | CameraX für Fotoaufnahme |
| UI Framework | Jetpack Compose mit Material3 |
| Datenhaltung MVP | Lokal auf dem Gerät |
| Geräte | Werkstatt-Handys (ein Gerät pro Mechaniker) |

### Organisatorisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Entwicklung | Spec-driven, iterativ in Sprints |
| Specs | Markdown-Dateien in `docs/specs/`, referenziert durch GitHub Issues |
| Versionierung | Git, GitHub |

## 6. Spec-Referenz

Feature-Specs werden in `docs/specs/` abgelegt und folgen dem Schema:

- `F-XXX-name.md` - Funktionale Feature-Specs
- `NF-XXX-name.md` - Nicht-funktionale Specs

GitHub Issues referenzieren Specs im Titel: `[F-001] Beschreibung`
