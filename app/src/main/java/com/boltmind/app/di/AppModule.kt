package com.boltmind.app.di

import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.local.BoltMindDatabase
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.data.repository.RoomTransaktionsLauf
import com.boltmind.app.data.repository.TransaktionsLauf
import com.boltmind.app.service.zeiterfassung.ZeiterfassungService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { BoltMindDatabase.create(androidContext()) }
    single { get<BoltMindDatabase>().reparaturvorgangDao() }
    single { get<BoltMindDatabase>().schrittDao() }
    single { get<BoltMindDatabase>().schrittFotoDao() }
    single { get<BoltMindDatabase>().zeitMessungDao() }

    single<TransaktionsLauf> { RoomTransaktionsLauf(get()) }
    single { ReparaturRepository(get(), get(), get(), get()) }
    single { ZeiterfassungService(get()) }
    single { FotoManager(androidContext().filesDir) }
}
