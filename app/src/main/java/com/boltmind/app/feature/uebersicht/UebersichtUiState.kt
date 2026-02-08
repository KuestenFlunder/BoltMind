package com.boltmind.app.feature.uebersicht

import com.boltmind.app.data.model.Reparaturvorgang
import com.boltmind.app.data.model.ReparaturvorgangMitSchrittanzahl
import com.boltmind.app.data.model.VorgangStatus

data class UebersichtUiState(
    val vorgaenge: List<ReparaturvorgangMitSchrittanzahl> = emptyList(),
    val aktuellerTab: VorgangStatus = VorgangStatus.OFFEN,
    val loeschDialog: Reparaturvorgang? = null,
    val auswahlDialog: ReparaturvorgangMitSchrittanzahl? = null
)
