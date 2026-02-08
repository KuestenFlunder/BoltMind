package com.boltmind.app

import android.app.Application
import com.boltmind.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BoltMindApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BoltMindApplication)
            modules(appModule)
        }
    }
}
