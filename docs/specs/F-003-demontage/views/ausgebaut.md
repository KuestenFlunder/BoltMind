# Ausgebaut-View

## Zweck

Zeigt die aktuelle Schrittnummer (gross und prominent) zusammen mit dem aufgenommenen Bauteil-Foto. Der "Ausgebaut"-Button fuehrt zum Dialog mit den naechsten Optionen. Im MVP ist dieser Screen der Platzhalter fuer die spaetere Arbeitsphase (Zeiterfassung, Kommentare, Pausieren -- siehe `F-003-arbeitsphase-ideen.md`).

## UI-Elemente

- **Schrittnummer** (gross, prominent, deutlich lesbar -- damit der Mechaniker die Nummer auf ein physisches Label uebertragen kann)
- **Bauteil-Foto** (das bestaetigte Foto aus der Preview-View)
- **"Ausgebaut"-Button** (grosser Touch-Target, fuer dreckige Haende/Handschuhe geeignet)

## Verhalten + DB-Interaktion

### Aus US-003.2 AK 1: Ausgebaut-Screen Anzeige

- **Given** der Mechaniker hat ein Bauteil-Foto aufgenommen und in der Preview-View bestaetigt
  **When** die Preview-View geschlossen wird
  **Then** erscheint der Ausgebaut-Screen mit der aktuellen Schrittnummer (gross, prominent), dem aufgenommenen Bauteil-Foto und einem "Ausgebaut"-Button

### Aus US-003.4 AK 2: Schrittnummer auf Ausgebaut-Screen

- **Given** der Ausgebaut-Screen wird angezeigt
  **When** der Mechaniker die Schrittnummer sieht
  **Then** ist die Nummer gross und deutlich lesbar (der Mechaniker kann sie auf ein physisches Label uebertragen)

## DB-Interaktion

Keine direkte DB-Interaktion auf diesem Screen. Der Schritt ist bereits in der DB angelegt (Preview-View) und das Bauteil-Foto ist bereits persistiert (Preview-View). Dieser Screen liest nur den bestehenden Zustand.

## Nicht-funktionale Anforderungen

- **Grosse Touch-Targets:** "Ausgebaut"-Button muss mit dreckigen Haenden/Handschuhen bedienbar sein
- **Debounce:** 300ms fuer "Ausgebaut"-Button (Schutz gegen Doppel-Tap)

## Technische Hinweise

- **MVP-Platzhalter:** Spaetere Erweiterung fuer Arbeitsphase (Timer, Kommentare, Sprachnotizen) -- siehe `F-003-arbeitsphase-ideen.md`
- **State Hoisting:** Screen erhaelt State (Schrittnummer, Foto-Pfad) und Callbacks vom ViewModel
