package com.boltmind.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ReparaturvorgangMitSchrittanzahl(
    @Embedded val vorgang: Reparaturvorgang,
    @ColumnInfo(name = "schrittAnzahl") val schrittAnzahl: Int
)
