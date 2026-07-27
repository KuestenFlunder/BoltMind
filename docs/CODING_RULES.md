# BoltMind Coding Rules

## Architektur

### Pattern: MVVM (Model-View-ViewModel)

- **View** (Compose Screens): Stateless, empfängt State und Events vom ViewModel
- **ViewModel**: Hält UI-State als `StateFlow`, verarbeitet User-Aktionen, delegiert an Repository/UseCase
- **Model** (Data Layer): Room Entities, Repositories, DAOs

```
Screen (Composable)
    ↓ observes StateFlow
ViewModel
    ↓ calls
Repository
    ↓ queries/inserts
Room DAO → SQLite
```

### Package-Struktur: Feature-basiert

```
com.boltmind.app/
├── di/                              # Koin Module
│   └── AppModule.kt
├── data/
│   ├── local/
│   │   ├── BoltMindDatabase.kt      # Room Database
│   │   ├── ReparaturvorgangDao.kt
│   │   ├── SchrittDao.kt
│   │   ├── SchrittFotoDao.kt        # Fotos eines Schritts (F-003)
│   │   └── ZeitMessungDao.kt        # Timer-Service DAO
│   ├── model/
│   │   ├── Reparaturvorgang.kt      # Room Entity
│   │   ├── Schritt.kt              # Room Entity (ohne Typ, hält keine Foto-Pfade)
│   │   ├── SchrittFoto.kt          # Room Entity (0..n je Schritt, drei Label-Flags)
│   │   ├── SchrittMitFotos.kt      # Room @Relation (Schritt + List<SchrittFoto>)
│   │   └── ZeitMessung.kt          # Room Entity (Timer-Service)
│   └── repository/
│       └── ReparaturRepository.kt
├── feature/
│   ├── uebersicht/                  # F-001
│   │   ├── UebersichtScreen.kt
│   │   └── UebersichtViewModel.kt
│   ├── neuervorgang/                # F-002
│   │   ├── NeuerVorgangScreen.kt
│   │   └── NeuerVorgangViewModel.kt
│   ├── demontage/                   # F-003
│   │   ├── DemontageScreen.kt
│   │   └── DemontageViewModel.kt
│   └── montage/                     # F-004
│       ├── MontageScreen.kt
│       └── MontageViewModel.kt
├── service/
│   └── zeiterfassung/               # F-005: Timer-Service
│       └── ZeiterfassungService.kt
├── ui/
│   ├── navigation/
│   │   └── BoltMindNavHost.kt
│   ├── schrittbrowser/              # F-006: gemeinsames UI-Modul, stateless,
│   │   ├── SchrittBrowser.kt        #        kein ViewModel, kein UiState
│   │   ├── SchrittBrowserState.kt
│   │   ├── SchrittThumbnailLeiste.kt
│   │   └── SchrittFotoKarussell.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── MainActivity.kt
└── BoltMindApplication.kt
```

`ui/schrittbrowser/` ist bewusst **kein** Feature-Package: der Schritt-Browser (F-006) wird von F-001,
F-003 und F-004 gemeinsam genutzt, ist zustandslos (State Hoisting) und greift weder auf Repository
noch auf DAO zu. Deshalb dort kein `*ViewModel.kt` und kein `*UiState.kt`.

## Sprache & Naming (DDD)

### Domain-Begriffe: Deutsch

Domänen-Klassen und -Felder verwenden die deutsche Fachsprache aus der Architecture-Doku.

| Domain-Begriff | Klasse / Variable | Nicht verwenden |
|----------------|-------------------|-----------------|
| Reparaturvorgang | `Reparaturvorgang` | `RepairJob` |
| Schritt | `Schritt` | `Step` |
| SchrittFoto | `SchrittFoto`, `schrittFotoDao` | `StepPhoto` |
| Foto-Label Bauteil | `istBauteil` | `isComponent` |
| Foto-Label Übersicht | `istUebersicht` | `isOverview` |
| Foto-Label Ablageort | `istAblageort` | `isStorageLocation` |
| Reihenfolge (Foto im Schritt) | `reihenfolge` | `order`, `index` |
| Fahrzeugfoto | `fahrzeugFotoPfad` | `vehiclePhotoPath` |
| Auftragsnummer | `auftragsnummer` | `orderNumber` |
| Beschreibung | `beschreibung` | `description` |
| ZeitMessung | `ZeitMessung` | `TimeMeasurement` |

**Ein Schritt hält keine Foto-Pfade und keinen Typ.** `bauteilFotoPfad`, `ablageortFotoPfad`, `typ`
und das Enum `SchrittTyp` entfallen ersatzlos (F-003 README, governance.md). Ein Schritt hat 0..n
`SchrittFoto`-Zeilen; **der Ablageort ist ein Foto-Label, kein eigener Schritt und kein Schritt-Typ.**
Die drei Label sind unabhängig und kombinierbar, Default ist `istBauteil = true`.

### Technische Begriffe: Englisch

Alles was nicht Domain ist, bleibt englisch:

```kotlin
// Domain: Deutsch
data class Reparaturvorgang(
    val id: Long = 0,
    val fahrzeugFotoPfad: String,
    val auftragsnummer: String,
    val beschreibung: String? = null,
    val status: VorgangStatus,
    val erstelltAm: Instant
)

// Technisch: Englisch
class ReparaturRepository(
    private val vorgangDao: ReparaturvorgangDao,
    private val schrittDao: SchrittDao
)
class DemontageViewModel(private val repository: ReparaturRepository) : ViewModel()
@Composable fun DemontageScreen(viewModel: DemontageViewModel)
```

### Naming Conventions

| Artefakt | Konvention | Beispiel |
|----------|-----------|---------|
| Entity | PascalCase, Domänen-Name | `Reparaturvorgang`, `Schritt` |
| DAO | Entity + Dao | `ReparaturvorgangDao` |
| Repository | Domäne + Repository | `ReparaturRepository` |
| ViewModel | Feature + ViewModel | `DemontageViewModel` |
| Screen | Feature + Screen | `DemontageScreen` |
| State | Feature + UiState | `DemontageUiState` |
| Package | lowercase, Feature-Name | `feature.demontage` |

### String-Ressourcen

Ein Key trägt **eine** Bedeutung. Zwei Regeln folgen daraus:

- **Keys der Schritt-Navigation (F-006) tragen das Präfix `browser_`** — `browser_zurueck`,
  `browser_weiter`, `browser_naechstes`. Sie gehören dem Inhaltsbereich.
- **Screen-Chrome vergibt die Beschriftung „Zurück" nie.** Der Ausstieg aus einem Screen läuft über
  die Android-Zurück-Geste und den Zurück-Pfeil in der TopBar; dessen contentDescription heißt
  `zurueck_navigation_beschreibung`.

Sonst kollidieren zwei Bedeutungen auf einem Key und in Tests wie Screenshots ist nicht mehr
unterscheidbar, welches Element gemeint war.

Obsolete Keys sterben in der Änderung, die ihren letzten Consumer löscht. Neue Keys entstehen dort,
wo sie gerendert werden. Ein Sammel-Issue zum Strings-Aufräumen gibt es bewusst nicht.

## Dependency Injection: Koin

```kotlin
// di/AppModule.kt
val appModule = module {
    single { BoltMindDatabase.create(get()) }
    single { get<BoltMindDatabase>().reparaturvorgangDao() }
    single { get<BoltMindDatabase>().schrittDao() }
    single { get<BoltMindDatabase>().schrittFotoDao() }
    single { ReparaturRepository(get(), get()) }
    viewModel { UebersichtViewModel(get()) }
    viewModel { DemontageViewModel(get()) }
}
```

## Testing: TDD (verbindlich)

### Grundregel

Kein Produktivcode ohne vorherigen Test. TDD ist keine Empfehlung, sondern der verbindliche Entwicklungsflow für jede Code-Änderung.

### TDD-Zyklus (zwingend bei jeder Änderung)

```
1. RED:    Test schreiben / anpassen → Test muss fehlschlagen
           └─ ./gradlew test  (rot bestätigen)
2. GREEN:  Minimal implementieren bis Test grün
           └─ ./gradlew test  (grün bestätigen)
3. REFACTOR: Code aufräumen, Tests müssen grün bleiben
           └─ ./gradlew test  (grün bestätigen)
```

Der Zyklus schreibt bewusst **nur real existierende Gradle-Tasks** vor. `ktlintCheck` und `detekt`
sind im Projekt nicht eingerichtet und deshalb kein Pflichtschritt — siehe
[Werkzeug-Lücken](#werkzeug-lücken-stand-2026-07-26). Optional zusätzlich verfügbar und lauffähig:
`./gradlew lint` (Android Lint) und `./gradlew assembleDebug`.

### Gilt für ALLE Änderungstypen

| Änderungstyp | Test-Aktion |
|--------------|-------------|
| Neues Feature | Neue Tests aus Spec-User-Stories ableiten (US-XXX.Y → @Nested class) |
| Bugfix | Erst Test schreiben der den Bug reproduziert, dann fixen |
| Refactoring | Bestehende Tests müssen vor UND nach dem Refactoring grün sein |
| Spec-Änderung | Betroffene Tests anpassen bevor Code angepasst wird |

### Verboten

- Produktivcode schreiben bevor der zugehörige Test existiert
- Tests nach der Implementierung schreiben ("test-after")
- Tests deaktivieren, löschen oder überspringen um Build grün zu bekommen
- Implementierung ohne abschließendes `./gradlew test`

### Test-Typen

1. **Unit Tests**: ViewModels und Repositories (Hauptfokus)
2. **Integration Tests**: Room Database, Repository + DAO zusammen
3. **UI Tests**: Erst wenn die UI steht (nicht im MVP-Startfokus)

### Test-Struktur

Ist-Stand, nicht Zielbild:

```
app/src/
├── test/java/com/boltmind/app/               # JVM, JUnit 5
│   ├── feature/
│   │   ├── FormatierungTest.kt               # Datums- und Zeitformate
│   │   ├── abschluss/AbschlussViewModelTest.kt
│   │   └── browser/BrowserViewModelTest.kt   # F-003, F-004 und F-001-Archiv
│   ├── data/
│   │   ├── foto/FotoManagerTest.kt
│   │   └── repository/ReparaturRepositoryTest.kt
│   ├── service/zeiterfassung/ZeiterfassungServiceTest.kt
│   └── ui/components/DebounceClickTest.kt
└── androidTest/java/com/boltmind/app/        # Geraet/Emulator, JUnit 4
    └── data/local/MigrationTest.kt           # Room-Migrationen
```

Ein einziger `BrowserViewModelTest` deckt Demontage, Montage und Archiv ab, weil ein einziges
ViewModel alle drei Betriebsarten bedient. Getrennte `DemontageViewModelTest`/`MontageViewModelTest`
gibt es nicht mehr.

**Beide Zweige sind verbindlich.** `./gradlew test` und `./gradlew connectedDebugAndroidTest` laufen
und sind zu benutzen — Letzteres braucht ein Gerät oder den Emulator (siehe `CLAUDE.md`).

#### Konvention für `androidTest`

Instrumentierte Tests laufen über `AndroidJUnitRunner` und damit unter **JUnit 4**, nicht JUnit 5.
`@Nested inner class` gibt es dort nicht. Äquivalent:

| JVM (JUnit 5) | Instrumentiert (JUnit 4) |
|---|---|
| `@Nested inner class \`US-XXX_Y ...\`` | eine **Testklasse** je User Story, benannt `UsXxxYBeschreibung` |
| `@Test fun \`Verhalten aus dem Then\`()` | `@Test fun verhaltenAusDemThen()` — Backticks sind auf dem Gerät zulässig, aber `@DisplayName` fehlt, also trägt der Methodenname die Aussage |
| `@BeforeEach` / `@AfterEach` | `@Before` / `@After` |

Instrumentierte Tests werden **lokal vor dem PR** ausgeführt. Es gibt keine Build-Pipeline, die das
übernimmt — wer sie nicht ausgeführt hat, schreibt das in den PR, statt sie als grün zu melden.

### User-Story-Traceability in Tests

Jede User Story aus der Spec (`US-XXX.Y`) wird als `@Nested inner class` im zugehörigen Test abgebildet. So lässt sich jeder Test direkt auf eine User Story und deren Akzeptanzkriterien zurückverfolgen.

**Regeln:**

1. **1 User Story = 1 `@Nested inner class`** mit der US-ID im Namen
2. **1 Akzeptanzkriterium = 1 `@Test`** — Testname beschreibt das erwartete Verhalten
3. **Given/When/Then** aus der Spec als Kommentare im Test-Body (Arrange/Act/Assert)
4. **Spec ist Source of Truth** — Tests leiten sich aus den Akzeptanzkriterien ab, nicht umgekehrt

### Der `aktualisiertAm`-Guard

`governance.md` verlangt, dass **jede** datenverändernde Repository-Operation in derselben Operation
`Reparaturvorgang.aktualisiertAm` nachzieht. Verstöße sind im Betrieb unsichtbar — die Übersicht
sortiert dann faktisch nach Erstellungsdatum statt nach letzter Bearbeitung, und das Abschlussdatum
im Archiv stimmt nicht.

Deshalb wird die Invariante nicht nur aufgeschrieben, sondern **erzwungen**. `ReparaturRepositoryTest`
führt vier Listen, in die jede öffentliche Methode des Repositories einsortiert sein muss:

| Liste | Bedeutung |
|---|---|
| `UEBER_DEN_TRICHTER` | schreibt Daten und stempelt über den Transaktions-Helper — ein `@TestFactory` prüft jede einzeln |
| `EIGENER_ZEITSTEMPEL` | setzt `aktualisiertAm` selbst (Anlegen, Archivieren) — braucht einen eigenen Test |
| `OHNE_ZEITSTEMPEL` | schreibt bewusst ohne zu stempeln |
| `NUR_LESEND` | liest nur |

Ein zweiter Test liest die tatsächliche Methodenliste per Reflection und vergleicht sie mit der
Summe der vier Listen — in **beide** Richtungen. Eine neue Methode ohne Einsortierung lässt ihn
fehlschlagen, ein Eintrag ohne Methode ebenso.

**Regel: Wer eine öffentliche Repository-Methode anlegt, umbenennt oder löscht, pflegt diese Listen
mit.** Das Fehlschlagen ist Absicht und keine Testschwäche.

### Test-Naming

Konvention: `@Nested inner class` mit US-ID, `@Test` mit Verhaltensbeschreibung aus der Spec.

```kotlin
class UebersichtViewModelTest {

    @Nested
    inner class `US-001_1 Offene Vorgaenge anzeigen` {

        @Test
        fun `zeigt alle offenen Vorgaenge sortiert nach letzter Bearbeitung`() {
            // Given: offene Vorgänge existieren
            val vorgaenge = listOf(
                testVorgang(auftragsnummer = "ALT", aktualisiertAm = gestern),
                testVorgang(auftragsnummer = "NEU", aktualisiertAm = heute)
            )
            // When: Startscreen geladen
            val uiState = viewModel.uiState.value
            // Then: sortiert nach aktualisiertAm DESC (neueste oben)
            assertEquals("NEU", uiState.vorgaenge.first().auftragsnummer)
        }

        @Test
        fun `zeigt Hinweis wenn keine offenen Vorgaenge existieren`() {
            // Given: keine offenen Vorgänge existieren
            // When: Startscreen geladen
            // Then: leerer Zustand mit Hinweis-Text
        }
    }

    @Nested
    inner class `US-001_2 Vorgang fuer Weiterarbeit oeffnen` {

        @Test
        fun `oeffnet direkt Demontage-Flow bei Vorgang mit 0 Schritten`() {
            // Given: offener Vorgang mit 0 Schritten (frisch angelegt)
            // When: Mechaniker tippt Vorgang an
            // Then: Demontage-Flow (F-003) öffnet sich direkt
        }

        @Test
        fun `zeigt Auswahl-Dialog bei Vorgang mit mindestens 1 Schritt`() {
            // Given: offener Vorgang mit mindestens 1 Schritt
            // When: Mechaniker tippt Vorgang an
            // Then: Dialog mit "Weiter demontieren" und "Montage starten"
        }
    }

    @Nested
    inner class `US-001_4 Vorgang loeschen` {

        @Test
        fun `zeigt Bestaetigungsdialog vor Loeschung`() {
            // Given: Löschen-Button ist sichtbar
            // When: Mechaniker tippt "Löschen"
            // Then: Bestätigungsdialog erscheint
        }

        @Test
        fun `loescht Vorgang mit allen Schritten und Fotos nach Bestaetigung`() {
            // Given: Bestätigungsdialog ist sichtbar
            // When: Mechaniker bestätigt "Löschen"
            // Then: Vorgang, Schritte und Fotos gelöscht
        }

        @Test
        fun `bricht Loeschung bei Abbrechen ab`() {
            // Given: Bestätigungsdialog ist sichtbar
            // When: Mechaniker wählt "Abbrechen"
            // Then: Vorgang bleibt erhalten
        }
    }
}

class NeuerVorgangViewModelTest {

    @Nested
    inner class `US-002_1 Fahrzeug fotografieren` {

        @Test
        fun `oeffnet System-Kamera sofort beim Start des Anlage-Flows`() {
            // Given: Mechaniker hat auf "+" getippt
            // When: Anlage-Flow startet
            // Then: System-Kamera-Intent wird ausgelöst, kein app-eigener Zwischenscreen
        }

        @Test
        fun `legt keinen Vorgang an bei Back ohne Foto`() {
            // Given: System-Kamera ist gestartet
            // When: System-Back-Button gedrückt
            // Then: zurück zur Übersicht, kein Vorgang angelegt
        }
    }

    @Nested
    inner class `US-002_2 Auftragsdaten erfassen und Vorgang starten` {

        @Test
        fun `speichert Vorgang mit Foto und Auftragsnummer in DB`() {
            // Given: Foto aufgenommen, Auftragsnummer eingegeben
            // When: "Starten" getippt
            // Then: Vorgang in DB gespeichert, Demontage-Flow öffnet
        }

        @Test
        fun `zeigt Validierungsfehler bei fehlender Auftragsnummer`() {
            // Given: Formular angezeigt
            // When: "Starten" ohne Auftragsnummer
            // Then: Validierungsmeldung "Auftragsnummer ist erforderlich"
        }

        @Test
        fun `speichert Vorgang auch ohne Beschreibung`() {
            // Given: Foto und Auftragsnummer vorhanden, keine Beschreibung
            // When: "Starten" getippt
            // Then: Vorgang wird gespeichert (Beschreibung optional)
        }
    }

    @Nested
    inner class `US-002_3 Foto wiederholen` {

        @Test
        fun `ersetzt altes Foto durch neues nach Wiederholung`() {
            // Given: Foto-Preview wird angezeigt
            // When: "Bild wiederholen" → neues Foto aufgenommen
            // Then: neues Foto ersetzt altes, altes wird gelöscht
        }

        @Test
        fun `behaelt Formulardaten bei Foto-Wiederholung`() {
            // Given: Auftragsnummer und Beschreibung eingegeben
            // When: "Bild wiederholen" → Back
            // Then: Formulardaten sind erhalten
        }
    }
}
```

## Compose Conventions

### State Hoisting

```kotlin
// ViewModel hält den State
class DemontageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DemontageUiState())
    val uiState: StateFlow<DemontageUiState> = _uiState.asStateFlow()

    fun onFotoAufgenommen(pfad: String) { ... }
    fun onLabelGeaendert(fotoId: Long, label: FotoLabel, aktiv: Boolean) { ... }
    fun onWeiteresFoto() { ... }
    fun onNaechsterSchritt() { ... }
    fun onBeenden() { ... }
}

// Screen ist stateless
@Composable
fun DemontageScreen(
    uiState: DemontageUiState,
    onFotoAufgenommen: (String) -> Unit,
    onLabelGeaendert: (Long, FotoLabel, Boolean) -> Unit,
    onWeiteresFoto: () -> Unit,
    onNaechsterSchritt: () -> Unit,
    onBeenden: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Preview

Jeder Screen bekommt eine `@Preview` mit realistischen Daten:

```kotlin
@Preview(showBackground = true)
@Composable
fun DemontageScreenPreview() {
    BoltMindTheme {
        DemontageScreen(
            uiState = DemontageUiState(
                schrittNummer = 5,
                fotos = listOf(
                    SchrittFoto(
                        pfad = "/path/to/photo.jpg",
                        reihenfolge = 0,
                        istBauteil = true
                    )
                ),
                sichtbaresFotoIndex = 0
            ),
            onFotoAufgenommen = {},
            onLabelGeaendert = { _, _, _ -> },
            onWeiteresFoto = {},
            onNaechsterSchritt = {},
            onBeenden = {}
        )
    }
}
```

## Code-Qualität

### Real vorhandene Checks

Nur diese Gradle-Tasks existieren und dürfen in Zyklen, Checklisten oder PR-Beschreibungen
vorgeschrieben werden:

| Task | Zweck |
|------|-------|
| `./gradlew test` | Unit Tests (JVM, JUnit 5) — der verbindliche Check jedes TDD-Schritts |
| `./gradlew connectedDebugAndroidTest` | Instrumentierte Tests (Room-Migrationen). Braucht Gerät oder Emulator |
| `./gradlew lint` | Android Lint |
| `./gradlew assembleDebug` | Debug-Build |

### Werkzeug-Lücken (Stand 2026-07-27)

Formatierung, statische Analyse und Compose-UI-Tests sind **gewollt, aber nicht eingerichtet**.
Die folgende Tabelle beschreibt ein Soll, keinen Ist-Zustand:

| Werkzeug | Soll | Ist |
|----------|------|-----|
| ktlint | Automatische Formatierung nach Kotlin Coding Conventions, `./gradlew ktlintCheck` / `ktlintFormat` | **Nicht eingerichtet.** Kein ktlint-Plugin in `build.gradle.kts` oder `gradle/libs.versions.toml`; die Tasks existieren nicht. |
| detekt | Statische Code-Analyse (Complexity, Code Smells, Style), `./gradlew detekt`, Konfiguration in `config/detekt/detekt.yml` | **Nicht eingerichtet.** Kein detekt-Plugin, kein `config/`-Verzeichnis; die Task existiert nicht. |
| Compose-UI-Tests | UI-Tests je Modus und für Mindesthöhen, `createAndroidComposeRule` | **Nicht geschrieben.** Die Abhängigkeiten (`ui-test-junit4`, `ui-test-manifest`) sind eingerichtet, aber es existiert kein einziger UI-Test. |

Bis zur Einrichtung ist **keiner dieser Punkte eine Anforderung**: Er darf in keinem TDD-Zyklus als
Pflichtschritt stehen, kein PR darf an ihm scheitern, und keine Zusammenfassung darf behaupten, der
Check sei gelaufen. Wer ein Werkzeug einführt, aktualisiert diesen Abschnitt **und** den TDD-Zyklus
in einem Zug — und erst danach gilt es als Pflicht.

## Git Workflow

### Branch-Naming

```
feature/F-XXX/kurze-beschreibung
```

Beispiele:
- `feature/F-001/room-db-setup`
- `feature/F-001/vorgangs-uebersicht-screen`
- `feature/F-003/kamera-integration`

### Commit-Messages

```
[F-XXX] Kurze Beschreibung

Optionale Details

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

### Workflow

```
1. Issue wählen, Spec lesen
2. Branch erstellen: feature/F-XXX/beschreibung
3. TDD-Zyklus (pro Akzeptanzkriterium wiederholen):
   a) Test schreiben/anpassen → ./gradlew test (rot)
   b) Implementieren          → ./gradlew test (grün)
   c) Refactoren              → ./gradlew test (grün bleiben)
4. PR erstellen → Review → Merge nach main
5. Issue schließen
```

Kein Schritt dieses Workflows ruft `ktlintCheck`, `detekt` oder `connectedAndroidTest` auf — diese
Tasks existieren nicht (siehe [Werkzeug-Lücken](#werkzeug-lücken-stand-2026-07-26)).

## Verbotene Patterns

- **Kein God-ViewModel**: Max 200 Zeilen pro ViewModel, bei mehr aufteilen
- **Kein Business-Logic in Composables**: Logik gehört ins ViewModel
- **Keine hardcoded Strings in UI**: Alles in `strings.xml`
- **Kein `GlobalScope`**: Immer `viewModelScope` oder strukturierte Coroutines
- **Keine synchronen DB-Aufrufe**: Room-Queries immer als `suspend` oder `Flow`
- **Keine Wildcart-Imports**: Explizite Imports
