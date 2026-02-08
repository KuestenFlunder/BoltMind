package com.boltmind.app.di

import com.boltmind.app.data.local.BoltMindDatabase
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.feature.uebersicht.UebersichtViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { BoltMindDatabase.create(androidContext()) }
    single { get<BoltMindDatabase>().reparaturvorgangDao() }
    single { get<BoltMindDatabase>().schrittDao() }
    single { ReparaturRepository(get(), get()) }
    viewModel { UebersichtViewModel(get()) }
}
