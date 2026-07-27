package com.boltmind.app

import android.app.Application
import android.util.Log
import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BoltMindApplication : Application() {

    private val repository: ReparaturRepository by inject()
    private val fotoManager: FotoManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BoltMindApplication)
            modules(appModule)
        }
        bereinigeVerwaisteFotos()
    }

    /**
     * Raeumt beim Start Foto-Dateien weg, auf die keine Datenbankzeile mehr
     * verweist -- etwa weil die Kamera geschrieben hat und der Nutzer danach
     * abgebrochen oder den Vorgang geloescht hat.
     *
     * Schlaegt das Lesen der Datenbank fehl, wird **nichts** geloescht. Andernfalls
     * wuerde ein einzelner Lesefehler den kompletten Fotobestand abraeumen -- ein
     * Datenverlust, den der Nutzer erst Wochen spaeter im Archiv bemerkt.
     */
    private fun bereinigeVerwaisteFotos() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val bekannt = runCatching {
                buildSet {
                    addAll(repository.holeAlleFotoPfade())
                    addAll(repository.holeAlleFahrzeugFotoPfade())
                }
            }.getOrElse {
                Log.w(TAG, "Fotobestand nicht lesbar, es wird nichts geloescht.", it)
                null
            }
            val anzahl = fotoManager.bereinigeVerwaisteFotos(bekannt)
            if (anzahl > 0) Log.i(TAG, "$anzahl verwaiste Foto-Datei(en) entfernt.")
        }
    }

    private companion object {
        const val TAG = "BoltMind"
    }
}
