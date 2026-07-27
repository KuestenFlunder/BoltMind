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
| Wo die Dauer angezeigt wird | -- | Consumer-UI (z.B. F-003 Schritt-Ansicht) |
| Gesamtdauer berechnen | -- | Consumer-Logik (z.B. F-001 Uebersicht) |

## Consumer

| Feature | Nutzung | Beschrieben in |
|---|---|---|
| F-003 Demontage | Timer pro Demontage-Schritt (Anker: Schritt-Ansicht) | F-003 views/schritt-ansicht.md, Abschnitt "Ankerpunkt F-005" |
| F-004 Montage | Timer pro Montage-Schritt — **zurueckgestellt, noch nicht beschrieben** | (nirgends) — F-004/README.md listet nur die Abhaengigkeit `→ F-005` |
| F-001 Uebersicht | Liest `ZeitMessung`-Daten fuer Dauer-Anzeige | F-001 uebersicht.md (Darstellungslogik, Technische Hinweise) |

Die Tabelle ist ein reiner Verweis auf die Consumer-Specs. Wann der Timer laeuft und wo die Dauer erscheint, legt die jeweilige Consumer-Spec fest, nicht F-005 (Governance: Service-Architektur, Punkt 3).

**Zur Zeile F-004:** Die Montage-Zeitmessung ist bewusst zurueckgestellt. F-004 beschreibt bisher weder Start- noch Stopp-Trigger und erwaehnt F-005 in montage.md nicht. Solange das so ist, gibt es **keine** Montage-Zeitmessung — nicht weil der Service sie nicht koennte, sondern weil der Consumer die Nutzung noch nicht festgelegt hat. F-005 darf diese Luecke nicht selbst schliessen (der Service kennt seine Consumer nicht). Die Zeile bleibt hier stehen, damit die geplante Beziehung sichtbar ist; sie ist erst dann erfuellt, wenn F-004 einen eigenen Abschnitt "Ankerpunkt F-005" bekommt, so wie F-003 ihn hat.

**Hinweis (Ist-Zustand, 2026-07-27):** F-005 ist implementiert — `ZeitMessung`, `ZeitMessungDao` und `ZeiterfassungService` stehen, die Tabelle `zeit_messung` kam mit `MIGRATION_2_3`. Consumer koennen Dauern anzeigen; F-001 tut es in der Archiv-Liste. Die frueher als `[F-005-abhaengig]` markierten Consumer-AKs sind damit erfuellbar.

## Ordner-Inhalt

| Datei | Typ | Beschreibung |
|---|---|---|
| [service.md](service.md) | Service-Spec | Entity, Interface, DAO, Lifecycle, **User Stories US-005.1 bis US-005.6** mit Akzeptanzkriterien, Edge Cases |

## Abhaengigkeiten

- Keine Feature-Abhaengigkeiten. Der Service ist autark.
- Consumer haben eine Abhaengigkeit auf F-005 (nicht umgekehrt).

## Offene Fragen

Fuer beide Punkte gilt die MVP-Antwort als **bindend**: Sie ist der Stand, gegen den implementiert und getestet wird. Offen ist jeweils nur die spaetere Erweiterung — bis zu einer Entscheidung darueber ist sie **keine Anforderung** und in keinem Feature zu implementieren.

- [ ] **OFFEN:** Soll der Timer pausierbar sein (z.B. bei laengerer Unterbrechung)? **MVP (bindend): Nein**, der Timer laeuft durch (US-005.5). Begruendung: Der Mechaniker arbeitet physisch auch waehrend App-Pausen. Eine Pause-Funktion ist bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
- [ ] **OFFEN:** Braucht die ZeitMessung ein Feld fuer den Abbruch-Fall (Timer gestartet, aber nie gestoppt)? **MVP (bindend): Nein**, `gestopptAm = null` ist der Indikator (US-005.3, US-005.5). Ein zusaetzliches Abbruch-Feld ist bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
