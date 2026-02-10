# F-004: Montage-Flow

## Intention

Der Montage-Flow ist das **Gegenstueck zum Demontage-Flow** (F-003). Er spielt die Demontage-Dokumentation rueckwaerts ab, sodass der Mechaniker Schritt fuer Schritt das Fahrzeug wieder zusammenbauen kann.

## Problem

Nach Tagen oder Wochen erinnert sich der Mechaniker nicht mehr an die Reihenfolge und die Ablageorte der ausgebauten Teile. Ohne visuelle Anleitung werden Teile vergessen oder falsch eingebaut.

## Loesung

Die Demontage-Schritte werden in umgekehrter Reihenfolge angezeigt (letztes ausgebautes Teil = erstes einzubauendes Teil). Pro Schritt sieht der Mechaniker das Bauteil-Foto, optional das Ablageort-Foto und die Schrittnummer. Erledigte Schritte werden abgehakt. Nach Abschluss aller Schritte wird der Vorgang archiviert.

## Primaerer Nutzer

Mechaniker in der Werkstatt, der ein zuvor demontiertes Fahrzeug wieder zusammenbaut. Die ausgebauten Teile liegen auf nummerierten Ablageplaetzen.

## Kernfaehigkeiten

- Schritte in umgekehrter Reihenfolge anzeigen (Bauteil-Foto + Ablageort)
- Schritte als "eingebaut" abhaken (Toggle, Sofort-Save)
- Fortschrittsanzeige (Schritt X von Y, Fortschrittsbalken)
- Frei zwischen Schritten blaettern (nicht nur linear)
- Direkt zu Schrittnummer springen (Ablageort-Korrelation)
- Montage abschliessen und Vorgang archivieren

## Zusammenspiel mit Schrittnummer (F-003)

Die Schrittnummer aus der Demontage dient als Orientierung:
- **Fortschritt:** "Schritt 3 von 15" zeigt den Zusammenbau-Fortschritt
- **Ablageort-Korrelation:** Physische Ablageorte mit Schrittnummern beschriftet → Nummer in der App zeigt, wo das Teil liegt
- **SchrittTyp:** `AUSGEBAUT` zeigt Ablageort-Foto, `AM_FAHRZEUG` zeigt Hinweis "Am Fahrzeug"

## Abhaengigkeiten

| Richtung | Feature | Beziehung |
|---|---|---|
| ← | F-001 | "Montage starten" in Uebersicht oeffnet diesen Flow |
| ← | F-003 | Liest Demontage-Schritte (Fotos, Schrittnummern, SchrittTyp) |
| ← | F-005 | Timer-Service fuer Einbau-Zeitmessung (spaeter) |

## Ordner-Inhalt

| Datei | Beschreibung |
|---|---|
| [montage.md](montage.md) | User Stories, Akzeptanzkriterien, UI-Skizzen, technische Hinweise |
