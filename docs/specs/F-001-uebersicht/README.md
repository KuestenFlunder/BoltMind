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

- Offene Vorgaenge auflisten (sortiert nach letzter Bearbeitung, mit Fahrzeugfoto, Auftragsnummer, Schrittanzahl)
- Vorgang antippen → Auswahl-Dialog ab dem ersten Schritt mit den beiden verbindlichen Beschriftungen "Weiter demontieren" und "Montage starten" (der Dialog gehoert F-001, siehe uebersicht.md US-001.2)
- Neuen Vorgang anlegen (→ F-002)
- Vorgang loeschen (Swipe + Bestaetigungsdialog, kaskadierend inkl. aller Schritte und Fotos)
- Archivierte Vorgaenge auflisten (ebenfalls sortiert nach letzter Bearbeitung, mit Abschlussdatum). Ein eigenes Archivierungs-Feld existiert nicht: das Abschlussdatum ist `aktualisiertAm` zum Zeitpunkt des Archivierens und aendert sich danach nicht mehr, weil archivierte Vorgaenge nur lesend geoeffnet werden (governance.md, Invariante `aktualisiertAm`)
- Gesamtdauer und Dauer je Schritt anzeigen — **setzt F-005 voraus** (Quelle: Tabelle `zeit_messung`). Seit 2026-07-27 erfuellt: die Archiv-Liste summiert die abgeschlossenen Messungen beider `referenzTyp`-Werte je Vorgang in derselben Abfrage
- Archivierten Vorgang im F-006-Modus **nur-lesen** durchsehen: Schritt-Navigation (vor/zurueck und Thumbnail-Sprung) und Foto-Karussell liefert F-006, F-001 implementiert davon nichts selbst. F-001 uebergibt die Schritte **aufsteigend nach `schrittNummer`** (Demontage-Reihenfolge, siehe uebersicht.md US-001.5)
- Die Archiv-Detailansicht wird ueber die Android-Zurueck-Geste und den Zurueck-Pfeil in der TopBar verlassen — "Zurueck"/"Weiter" als Beschriftung gehoeren allein der F-006-Schritt-Navigation im Inhaltsbereich

## Abhaengigkeiten

| Richtung | Feature | Beziehung |
|---|---|---|
| → | F-002 | "+"-Button startet Anlage-Flow |
| → | F-003 | Tap auf Vorgang oeffnet Demontage-Flow |
| → | F-004 | Tap auf Vorgang oeffnet Montage-Flow |
| → | F-006 | Archiv-Detailansicht bindet den Schritt-Browser im Modus **nur-lesen** ein: Thumbnail-Leiste, Foto-Karussell, Vollbild und die Vor/Zurueck-Navigation zwischen Schritten kommen vollstaendig aus F-006. F-001 liefert als Eingabe die Schritte **aufsteigend nach `schrittNummer`** |
| → | F-005 | Liest `zeit_messung`-Daten fuer die Dauer-Anzeige im Archiv. Implementiert |

## Ordner-Inhalt

| Datei | Beschreibung |
|---|---|
| [uebersicht.md](uebersicht.md) | User Stories, Akzeptanzkriterien, UI-Skizzen, technische Hinweise |
