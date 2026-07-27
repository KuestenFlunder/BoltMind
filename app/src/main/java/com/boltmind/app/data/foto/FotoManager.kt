package com.boltmind.app.data.foto

import androidx.exifinterface.media.ExifInterface
import java.io.File

class FotoManager(private val baseDir: File) {

    private val photosDir: File
        get() = File(baseDir, "photos").also { it.mkdirs() }

    private val tempDir: File
        get() = File(photosDir, "temp").also { it.mkdirs() }

    fun erstelleTempDatei(prefix: String): File {
        return File(tempDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    }

    fun bestaetigeFoto(tempPfad: String, zielName: String): String? {
        val tempFile = File(tempPfad)
        if (!tempFile.exists()) return null
        entferneExifMetadaten(tempPfad)
        val zielDatei = File(photosDir, "$zielName.jpg")
        return if (tempFile.renameTo(zielDatei)) zielDatei.absolutePath else null
    }

    fun loescheTempFoto(pfad: String) {
        val file = File(pfad)
        if (file.exists()) file.delete()
    }

    fun bereinigeTempOrdner() {
        val temp = File(File(baseDir, "photos"), "temp")
        if (temp.exists()) {
            temp.listFiles()?.forEach { it.delete() }
        }
    }

    // ------------------------------------------------------------------
    // Zielzustand: System-Kamera schreibt direkt nach photos/
    //
    // Die drei Methoden hier ersetzen mittelfristig den temp-Zyklus darueber
    // (governance.md, Abschnitt "Speicherort": es gibt keinen photos/temp/-Ordner
    // mehr). Der alte Weg bleibt vorerst stehen, bis alle Screens umgestellt sind.
    // ------------------------------------------------------------------

    /**
     * Legt die Zieldatei einer Aufnahme direkt unter `photos/` an.
     *
     * Die Datei wird leer vorangelegt, damit `FileProvider` sie aufloesen kann und
     * die System-Kamera hineinschreiben darf. Bricht der Nutzer ab, raeumt der
     * Aufrufer sie ueber [loescheFoto] wieder weg.
     */
    fun erstelleZieldatei(prefix: String): File {
        val ziel = File(photosDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        if (!ziel.exists()) {
            runCatching { ziel.createNewFile() }
        }
        return ziel
    }

    /**
     * Uebernimmt eine von der System-Kamera geschriebene Datei: EXIF-Spuren raus,
     * Pfad zurueck. Ist nichts angekommen, wird die leere Huelle geloescht und
     * `null` gemeldet -- fuer den Aufrufer ist das ein Abbruch.
     */
    fun uebernimmAufnahme(pfad: String): String? {
        val datei = File(pfad)
        if (!datei.exists() || datei.length() == 0L) {
            datei.delete()
            return null
        }
        entferneExifMetadaten(pfad)
        return datei.absolutePath
    }

    /** Loescht eine Foto-Datei. Fehlt sie schon, passiert nichts. */
    fun loescheFoto(pfad: String?) {
        if (pfad.isNullOrBlank()) return
        val datei = File(pfad)
        if (datei.exists()) datei.delete()
    }

    fun entferneExifMetadaten(pfad: String) {
        try {
            val exif = ExifInterface(pfad)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_DATETIME, null)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null)
            exif.saveAttributes()
        } catch (_: Throwable) {
            // EXIF-Stripping ist best-effort (Throwable fuer JVM-Tests ohne Android-Framework)
        }
    }

    fun fotoExistiert(pfad: String?): Boolean {
        if (pfad == null) return false
        return File(pfad).exists()
    }
}
