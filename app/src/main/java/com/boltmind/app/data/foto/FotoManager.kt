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
