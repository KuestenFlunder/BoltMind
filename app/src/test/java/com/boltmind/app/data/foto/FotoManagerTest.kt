package com.boltmind.app.data.foto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@DisplayName("FotoManager")
class FotoManagerTest {

    @TempDir
    lateinit var basis: File

    private lateinit var fotoManager: FotoManager

    private val photosDir: File get() = File(basis, "photos")

    @BeforeEach
    fun setUp() {
        fotoManager = FotoManager(basis)
    }

    private fun legeDateiAn(name: String, inhalt: String = "x"): File =
        File(photosDir, name).also {
            it.parentFile?.mkdirs()
            it.writeText(inhalt)
        }

    @Nested
    @DisplayName("Zieldatei anlegen")
    inner class Zieldatei {

        @Test
        fun `liegt direkt unter photos, nicht in einem temp-Ordner`() {
            val ziel = fotoManager.erstelleZieldatei("schritt")
            assertEquals(photosDir.absolutePath, ziel.parentFile?.absolutePath)
            assertFalse(
                File(photosDir, "temp").exists(),
                "Der temp-Zyklus ist abgeschafft; es darf kein temp-Ordner mehr entstehen."
            )
        }

        @Test
        fun `wird leer vorangelegt, damit der FileProvider sie aufloesen kann`() {
            val ziel = fotoManager.erstelleZieldatei("schritt")
            assertTrue(ziel.exists())
            assertEquals(0L, ziel.length())
        }

        @Test
        fun `zwei Aufrufe liefern verschiedene Dateien`() {
            val a = fotoManager.erstelleZieldatei("schritt")
            Thread.sleep(2)
            val b = fotoManager.erstelleZieldatei("schritt")
            assertTrue(a.absolutePath != b.absolutePath)
        }
    }

    @Nested
    @DisplayName("Aufnahme uebernehmen")
    inner class Uebernehmen {

        @Test
        fun `liefert den Pfad, wenn die Kamera geschrieben hat`() {
            val datei = legeDateiAn("schritt_1.jpg", "bilddaten")
            assertEquals(datei.absolutePath, fotoManager.uebernimmAufnahme(datei.absolutePath))
        }

        @Test
        fun `wertet eine leere Datei als Abbruch und raeumt sie weg`() {
            val huelle = fotoManager.erstelleZieldatei("schritt")
            assertNull(
                fotoManager.uebernimmAufnahme(huelle.absolutePath),
                "Eine leere Huelle bedeutet: die Kamera hat nichts geliefert."
            )
            assertFalse(huelle.exists(), "Die leere Huelle darf nicht liegen bleiben.")
        }

        @Test
        fun `meldet Abbruch, wenn die Datei gar nicht existiert`() {
            assertNull(
                fotoManager.uebernimmAufnahme(File(photosDir, "gibtsnicht.jpg").absolutePath)
            )
        }
    }

    @Nested
    @DisplayName("Foto loeschen")
    inner class Loeschen {

        @Test
        fun `entfernt eine vorhandene Datei`() {
            val datei = legeDateiAn("weg.jpg")
            fotoManager.loescheFoto(datei.absolutePath)
            assertFalse(datei.exists())
        }

        @Test
        fun `vertraegt null, Leerstring und fehlende Datei`() {
            fotoManager.loescheFoto(null)
            fotoManager.loescheFoto("")
            fotoManager.loescheFoto(File(photosDir, "nie.jpg").absolutePath)
        }
    }

    @Nested
    @DisplayName("Verwaiste Dateien aufraeumen")
    inner class Verwaiste {

        @Test
        fun `loescht genau die Dateien ohne Datenbankzeile`() {
            val bekannt = legeDateiAn("bekannt.jpg")
            val verwaist = legeDateiAn("verwaist.jpg")

            val anzahl = fotoManager.bereinigeVerwaisteFotos(setOf(bekannt.absolutePath))

            assertEquals(1, anzahl)
            assertTrue(bekannt.exists(), "Referenzierte Dateien bleiben.")
            assertFalse(verwaist.exists())
        }

        @Test
        fun `loescht nichts, wenn die Datenbank nicht lesbar war`() {
            val a = legeDateiAn("a.jpg")
            val b = legeDateiAn("b.jpg")

            val anzahl = fotoManager.bereinigeVerwaisteFotos(null)

            assertEquals(0, anzahl)
            assertTrue(a.exists() && b.exists())
            // Sonst raeumt ein einzelner Lesefehler den kompletten Bestand ab -- ein
            // Datenverlust, den der Nutzer erst Wochen spaeter im Archiv bemerkt.
        }

        @Test
        fun `laesst das Fahrzeugfoto stehen, auch wenn es zu keinem Schritt gehoert`() {
            val fahrzeug = legeDateiAn("fahrzeug_1.jpg")
            val schrittFoto = legeDateiAn("schritt_1.jpg")

            fotoManager.bereinigeVerwaisteFotos(
                setOf(fahrzeug.absolutePath, schrittFoto.absolutePath)
            )

            assertTrue(fahrzeug.exists())
            assertTrue(schrittFoto.exists())
        }

        @Test
        fun `kommt mit einem leeren Ordner zurecht`() {
            assertEquals(0, fotoManager.bereinigeVerwaisteFotos(emptySet()))
        }
    }

    @Nested
    @DisplayName("Foto-Existenz")
    inner class Existenz {

        @Test
        fun `erkennt vorhandene und fehlende Dateien`() {
            val da = legeDateiAn("da.jpg")
            assertTrue(fotoManager.fotoExistiert(da.absolutePath))
            assertFalse(fotoManager.fotoExistiert(File(photosDir, "weg.jpg").absolutePath))
            assertFalse(fotoManager.fotoExistiert(null))
        }
    }

    @Nested
    @DisplayName("EXIF-Metadaten")
    inner class Exif {

        @Test
        fun `wirft nicht, wenn die Datei kein gueltiges JPEG ist`() {
            // Auf der JVM steht kein Android-Framework bereit; das Stripping ist
            // best-effort und darf den Aufrufer niemals mitreissen.
            val datei = legeDateiAn("kaputt.jpg", "kein jpeg")
            fotoManager.entferneExifMetadaten(datei.absolutePath)
            assertTrue(datei.exists())
        }

        @Test
        fun `wirft nicht bei fehlender Datei`() {
            fotoManager.entferneExifMetadaten(File(photosDir, "nie.jpg").absolutePath)
        }
    }
}
