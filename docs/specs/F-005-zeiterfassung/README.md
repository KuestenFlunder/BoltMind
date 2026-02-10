# F-005: Zeiterfassung (Timer-Service)

## Kontext

Die Zeiterfassung ist ein **eigenstaendiger Service**, der Zeitmessungen mit Referenz auf beliebige Entities verwaltet. Der Service misst Zeitspannen (Start/Stopp) und speichert sie in einer eigenen Tabelle. Er weiß nicht, welches Feature ihn aufruft.

**Problem:** Der Mechaniker und der Werkstattleiter moechten wissen, wie lange einzelne Arbeitsschritte dauern — fuer Abrechnung, Prozessoptimierung und Erfahrungswerte.

**Loesung:** Ein Timer-Service, der von Features (Demontage, Montage) aufgerufen wird. Der Service misst die Zeit und gibt sie zurueck. Die Features entscheiden selbst, wann sie den Timer starten und stoppen.

**Architektur-Entscheidung:** Der Service besitzt eine eigene Tabelle (`zeit_messung`) und schreibt nicht in fremde Entities. Grund: Keine Dual-Purpose-Felder, keine Kopplung zwischen Timer-Logik und Feature-Logik. Der Service kann unabhaengig von den Features weiterentwickelt werden.

## Abgrenzung

| Verantwortung | Gehoert zu F-005 | Gehoert NICHT zu F-005 |
|---|---|---|
| Timer starten/stoppen | Ja | -- |
| Zeitmessung in DB speichern | Ja | -- |
| Eigene Tabelle verwalten | Ja | -- |
| Wann Timer gestartet wird | -- | Consumer-Entscheidung (z.B. F-003) |
| Wo die Dauer angezeigt wird | -- | Consumer-UI (z.B. F-003 Arbeitsphase) |
| Gesamtdauer berechnen | -- | Consumer-Logik (z.B. F-001 Uebersicht) |

## Consumer

| Feature | Nutzung | Beschrieben in |
|---|---|---|
| F-003 Demontage | Timer pro Arbeitsphase-Schritt | F-003 views/arbeitsphase.md |
| F-004 Montage | Timer pro Einbau-Schritt (spaeter) | F-004 (noch zu spezifizieren) |
| F-001 Uebersicht | Liest `ZeitMessung`-Daten fuer Dauer-Anzeige | F-001 (Darstellungslogik) |

## Ordner-Inhalt

| Datei | Typ | Beschreibung |
|---|---|---|
| [service.md](service.md) | Service-Spec | Interface, Entity, Lifecycle, Edge Cases |

## Abhaengigkeiten

- Keine Feature-Abhaengigkeiten. Der Service ist autark.
- Consumer haben eine Abhaengigkeit auf F-005 (nicht umgekehrt).

## Offene Fragen

- [ ] **OFFEN:** Soll der Timer pausierbar sein (z.B. bei laengerer Unterbrechung)? MVP: Nein, Timer laeuft durch. Begruendung: Der Mechaniker arbeitet physisch auch waehrend App-Pausen.
- [ ] **OFFEN:** Braucht die ZeitMessung ein Feld fuer den Abbruch-Fall (Timer gestartet, aber nie gestoppt)? MVP: `gestopptAm = null` ist ausreichend als Indikator.
