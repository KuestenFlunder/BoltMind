# Governance: Projektweite Spec-Regeln

Regeln und Invarianten, die fuer ALLE Feature-Specs gelten. Jede Feature-Spec erbt diese Regeln implizit. Aenderungen hier wirken sich auf alle Features aus.

Fuer alles Visuelle — Farbe, Typografie, Glasflaechen, Hintergruende, Groessen und Abstaende — ist `design-system.md` die verbindliche Quelle. Diese Datei regelt, was darueber hinaus projektweit gilt. Wo beide dieselbe Groesse nennen, gewinnt der hier verankerte Mindestwert (siehe "Touch-Targets").

## Plattform-Randbedingungen

| Aspekt | Wert |
|---|---|
| `minSdk` | **31** (Android 12) |
| `targetSdk` / `compileSdk` | 36 |

**Grund fuer minSdk 31:** Das Design-System setzt Glasflaechen mit echtem Hintergrund-Blur ein — es ist die tragende Eigenschaft des Erscheinungsbilds, nicht Dekoration. Compose erreicht das nur ueber `RenderEffect`, und der existiert erst ab API 31. Die Alternative waere eine zweite, blurfreie Darstellung fuer aeltere Geraete gewesen; sie wurde verworfen (siehe `design-system.md`, Abschnitt "Glas").

**Folge:** Android 8, 9, 10 und 11 (API 26–30) werden nicht unterstuetzt. Kein Feature spezifiziert oder implementiert einen Fallback fuer diese Versionen.

## Sofort-Save Strategie

Jede Nutzeraktion, die Daten erzeugt oder veraendert, wird **sofort** in die DB geschrieben. Kein Batch-Save, kein "Speichern"-Button. Grund: Werkstatt-Umgebung — Unterbrechungen (Anrufe, Kollegen, Akku leer) sind der Normalfall, nicht die Ausnahme.

**Regel:** Wenn eine Aktion DB-relevant ist, wird sie in der gleichen Operation persistiert, in der sie ausgeloest wird. Kein Zwischenpuffer, kein "Speichern beim Verlassen".

### Invariante: `aktualisiertAm`

**Regel:** Jede Aktion, die Daten eines Reparaturvorgangs oder seiner Schritte und Fotos erzeugt, aendert oder loescht, setzt in derselben Operation `Reparaturvorgang.aktualisiertAm` auf den Zeitpunkt der Aktion. Das gilt auch fuer das Archivieren.

Betroffen sind unter anderem: Schritt anlegen, Foto anhaengen, Foto wiederholen, Foto loeschen, Label aendern, Schritt abschliessen, Schritt abhaken und zuruecknehmen, Vorgang archivieren.

**Grund:** `aktualisiertAm` ist der einzige projektweite Zeitstempel fuer "zuletzt angefasst". F-001 sortiert beide Listen (aktive Vorgaenge und Archiv) danach und leitet daraus das Abschlussdatum archivierter Vorgaenge ab. Ein Schreibvorgang, der das Feld nicht mitzieht, verfaelscht die Reihenfolge und das angezeigte Datum.

**Einschraenkung:** Es gibt kein eigenes Archivierungs-Datum. Das im Archiv angezeigte Abschlussdatum ist `aktualisiertAm` zum Zeitpunkt des Archivierens. Da archivierte Vorgaenge nur lesend geoeffnet werden, aendert sich der Wert danach nicht mehr.

Feature-Specs verweisen in ihren DB-Interaktions-Tabellen auf diese Invariante, statt sie neu zu formulieren.

## Bedienbarkeit

### Debounce

Global **300ms** fuer alle interaktiven Buttons. Grund: Mechaniker tragen Handschuhe und haben dreckige Haende — Doppel-Taps sind haeufig und muessen abgefangen werden.

### Touch-Targets

Alle primaeren Aktions-Buttons muessen mit Handschuhen bedienbar sein. **Verbindliche Mindestmasse:**

| Aspekt | Mindestwert |
|---|---|
| Hoehe eines primaeren Aktions-Buttons | **56dp** |
| Abstand zwischen benachbarten Touch-Targets | **8dp** |

Diese Werte sind projektweit verbindlich und pruefbar. Feature-Specs nennen in ihren NFR-Abschnitten **keine eigenen Zahlen**, sondern verweisen auf diesen Abschnitt. Sie gelten auch gegenueber `design-system.md`: wo der Entwurf darunter liegt, wird angehoben.

#### Optische Groesse vs. Trefferflaeche

Die 56dp gelten fuer die **antippbare Flaeche**, nicht zwingend fuer das gezeichnete Element. Ein Element darf kleiner aussehen, als es sich antippen laesst — vorausgesetzt, seine optische Groesse traegt Bedeutung, die Trefferflaeche erreicht 56dp und der 8dp-Abstand zum Nachbarn bleibt gewahrt.

Die Ausnahme ist eng: sie greift nur, wenn die Groesse eine Information transportiert, die sonst verloren ginge. Reine Aesthetik oder Platzmangel reichen nicht.

**Einziger heutiger Anwendungsfall:** die inaktiven Thumbnails der Schritt-Leiste. `browser.md` (F-006) verlangt in US-006.1, dass der aktive Schritt groesser dargestellt wird als die inaktiven; zieht man beide auf dieselbe Groesse, ist die Hervorhebung weg. Die Trefferflaechen liegen weiterhin 8dp auseinander — die kleinere Kachel sitzt zentriert in der groesseren Trefferflaeche, der Abstand schrumpft dadurch nicht.

| Element | Optische Groesse | Trefferflaeche |
|---|---|---|
| Inaktives Thumbnail der Schritt-Leiste | 54dp | 56dp |

Jede weitere Anwendung dieser Ausnahme wird in dieser Tabelle eingetragen und begruendet. Steht ein Element nicht darin, muss es auch optisch 56dp erreichen. Entscheidung und Begruendung siehe `design-system.md`, Abschnitt "Bewusste Abweichungen vom Prototyp", Zeile K-01.

## Foto-Handling

### N Fotos pro Schritt

Ein Schritt haelt **beliebig viele Fotos** (0..n). Es gibt keine festen Foto-Slots am Schritt selbst — jedes Foto ist ein eigener Datensatz mit Reihenfolge innerhalb des Schritts. Grund: Ein Demontage-Schritt braucht je nach Situation ein Detailfoto, mehrere Perspektiven, eine Uebersicht und/oder einen Ablageort — die Anzahl laesst sich nicht vorab festlegen.

### Foto-Label

Jedes Foto traegt drei **unabhaengige, kombinierbare** Label:

| Label | Default | Bedeutung |
|---|---|---|
| Bauteil | gesetzt | Das Bauteil selbst, typischerweise im Zustand vor dem Ausbau |
| Uebersicht | nicht gesetzt | Uebersichtsaufnahme des Umfelds / der Baugruppe |
| Ablageort | nicht gesetzt | Physischer Ort, an dem das ausgebaute Teil abgelegt wurde |

**Regeln:**

- Mehrere Label gleichzeitig sind erlaubt (z.B. Bauteil + Ablageort an einem Foto).
- Auch **alle drei abgewaehlt** ist ein gueltiger Zustand. Kein Zwang zu mindestens einem Label.
- **Prioritaetsregel:** Wo eine einzelne Kategorie gebraucht wird (z.B. Einfaerbung, Filter, Gruppierung), gilt **Ablageort > Uebersicht > Bauteil**. Ein Foto mit den Labeln Bauteil + Ablageort zaehlt dort als Ablageort.
- Ob ein Schritt einen Ablageort dokumentiert, ist aus den Fotos ableitbar — es gibt kein separates Feld und keinen separaten Schritt-Typ dafuer.
- **Label-Aenderung ist sofort-save.** Das An- oder Abwaehlen eines Labels wird unmittelbar persistiert (siehe Sofort-Save Strategie).

### Kamera

Projektweit wird **ausschliesslich die System-Kamera** verwendet:

- Aufnahme via `ActivityResultContracts.TakePicture()` + `FileProvider`. Keine app-eigene Kamera-Implementierung (kein CameraX).
- **Keine CAMERA-Permission** im Manifest. Die System-Kamera-App verwaltet ihre Berechtigung selbst.
- **Keine app-eigene Foto-Bestaetigung.** Die System-Kamera hat ihre eigene Bestaetigung; danach wird das Foto direkt an den Kontext (z.B. den Schritt) gehaengt und sofort persistiert. Es gibt keinen zusaetzlichen Preview-Screen mit "Bestaetigen"/"Wiederholen".
- "Wiederholen" existiert nur als Aktion **am bereits aufgenommenen Foto**. Die Reihenfolge ist verbindlich und in genau dieser Abfolge einzuhalten:
  1. Die System-Kamera wird gestartet. Das alte Foto bleibt dabei unangetastet — weder DB-Zeile noch Datei werden vorher geloescht.
  2. **Erst nach erfolgreicher neuer Aufnahme** werden die alte DB-Zeile und die alte Datei geloescht und durch die neuen ersetzt. Das neue Foto uebernimmt dabei die `reihenfolge` des alten.
  3. Bricht der Nutzer die Kamera ab, bleibt das alte Foto vollstaendig und unveraendert erhalten (DB-Zeile, Datei, `reihenfolge`, Label).

  Grund: Quality Goal "Zuverlaessigkeit" — ein bereits aufgenommenes Foto darf durch einen Kamera-Abbruch nie verloren gehen. Ein "erst loeschen, dann Kamera starten" ist projektweit unzulaessig.
- **Kamera-Abbruch:** Es wird keine DB-Zeile angelegt oder veraendert; geloescht wird ausschliesslich die fuer **diese** Aufnahme vorab erzeugte Zieldatei (siehe Abschnitt "Speicherort"). Beim "Wiederholen" bleiben die alte DB-Zeile und die alte Datei davon unberuehrt. Der Nutzer landet in der aufrufenden Ansicht.

Diese Regel gilt fuer alle Features ohne Ausnahme und ist deshalb hier verankert, nicht in einzelnen Feature-Specs.

### Speicherort

Fotos werden im **app-internen Speicher** abgelegt (`context.filesDir/photos/`). Nicht in der oeffentlichen Galerie. Grund: Datenschutz und Vermeidung versehentlicher Loeschung.

**Es gibt keinen `photos/temp/`-Ordner** — projektweit, auch nicht waehrend des Anlage-Flows in F-002. Die System-Kamera schreibt direkt in die Zieldatei unter `photos/`. Eine app-eigene Bestaetigung, nach der verschoben werden muesste, existiert nicht (siehe Abschnitt "Kamera"). Wird die Kamera abgebrochen oder ein Anlage-Flow verworfen, wird die angelegte Datei sofort geloescht.

**Cleanup-Regel (projektweit):** Beim App-Start werden alle Dateien in `photos/` geloescht, auf die keine DB-Zeile verweist — geprueft gegen `SchrittFoto.pfad` **und** `Reparaturvorgang.fahrzeugFotoPfad`. Das raeumt verwaiste Zieldateien aus abgebrochenen Kamera-Starts und verworfenen Anlage-Flows auf. Die Regel gilt fuer alle Features; Feature-Specs verweisen darauf, statt sie zu wiederholen.

**Sicherheitsregel:** Laesst sich die Datenbank nicht lesen, wird **nichts** geloescht. Ein fehlgeschlagener Lesevorgang darf nie als "keine Datei ist referenziert" gedeutet werden — sonst raeumt ein einzelner Fehler den kompletten Fotobestand ab, und der Verlust faellt erst Wochen spaeter im Archiv auf. Im Fehlerfall bleibt `photos/` unangetastet und der Cleanup wird beim naechsten Start erneut versucht.

Der Cleanup laeuft nebenlaeufig zum Start; er blockiert den ersten Screen nicht.

### Qualitaet

**Erwartungswert:** mittlere Kompression, ca. 2-3 MB pro Foto. Balance zwischen Qualitaet (Schrauben-Positionen muessen erkennbar sein) und Speicherplatz.

Dieser Wert ist eine Erwartung, **keine erzwingbare Anforderung**: Aufloesung und Kompression bestimmt die System-Kamera-App (siehe Abschnitt "Kamera"). Die App bekommt nur die fertige Datei und **uebernimmt sie unveraendert**; sie komprimiert nicht nach. Liefert ein Geraet deutlich groessere Dateien, ist das kein Spec-Verstoss.

- [ ] **OFFEN:** Optionale Nachkompression beim Uebernehmen (Herunterskalieren + JPEG-Requantisierung), falls die gelieferten Dateien in der Praxis den Speicher zu stark belasten. Bis zu einer Entscheidung ist das **keine Anforderung** und in keinem Feature zu implementieren.

### Fehlende Dateien

Wenn eine Foto-Datei fehlt (z.B. nach Backup/Restore oder manuellem Loeschen), wird ein **Platzhalter-Bild** angezeigt. Kein Crash, kein leerer Screen.

### Datensicherheit

- Keine Metadaten (GPS, Zeitstempel) in EXIF-Daten (Datenschutz)
- Fotos nur im app-internen Speicher, nicht extern zugaenglich

## Unterbrechungs-Verhalten

Die App muss jederzeit unterbrechbar sein, ohne Datenverlust. Beim naechsten Start wird der letzte konsistente Zustand wiederhergestellt. Jedes Feature definiert sein eigenes Unterbrechungs-Verhalten (welcher Screen wird bei Fortsetzung angezeigt), aber die Grundregel ist global:

**Regel:** Kein Datenverlust bei App-Unterbrechung. Sofort-Save + definierter Fortsetzungspunkt.

## DDD-Sprache

Domain-Begriffe auf **Deutsch**, technische Begriffe auf **Englisch**. Funktionsnamen fuer Domain-Events ebenfalls Deutsch.

Diese Regel bindet **Code und Spec, nicht die Oberflaeche.** Die App spricht Werkstatt-Sprache: kurz, gross, aus dem Mund eines Mechanikers. Wo der sichtbare Text vom Glossar-Begriff abweicht, steht er in der Spalte "UI-Text" und ist dort **woertlich verbindlich**. Ohne diese Spalte faende ein Reviewer den Text auf dem Bildschirm in keiner Spec wieder.

Die UI-Texte liegen als Ressourcen in `res/values/strings*.xml` und sind dort gegen die Spec pruefbar.

| Domain (DE) | Bedeutung | UI-Text (woertlich) |
|---|---|---|
| Reparaturvorgang | Ein Reparaturauftrag an einem Fahrzeug | "Auftrag" — `NEUER AUFTRAG`, `AUFTRAGSNUMMER`. Das Wort "Reparaturvorgang" erscheint nirgends auf dem Bildschirm |
| Schritt | Ein einzelner Demontage-/Montage-Schritt, haelt N Fotos | "Schritt" beim einzelnen (`SCHRITT`, `SCHRITT 12`), "Teil" beim Zaehlen (`7 TEILE`, `TEILE`) und im Claim `JEDES TEIL FINDET HEIM`. Die Weiter-Aktion heisst `NÄCHSTES` ohne Substantiv |
| SchrittFoto | Ein einzelnes Foto eines Schritts, mit Reihenfolge und Labeln | "Foto" — `FOTO 2/5`, `KEIN FOTO`, `NOCH'N FOTO` |
| Foto-Label | Bauteil / Uebersicht / Ablageort — kombinierbar, Default Bauteil | `BAUTEIL`, `ÜBERSICHT`, `ABLAGEORT`; im Vollbild in gemischter Schreibweise `Bauteil · Übersicht`, ohne Label `ohne Label` |
| Ablageort | Physischer Ort, an dem ein ausgebautes Teil abgelegt wird — dokumentiert als Foto-Label, nicht als eigener Schritt | `ABLAGEORT`. Fehlt das Label ganz, zeigt die Montage `AM FAHRZEUG GEBLIEBEN` |
| ZeitMessung | Eine Timer-Messung mit Start/Stopp (Service F-005) | `LÄUFT` / `STEHT` am Timer-Schalter |
| eingebautBeiMontage | Der Schritt ist bei der Montage wieder verbaut | `DRIN` fuer den Zustand, `SITZT!` fuer die Aktion, die ihn setzt, `%d VON %d DRIN` fuer den Fortschritt |
| Beenden | Den Vorgang verlassen, ohne ihn abzuschliessen | `FEIERABEND` in der Demontage, `RAUS` in der Montage |

Entscheidung und Begruendung siehe `design-system.md`, Abschnitt "Bewusste Abweichungen vom Prototyp", Zeile K-08.

- [ ] **OFFEN:** Glossar-Begriff fuer das Beenden festlegen. Der Code nennt das Domain-Event `onVerlassen`, die Tabelle oben "Beenden". Eines von beiden ist zu waehlen und das andere anzugleichen; bis dahin ist nur der UI-Text (`FEIERABEND` / `RAUS`) verbindlich.

## Service-Architektur

Features, die als eigenstaendige Services implementiert werden (z.B. Zeiterfassung), folgen diesen Regeln:

1. **Eigene Datenhaltung:** Der Service besitzt seine eigene Tabelle. Keine Dual-Purpose-Felder in fremden Entities.
2. **Keine Feature-Kenntnis:** Der Service weiß nicht, welches Feature ihn aufruft. Er arbeitet mit generischen Referenzen (`referenzId`, `referenzTyp`).
3. **Consumer beschreibt Nutzung:** Die Integration wird in der Feature-Spec beschrieben, nicht in der Service-Spec.
4. **Klare Schnittstelle:** Der Service definiert sein Interface (start/stop/query). Consumer rufen dieses Interface auf.

## Aenderungshistorie

| Datum | Aenderung |
|---|---|
| 2026-07-27 | `design-system.md` als verbindliche Quelle fuer alles Visuelle verankert |
| 2026-07-27 | Abschnitt "Plattform-Randbedingungen": minSdk 31, Android 8–11 nicht mehr unterstuetzt |
| 2026-07-27 | Touch-Targets: Unterscheidung "optische Groesse vs. Trefferflaeche" mit Ausnahmetabelle (K-01) |
| 2026-07-27 | DDD-Glossar um die Spalte "UI-Text" und die Eintraege `eingebautBeiMontage` und `Beenden` erweitert (K-08); Begriffswahl "Beenden" vs. `Verlassen` offen |
| 2026-07-27 | Cleanup verwaister Foto-Dateien: Sicherheitsregel bei nicht lesbarer Datenbank |
