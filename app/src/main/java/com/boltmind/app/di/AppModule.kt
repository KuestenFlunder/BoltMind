package com.boltmind.app.di

import androidx.room.Room
import com.boltmind.app.data.local.BoltMindDatabase
import com.boltmind.app.data.repository.ReparaturRepository
import com.boltmind.app.feature.demontage.DemontageViewModel
import com.boltmind.app.feature.neuervorgang.NeuerVorgangViewModel
import com.boltmind.app.feature.uebersicht.UebersichtViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            get(),
            BoltMindDatabase::class.java,
            "boltmind-db"
        ).build()
    }
    single { get<BoltMindDatabase>().reparaturvorgangDao() }
    single { get<BoltMindDatabase>().schrittDao() }
    single { ReparaturRepository(get(), get()) }
    viewModel { UebersichtViewModel(get()) }
    viewModel { NeuerVorgangViewModel(get()) }
    viewModel { (vorgangId: Long) -> DemontageViewModel(get(), vorgangId) }
}
