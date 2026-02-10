package com.boltmind.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Testbare Debounce-Logik fuer Click-Events.
 *
 * Verhindert versehentliche Doppelklicks indem Clicks innerhalb
 * des konfigurierbaren Zeitfensters ignoriert werden.
 *
 * @param debounceMs Minimale Zeitspanne zwischen zwei Clicks in Millisekunden
 */
class DebounceClickHandler(private val debounceMs: Long = 300L) {
    private var lastClickTime: Long = 0L

    /**
     * Fuehrt die Action nur aus wenn seit dem letzten Click
     * mindestens [debounceMs] Millisekunden vergangen sind.
     *
     * @param currentTime Aktueller Zeitstempel (fuer Testbarkeit)
     * @param action Die auszufuehrende Aktion
     */
    fun onClick(currentTime: Long = System.currentTimeMillis(), action: () -> Unit) {
        if (currentTime - lastClickTime >= debounceMs) {
            lastClickTime = currentTime
            action()
        }
    }
}

/**
 * Modifier-Extension die Debounce-Logik auf Click-Events anwendet.
 *
 * Verhindert versehentliche Doppelklicks in der Werkstatt-Umgebung,
 * wo schnelle Taps durch schmutzige Haende oder Handschuhe vorkommen.
 *
 * @param debounceMs Minimale Zeitspanne zwischen zwei Clicks (Standard: 300ms)
 * @param onClick Die auszufuehrende Aktion bei validem Click
 */
fun Modifier.debounceClick(
    debounceMs: Long = 300L,
    onClick: () -> Unit,
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceMs) {
            lastClickTime = currentTime
            onClick()
        }
    }
}
