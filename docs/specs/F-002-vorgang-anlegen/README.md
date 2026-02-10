# F-002: Reparaturvorgang anlegen

## Intention

Der Anlage-Flow erstellt einen neuen Reparaturvorgang. Er ist bewusst **Foto-first**: Zuerst wird das Fahrzeug fotografiert (visuelles Wiederfinden), dann die Auftragsdaten erfasst. Minimaler Aufwand — nur die Auftragsnummer ist Pflicht.

## Problem

Der Mechaniker steht vor einem neuen Fahrzeug und will moeglichst schnell mit der Demontage beginnen. Jede zusaetzliche Eingabe kostet Zeit und Geduld (dreckige Haende, Handschuhe).

## Loesung

Ein zweistufiger Flow: Foto aufnehmen → Auftragsnummer eingeben → Starten. Beschreibung ist optional. Nach dem Starten geht es direkt in den Demontage-Flow (F-003).

## Primaerer Nutzer

Mechaniker, der einen neuen Reparaturauftrag beginnt und das Fahrzeug vor sich stehen hat.

## Kernfaehigkeiten

- Fahrzeugfoto aufnehmen (System-Kamera)
- Auftragsnummer erfassen (Pflichtfeld)
- Optionale Beschreibung
- Foto wiederholen vor dem Starten
- Abbruch ueber Back-Button (kein Vorgang wird angelegt)

## Abhaengigkeiten

| Richtung | Feature | Beziehung |
|---|---|---|
| ← | F-001 | "+"-Button in Uebersicht startet diesen Flow |
| → | F-003 | Nach "Starten" wird Demontage-Flow geoeffnet |

## Ordner-Inhalt

| Datei | Beschreibung |
|---|---|
| [anlegen.md](anlegen.md) | User Stories, Akzeptanzkriterien, UI-Skizzen, technische Hinweise |
