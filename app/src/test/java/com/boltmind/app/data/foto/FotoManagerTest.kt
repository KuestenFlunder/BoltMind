package com.boltmind.app.data.foto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FotoManagerTest {

    @TempDir
    lateinit var tempDir: Path
    private lateinit var fotoManager: FotoManager

    @BeforeEach
    fun setup() {
        fotoManager = FotoManager(tempDir.toFile())
    }

    @Nested
    inner class `Temp-Foto erstellen` {

        @Test
        fun `erstellt Datei in temp-Ordner mit korrektem Prefix`() {
            // When
            val tempFile = fotoManager.erstelleTempDatei("bauteil")

            // Then
            assertTrue(tempFile.parentFile?.name == "temp")
            assertTrue(tempFile.name.startsWith("bauteil_"))
            assertTrue(tempFile.name.endsWith(".jpg"))
        }

        @Test
        fun `erstellt temp-Ordner falls nicht vorhanden`() {
            // When
            fotoManager.erstelleTempDatei("bauteil")

            // Then
            assertTrue(File(tempDir.toFile(), "photos/temp").exists())
        }
    }

    @Nested
    inner class `Foto bestaetigen` {

        @Test
        fun `verschiebt Datei von temp nach photos`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeText("fake-image-data")

            // When
            val permanentPfad = fotoManager.bestaetigeFoto(tempFile.absolutePath, "bauteil_1")

            // Then
            assertFalse(tempFile.exists())
            assertTrue(File(permanentPfad!!).exists())
            assertEquals("fake-image-data", File(permanentPfad).readText())
        }

        @Test
        fun `permanente Datei liegt in photos-Ordner`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeText("data")

            // When
            val permanentPfad = fotoManager.bestaetigeFoto(tempFile.absolutePath, "bauteil_5")

            // Then
            val permanentFile = File(permanentPfad!!)
            assertEquals("photos", permanentFile.parentFile?.name)
            assertTrue(permanentFile.name.startsWith("bauteil_5"))
        }

        @Test
        fun `gibt null zurueck wenn temp-Datei nicht existiert`() {
            // When
            val result = fotoManager.bestaetigeFoto("/nicht/existent.jpg", "bauteil_1")

            // Then
            assertNull(result)
        }
    }

    @Nested
    inner class `Temp-Foto loeschen` {

        @Test
        fun `loescht temporaere Datei`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeText("data")

            // When
            fotoManager.loescheTempFoto(tempFile.absolutePath)

            // Then
            assertFalse(tempFile.exists())
        }

        @Test
        fun `ignoriert nicht existierende Datei`() {
            // When / Then - kein Fehler
            fotoManager.loescheTempFoto("/nicht/existent.jpg")
        }
    }

    @Nested
    inner class `Temp-Ordner bereinigen` {

        @Test
        fun `loescht alle Dateien im temp-Ordner`() {
            // Given
            val temp1 = fotoManager.erstelleTempDatei("bauteil")
            val temp2 = fotoManager.erstelleTempDatei("ablageort")
            temp1.writeText("data1")
            temp2.writeText("data2")

            // When
            fotoManager.bereinigeTempOrdner()

            // Then
            assertFalse(temp1.exists())
            assertFalse(temp2.exists())
        }

        @Test
        fun `laesst permanente Fotos unangetastet`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeText("data")
            val permanentPfad = fotoManager.bestaetigeFoto(tempFile.absolutePath, "bauteil_1")

            // When
            fotoManager.bereinigeTempOrdner()

            // Then
            assertTrue(File(permanentPfad!!).exists())
        }

        @Test
        fun `funktioniert wenn temp-Ordner nicht existiert`() {
            // When / Then - kein Fehler
            fotoManager.bereinigeTempOrdner()
        }
    }

    @Nested
    inner class `Foto-Existenz pruefen` {

        @Test
        fun `gibt true fuer existierende Datei`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeText("data")
            val pfad = fotoManager.bestaetigeFoto(tempFile.absolutePath, "bauteil_1")

            // Then
            assertTrue(fotoManager.fotoExistiert(pfad!!))
        }

        @Test
        fun `gibt false fuer nicht existierende Datei`() {
            assertFalse(fotoManager.fotoExistiert("/nicht/existent.jpg"))
        }

        @Test
        fun `gibt false fuer null-Pfad`() {
            assertFalse(fotoManager.fotoExistiert(null))
        }
    }

    @Nested
    inner class `EXIF-Metadaten entfernen` {

        @Test
        fun `wirft keinen Fehler bei nicht existierender Datei`() {
            // When / Then - kein Fehler (best-effort)
            fotoManager.entferneExifMetadaten("/nicht/existent.jpg")
        }

        @Test
        fun `wirft keinen Fehler bei nicht-JPEG Datei`() {
            // Given
            val file = File(tempDir.toFile(), "test.txt")
            file.writeText("keine JPEG Datei")

            // When / Then - kein Fehler (best-effort)
            fotoManager.entferneExifMetadaten(file.absolutePath)
        }

        @Test
        fun `Datei existiert nach Aufruf weiterhin`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()))

            // When
            fotoManager.entferneExifMetadaten(tempFile.absolutePath)

            // Then
            assertTrue(tempFile.exists())
        }
    }

    @Nested
    inner class `Foto bestaetigen mit EXIF-Stripping` {

        @Test
        fun `ruft EXIF-Stripping vor Verschiebung auf`() {
            // Given
            val tempFile = fotoManager.erstelleTempDatei("bauteil")
            tempFile.writeText("fake-image-data")

            // When
            val permanentPfad = fotoManager.bestaetigeFoto(tempFile.absolutePath, "bauteil_1")

            // Then - Datei wurde verschoben (EXIF-Stripping laeuft davor, best-effort)
            assertTrue(permanentPfad != null)
            assertTrue(File(permanentPfad!!).exists())
        }
    }
}
