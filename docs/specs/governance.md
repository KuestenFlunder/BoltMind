# Governance: Projektweite Spec-Regeln

Regeln und Invarianten, die fuer ALLE Feature-Specs gelten. Jede Feature-Spec erbt diese Regeln implizit. Aenderungen hier wirken sich auf alle Features aus.

## Sofort-Save Strategie

Jede Nutzeraktion, die Daten erzeugt oder veraendert, wird **sofort** in die DB geschrieben. Kein Batch-Save, kein "Speichern"-Button. Grund: Werkstatt-Umgebung — Unterbrechungen (Anrufe, Kollegen, Akku leer) sind der Normalfall, nicht die Ausnahme.

**Regel:** Wenn eine Aktion DB-relevant ist, wird sie in der gleichen Operation persistiert, in der sie ausgeloest wird. Kein Zwischenpuffer, kein "Speichern beim Verlassen".

## Bedienbarkeit

### Debounce

Global **300ms** fuer alle interaktiven Buttons. Grund: Mechaniker tragen Handschuhe und haben dreckige Haende — Doppel-Taps sind haeufig und muessen abgefangen werden.

### Touch-Targets

Alle primaeren Aktions-Buttons muessen mit Handschuhen bedienbar sein. Grosse Touch-Targets, ausreichend Abstand zwischen Buttons.

## Foto-Handling

### Speicherort

Fotos werden im **app-internen Speicher** abgelegt (`context.filesDir/photos/`). Nicht in der oeffentlichen Galerie. Grund: Datenschutz und Vermeidung versehentlicher Loeschung.

### Qualitaet

Mittlere Kompression, ca. 2-3 MB pro Foto. Balance zwischen Qualitaet (Schrauben-Positionen muessen erkennbar sein) und Speicherplatz.

### Fehlende Dateien

Wenn eine Foto-Datei fehlt (z.B. nach Backup/Restore oder manuellem Loeschen), wird ein **Platzhalter-Bild** angezeigt. Kein Crash, kein leerer Screen.

### Datensicherheit

- Keine Metadaten (GPS, Zeitstempel) in EXIF-Daten (Datenschutz)
- Fotos nur im app-internen Speicher, nicht extern zugaenglich

## Unterbrechungs-Verhalten

Die App muss jederzeit unterbrechbar sein, ohne Datenverlust. Beim naechsten Start wird der letzte konsistente Zustand wiederhergestellt. Jedes Feature definiert sein eigenes Unterbrechungs-Verhalten (welcher Screen wird bei Fortsetzung angezeigt), aber die Grundregel ist global:

**Regel:** Kein Datenverlust bei App-Unterbrechung. Sofort-Save + definierter Fortsetzungspunkt.

## DDD-Sprache

Domain-Begriffe auf **Deutsch**, technische Begriffe auf **Englisch**. Funktionsnamen fuer Domain-Events ebenfalls Deutsch.

| Domain (DE) | Bedeutung |
|---|---|
| Reparaturvorgang | Ein Reparaturauftrag an einem Fahrzeug |
| Schritt | Ein einzelner Demontage-/Montage-Schritt |
| SchrittTyp | AUSGEBAUT oder AM_FAHRZEUG |
| Ablageort | Physischer Ort, an dem ein ausgebautes Teil abgelegt wird |
| ZeitMessung | Eine Timer-Messung mit Start/Stopp (Service F-005) |

## Service-Architektur

Features, die als eigenstaendige Services implementiert werden (z.B. Zeiterfassung), folgen diesen Regeln:

1. **Eigene Datenhaltung:** Der Service besitzt seine eigene Tabelle. Keine Dual-Purpose-Felder in fremden Entities.
2. **Keine Feature-Kenntnis:** Der Service weiß nicht, welches Feature ihn aufruft. Er arbeitet mit generischen Referenzen (`referenzId`, `referenzTyp`).
3. **Consumer beschreibt Nutzung:** Die Integration wird in der Feature-Spec beschrieben, nicht in der Service-Spec.
4. **Klare Schnittstelle:** Der Service definiert sein Interface (start/stop/query). Consumer rufen dieses Interface auf.
