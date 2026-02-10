package com.boltmind.app.di

import com.boltmind.app.data.foto.FotoManager
import com.boltmind.app.data.local.BoltMindDatabase
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.feature.demontage.DemontageViewModel
import com.boltmind.app.feature.neuervorgang.NeuerVorgangViewModel
import com.boltmind.app.feature.uebersicht.UebersichtViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { BoltMindDatabase.create(androidContext()) }
    single { get<BoltMindDatabase>().reparaturvorgangDao() }
    single { get<BoltMindDatabase>().schrittDao() }
    single { ReparaturRepository(get(), get()) }
    single { FotoManager(androidContext().filesDir) }
    viewModel { UebersichtViewModel(get()) }
    viewModel { NeuerVorgangViewModel(get()) }
    viewModel { DemontageViewModel(get(), get(), get()) }
}
