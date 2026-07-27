package com.boltmind.app.data.repository

import androidx.room.withTransaction
import com.boltmind.app.data.local.BoltMindDatabase

/**
 * Klammert mehrere Schreibzugriffe zu einer Einheit.
 *
 * Warum eine eigene Abstraktion statt direkt `db.withTransaction`: das Repository
 * wird in Unit-Tests mit gemockten DAOs betrieben, ohne echte Datenbank. Die
 * Erweiterungsfunktion auf `RoomDatabase` liesse sich dort nicht sinnvoll mocken.
 */
interface TransaktionsLauf {
    suspend fun <T> inTransaktion(block: suspend () -> T): T
}

/** Produktivvariante: echte Room-Transaktion. */
class RoomTransaktionsLauf(private val db: BoltMindDatabase) : TransaktionsLauf {
    override suspend fun <T> inTransaktion(block: suspend () -> T): T =
        db.withTransaction { block() }
}

/** Testvariante: fuehrt den Block direkt aus. */
class DirekterLauf : TransaktionsLauf {
    override suspend fun <T> inTransaktion(block: suspend () -> T): T = block()
}
