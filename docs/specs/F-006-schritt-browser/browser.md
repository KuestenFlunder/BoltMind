# Schritt-Browser: Navigation und Foto-Anzeige

## Aufbau der Komponente

Der Browser besteht aus vier Bereichen, die immer gemeinsam auftreten:

```
+-------------------------------------------------------+
|                                                       |
|            Foto-Karussell (aktueller Schritt)         |
|            < horizontal wischbar, N Fotos >           |
|                        [ 2 von 4 ]     [Wiederholen]  |  <- optionale Consumer-Aktion
|                                                       |     am sichtbaren Foto
+-------------------------------------------------------+
|  [x] Bauteil   [ ] Uebersicht   [x] Ablageort         |  <- Label des sichtbaren Fotos;
+-------------------------------------------------------+     bedienbar nur bearbeitbar
|  [ 5 ][ 6 ][*7*][ 8 ][ 9 ][10][11]  < scrollbar >     |  <- Thumbnail-Leiste, alle Schritte
+-------------------------------------------------------+
|  [ < Zurueck ]                        [ Weiter > ]    |  <- Schritt-Navigation, an den
+-------------------------------------------------------+     Raendern deaktiviert
```

Die Anordnung der Bereiche zueinander legt der Consumer fest (die Leiste kann oben oder unten stehen). Aktions-Buttons des Flows (z.B. "Weiteres Foto", "Naechster Schritt", "Beenden", "Eingebaut") gehoeren dem Consumer und sind nicht Teil des Browsers.

Auch Aktionen **am sichtbaren Foto** (z.B. "Wiederholen" in F-003) gehoeren fachlich dem Consumer. Der Browser stellt dafuer eine Andockstelle bereit, weil nur er weiss, welches Foto gerade sichtbar ist: der Consumer uebergibt Bezeichnung und Callback, der Browser rendert das Element neben dem Karussell und meldet den Tap zusammen mit dem sichtbaren Foto zurueck (US-006.11). Ohne uebergebene Aktion bleibt der Bereich leer.

**Wischgeste (verbindlich):** Horizontales Wischen im Bildbereich wechselt das **Foto innerhalb des Schritts**, niemals den Schritt (US-006.4). Der Schritt-Wechsel laeuft ausschliesslich ueber die Thumbnail-Leiste (US-006.2) und Vor/Zurueck (US-006.10).

**Modi:**

| Modus | Consumer | Thumbnail-Leiste + Vor/Zurueck | Karussell | Vollbild | Label am sichtbaren Foto | Label aenderbar | Aktion am Foto | Foto aufnehmen | Schritt-Aktionen |
|---|---|---|---|---|---|---|---|---|---|
| bearbeitbar | F-003 Demontage | Ja | Ja | Ja | sichtbar | **Ja** | Ja ("Wiederholen") | ja (Consumer) | ja (Consumer) |
| lesend-mit-Aktionen | F-004 Montage | Ja | Ja | Ja | sichtbar | Nein | Nein | nein | ja ("Eingebaut", Consumer) |
| nur-lesen | F-001 Archiv | Ja | Ja | Ja | sichtbar | Nein | Nein | nein | nein |

Die Spalten "Foto aufnehmen" und "Schritt-Aktionen" beschreiben Consumer-Chrome ausserhalb des Browsers und stehen hier nur zur Einordnung. Der Browser selbst rendert diese Buttons nie.

**Label werden ausschliesslich in der Demontage gesetzt.** In den beiden lesenden Modi zeigt der Browser die gespeicherten Label an, nimmt aber keine Aenderung entgegen und meldet kein `onLabelGeaendert`.

---

## User Stories

### US-006.1: Alle Schritte auf einen Blick sehen

**Als** Mechaniker
**moechte ich** alle Schritte eines Vorgangs als Bilderleiste sehen
**damit** ich mich sofort orientieren kann, wo ich gerade bin und was vorher und nachher kam.

#### Akzeptanzkriterien

- **Given** ein Reparaturvorgang mit 15 Schritten ist geoeffnet
  **When** der Schritt-Browser angezeigt wird
  **Then** enthaelt die Thumbnail-Leiste 15 Thumbnails in der vom Consumer vorgegebenen Anzeige-Reihenfolge

- **Given** ein Schritt hat mehrere Fotos
  **When** sein Thumbnail dargestellt wird
  **Then** zeigt das Thumbnail das **erste** Foto dieses Schritts (niedrigste Reihenfolge)

- **Given** die Thumbnail-Leiste wird angezeigt
  **When** ein Thumbnail dargestellt wird
  **Then** wird die Schrittnummer auf dem Thumbnail mit mindestens 20sp angezeigt

- **Given** Schritt 7 ist der aktuell angezeigte Schritt
  **When** die Leiste dargestellt wird
  **Then** ist genau das Thumbnail von Schritt 7 hervorgehoben (groesser dargestellt und mit einem zusaetzlichen Selektionsring) und kein anderes
  **And** seine Kategorie-Markierung aus US-006.3 bleibt daneben erkennbar

- **Given** 15 Thumbnails passen nicht gleichzeitig auf den Bildschirm
  **When** der Mechaniker horizontal ueber die Leiste wischt
  **Then** scrollt die Leiste, ohne dass der angezeigte Schritt wechselt

- **Given** der aktuelle Schritt liegt ausserhalb des sichtbaren Bereichs der Leiste
  **When** der aktuelle Schritt wechselt
  **Then** scrollt die Leiste automatisch so, dass das hervorgehobene Thumbnail sichtbar ist

---

### US-006.2: Direkt zu einem beliebigen Schritt springen

**Als** Mechaniker
**moechte ich** durch Antippen eines Thumbnails direkt zu diesem Schritt springen
**damit** ich schnell nachschauen kann, ohne mich Schritt fuer Schritt durchzuklicken oder eine Nummer eintippen zu muessen.

#### Akzeptanzkriterien

- **Given** Schritt 3 wird angezeigt
  **When** der Mechaniker das Thumbnail von Schritt 11 antippt
  **Then** wird Schritt 11 zum aktuellen Schritt gemeldet
  **And** das Karussell zeigt das erste Foto von Schritt 11
  **And** der Browser meldet diesen Reset ueber `onFotoGewaehlt(0)` (bzw. `-1`, wenn Schritt 11 keine Fotos hat)
  **And** der Consumer uebernimmt den gemeldeten Wert; er setzt das sichtbare Foto **nicht** selbst zurueck
  **And** das Thumbnail von Schritt 11 ist hervorgehoben

- **Given** Schritt 11 wird angezeigt
  **When** der Mechaniker das Thumbnail von Schritt 11 antippt
  **Then** bleibt die Ansicht unveraendert (kein Zuruecksetzen des Karussells)
  **And** es wird kein `onFotoGewaehlt` gemeldet (der Reset gehoert allein zum **Wechsel** des Schritts)

- **Given** ein beliebiger Schritt wird angezeigt
  **When** der Mechaniker zu einem anderen Schritt springt
  **Then** werden dabei keine Daten veraendert (kein Schritt wird abgeschlossen, abgehakt oder geloescht)

- **Given** der Browser laeuft in einem der beiden lesenden Modi
  **When** der Mechaniker ein Thumbnail antippt
  **Then** springt die Ansicht genauso zu diesem Schritt (der Sprung ist in allen drei Modi verfuegbar)

---

### US-006.3: Schritte mit Ablageort in der Leiste erkennen

**Als** Mechaniker
**moechte ich** in der Leiste farblich erkennen, welche Schritte ein Foto vom Ablageort haben
**damit** ich beim Zusammenbau sofort sehe, fuer welche Teile ich einen Ablageplatz suchen muss und welche am Fahrzeug geblieben sind.

#### Die vier Markierungen

Ein Thumbnail traegt genau eine Markierung. Sie besteht aus einem **umlaufenden Rahmen** in einer Farbe des Material3-Theme; unterschieden wird ueber Farb-Token und Rahmenstaerke. Andere Mittel (Fuellung, Deckkraft, Groesse) werden dafuer nicht verwendet: Groesse und ein zusaetzlicher, weiter aussen liegender **Selektionsring** sind allein der Hervorhebung des **aktuellen** Schritts vorbehalten (US-006.1). Beide Kennzeichnungen sind damit gleichzeitig sichtbar und koennen nicht verwechselt werden.

| Markierung | Bedingung | Rahmenfarbe (Theme-Token) | Rahmenstaerke |
|---|---|---|---|
| Ablageort-Markierung | mindestens ein Foto mit `istAblageort` | `colorScheme.tertiary` | 3dp |
| Uebersichts-Markierung | kein Ablageort, aber mindestens ein Foto mit `istUebersicht` | `colorScheme.secondary` | 3dp |
| Bauteil-Markierung | weder Ablageort noch Uebersicht, aber mindestens ein Foto mit `istBauteil` | `colorScheme.primary` | 3dp |
| neutrale Markierung | kein Foto, oder an allen Fotos alle drei Label abgewaehlt | `colorScheme.outlineVariant` | 1dp |

Die Reihenfolge der Tabelle ist zugleich die Auswerteregel: es gilt die **Prioritaet Ablageort > Uebersicht > Bauteil** (Governance). Die Ableitung ist als reine Funktion `kategorieVon(...)` unter "Technische Hinweise" beschrieben und als Unit-Test pruefbar.

#### Akzeptanzkriterien

- **Given** ein Schritt hat mindestens ein Foto mit dem Label "Ablageort"
  **When** sein Thumbnail in der Leiste dargestellt wird
  **Then** traegt es die Ablageort-Markierung (3dp Rahmen in `colorScheme.tertiary`)

- **Given** ein Schritt hat kein Foto mit "Ablageort", aber mindestens eines mit "Uebersicht"
  **When** sein Thumbnail dargestellt wird
  **Then** traegt es die Uebersichts-Markierung (3dp Rahmen in `colorScheme.secondary`)

- **Given** ein Schritt hat nur Fotos mit dem Label "Bauteil"
  **When** sein Thumbnail dargestellt wird
  **Then** traegt es die Bauteil-Markierung (3dp Rahmen in `colorScheme.primary`)

- **Given** ein Schritt hat Fotos, bei denen alle drei Labels abgewaehlt sind
  **When** sein Thumbnail dargestellt wird
  **Then** traegt es die neutrale Markierung (1dp Rahmen in `colorScheme.outlineVariant`, keine Kategorie-Farbe)

- **Given** ein Foto traegt gleichzeitig "Bauteil" und "Ablageort"
  **When** die Markierung des Thumbnails bestimmt wird
  **Then** gilt die Ablageort-Markierung (Prioritaet Ablageort > Uebersicht > Bauteil)

- **Given** der Mechaniker setzt im bearbeitbaren Modus am aktuell sichtbaren Foto das Label "Ablageort"
  **When** die Aenderung uebernommen ist
  **Then** wechselt die Markierung des zugehoerigen Thumbnails sofort, ohne dass der Screen neu geladen werden muss

- **Given** ein Schritt ist der aktuell angezeigte Schritt und traegt die Ablageort-Markierung
  **When** die Leiste dargestellt wird
  **Then** ist sein Thumbnail zusaetzlich als aktueller Schritt hervorgehoben (US-006.1)
  **And** die Ablageort-Markierung bleibt daran erkennbar

---

### US-006.4: Alle Fotos eines Schritts durchwischen

**Als** Mechaniker
**moechte ich** die Fotos eines Schritts durch Wischen nacheinander ansehen
**damit** ich mehrere Perspektiven auf dasselbe Bauteil pruefen kann (Detail, Uebersicht, Ablageort).

**Verbindliche Gestenbelegung:** Die horizontale Wischgeste im Bildbereich ist ausschliesslich mit dem **Foto-Wechsel innerhalb des Schritts** belegt. Ein Schritt-Wechsel per Wischen findet nicht statt — weder am Rand des Karussells noch bei einem Schritt mit nur einem Foto. Consumer duerfen dieselbe Geste im Bildbereich nicht anderweitig belegen.

#### Akzeptanzkriterien

- **Given** der aktuelle Schritt hat 4 Fotos
  **When** der Schritt angezeigt wird
  **Then** zeigt das Karussell das erste Foto gross
  **And** ein Indikator zeigt "1 von 4"

- **Given** das Karussell zeigt Foto 1 von 4
  **When** der Mechaniker nach links wischt
  **Then** zeigt das Karussell Foto 2
  **And** der Indikator zeigt "2 von 4"
  **And** das gewechselte Foto wird als neues sichtbares Foto gemeldet

- **Given** das Karussell zeigt das letzte Foto des Schritts
  **When** der Mechaniker weiter in dieselbe Richtung wischt
  **Then** bleibt das letzte Foto sichtbar
  **And** es findet kein Wechsel zum naechsten Schritt statt

- **Given** der aktuelle Schritt hat genau ein Foto
  **When** der Mechaniker im Karussell wischt
  **Then** bleibt dieses Foto sichtbar und es wird kein Foto-Wechsel gemeldet
  **And** der angezeigte Schritt bleibt derselbe

- **Given** der aktuelle Schritt ist nicht der letzte der Anzeige-Reihenfolge und das Karussell zeigt sein letztes Foto
  **When** der Mechaniker weiter in dieselbe Richtung wischt
  **Then** wird weder `onSchrittGewaehlt` noch `onNaechsterSchritt` gemeldet (Wischen wechselt nie den Schritt)

- **Given** der Schritt hat ein weiteres Foto erhalten und der Consumer gibt dieses als sichtbares Foto vor
  **When** die Ansicht aktualisiert wird
  **Then** zeigt das Karussell das neue Foto
  **And** der Indikator zeigt die erhoehte Gesamtzahl

---

### US-006.5: Ein Foto als Vollbild ansehen

**Als** Mechaniker
**moechte ich** ein Foto mit einem Tap gross ansehen
**damit** ich Details wie Schraubenpositionen oder Steckerbelegungen erkennen kann.

#### Akzeptanzkriterien

- **Given** das Karussell zeigt ein Foto
  **When** der Mechaniker das Foto antippt
  **Then** wird das Foto formatfuellend als Vollbild ueber dem Screen angezeigt

- **Given** die Vollbild-Anzeige ist geoeffnet
  **When** der Mechaniker sie schliesst (Schliessen-Element oder Zurueck-Geste)
  **Then** kehrt er zur Schritt-Ansicht zurueck
  **And** dasselbe Foto ist im Karussell weiterhin sichtbar

- **Given** die Vollbild-Anzeige ist geoeffnet
  **When** sie dargestellt wird
  **Then** verdecken weder Label-Anzeige, Foto-Aktion, Thumbnail-Leiste noch die Vor-/Zurueck-Bedienelemente das Foto

- **Given** der Browser laeuft in einem der beiden lesenden Modi
  **When** der Mechaniker ein Foto antippt
  **Then** oeffnet sich die Vollbild-Anzeige genauso

---

### US-006.6: Fotos beschriften

**Als** Mechaniker
**moechte ich** direkt am angezeigten Foto ankreuzen, was es zeigt
**damit** ich beim Zusammenbau sofort erkenne, welches Bild mir den Ablageort verraet und welches das Bauteil.

Diese Story gilt **ausschliesslich fuer den bearbeitbaren Modus** (F-003 Demontage). Label werden nur dort gesetzt. Wie die Label in den beiden lesenden Modi dargestellt werden, beschreibt US-006.7.

#### Akzeptanzkriterien

- **Given** der Browser laeuft im bearbeitbaren Modus und das Karussell zeigt ein Foto
  **When** die Schritt-Ansicht dargestellt wird
  **Then** sind drei bedienbare Checkboxen sichtbar: "Bauteil", "Uebersicht", "Ablageort"
  **And** sie zeigen den gespeicherten Zustand genau dieses Fotos

- **Given** ein gerade aufgenommenes Foto wird angezeigt
  **When** die Checkboxen dargestellt werden
  **Then** ist "Bauteil" angehakt und "Uebersicht" und "Ablageort" sind nicht angehakt

- **Given** am sichtbaren Foto ist nur "Bauteil" angehakt
  **When** der Mechaniker "Ablageort" antippt
  **Then** wird die Label-Aenderung sofort gemeldet (Foto, Label, neuer Wert)
  **And** "Ablageort" erscheint angehakt
  **And** "Bauteil" bleibt angehakt (Mehrfachauswahl ist erlaubt)

- **Given** am sichtbaren Foto sind alle drei Labels angehakt
  **When** der Mechaniker alle drei abwaehlt
  **Then** wird jede Abwahl gemeldet
  **And** es erscheint keine Fehlermeldung (kein Label ist ein gueltiger Zustand)

- **Given** der Mechaniker aendert ein Label
  **When** die Aenderung erfolgt ist
  **Then** gibt es keine Sammel-Bestaetigung und keinen "Speichern"-Button (Sofort-Save, siehe Governance)

- **Given** das Karussell zeigt Foto 2 mit dem Label "Ablageort"
  **When** der Mechaniker zu Foto 3 wischt, das nur "Bauteil" traegt
  **Then** zeigen die Checkboxen den Zustand von Foto 3

---

### US-006.7: Sehen, was ein Foto zeigt, ohne es aendern zu koennen

**Als** Mechaniker
**moechte ich** beim Zusammenbauen und beim Nachschlagen am Foto ablesen koennen, ob es das Bauteil, eine Uebersicht oder den Ablageort zeigt, ohne dabei etwas veraendern zu koennen
**damit** ich den Ablageort sofort erkenne und die Dokumentation trotzdem nicht versehentlich verfaelsche.

#### Akzeptanzkriterien

- **Given** der Browser laeuft in einem der beiden lesenden Modi und das Karussell zeigt ein Foto
  **When** die Ansicht dargestellt wird
  **Then** sind dieselben drei Label sichtbar wie im bearbeitbaren Modus und zeigen den gespeicherten Zustand genau dieses Fotos
  **And** sie sind als nicht bedienbar erkennbar (deaktivierte Darstellung)

- **Given** der Browser laeuft in einem der beiden lesenden Modi und das sichtbare Foto traegt das Label "Ablageort"
  **When** die Ansicht dargestellt wird
  **Then** ist am Foto erkennbar, dass es den Ablageort zeigt (F-004 US-004.1 stuetzt sich darauf)

- **Given** der Browser laeuft in einem der beiden lesenden Modi
  **When** der Mechaniker eines der drei Label antippt
  **Then** aendert sich nichts
  **And** es wird kein `onLabelGeaendert` gemeldet

- **Given** der Browser laeuft in einem der beiden lesenden Modi
  **When** ein Schritt angezeigt wird
  **Then** ist weder eine Aktion zur Foto-Aufnahme noch eine Aktion am sichtbaren Foto ("Wiederholen") im Browser sichtbar — auch dann nicht, wenn der Consumer eine Foto-Aktion uebergibt

- **Given** der Browser laeuft in einem der beiden lesenden Modi
  **When** der Mechaniker durch Schritte springt, vor- und zurueckblaettert, im Karussell wischt und ein Foto als Vollbild oeffnet
  **Then** funktionieren alle vier Bedienungen unveraendert
  **And** es wird kein Datensatz veraendert

- **Given** der Browser laeuft im Modus nur-lesen
  **When** der Mechaniker die Ansicht verlaesst und erneut oeffnet
  **Then** sind Schritte, Fotos und Labels unveraendert

---

### US-006.8: Fehlende Foto-Dateien abfangen

**Als** Mechaniker
**moechte ich** die Dokumentation auch dann durchblaettern koennen, wenn eine Bilddatei fehlt
**damit** ein einzelnes verlorenes Foto nicht den gesamten Vorgang unbrauchbar macht.

#### Akzeptanzkriterien

- **Given** ein Foto-Datensatz verweist auf eine nicht mehr vorhandene Datei
  **When** das Karussell dieses Foto anzeigt
  **Then** erscheint das Platzhalter-Bild (App-Icon) und die App stuerzt nicht ab

- **Given** das erste Foto eines Schritts fehlt als Datei
  **When** die Thumbnail-Leiste angezeigt wird
  **Then** zeigt das Thumbnail das Platzhalter-Bild mit der Schrittnummer

- **Given** das Karussell zeigt einen Platzhalter fuer eine fehlende Datei
  **When** der Mechaniker das Bild antippt
  **Then** oeffnet sich die Vollbild-Anzeige mit dem Platzhalter, ohne Absturz

- **Given** eine Foto-Datei fehlt und der Browser laeuft im bearbeitbaren Modus
  **When** der Platzhalter angezeigt wird
  **Then** sind die Label-Checkboxen weiterhin bedienbar (die Metadaten des Fotos existieren noch)

---

### US-006.9: Schritt ohne Fotos verstaendlich anzeigen

**Als** Mechaniker
**moechte ich** bei einem Schritt ohne Fotos eine klare Aussage sehen
**damit** ich weiss, dass hier nichts dokumentiert wurde, und nicht an einen Fehler der App glaube.

#### Definition: Leer-Zustand vs. Platzhalter-Bild

Der Leer-Zustand des Karussells gehoert F-006 und wird nur hier definiert. Consumer verweisen darauf, statt ihn neu zu beschreiben. Es sind **zwei verschiedene Faelle**, die nicht vermischt werden duerfen:

| Fall | Bedingung | Darstellung im Karussell |
|---|---|---|
| **Leer-Zustand** | Der Schritt hat **keinen einzigen** `SchrittFoto`-Datensatz | Flaeche mit dem Text "Keine Fotos zu diesem Schritt". **Kein** Bild, kein Platzhalter-Bild, kein Foto-Indikator, keine Label-Anzeige, keine Foto-Aktion |
| **Platzhalter-Bild** | Es gibt einen Foto-Datensatz, aber die **Datei fehlt** | Das App-Icon anstelle des Fotos (US-006.8). Foto-Indikator, Label-Anzeige und Foto-Aktion bleiben normal vorhanden, weil die Metadaten existieren |

Der Leer-Zustand ist damit ausschliesslich an "0 Foto-Datensaetze" gebunden und der Platzhalter ausschliesslich an "Datei nicht lesbar".

#### Akzeptanzkriterien

- **Given** der aktuelle Schritt hat keine Fotos
  **When** der Schritt angezeigt wird
  **Then** zeigt der Karussell-Bereich den Leer-Zustand mit dem Hinweis "Keine Fotos zu diesem Schritt"
  **And** es erscheint kein Foto-Indikator
  **And** es erscheint kein Platzhalter-Bild (das ist der Fall "fehlende Datei", US-006.8)

- **Given** der aktuelle Schritt hat keine Fotos
  **When** der Consumer den State dafuer aufbaut
  **Then** setzt er `sichtbaresFotoIndex = -1` ("kein sichtbares Foto"); `0` ist hier kein gueltiger Wert
  **And** der Browser stellt den Leer-Zustand dar, ohne den bereitgestellten State zu korrigieren oder einen Callback zu melden (davon unberuehrt bleibt der Reset beim **Schrittwechsel**, der `onFotoGewaehlt(-1)` meldet — US-006.10)

- **Given** ein Schritt hat keine Fotos
  **When** die Thumbnail-Leiste angezeigt wird
  **Then** zeigt sein Thumbnail das Platzhalter-Bild mit der Schrittnummer
  **And** das Thumbnail traegt die neutrale Markierung

- **Given** der aktuelle Schritt hat keine Fotos
  **When** der Schritt in einem beliebigen der drei Modi angezeigt wird
  **Then** ist keine Label-Anzeige sichtbar (es gibt kein Foto zum Beschriften)
  **And** es ist keine Aktion am sichtbaren Foto sichtbar

- **Given** der aktuelle Schritt hat keine Fotos
  **When** der Schritt angezeigt wird
  **Then** bleiben Thumbnail-Leiste und Vor-/Zurueck-Bedienelemente unveraendert nutzbar (deaktiviert sind sie nur an den Raendern der Anzeige-Reihenfolge, US-006.10)

- **Given** der Vorgang enthaelt ueberhaupt keine Schritte
  **When** der Browser angezeigt wird
  **Then** bleibt die Thumbnail-Leiste leer und der Karussell-Bereich zeigt den Leer-Zustand
  **And** beide Vor-/Zurueck-Bedienelemente sind deaktiviert
  **And** der Browser meldet keinen Fehler (ein erklaerender Hinweistext auf Screen-Ebene ist Consumer-Sache)

- **Given** der Vorgang enthaelt ueberhaupt keine Schritte
  **When** der Consumer den State dafuer aufbaut
  **Then** setzt er `aktuellerIndex = -1` und `sichtbaresFotoIndex = -1` (Definition siehe "Interface-Skizze")
  **And** kein Thumbnail ist hervorgehoben

- **Given** die Schrittliste ist leer und der Consumer uebergibt trotzdem `aktuellerIndex = 0` (oder einen anderen Index ausserhalb des gueltigen Bereichs)
  **When** der Browser angezeigt wird
  **Then** verhaelt er sich genau wie bei `-1`: Leer-Zustand, deaktiviertes Vor/Zurueck, kein Absturz
  **And** er korrigiert den State nicht und meldet keinen Callback

---

### US-006.10: Schrittweise vor- und zurueckblaettern

**Als** Mechaniker
**moechte ich** mit einem Tap zum benachbarten Schritt wechseln
**damit** ich mich der Reihe nach durch die Dokumentation arbeiten kann, ohne im Thumbnail das richtige Bild treffen zu muessen.

Die Schritt-Navigation gehoert **vollstaendig** F-006: Thumbnail-Sprung (US-006.2) und Vor/Zurueck (diese Story) sind die einzigen beiden Wege, den Schritt zu wechseln. F-001 (Blaettern im Archiv) und F-004 (Zurueck/Weiter in der Montage) bringen dafuer keine eigenen Bedienelemente mit, sondern verweisen auf diese Story. Wie in US-006.2 schreibt der Browser den Index nicht selbst fort — er meldet nur die Absicht.

**Reset des sichtbaren Fotos:** Sobald der angezeigte Schritt gewechselt hat, setzt der Browser das Karussell auf das erste Foto des neuen Schritts und meldet das ueber `onFotoGewaehlt`. Das ist seine Pflicht, nicht die des Consumers: Der Consumer haelt `sichtbaresFotoIndex` zwar weiter (er braucht ihn fuer Label-Anzeige und Foto-Aktion), uebernimmt aber nur den gemeldeten Wert und setzt ihn nie selbst zurueck.

#### Akzeptanzkriterien

- **Given** die Anzeige-Reihenfolge hat 15 Schritte und der aktuelle Schritt steht an Position 5
  **When** der Mechaniker "Weiter" antippt
  **Then** wird Position 6 als gewuenschter Schritt gemeldet

- **Given** die Anzeige-Reihenfolge hat 15 Schritte und der aktuelle Schritt steht an Position 5
  **When** der Mechaniker "Zurueck" antippt
  **Then** wird Position 4 als gewuenschter Schritt gemeldet

- **Given** der Consumer hat den Index fortgeschrieben
  **When** die Ansicht aktualisiert wird
  **Then** zeigt das Karussell das erste Foto des neuen Schritts
  **And** der Browser meldet diesen Reset ueber `onFotoGewaehlt(0)` (bzw. `-1`, wenn der neue Schritt keine Fotos hat)
  **And** der Consumer uebernimmt den gemeldeten Wert; er setzt das sichtbare Foto **nicht** selbst zurueck
  **And** das Thumbnail des neuen Schritts ist hervorgehoben und in den sichtbaren Bereich der Leiste gescrollt

- **Given** der aktuelle Schritt ist der **erste** der Anzeige-Reihenfolge
  **When** die Ansicht dargestellt wird
  **Then** ist "Zurueck" sichtbar, aber deaktiviert
  **And** ein Tap darauf meldet nichts

- **Given** der aktuelle Schritt ist der **letzte** der Anzeige-Reihenfolge
  **When** die Ansicht dargestellt wird
  **Then** ist "Weiter" sichtbar, aber deaktiviert
  **And** ein Tap darauf meldet nichts
  **And** der Browser aendert weder Beschriftung noch Bedeutung des Elements (ein Abschluss- oder Archivieren-Verhalten am letzten Schritt gehoert dem Consumer, siehe F-004 US-004.5)

- **Given** der Vorgang hat genau einen Schritt
  **When** die Ansicht dargestellt wird
  **Then** sind "Zurueck" und "Weiter" beide deaktiviert

- **Given** ein beliebiger Schritt wird angezeigt
  **When** der Mechaniker vor- oder zurueckblaettert
  **Then** werden dabei keine Daten veraendert (kein Schritt wird abgeschlossen, abgehakt oder geloescht)

- **Given** der Browser laeuft in einem der beiden lesenden Modi
  **When** der Mechaniker vor- oder zurueckblaettert
  **Then** funktioniert das Blaettern unveraendert (Vor/Zurueck ist in allen drei Modi verfuegbar)

---

### US-006.11: Eine Aktion am gerade sichtbaren Foto ausloesen

**Als** Mechaniker
**moechte ich** eine Aktion direkt an dem Foto ausloesen koennen, das ich gerade sehe
**damit** ich zum Beispiel ein misslungenes Foto wiederholen kann, ohne vorher zu ueberlegen, welches Foto ich damit treffe.

Der Browser fuehrt die Aktion nicht aus und kennt ihre Bedeutung nicht. Er stellt nur die Andockstelle bereit, weil er als Einziger weiss, welches Foto sichtbar ist. F-003 haengt hier "Wiederholen" ein; F-004 und F-001 nutzen die Andockstelle nicht.

#### Akzeptanzkriterien

- **Given** der Browser laeuft im bearbeitbaren Modus und der Consumer hat eine Foto-Aktion mit der Bezeichnung "Wiederholen" uebergeben
  **When** ein Foto im Karussell sichtbar ist
  **Then** ist neben dem Karussell ein Bedienelement mit dieser Bezeichnung sichtbar

- **Given** das Bedienelement ist sichtbar und das Karussell zeigt Foto 2 von 4
  **When** der Mechaniker es antippt
  **Then** wird die Aktion fuer genau dieses Foto gemeldet
  **And** der Browser aendert von sich aus nichts an Foto, Schritt oder Labeln

- **Given** der Consumer hat keine Foto-Aktion uebergeben
  **When** der Schritt angezeigt wird
  **Then** ist kein solches Bedienelement sichtbar und der Platz wird nicht freigehalten

- **Given** der Browser laeuft in einem der beiden lesenden Modi und der Consumer uebergibt trotzdem eine Foto-Aktion
  **When** der Schritt angezeigt wird
  **Then** ist kein Bedienelement sichtbar (Aktionen am Foto gibt es nur im bearbeitbaren Modus)

- **Given** der aktuelle Schritt hat keine Fotos
  **When** der Schritt im bearbeitbaren Modus angezeigt wird
  **Then** ist kein Bedienelement sichtbar (es gibt kein Foto, auf das sich die Aktion beziehen koennte)

- **Given** das Karussell zeigt einen Platzhalter, weil die Datei fehlt
  **When** der Mechaniker die Foto-Aktion antippt
  **Then** wird sie fuer diesen Foto-Datensatz gemeldet (die Metadaten existieren)

---

## Nicht-funktionale Anforderungen

**Performance (Quality Goal #3):**

- Thumbnails werden auf die Kachelgroesse skaliert geladen, niemals in Originalaufloesung
- Das Karussell-Foto wird auf Bildschirmgroesse skaliert geladen; nur die Vollbild-Anzeige darf die hoehere Aufloesung anfordern
- Eine Leiste mit 50 Schritten scrollt ruckelfrei; nur sichtbare Thumbnails werden geladen
- Der Wechsel zwischen Schritten erfolgt ohne spuerbare Verzoegerung

**Bedienbarkeit (Quality Goal #1):**

- Thumbnails, Vor-/Zurueck-Bedienelemente, Label-Checkboxen und die Foto-Aktion halten die verbindlichen Touch-Target-Mindestmasse aus [../governance.md](../governance.md) ein (Mindesthoehe und Mindestabstand stehen dort; F-006 nennt keine eigenen Zahlen)
- Die Wisch-Geste im Karussell reagiert sofort und folgt dem Finger
- 300ms Debounce auf Thumbnail-Taps, Vor/Zurueck, Checkboxen und die Foto-Aktion (Governance)
- Schrittnummer auf dem Thumbnail mit mindestens 20sp, damit sie mit Armlaenge Abstand lesbar ist
- Ein Tap genuegt fuer Sprung, Blaettern, Vollbild und Label-Wechsel

**Zuverlaessigkeit (Quality Goal #2):**

- Fehlende Foto-Dateien fuehren nie zu einem Absturz, sondern zum Platzhalter-Bild (Governance)
- Der Browser haelt keinen eigenen persistenten Zustand; es gibt nichts, was verloren gehen kann
- Label-Aenderungen werden sofort an den Consumer gemeldet, damit dieser sofort persistieren kann (Sofort-Save)
- In den beiden lesenden Modi kann der Browser keine Datenaenderung ausloesen: er meldet dort weder Label-Aenderungen noch Foto-Aktionen

---

## Technische Hinweise

### Zustandslosigkeit

Der Browser ist ein stateless Composable mit State Hoisting. Kein eigenes ViewModel, kein Repository- oder DAO-Zugriff, keine Coroutine, die Daten schreibt. Einzige komponenteninterne Zustaende sind reine Darstellungszustaende (Scroll-Position der Leiste, geoeffnete Vollbild-Anzeige).

### Interface-Skizze

```kotlin
enum class BrowserModus {
    BEARBEITBAR,          // F-003 Demontage: Label aenderbar, Foto-Aktion sichtbar
    LESEND_MIT_AKTIONEN,  // F-004 Montage: Label nur sichtbar, Schritt-Aktionen beim Consumer
    NUR_LESEN             // F-001 Archiv: Label nur sichtbar, keine Aktionen
}

enum class FotoLabel { BAUTEIL, UEBERSICHT, ABLAGEORT }

/** Optionale Consumer-Aktion am gerade sichtbaren Foto (F-003: "Wiederholen"). */
data class FotoAktion(
    val bezeichnung: String,
    val onAusgeloest: (fotoId: Long) -> Unit
)

data class SchrittBrowserState(
    val schritte: List<SchrittMitFotos>, // Anzeige-Reihenfolge bestimmt der Consumer
    val aktuellerIndex: Int,             // Position in schritte; -1 = kein aktueller Schritt (leere Liste)
    val sichtbaresFotoIndex: Int,        // Position in den Fotos des aktuellen Schritts; -1 = kein Foto
    val modus: BrowserModus
)

@Composable
fun SchrittBrowser(
    state: SchrittBrowserState,
    onSchrittGewaehlt: (index: Int) -> Unit,
    onVorherigerSchritt: () -> Unit,
    onNaechsterSchritt: () -> Unit,
    onFotoGewaehlt: (fotoIndex: Int) -> Unit, // Wischen (US-006.4) UND Reset beim Schrittwechsel
    onLabelGeaendert: (fotoId: Long, label: FotoLabel, aktiv: Boolean) -> Unit,
    fotoAktion: FotoAktion? = null,
    modifier: Modifier = Modifier
)
```

`SchrittMitFotos` ist eine Room-`@Relation` aus `Schritt` und `List<SchrittFoto>` und gehoert der Data-Layer (Datenmodell siehe F-003). Der Browser liest daraus nur `SchrittFoto.id`, `schrittNummer`, `pfad`, `reihenfolge` und die drei Label-Flags. Die `id` braucht er, weil er sie in `onLabelGeaendert(fotoId, ...)` und `fotoAktion.onAusgeloest(fotoId)` zurueckmeldet — sie ist damit ein **Pflichtfeld** der Eingabe, auch wenn der Browser sie nicht anzeigt. `Schritt.id`, `reparaturvorgangId`, `abgeschlossenAm` und `eingebautBeiMontage` liest er nicht.

**Leere Liste und Schritt ohne Fotos (Index-Grenzfaelle):**

| Situation | `aktuellerIndex` | `sichtbaresFotoIndex` | Darstellung |
|---|---|---|---|
| Vorgang ohne Schritte (`schritte` leer) | `-1` | `-1` | Leere Thumbnail-Leiste, Leer-Zustand im Karussell, "Zurueck" und "Weiter" beide deaktiviert (US-006.9) |
| Aktueller Schritt ohne Fotos | `0..lastIndex` | `-1` | Leer-Zustand im Karussell, keine Label-Zeile, keine Foto-Aktion; Leiste und Vor/Zurueck normal nutzbar (US-006.9) |
| Normalfall | `0..lastIndex` | `0..fotos.lastIndex` | Karussell mit Indikator "n von m" |

`-1` ist damit der einzige gueltige Wert fuer "es gibt kein aktuelles Element" — nicht `0`, weil `0` bei einer leeren Liste ein gueltig aussehender, aber unerfuellbarer Index waere. Uebergibt ein Consumer trotzdem einen Wert ausserhalb des gueltigen Bereichs, behandelt der Browser ihn wie den jeweiligen Leer-Zustand und stuerzt nicht ab (defensive Grenzpruefung, siehe US-006.9). Der Browser korrigiert den State dabei nicht und meldet auch nichts zurueck — er haelt keinen eigenen Index.

**Modus-Abhaengigkeit der Callbacks:**

| Callback | BEARBEITBAR | LESEND_MIT_AKTIONEN | NUR_LESEN |
|---|---|---|---|
| `onSchrittGewaehlt`, `onVorherigerSchritt`, `onNaechsterSchritt`, `onFotoGewaehlt` | wird gemeldet | wird gemeldet | wird gemeldet |
| `onLabelGeaendert` | wird gemeldet | wird **nie** gemeldet | wird **nie** gemeldet |
| `fotoAktion.onAusgeloest` | wird gemeldet | Bedienelement wird nicht komponiert | Bedienelement wird nicht komponiert |

`onVorherigerSchritt` und `onNaechsterSchritt` werden nur ausgeloest, wenn das jeweilige Bedienelement aktiv ist — die Grenzpruefung (`aktuellerIndex == 0` bzw. `== schritte.lastIndex`) macht der Browser, nicht der Consumer. Der Consumer setzt daraufhin `aktuellerIndex` neu; der Browser haelt ihn nicht selbst.

**`onFotoGewaehlt` hat zwei Ausloeser:** den Foto-Wechsel im Karussell (US-006.4) und den **Reset beim Schrittwechsel**. Sobald sich `aktuellerIndex` geaendert hat, setzt der Browser das Karussell auf das erste Foto des neuen Schritts und meldet `onFotoGewaehlt(0)` — bzw. `onFotoGewaehlt(-1)`, wenn der neue Schritt keine Fotos hat (Leer-Zustand, US-006.9). Das gilt in allen drei Modi und fuer beide Navigationswege (Thumbnail-Sprung und Vor/Zurueck). Tippt der Nutzer das Thumbnail des bereits aktuellen Schritts an, wechselt der Schritt nicht und es wird nichts gemeldet (US-006.2).

Die Aufgabenteilung dabei: Der **Browser** entscheidet, dass zurueckgesetzt wird, und meldet den neuen Wert. Der **Consumer** haelt `sichtbaresFotoIndex` weiterhin in seinem State — er braucht ihn fuer die Label-Anzeige und `fotoAktion` —, uebernimmt aber ausschliesslich den gemeldeten Wert. Er darf ihn beim Schrittwechsel **nicht** selbst auf `0` setzen; sonst gaebe es zwei Schreiber fuer denselben Wert.

### Kategorie-Ableitung fuer die Einfaerbung

Reine Funktion ohne Compose-Bezug, damit sie als Unit-Test pruefbar ist:

```kotlin
fun kategorieVon(fotos: List<SchrittFoto>): FotoLabel? = when {
    fotos.any { it.istAblageort } -> FotoLabel.ABLAGEORT
    fotos.any { it.istUebersicht } -> FotoLabel.UEBERSICHT
    fotos.any { it.istBauteil } -> FotoLabel.BAUTEIL
    else -> null // neutrale Markierung, auch bei leerer Liste
}
```

Zuordnung des Ergebnisses auf die Markierung (siehe Tabelle in US-006.3):

| `kategorieVon(...)` | Rahmenfarbe | Rahmenstaerke |
|---|---|---|
| `ABLAGEORT` | `MaterialTheme.colorScheme.tertiary` | 3dp |
| `UEBERSICHT` | `MaterialTheme.colorScheme.secondary` | 3dp |
| `BAUTEIL` | `MaterialTheme.colorScheme.primary` | 3dp |
| `null` | `MaterialTheme.colorScheme.outlineVariant` | 1dp |

Die Hervorhebung des aktuellen Schritts (US-006.1) ist davon unabhaengig und wird ueber Groesse und einen zusaetzlichen Selektionsring geloest, damit sie die Kategorie-Farbe nicht ueberschreibt.

### Compose-Bausteine

- **Thumbnail-Leiste:** `LazyRow` mit `rememberLazyListState()`. Auto-Scroll zum aktuellen Schritt per `LaunchedEffect(aktuellerIndex) { state.animateScrollToItem(...) }`.
- **Vor/Zurueck:** zwei Buttons, `enabled = aktuellerIndex > 0` bzw. `enabled = aktuellerIndex in 0 until schritte.lastIndex` (die zweite Form haelt "Weiter" auch bei leerer Liste und bei `aktuellerIndex = -1` deaktiviert). Sichtbar bleiben sie in jedem Fall (deaktiviert, nicht ausgeblendet), damit sich das Layout beim Blaettern nicht verschiebt. Sie sind Teil von `SchrittBrowser.kt` und werden in allen drei Modi komponiert.
- **Karussell:** `HorizontalPager`. Der `PagerState` wird mit `sichtbaresFotoIndex` synchronisiert; Seitenwechsel loest `onFotoGewaehlt` aus, Aenderungen von aussen loesen `scrollToPage` aus. `beyondViewportPageCount` klein halten, um Speicher zu sparen. Der Pager umfasst ausschliesslich die Fotos des aktuellen Schritts — es gibt keinen schrittuebergreifenden Pager, damit Wischen nie den Schritt wechselt.
- **Vollbild:** `Dialog` bzw. Overlay innerhalb der Komponente, keine eigene Navigations-Route — sonst entsteht Kopplung an den NavHost des Consumers.
- **Label-Zeile:** Row mit drei `Checkbox` plus Label-Text; der gesamte Bereich ist tappbar (Touch-Target). In den Modi `LESEND_MIT_AKTIONEN` und `NUR_LESEN` wird derselbe Block mit `enabled = false` komponiert und `onCheckedChange = null` gesetzt — die Label bleiben ablesbar, sind aber weder tappbar noch fokussierbar. Hat der Schritt keine Fotos, wird der Block in allen Modi gar nicht komponiert.
- **Foto-Aktion:** wird nur komponiert, wenn `modus == BEARBEITBAR`, `fotoAktion != null` und der aktuelle Schritt mindestens ein Foto hat. Sie erhaelt beim Tap die `id` des Fotos an `sichtbaresFotoIndex`.

### Bildladen

- Coil `AsyncImage` mit expliziter Groessenbegrenzung: fuer Thumbnails `ImageRequest.size(...)` auf die Kachelgroesse, fuer das Karussell auf Bildschirmbreite. Kein Laden in Originalaufloesung.
- `placeholder` und `error` auf das App-Icon setzen — damit ist die Anforderung "Platzhalter statt Crash" ohne eigene Datei-Existenzpruefung erfuellt.
- `contentScale` so waehlen, dass Thumbnails beschnitten (Crop) und Karussell-/Vollbild-Fotos vollstaendig (Fit) dargestellt werden.

### Package und Strings

- Package: `com.boltmind.app.ui.schrittbrowser/` mit `SchrittBrowser.kt`, `SchrittBrowserState.kt`, `SchrittThumbnailLeiste.kt`, `SchrittFotoKarussell.kt`. Die Vor-/Zurueck-Bedienelemente und die Andockstelle fuer die Foto-Aktion liegen in `SchrittBrowser.kt`, weil sie den Rahmen um Leiste und Karussell bilden. Kein `*ViewModel.kt` und kein `*UiState.kt`, weil die Komponente zustandslos ist und keinem Feature-Package gehoert.
- Alle Texte ("Keine Fotos zu diesem Schritt", "Bauteil", "Uebersicht", "Ablageort", "Zurueck", "Weiter", "%1$d von %2$d") in `res/values/strings.xml`. Die Bezeichnung der Foto-Aktion liefert der Consumer.
- `@Preview` fuer alle drei Modi sowie fuer die Sonderfaelle "fehlende Datei", "Schritt ohne Fotos" und "erster bzw. letzter Schritt" (deaktiviertes Vor/Zurueck).

### Tests

- Die Kategorie-Ableitung und die Index-Grenzfaelle (erster/letzter Schritt, leere Liste mit `aktuellerIndex = -1`, Schritt ohne Fotos mit `sichtbaresFotoIndex = -1`, Index ausserhalb des gueltigen Bereichs) sind reine Unit-Tests (keine DB, kein Emulator noetig).
- Die User Stories werden je als `@Nested inner class` abgebildet (siehe `docs/CODING_RULES.md`); UI-Verhalten (Sprung, Blaettern inkl. deaktivierter Raender, Wischen, Vollbild, Checkbox-Zustand je Modus, Sichtbarkeit der Foto-Aktion) als Compose-UI-Test.
- Je Modus ein UI-Test, der belegt, dass in den lesenden Modi kein `onLabelGeaendert` und kein `fotoAktion.onAusgeloest` ausgeloest werden kann.

---

## Offene Fragen

Siehe [README.md](README.md#offene-fragen).
