package com.boltmind.app.data.foto

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

    fun fotoExistiert(pfad: String?): Boolean {
        if (pfad == null) return false
        return File(pfad).exists()
    }
}
