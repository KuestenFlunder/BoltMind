# F-004: Montage-Flow

## Intention

Der Montage-Flow ist das **Gegenstueck zum Demontage-Flow** (F-003). Er spielt die Demontage-Dokumentation rueckwaerts ab, sodass der Mechaniker Schritt fuer Schritt das Fahrzeug wieder zusammenbauen kann.

## Problem

Nach Tagen oder Wochen erinnert sich der Mechaniker nicht mehr an die Reihenfolge und die Ablageorte der ausgebauten Teile. Ohne visuelle Anleitung werden Teile vergessen oder falsch eingebaut.

## Loesung

Die Demontage-Schritte werden in umgekehrter Reihenfolge angezeigt (letztes ausgebautes Teil = erstes einzubauendes Teil). Pro Schritt sieht der Mechaniker alle Fotos dieses Schritts im Foto-Karussell (F-006) und die Schrittnummer aus der Demontage. Fotos mit dem Label "Ablageort" sind als solche gekennzeichnet; Label sind in der Montage sichtbar, aber nicht aenderbar. Erledigte Schritte werden abgehakt. Sind alle Schritte abgehakt, erscheint ein Abschluss-Screen, auf dem der Mechaniker die Archivierung bestaetigt.

## Primaerer Nutzer

Mechaniker in der Werkstatt, der ein zuvor demontiertes Fahrzeug wieder zusammenbaut. Die ausgebauten Teile liegen auf nummerierten Ablageplaetzen.

## Kernfaehigkeiten

- Schritte in umgekehrter Reihenfolge anzeigen (Foto-Karussell des Schritts, F-006)
- Ablageort ueber das Foto-Label erkennbar, sonst Hinweis "Am Fahrzeug"
- Label des sichtbaren Fotos anzeigen — sichtbar, aber nicht aenderbar (F-006, Modus "lesend-mit-Aktionen")
- Navigation per Zurueck/Weiter und Klick auf ein Thumbnail — beides aus F-006. Horizontales Wischen wechselt nur das Foto innerhalb des Schritts, nie den Schritt
- Schritte als "eingebaut" abhaken (Sofort-Save; Zuruecknehmen nur nach Warndialog)
- Fortschrittsanzeige "N von M eingebaut", getrennt von der Schrittnummer
- Montage ueber einen Abschluss-Screen mit Bestaetigung abschliessen und Vorgang archivieren
- Flow jederzeit ueber die Android-Zurueck-Geste verlassen (Fortschritt bleibt erhalten, keine Archivierung) und spaeter beim ersten nicht abgehakten Schritt wieder einsteigen. Der Button "Zurueck" ist ausschliesslich Schritt-Navigation und verlaesst den Flow nie

## Zusammenspiel mit Schrittnummer (F-003)

Die Schrittnummer aus der Demontage dient als Orientierung. Schrittnummer und Fortschritt sind zwei verschiedene Dinge:

- **Schrittnummer:** Immer die Demontage-Schrittnummer (z.B. "Schritt 12"). Sie ist die Identitaet des Schritts und wird in der Montage nie umnummeriert, obwohl die Schritte hier rueckwaerts durchlaufen werden
- **Fortschritt:** Getrennt davon als "3 von 15 eingebaut" formuliert; die Thumbnail-Leiste (F-006) zeigt alle Schritte. Formulierungen wie "Schritt 3 von 15" vermischen beides und sind nicht zulaessig
- **Ablageort-Korrelation:** Physische Ablageorte mit Schrittnummern beschriftet → Nummer in der App zeigt, wo das Teil liegt bzw. Ablageort-Foto. Genau deshalb bleibt die Nummer stabil
- **Ablageort statt SchrittTyp:** Ein `SchrittTyp` existiert nicht mehr. Ein Schritt hat einen Ablageort, wenn mindestens eines seiner Fotos das Label "Ablageort" traegt. Fehlt ein solches Foto, erscheint der Hinweis "Am Fahrzeug"

## Abhaengigkeiten

Pfeil-Konvention nach [../../SpecBestPractices.md](../../SpecBestPractices.md), Abschnitt "Pfeil-Konvention in Abhaengigkeits-Tabellen": `→ F-XXX` = **dieses** Feature nutzt oder ruft F-XXX, `← F-XXX` = F-XXX nutzt oder ruft **dieses** Feature.

| Richtung | Feature | Beziehung                                                              |
| -------- | ------- | ---------------------------------------------------------------------- |
| ←        | F-001   | "Montage starten" in Uebersicht oeffnet diesen Flow                    |
| →        | F-001   | Nach "Archivieren" Rueckkehr zur Uebersicht, Vorgang erscheint im Archiv-Tab; auch der Flow-Ausstieg ohne Archivierung fuehrt dorthin |
| →        | F-003   | Liest Demontage-Schritte (Schritt-Fotos mit Labels, Schrittnummern). F-003 kennt den Montage-Flow nicht (F-003/README.md: "Abhaengigkeitsrichtung F-004 -> F-003") |
| →        | F-006   | Foto-Karussell, Thumbnail-Leiste, Vollbild, Label-Anzeige und Schritt-Navigation (Vor/Zurueck, Thumbnail-Sprung) — Modus "lesend-mit-Aktionen" |
| →        | F-005   | Timer-Service fuer Einbau-Zeitmessung (spaeter)                        |

## Ordner-Inhalt

| Datei                    | Beschreibung                                                      |
| ------------------------ | ----------------------------------------------------------------- |
| [montage.md](montage.md) | User Stories, Akzeptanzkriterien, technische Hinweise, offene Fragen |
