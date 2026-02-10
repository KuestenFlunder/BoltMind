# F-001: Vorgangs-Uebersicht

## Intention

Die Vorgangs-Uebersicht ist der **Startscreen** der App. Sie gibt dem Mechaniker beim Oeffnen der App sofort Orientierung: Was ist offen, wo mache ich weiter?

## Problem

Der Mechaniker hat 1-3 Reparaturen gleichzeitig laufen. Ohne Uebersicht muss er sich merken, welche Vorgaenge offen sind und wie weit er jeweils ist. Das ist fehleranfaellig und kostet Zeit.

## Loesung

Eine Liste aller Reparaturvorgaenge mit Fahrzeugfoto, Auftragsnummer und Status-Informationen. Ein Tap oeffnet den passenden Flow (Demontage oder Montage). Abgeschlossene Vorgaenge sind im Archiv einsehbar.

## Primaerer Nutzer

Mechaniker in der Werkstatt, der morgens die App oeffnet oder zwischen zwei Fahrzeugen wechselt.

## Kernfaehigkeiten

- Offene Vorgaenge auflisten (sortiert nach letzter Bearbeitung)
- Vorgang antippen → Demontage oder Montage fortsetzen
- Neuen Vorgang anlegen (→ F-002)
- Vorgang loeschen (Swipe + Bestaetigung)
- Archivierte Vorgaenge einsehen (Reklamationen, Zeitanalyse)

## Abhaengigkeiten

| Richtung | Feature | Beziehung |
|---|---|---|
| → | F-002 | "+"-Button startet Anlage-Flow |
| → | F-003 | Tap auf Vorgang oeffnet Demontage-Flow |
| → | F-004 | Tap auf Vorgang oeffnet Montage-Flow |
| ← | F-005 | Liest ZeitMessung-Daten fuer Dauer-Anzeige im Archiv |

## Ordner-Inhalt

| Datei | Beschreibung |
|---|---|
| [uebersicht.md](uebersicht.md) | User Stories, Akzeptanzkriterien, UI-Skizzen, technische Hinweise |
