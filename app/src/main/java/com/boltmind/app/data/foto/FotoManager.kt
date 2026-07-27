package com.boltmind.app.data.foto

import androidx.exifinterface.media.ExifInterface
import java.io.File

class FotoManager(private val baseDir: File) {

    private val photosDir: File
        get() = File(baseDir, "photos").also { it.mkdirs() }

    // ------------------------------------------------------------------
    // Die System-Kamera schreibt direkt nach photos/. Einen temp-Ordner gibt es
    // nicht mehr (governance.md, Abschnitt "Speicherort").
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

    /**
     * Loescht Dateien in `photos/`, auf die keine Datenbankzeile mehr verweist.
     *
     * [bekanntePfade] muss **alle** referenzierten Pfade enthalten: die der
     * `SchrittFoto`-Zeilen und die Fahrzeugfotos der Vorgaenge.
     *
     * Sicherheitsregel: Konnte der Aufrufer die Datenbank nicht lesen, uebergibt er
     * `null` -- dann wird **nichts** geloescht. Ein Lesefehler wuerde sonst den
     * kompletten Fotobestand abraeumen.
     *
     * @return Anzahl geloeschter Dateien.
     */
    fun bereinigeVerwaisteFotos(bekanntePfade: Set<String>?): Int {
        if (bekanntePfade == null) return 0
        val dateien = photosDir.listFiles()?.filter { it.isFile } ?: return 0
        var geloescht = 0
        dateien.forEach { datei ->
            if (datei.absolutePath !in bekanntePfade) {
                if (datei.delete()) geloescht++
            }
        }
        return geloescht
    }
}
