# Design-System „BoltMind"

**Status:** verbindlich, seit 2026-07-27
**Quelle:** Design-Prototyp `BoltMind App.dc.html` (claude.ai/design, Projekt `42ab79cf`)
**Gilt für:** alle Screens. Diese Datei ersetzt das frühere Farbschema „Titanium Forge".

Der Prototyp ist die Referenz. Wo diese Datei und der Prototyp sich widersprechen,
gewinnt der Prototyp — mit Ausnahme der unter „Bewusste Abweichungen" aufgeführten
Punkte, die aus der Governance folgen.

---

## 1. Grundsatz

Orange auf Nahezu-Schwarz. Glasflächen über einem farbigen Mesh-Verlauf. Nur Dark Mode,
kein Light Mode.

Das frühere Schema war Cyan `#00E5FF` auf Navy `#060B14`. Die `CLAUDE.md` behauptete
davon abweichend Orange `#FF741F` — beides ist hinfällig. Die Leitfarbe ist jetzt
`#FF7A1A`.

---

## 2. Farbe

Alle Token stehen in `ui/theme/Color.kt`. Der Prototyp enthält 37 Hex- und 98
rgba-Werte; jeder davon ist dort abgebildet, keiner ist interpoliert.

| Rolle | Token | Wert |
|---|---|---|
| Grundfläche | `BoltHintergrund` | `#0B0C0D` |
| Browser, Vollbild | `BoltSchwarz` | `#000000` |
| Leitfarbe | `BoltOrange` | `#FF7A1A` |
| Leitfarbe hell | `BoltOrangeHell` | `#FF9A4D` |
| Erfolg, Label „Bauteil" | `BoltGruen` | `#24C48A` |
| Info, Label „Übersicht" | `BoltBlau` | `#3B9DFF` |
| Fehler | `BoltFehler` | `#EF4444` |
| Text primär | `BoltTextPrimaer` | `#EDEFF1` |

Dazu zwei durchgehende Alpha-Leitern (`BoltWeiss03` … `BoltWeiss97`,
`BoltSchwarz08` … `BoltSchwarz93`), aus denen sich sämtliche Ränder, Glasfüllungen und
Verläufe zusammensetzen.

**Farbcodierung eines Schritts** in der Thumbnail-Leiste, Priorität absteigend:
Ablageort (orange) → Übersicht (blau) → Bauteil (grün) → ohne Label (`#5A6167`).

---

## 3. Typografie

Drei Familien, in `res/font/` als statische TTF, OFL-Lizenzen unter `assets/licenses/`.

| Familie | Schnitte | Rolle |
|---|---|---|
| Barlow Condensed | 700 | Großschrift, Aktionen, Schrittnummer |
| Barlow | 400/500/600/700 | Fließtext, Beschriftungen |
| JetBrains Mono | 500/600/700 | Zahlen: Auftragsnummern, Zeiten, Zähler |

Der Prototyp verwendet 57 verschiedene Textstile. Sie sind in `ui/theme/Type.kt` zu
benannten Rollen verdichtet (`BoltTypo.schrittZifferRiesig`, `BoltTypo.tab`, …);
Einzelfälle bauen über `barlow()`, `barlowCondensed()` und `mono()`.

**Wichtig:** Jeder Stil setzt `includeFontPadding = false` und `LineHeightStyle.Trim.Both`.
CSS misst Zeilenhöhe ab Grundlinie und kennt kein Font-Padding; ohne diese beiden
Einstellungen sitzt jeder Text ein paar dp zu tief. Bei der 118sp großen Schrittnummer mit
Zeilenhöhe 0.72 fällt das sofort auf.

---

## 4. Glas

Der Prototyp setzt `backdrop-filter: blur(14px)` an **53** Stellen. Das ist nicht
Dekoration, sondern die tragende Eigenschaft des Erscheinungsbilds.

### 4.1 Technische Umsetzung

Compose kennt keinen Hintergrundfilter. Echter Hintergrund-Blur braucht `RenderEffect`
und damit **API 31**. Deshalb wurde **minSdk von 26 auf 31 angehoben** — Android 8 bis 11
fallen aus der Unterstützung. Das ist eine Produktentscheidung, keine technische
Notwendigkeit; die Alternative wäre eine zweistufige Darstellung gewesen.

`GlasBuehne` (in `ui/theme/Glas.kt`) zeichnet den Hintergrund einmal pro Frame in eine
`GraphicsLayer` und legt davon **genau eine** unscharfe Kopie an. Jede Glasfläche stanzt
sich daraus ihren Ausschnitt, versetzt um ihre eigene Position. Das ist O(1) in Ebenen,
nicht O(n).

Ohne umgebende `GlasBuehne` entfällt lediglich die Unschärfe; Füllung, Rand und Lichtkante
bleiben. Previews und Tests brauchen deshalb keine Sonderbehandlung.

### 4.2 Rezepte

In `ui/theme/GlasRezepte.kt`, benannt nach der Extraktion:

| Kürzel | Rezept | Verwendung |
|---|---|---|
| GN-05 … GN-12 | neutrale Weiß-Leiter, elf Stufen | Tabs, Sekundärbuttons, Schließer |
| GO-F | Orange flach | aktiver Tab, FAB, „LOS GEHT'S" |
| GO-V | Orange voll | „NÄCHSTES" |
| GO-A | Orange Archiv | „AB INS ARCHIV" |
| GO-T | Orange Timer | Timer im Zustand „LÄUFT" |
| GG-V | Grün voll | „SITZT!" |
| GG-Z | Grün Zustand, flach ohne Verlauf | „DRIN" — Zustand statt Aktion |
| GL | Label-Chip, dreifarbig | Bauteil / Übersicht / Ablageort |
| GD | dunkle Panels | Vorgangskarte, Timer-Kapsel, Badges |
| GS | Sheet | Bottom-Sheets |

Ein Rezept besteht aus Füllung (Farbe oder Verlauf), Randfarbe und -breite, optionaler
Lichtkante (`inset 0 1px 0`) und einer Liste von Leuchten. Leuchten werden über
`setShadowLayer` gezeichnet, damit sie über die Fläche hinausreichen.

---

## 5. Hintergründe

**Mesh-Verlauf** (Übersicht, Neuer Auftrag): sieben **elliptische** Radialverläufe auf
`BoltHintergrund`, darüber die Stahltextur im Overlay-Modus, darüber ein abdunkelnder
Schleier für die Lesbarkeit.

Compose kennt nur kreisrunde Radialverläufe. Die Ellipsen entstehen, indem beim Zeichnen
in Y gestaucht wird. Alle Positionen und Radien sind **Anteile** der Fläche, keine Pixel —
der Hintergrund wächst dadurch mit dem Gerät mit.

**Stahltextur:** Das Original ließ sich nicht exportieren — der Design-Abruf bricht am
256-KiB-Limit mitten im letzten IDAT-Chunk ab. Ersatz ist eine prozedural erzeugte Textur
aus periodischen Sinusanteilen: dadurch **nahtlos kachelbar** (das Original ist es nicht),
512×512 Graustufen statt 1024×1024 RGBA, 112 statt 192 KB, Richtungsanisotropie
dY/dX = 15.3 (deutlich horizontale Bürstung wie im Original).

> **Offen:** Für exakte Übereinstimmung muss das Original manuell exportiert und
> `res/drawable-nodpi/steel_texture.png` ersetzt werden. Ebenso fehlt `splash-loop.mp4`.

---

## 6. Maße und Trefferflächen

Der Prototyp positioniert in einem festen Rahmen von **372 × 806 px**, fast alles absolut
und von unten gemessen. Das ist kein reales Android-Format (1:2.167).

**Regel:** Größen und Abstände werden 1:1 übernommen, die Positionierung nicht. Screens
bauen ein mitwachsendes Layout (Anteile, Gewichte, Arrangement). Gleiches Bild, aber es
darf auf keinem Gerät brechen.

### Trefferflächen

Die Governance fordert 56dp Mindesthöhe und 8dp Mindestabstand. Die **Primäraktionen** des
Entwurfs halten das bereits ein (Rundbuttons 60/86/100/124dp, „LOS GEHT'S" 78dp, FAB 74dp).
Unterschritten wird es nur bei Sekundärelementen. Diese sind in `Dimensions.kt` angehoben:

| Element | Entwurf | Umsetzung |
|---|---|---|
| Tabs | 52dp | 56dp |
| Zurück-Chip | 46dp | 56dp |
| „NEU KNIPSEN" | 44dp | 56dp |
| „FEIERABEND" | 50dp | 56dp |
| Schließer | 50dp | 56dp |
| Timer-Schalter | 54dp | 56dp |
| Label-Chips | 46dp | 56dp |
| Abstände | 7dp | 8dp |

**Eine dokumentierte Ausnahme:** Inaktive Thumbnails bleiben optisch bei 54dp. Ihre Größe
trägt Bedeutung — F-006 verlangt, dass der aktive Schritt größer dargestellt ist. Beide auf
66dp zu ziehen würde die Hervorhebung zerstören. Stattdessen bleibt die optische Kachel bei
54dp und nur die **Trefferfläche** wächst auf 56dp. Die Regel ist damit erfüllt.

---

## 7. Bewusste Abweichungen vom Prototyp

Zehn Kollisionen zwischen Entwurf und Governance wurden geprüft. In sieben Fällen gewinnt
der Entwurf, in drei die Regel — jeweils dort, wo Datenintegrität dranhängt.

| # | Punkt | Entscheidung |
|---|---|---|
| K-01 | Trefferflächen | **Regel** — Sekundärelemente auf 56dp, Thumbnails mit Ausnahme (oben) |
| K-02 | „Wiederholen" löscht vor dem Kamerastart | **Regel** — erst nach bestätigter Neuaufnahme löschen. Ein Abbruch darf nie ein Foto vernichten. Das neue Foto übernimmt die `reihenfolge` des alten |
| K-03 | Timer von Hand start-/stoppbar | **Entwurf** — die F-005-Spec sagt „läuft durch, keine Pause". Der Entwurf hat einen Play/Pause-Schalter, und `ZeitMessung` trägt Pausen ohnehin (jedes Start/Stopp-Paar ist eine Zeile). **F-005 muss nachgezogen werden** |
| K-04 | Timer auch in der Montage | **Entwurf** — bisher unspezifiziert. `referenzTyp = MONTAGE_SCHRITT` |
| K-05 | Vor/Zurück je Modus verschieden | **Entwurf** für den Button-Satz, **Regel** für die Ränder: an den Enden sichtbar deaktiviert statt still klemmend |
| K-06 | Zwei Ausstiege in der Montage | **Regel** — „✕" nur noch im Archiv, „RAUS" bleibt in der Montage |
| K-07 | Aktionen unterscheiden betrachteten und offenen Schritt nicht | **Regel** — am offenen Schritt „NÄCHSTES nn", beim Nachschlagen „ZURÜCK ZU nn". Sonst verbrennt ein Fehltipp eine Schrittnummer, die schon auf einem Etikett klebt |
| K-08 | Werkstatt-Wortlaute statt Glossar-Begriffen | **Entwurf** — die DDD-Regel bindet Code und Spec, nicht UI-Texte. „Weiter demontieren" und „Montage starten" bleiben wörtlich verbindlich |
| K-09 | Datumsformat | **gemischt** — vier Stufen: `< 60 s` → „Gerade eben", heute → „Heute, hh:mm", gestern → „Gestern, hh:mm", älter → `TT.MM.JJJJ`. Der Monatsname „12. Juli" entfällt (kein Jahr, im Langzeitarchiv mehrdeutig) |
| K-10 | Auswahl-Sheet auch bei 0 Schritten | **Regel** — direkt in die Demontage. „Montage starten" führt bei null Schritten garantiert in eine Sackgasse |

### Nicht gebaut

**Der Kamera-Screen des Prototyps.** Er ist ein Platzhalter — eine Webseite kann die
OS-Kamera nicht starten, deshalb wurde sie nachgebaut; die Kopfzeile sagt selbst
„SYSTEM-KAMERA". Real gilt weiter: `ActivityResultContracts.TakePicture()` plus
FileProvider, keine `CAMERA`-Permission, kein CameraX.

---

## 8. Was das für die bestehenden Specs bedeutet

| Spec | Nachzuziehen |
|---|---|
| `governance.md` | minSdk 31, Trefferflächen-Ausnahme „optische Größe vs. Trefferfläche", UI-Text-Spalte im Glossar |
| `F-001/uebersicht.md` | Vier-Stufen-Datumsregel, Dauer auf der Archivkarte, kein Sheet bei 0 Schritten |
| `F-002/anlegen.md` | „LOS GEHT'S" legt Vorgang **und** Schritt 1 an und springt direkt in die Demontage |
| `F-003/views/schritt-ansicht.md` | Aktionskreise statt Leiste, „ZURÜCK ZU nn" am abgeschlossenen Schritt |
| `F-004/montage.md` | nur ein Ausstieg, Fortschrittsbalken, Abschluss-Screen-Wortlaute |
| `F-005/service.md` | **Pause ist jetzt erlaubt** (K-03), Montage-Messung (K-04) — beides kippt eine bisher bindende MVP-Antwort |
| `F-006/browser.md` | Vor/Zurück ist eine Andockstelle, die der Consumer nutzt oder nicht (K-05) |
| neu | Splash-Screen |
