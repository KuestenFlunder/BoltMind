package com.boltmind.app.data.model

import androidx.room.Embedded

data class ReparaturvorgangMitAnzahl(
    @Embedded val vorgang: Reparaturvorgang,
    val schrittAnzahl: Int
)
