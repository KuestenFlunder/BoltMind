# F-007: Splash-Screen

## Kontext

Der erste Bildschirm nach dem Start. Er zeigt Wortmarke und Claim, laeuft von selbst durch und uebergibt an die Uebersicht (F-001).

Der Mechaniker steht dabei in der Halle, oft mit Handschuhen, und will arbeiten. Der Splash darf deshalb niemanden aufhalten: er laedt nichts nach, blockiert nichts und ist jederzeit mit einem Tipp irgendwo auf die Flaeche zu ueberspringen. Er ist eine einzige View ohne Workflow und ohne eigenen Zustand — deshalb eine Einzeldatei (siehe `docs/SpecBestPractices.md`, Abschnitt „Wann Ordner, wann Einzeldatei?").

Farben, Schrift und Hintergrund folgen `docs/specs/design-system.md`.

---

## User Stories

### US-007.1: Ohne Wartezeit in die Uebersicht kommen

**Als** Mechaniker
**moechte ich** beim Start kurz sehen, dass die App bereit ist, und sofort weiterkommen
**damit** ich nicht auf eine Animation warten muss, bevor ich mit der Arbeit anfangen kann.

#### Akzeptanzkriterien

- **Given** die App wird gestartet
  **When** der erste Bildschirm erscheint
  **Then** stehen im unteren Drittel untereinander die Wortmarke „BOLTMIND", darunter der Claim „JEDES TEIL FINDET HEIM" und darunter ein waagerechter Ladebalken von 150 × 3 dp

- **Given** der Splash ist sichtbar
  **When** 2,1 Sekunden vergangen sind
  **Then** ist der Ladebalken vollstaendig gefuellt
  **And** er ist von links nach rechts gleichmaessig gelaufen (lineare Geschwindigkeit, kein Beschleunigen)

- **Given** der Splash ist sichtbar und niemand tippt
  **When** 2,4 Sekunden seit dem Erscheinen vergangen sind
  **Then** erscheint die Uebersicht

- **Given** der Splash ist sichtbar
  **When** der Mechaniker eine beliebige Stelle der Flaeche antippt
  **Then** erscheint die Uebersicht sofort, ohne den Ablauf der 2,4 Sekunden abzuwarten

- **Given** der Mechaniker hat den Splash uebersprungen
  **When** die 2,4 Sekunden danach ablaufen
  **Then** geschieht nichts weiter — der Uebergang wird genau einmal ausgeloest

- **Given** die Uebersicht ist erschienen
  **When** der Mechaniker die Zurueck-Geste ausfuehrt
  **Then** kehrt er **nicht** in den Splash zurueck — er liegt nicht mehr auf dem Rueckwaertsstapel

- **Given** der Splash ist sichtbar
  **When** ein Screenreader die Flaeche vorliest
  **Then** ist sie als „Weiter zur Übersicht" beschrieben

#### UI-Verhalten

| Element | Darstellung |
|---|---|
| Grundflaeche | Hintergrund mit abdunkelndem Verlauf darueber (`design-system.md`, Abschnitt „Hintergründe") |
| Wortmarke | „BOLTMIND", Barlow Condensed, weit gesperrt, `BoltTypo.wortmarkeGross`; blendet in 0,8 s ein und faehrt dabei 10dp hoch |
| Claim | „JEDES TEIL FINDET HEIM", `BoltTypo.splashClaim` in `BoltOrangeHell` |
| Ladebalken | Spur in `BoltWeiss14`, Fuellung in `BoltOrange`, Eckenradius 2dp; waechst in 2,1 s von 0 auf volle Breite |
| Ganze Flaeche | eine einzige Trefferflaeche fuer das Ueberspringen, ohne eigene Darstellung und ohne Stauchung beim Tippen |

#### Woertliche UI-Texte

Verbindlich fuer UI-Tests, alle in `res/values/strings_splash.xml`:

| Text | Ressource | Sichtbar |
|---|---|---|
| `BOLTMIND` | `splash_wortmarke` | ja |
| `JEDES TEIL FINDET HEIM` | `splash_claim` | ja |
| `Weiter zur Übersicht` | `splash_ueberspringen` | nein, nur Sprachausgabe |

---

## Nicht-funktionale Anforderungen

**Bedienbarkeit (Quality Goal #1):** Die Trefferflaeche zum Ueberspringen ist der gesamte Bildschirm; das Mindestmass aus `governance.md`, Abschnitt „Touch-Targets", ist damit uebererfuellt. Der Uebergang meldet sich genau einmal, auch bei Doppel-Tap mit Handschuhen.

**Performance (Quality Goal #3):** Der Splash verlaengert den Start nicht. Er laedt keine Daten, wartet auf nichts und haelt keinen Zustand — die 2,4 Sekunden sind eine reine Anzeigedauer, keine Ladezeit.

**Zuverlaessigkeit (Quality Goal #2):** Es gibt nichts zu speichern und nichts zu verlieren. Wird der Screen waehrend der Animation verlassen, laeuft kein Timer weiter.

---

## Technische Hinweise

- `feature/splash/SplashScreen.kt`: ein zustandsloses Composable mit einem einzigen Rueckruf `onFertig`. Kein ViewModel, kein `*UiState`.
- Zeiten als benannte Konstanten in derselben Datei: Ladebalken 2100 ms, Uebergang 2400 ms, Einblendung der Flaeche 400 ms, Einblendung der Wortmarke 800 ms.
- Masse in `ui/theme/Dimensions.kt` (`splashBalkenBreite`, `splashBalkenHoehe`, `splashBalkenRadius`, `splashTextblockAbstandUnten`, `splashElementAbstand`, `splashBalkenAbstand`, `splashEinblendVersatz`).
- Route `splash` ist `startDestination` in `ui/navigation/BoltMindNavHost.kt`. Der Uebergang navigiert zur Uebersicht mit `popUpTo(SPLASH) { inclusive = true }` — das ist die Umsetzung des Akzeptanzkriteriums „nicht mehr auf dem Rueckwaertsstapel".
- Der Ladebalken bekommt seinen Fortschritt als Funktion (`() -> Float`) und wird ueber `graphicsLayer` skaliert, damit 2,1 Sekunden Animation keine Rekomposition ausloesen.
- `@Preview` mit 372 × 806 dp, dem Rahmenmass des Entwurfs.

---

## Offene Fragen

- **[OFFEN]** Der Entwurf legt hinter Wortmarke und Claim eine **Videoschleife** (`assets/splash-loop.mp4`, formatfuellend, Deckkraft 0,85, stumm, endlos). Die Datei liess sich aus dem Design-Projekt nicht exportieren. Der gebaute Stand zeigt stattdessen eine ruhige, einfarbige Flaeche (`BoltRuhigerHintergrund`) — die zwischenzeitlich verwendete Stahltextur ist am 2026-07-27 projektweit entfernt worden. Zu entscheiden: Video nachreichen oder die ruhige Flaeche festschreiben.
- **[OFFEN]** Erscheint der Splash bei **jedem** Start oder nur beim Kaltstart? Der gebaute Stand macht ihn zur Start-Route des NavHost; er erscheint also immer dann, wenn der Prozess neu aufgebaut wird — auch nach einem Kill im Hintergrund waehrend der Arbeit an einem Vorgang. Ob das gewollt ist oder ein Warmstart direkt in die Uebersicht fuehren soll, ist nicht entschieden.

---

## Aenderungshistorie

| Datum | Aenderung |
|---|---|
| 2026-07-27 | Neu angelegt. Grundlage: Design-Prototyp und `design-system.md`, Abschnitt „Was das für die bestehenden Specs bedeutet" (Zeile „neu — Splash-Screen"). |
