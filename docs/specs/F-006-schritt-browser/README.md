# F-006: Schritt-Browser

## Intention

Der Schritt-Browser ist die **gemeinsame UI-Komponente** fuer das Navigieren durch die Schritte eines Reparaturvorgangs und das Anzeigen der Fotos eines Schritts. Er wird von drei Features genutzt (Demontage, Montage, Archiv-Detailansicht) und existiert genau einmal.

## Problem

Demontage, Montage und Archiv brauchen dieselbe Faehigkeit: durch die Schritte eines Vorgangs blaettern und die Fotos eines Schritts ansehen. Ohne gemeinsame Komponente entstuende dieselbe Logik dreimal — mit drei unterschiedlichen Bedienkonzepten (frueher diskutiert: animierte Kreis-Kette in der Montage, Sprung-Dialog mit Nummernfeld in der Demontage, Foto-Liste im Archiv). Der Mechaniker muesste in jedem Flow etwas anderes lernen, und jede Aenderung an der Foto-Darstellung muesste dreimal nachgezogen werden.

## Loesung

Eine wiederverwendbare, zustandslose Komponente mit klarem Interface:

- Eine **horizontale Thumbnail-Leiste** ueber alle Schritte des Vorgangs. Jedes Thumbnail zeigt das erste Foto des Schritts und die Schrittnummer. Ein Tap springt direkt zu diesem Schritt.
- **Vor-/Zurueck-Bedienelemente** fuer den schrittweisen Wechsel zum benachbarten Schritt. Am ersten bzw. letzten Schritt der Anzeige-Reihenfolge ist das jeweilige Element deaktiviert.
- Ein **Bildkarussell** fuer die N Fotos des aktuellen Schritts, horizontal wischbar. Ein Tap auf ein Foto oeffnet es als Vollbild.
- **Label-Anzeige** am aktuell sichtbaren Foto (Bauteil / Uebersicht / Ablageort) — in allen Modi sichtbar, aenderbar nur im bearbeitbaren Modus.

Der Browser ist das einzige Bedienkonzept fuer Schritt-Navigation; frueher diskutierte Alternativen (Kreis-Kette in F-004, Nummernfeld-Dialog in F-003) sind damit hinfaellig.

**Wischgeste (verbindlich):** Horizontales Wischen im Bildbereich wechselt ausschliesslich das **Foto innerhalb des aktuellen Schritts**. Es wechselt **nie** den Schritt. Der Schritt-Wechsel laeuft ueber die Thumbnail-Leiste und die Vor-/Zurueck-Bedienelemente. Diese Zuordnung ist die Kollisionsaufloesung gegenueber F-004, das dieselbe Geste frueher fuer den Schritt-Wechsel vorgesehen hatte.

**Architektur-Entscheidung:** F-006 ist **kein Service** im Sinne von F-005. Der Browser besitzt keine eigene Tabelle, kein eigenes ViewModel und greift nicht auf Repository oder DAO zu. Er ist eine stateless Compose-Komponente mit State Hoisting: der Consumer liefert den kompletten Zustand hinein und reagiert auf Callbacks. Grund: Die Komponente hat keine eigenen Daten — sie stellt Daten dar, die dem Consumer gehoeren, und meldet Nutzerabsichten zurueck. Persistenz bleibt dort, wo die fachliche Entscheidung faellt.

**Gemeinsam mit F-005 gilt:** Der Browser kennt seine Consumer nicht. Die Integration wird in den Consumer-Specs beschrieben, nicht hier.

## Primaerer Nutzer

Mechaniker in der Werkstatt — beim Dokumentieren (Demontage), beim Zusammenbauen (Montage) und beim Nachschlagen in einem archivierten Vorgang.

## Kernfaehigkeiten

- Thumbnail-Leiste ueber alle Schritte, horizontal scrollbar, aktueller Schritt hervorgehoben
- Sprung zu einem beliebigen Schritt per Tap auf sein Thumbnail
- Schrittweises Vor- und Zurueckblaettern zum benachbarten Schritt, an den Raendern deaktiviert
- Farbliche Markierung des Thumbnails nach Foto-Kategorie des Schritts (Ablageort > Uebersicht > Bauteil)
- Bildkarussell ueber die N Fotos des aktuellen Schritts, horizontal wischbar (wechselt nur das Foto, nie den Schritt)
- Vollbild-Anzeige eines Fotos per Tap
- Label-Anzeige am sichtbaren Foto in allen Modi; Label-Checkboxen bedienbar nur im bearbeitbaren Modus
- Optionale Consumer-Aktion am sichtbaren Foto (F-003 haengt dort "Wiederholen" ein)
- Drei Modi: bearbeitbar, lesend-mit-Aktionen, nur-lesen
- Platzhalter-Bild bei fehlender Foto-Datei, definierter Leer-Zustand ohne Fotos

## Modi

Die drei Modi unterscheiden sich ausschliesslich darin, was der Mechaniker im Browser **veraendern** kann. Navigation und Darstellung sind identisch: Thumbnail-Leiste, Vor/Zurueck, Karussell, Vollbild und die Label des sichtbaren Fotos gibt es in allen drei Modi. Nur die Bedienbarkeit der Label und die Andockstelle fuer eine Aktion am Foto haengen am Modus.

| Modus | Consumer | Label-Checkboxen | Foto aufnehmen | Schritt-Aktionen |
|---|---|---|---|---|
| bearbeitbar | F-003 Demontage | aktiv | ja | ja |
| lesend-mit-Aktionen | F-004 Montage | sichtbar, nicht aenderbar | nein | ja ("Eingebaut") |
| nur-lesen | F-001 Archiv | sichtbar, nicht aenderbar | nein | nein |

**Label werden ausschliesslich in der Demontage gesetzt.** In den beiden lesenden Modi zeigt der Browser die gespeicherten Label des sichtbaren Fotos an, nimmt aber keine Aenderung entgegen.

"Foto aufnehmen" und "Schritt-Aktionen" liegen beim Consumer — der Browser rendert diese Buttons nicht. Der Modus steuert nur, ob der Browser die zugehoerigen Andockstellen anbietet (Consumer-Aktion am sichtbaren Foto) bzw. ob seine Label-Checkboxen bedienbar sind. Vor/Zurueck und Thumbnail-Sprung sind in **allen drei** Modi verfuegbar.

## Schnittstelle im Ueberblick

| Richtung | Inhalt |
|---|---|
| Rein | Liste der Schritte in **Anzeige-Reihenfolge** (der Consumer bestimmt vorwaerts oder rueckwaerts) |
| Rein | Fotos je Schritt: **ID**, Pfad, Reihenfolge, die drei Label-Flags (Bauteil / Uebersicht / Ablageort). Die ID ist Pflicht, weil der Browser sie in den Callbacks zurueckmeldet |
| Rein | Modus: bearbeitbar, lesend-mit-Aktionen oder nur-lesen |
| Rein | Index des aktuell angezeigten Schritts (`-1`, wenn der Vorgang keine Schritte hat) |
| Rein | Index des aktuell sichtbaren Fotos innerhalb des Schritts (`-1`, wenn der Schritt keine Fotos hat) |
| Rein | Optionale **Consumer-Aktion am sichtbaren Foto** (Bezeichnung + Callback), nur im bearbeitbaren Modus dargestellt |
| Raus | **Schritt gewaehlt** — der Nutzer hat ein Thumbnail angetippt |
| Raus | **Vorheriger Schritt** — der Nutzer hat "Zurueck" angetippt |
| Raus | **Naechster Schritt** — der Nutzer hat "Weiter" angetippt |
| Raus | **Foto gewaehlt** — das im Karussell sichtbare Foto hat gewechselt: durch Wischen (US-006.4) **oder** weil der Browser beim Schrittwechsel auf das erste Foto des neuen Schritts zurueckgesetzt hat (US-006.2, US-006.10) |
| Raus | **Label geaendert** — eine Checkbox am sichtbaren Foto wurde gesetzt oder entfernt (nur bearbeitbarer Modus) |
| Raus | **Foto-Aktion ausgeloest** — die Consumer-Aktion am sichtbaren Foto wurde angetippt |

Was der Consumer mit den Callbacks macht (Index fortschreiben, Label persistieren, Kamera starten, Timer stoppen), entscheidet der Consumer. Der Browser schreibt den Index nicht selbst fort — auch nicht bei Vor/Zurueck.

**Reset des sichtbaren Fotos beim Schrittwechsel:** Wechselt der angezeigte Schritt (Thumbnail-Sprung oder Vor/Zurueck), setzt der Browser das Karussell auf das **erste** Foto des neuen Schritts und meldet das ueber "Foto gewaehlt". Der Consumer **haelt** den Wert weiterhin — er braucht ihn fuer die Label-Anzeige und die Foto-Aktion —, **setzt ihn aber nicht selbst zurueck**. Er uebernimmt schlicht, was gemeldet wird.

Details siehe [browser.md](browser.md).

## Abgrenzung

| Verantwortung | Gehoert zu F-006 | Gehoert NICHT zu F-006 |
|---|---|---|
| Thumbnail-Leiste rendern und scrollen | Ja | -- |
| **Schritt-Navigation vollstaendig**: Thumbnail-Sprung sowie Vor/Zurueck inkl. Deaktivierung an den Raendern | Ja | -- |
| Foto-Karussell und Vollbild-Anzeige | Ja | -- |
| Label des sichtbaren Fotos anzeigen (alle Modi) und Aenderung melden (nur bearbeitbar) | Ja | -- |
| Kategorie eines Schritts fuer die Einfaerbung ableiten | Ja | -- |
| Leer-Zustand des Karussells bei einem Schritt **ohne Fotos** | Ja | -- |
| Platzhalter-Bild bei **fehlender Foto-Datei** | Ja | -- |
| **Reset des sichtbaren Fotos beim Schrittwechsel**: Karussell auf das erste Foto des neuen Schritts setzen und ueber "Foto gewaehlt" melden | Ja | -- |
| Label-Aenderung **persistieren** | -- | Consumer (Repository, Sofort-Save) |
| Index nach Vor/Zurueck oder Sprung fortschreiben | -- | Consumer (haelt `aktuellerIndex`) |
| Wert des sichtbaren Fotos halten (`sichtbaresFotoIndex`) | -- | Consumer — er haelt ihn (fuer Label-Anzeige und Foto-Aktion) und uebernimmt den gemeldeten Wert, **setzt ihn beim Schrittwechsel aber nicht selbst zurueck** |
| Schritte abhaken (`eingebautBeiMontage`) | -- | Consumer F-004 |
| Kamera starten, Foto aufnehmen und anhaengen | -- | Consumer F-003 |
| Ausfuehren der Aktion am sichtbaren Foto (z.B. "Wiederholen") | -- | Consumer F-003 (F-006 rendert nur die Andockstelle und meldet den Tap) |
| Schritt anlegen, abschliessen, Flow beenden | -- | Consumer F-003 / F-004 |
| Abschluss-Screen und Archivieren | -- | Consumer F-004 |
| Anzeige-Reihenfolge der Schritte (vorwaerts / rueckwaerts) | -- | Consumer (liefert die sortierte Liste) |
| Daten aus der DB laden | -- | Consumer (ViewModel + Repository) |
| Zeitmessung pro Schritt | -- | F-005 Zeiterfassung, gesteuert vom Consumer |
| Hinweistext bei Vorgang ganz ohne Schritte | -- | Consumer (Screen-Ebene) |

## Consumer

| Feature | Nutzung | Modus | Beschrieben in |
|---|---|---|---|
| F-003 Demontage | Schritt-Ansicht: Karussell des aktuellen Schritts, Label-Checkboxen, Aktion "Wiederholen" am sichtbaren Foto, Leiste aller bisherigen Schritte | bearbeitbar | F-003 workflow.md / views |
| F-004 Montage | Navigation durch die Schritte in Montage-Reihenfolge; Label werden nur gelesen, die Schritt-Aktion "Eingebaut" gehoert F-004 | lesend-mit-Aktionen | F-004 montage.md |
| F-001 Archiv-Detailansicht | Durchblaettern der Dokumentation eines archivierten Vorgangs | nur-lesen | F-001 uebersicht.md |

F-001 und F-004 bringen **keine eigene Schritt-Navigation** mit. Vor/Zurueck und Thumbnail-Sprung kommen aus F-006; die Consumer reagieren nur auf die Callbacks und schreiben ihren `aktuellerIndex` fort.

## Abhaengigkeiten

- **Keine Feature-Abhaengigkeiten.** Die Komponente ist autark und kennt keinen ihrer Consumer. Abhaengigkeitsrichtung: Consumer → F-006.
- **Datenmodell:** Die Entities `Schritt` und `SchrittFoto` gehoeren zum Demontage-Datenmodell (F-003). F-006 nimmt sie nur als Eingabe-Datenstruktur entgegen und greift nicht selbst auf die DB zu.
- **Governance:** Sofort-Save, 300ms Debounce, Platzhalter-Bild bei fehlender Datei, Foto-Label und Prioritaetsregel (Ablageort > Uebersicht > Bauteil) sowie die verbindlichen **Touch-Target-Mindestmasse** — alles in [../governance.md](../governance.md). F-006 nennt dafuer keine eigenen Zahlen.

## Ordner-Inhalt

| Datei | Typ | Beschreibung |
|---|---|---|
| [browser.md](browser.md) | Komponenten-Spec | User Stories, Akzeptanzkriterien, Interface, Nicht-funktionale Anforderungen, Technische Hinweise |

## Entschieden

- [x] **ENTSCHIEDEN:** Der Browser kennt **drei** Modi (bearbeitbar / lesend-mit-Aktionen / nur-lesen). Label werden ausschliesslich in der Demontage gesetzt. Siehe Abschnitt "Modi".
- [x] **ENTSCHIEDEN:** Die gesetzten Label des sichtbaren Fotos sind **in allen Modi** sichtbar — in den lesenden Modi als nicht bedienbare Anzeige. F-004 braucht das fuer die Ablageort-Kennzeichnung am Foto (US-004.1). Die frueher offene Frage "Labels im nur-lesen-Modus sichtbar?" ist damit mit **Ja** beantwortet.
- [x] **ENTSCHIEDEN:** Die Schritt-Navigation gehoert vollstaendig F-006 — Thumbnail-Sprung **und** Vor/Zurueck (US-006.10). F-001 und F-004 spezifizieren dafuer keine eigenen Bedienelemente mehr.
- [x] **ENTSCHIEDEN:** Horizontales Wischen im Bildbereich wechselt das Foto, nie den Schritt (US-006.4).

## Offene Fragen

Fuer alle drei Punkte gilt die MVP-Antwort als **bindend**: Sie ist der Stand, gegen den implementiert und getestet wird. Offen ist jeweils nur die spaetere Erweiterung — bis zu einer Entscheidung darueber ist sie **keine Anforderung** und in keinem Feature zu implementieren.

- [ ] **OFFEN:** Soll die Kategorie eines Thumbnails zusaetzlich zur Farbe durch ein Symbol erkennbar sein (Farbfehlsichtigkeit, Sonnenlicht in der Halle)? **MVP (bindend):** nur die in browser.md US-006.3 festgelegte Markierung (Rahmenfarbe + Rahmenstaerke). Ein Symbol ist bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
- [ ] **OFFEN:** Braucht die Vollbild-Anzeige Pinch-Zoom? **MVP (bindend): Nein**, die formatfuellende Anzeige aus US-006.5 reicht. Pinch-Zoom ist bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
- [ ] **OFFEN:** Braucht die Leiste bei sehr vielen Schritten (>50) eine Filter- oder Sprungfunktion (z.B. "nur Schritte mit Ablageort")? **MVP (bindend): Nein**, reines Scrollen (US-006.1). Filter oder Sprungfunktion sind bis zu einer Entscheidung keine Anforderung und in keinem Feature zu implementieren.
