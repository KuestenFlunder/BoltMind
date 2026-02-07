# BoltMind - Architecture Overview

## 1. Einführung

### Vision

BoltMind ist eine Android-App für Kfz-Mechaniker in Autowerkstätten. Sie unterstützt beim systematischen Auseinander- und Zusammenbau von Fahrzeugkomponenten im Rahmen von Reparaturen.

### Kernidee

- Beim **Auseinanderbau** erstellt der Mechaniker eine Foto-Historie mit Teilreferenzen - Schritt für Schritt wird dokumentiert, welches Teil wo war und wo es abgelegt wird.
- Beim **Zusammenbau** wird die Historie rückwärts abgespielt, sodass nichts vergessen wird und jedes Teil wiedergefunden werden kann.
- Die App verwaltet **Ablageorte** für ausgebaute Teile, damit diese beim Zusammenbau schnell lokalisiert werden können.

### Zielgruppe

Kfz-Mechaniker in Autowerkstätten, die Reparaturen mit vielen Einzelteilen durchführen und eine visuelle Dokumentation des Demontageprozesses benötigen.

## 2. Domäne

> Dieser Abschnitt wird im Rahmen der Spec-Erarbeitung weiter ausgearbeitet.

### Zentrale Begriffe (vorläufig)

| Begriff | Beschreibung |
|---------|-------------|
| **Reparaturvorgang** | Ein abgeschlossener Reparaturauftrag an einem Fahrzeug |
| **Schritt** | Ein einzelner Demontage-/Montageschritt mit Foto und Teilreferenz |
| **Teil** | Eine Fahrzeugkomponente die aus-/eingebaut wird |
| **Ablageort** | Von der App verwalteter Ort, an dem ausgebaute Teile abgelegt werden |
| **Historie** | Chronologische Abfolge aller Schritte eines Reparaturvorgangs |

### Offene Fragen

- Wie granular sind die Schritte? (pro Teil, pro Baugruppe, frei wählbar?)
- Gibt es wiederkehrende Teile/Abläufe die als Vorlage dienen können?
- Arbeiten mehrere Mechaniker am selben Vorgang?
- Wie werden Ablageorte definiert und verwaltet? (Foto, Text, QR-Code?)
- Gibt es eine Anbindung an bestehende Werkstatt-Software?

## 3. Quality Goals

> Werden nach Verständnis der Domäne und der Flows abgeleitet.

## 4. Randbedingungen

### Technisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Plattform | Android (Kotlin, Jetpack Compose) |
| Min SDK | API 26 (Android 8.0) |
| Kamera | CameraX für Fotoaufnahme |
| UI Framework | Jetpack Compose mit Material3 |

### Organisatorisch

| Randbedingung | Beschreibung |
|---------------|-------------|
| Entwicklung | Spec-driven, iterativ in Sprints |
| Specs | Markdown-Dateien in `docs/specs/`, referenziert durch GitHub Issues |
| Versionierung | Git, GitHub |

## 5. Spec-Referenz

Feature-Specs werden in `docs/specs/` abgelegt und folgen dem Schema:

- `F-XXX-name.md` - Funktionale Feature-Specs
- `NF-XXX-name.md` - Nicht-funktionale Specs

GitHub Issues referenzieren Specs im Titel: `[F-001] Beschreibung`
