package com.boltmind.app.feature.demontage

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

class FotoManager(private val context: Context) {

    private val fotoVerzeichnis: File
        get() = File(context.filesDir, "fotos").also { it.mkdirs() }

    fun erstelleFotoPfad(): File {
        val zeitstempel = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ROOT).format(Date())
        return File(fotoVerzeichnis, "FOTO_$zeitstempel.jpg")
    }

    fun fotoAufnehmen(
        imageCapture: ImageCapture,
        executor: Executor,
        onFotoGespeichert: (String) -> Unit,
        onFehler: (String) -> Unit
    ) {
        val fotoDatei = erstelleFotoPfad()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(fotoDatei).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onFotoGespeichert(fotoDatei.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    fotoDatei.delete()
                    onFehler(exception.message ?: "Foto konnte nicht gespeichert werden")
                }
            }
        )
    }
}
