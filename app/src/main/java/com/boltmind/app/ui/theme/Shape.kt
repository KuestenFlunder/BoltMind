package com.boltmind.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Der Entwurf verwendet durchgehend gewoehnliche `border-radius`-Ecken, keine
 * Squircles. Die frueher benutzte Squircle-Bibliothek entfaellt damit.
 */
val BoltMindShapes = Shapes(
    extraSmall = RoundedCornerShape(BoltMindDimensions.radiusXs),
    small = RoundedCornerShape(BoltMindDimensions.radiusS),
    medium = RoundedCornerShape(BoltMindDimensions.radiusStandard),
    large = RoundedCornerShape(BoltMindDimensions.radiusXl),
    extraLarge = RoundedCornerShape(BoltMindDimensions.radiusSheet)
)
