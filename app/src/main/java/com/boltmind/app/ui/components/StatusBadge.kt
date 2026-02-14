package com.boltmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boltmind.app.R
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOnSurfaceVariant
import com.boltmind.app.ui.theme.BoltSuccess

enum class StatusBadgeTyp {
    Offen,
    Archiviert,
}

@Composable
fun StatusBadge(
    typ: StatusBadgeTyp,
    modifier: Modifier = Modifier,
) {
    val text = when (typ) {
        StatusBadgeTyp.Offen -> stringResource(R.string.status_offen)
        StatusBadgeTyp.Archiviert -> stringResource(R.string.status_archiviert)
    }
    val textColor = when (typ) {
        StatusBadgeTyp.Offen -> BoltSuccess
        StatusBadgeTyp.Archiviert -> BoltOnSurfaceVariant
    }
    val backgroundColor = when (typ) {
        StatusBadgeTyp.Offen -> BoltSuccess.copy(alpha = 0.15f)
        StatusBadgeTyp.Archiviert -> BoltOnSurfaceVariant.copy(alpha = 0.12f)
    }

    val pillShape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun StatusBadgeOffenPreview() {
    BoltMindTheme {
        StatusBadge(typ = StatusBadgeTyp.Offen)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun StatusBadgeArchiviertPreview() {
    BoltMindTheme {
        StatusBadge(typ = StatusBadgeTyp.Archiviert)
    }
}
