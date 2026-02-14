package com.boltmind.app.feature.uebersicht

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boltmind.app.R
import com.boltmind.app.ui.components.BoltMindButton
import com.boltmind.app.ui.components.BoltMindButtonStyle
import com.boltmind.app.ui.components.BoltMindDialog
import com.boltmind.app.ui.components.FotoPreview
import com.boltmind.app.ui.components.StatusBadge
import com.boltmind.app.ui.components.StatusBadgeTyp
import com.boltmind.app.ui.components.ambientGlow
import com.boltmind.app.ui.components.sollteFotoAnzeigen
import com.boltmind.app.ui.components.staggeredItems
import com.boltmind.app.ui.theme.BoltMindDimensions
import com.boltmind.app.ui.theme.BoltMindTheme
import com.boltmind.app.ui.theme.BoltOnPrimary
import com.boltmind.app.ui.theme.BoltOnSurfaceVariant
import com.boltmind.app.ui.theme.BoltOutline
import com.boltmind.app.ui.theme.BoltPrimary
import com.boltmind.app.ui.theme.BoltPrimaryLight
import com.boltmind.app.ui.theme.BoltSurface
import com.boltmind.app.ui.theme.BoltSurfaceContainerHigh
import com.boltmind.app.ui.theme.BoltSurfaceVariant
import java.io.File

@Composable
private fun datumText(datum: DatumAnzeige): String = when (datum) {
    is DatumAnzeige.Heute -> stringResource(R.string.datum_heute)
    is DatumAnzeige.Gestern -> stringResource(R.string.datum_gestern)
    is DatumAnzeige.Formatiert -> datum.text
}

@Composable
private fun dauerText(dauer: DauerAnzeige): String = when (dauer) {
    is DauerAnzeige.WenigerAlsEineMinute -> stringResource(R.string.dauer_weniger_als_eine_minute)
    is DauerAnzeige.Minuten -> stringResource(R.string.dauer_minuten, dauer.minuten)
    is DauerAnzeige.StundenMinuten -> stringResource(R.string.dauer_stunden_minuten, dauer.stunden, dauer.minuten)
}

// ============================================================
// Haupt-Screen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UebersichtScreen(
    uiState: UebersichtUiState,
    onVorgangGetippt: (Long) -> Unit,
    onNeuerVorgangGetippt: () -> Unit,
    onWeiterDemontieren: () -> Unit,
    onMontageStarten: () -> Unit,
    onDialogVerwerfen: () -> Unit,
    onLoeschenAngefragt: (Long) -> Unit,
    onLoeschenBestaetigt: () -> Unit,
    onLoeschenAbgebrochen: () -> Unit,
    onTabGewaehlt: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        floatingActionButton = {
            if (uiState.selectedTab == 0 && uiState.vorgaenge.isNotEmpty()) {
                BoltMindFab(
                    onClick = onNeuerVorgangGetippt,
                    modifier = Modifier.offset(x = BoltMindDimensions.spacingXl),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            FilterChipBar(
                selectedTab = uiState.selectedTab,
                offeneAnzahl = uiState.vorgaenge.size,
                archivAnzahl = uiState.archivierteVorgaenge.size,
                onTabGewaehlt = onTabGewaehlt,
            )

            when (uiState.selectedTab) {
                0 -> {
                    if (uiState.vorgaenge.isEmpty()) {
                        EmptyStateOffen(onNeuerVorgangGetippt = onNeuerVorgangGetippt)
                    } else {
                        OffeneVorgaengeListe(
                            vorgaenge = uiState.vorgaenge,
                            onVorgangGetippt = onVorgangGetippt,
                            onLoeschenAngefragt = onLoeschenAngefragt,
                        )
                    }
                }
                1 -> {
                    if (uiState.archivierteVorgaenge.isEmpty()) {
                        EmptyStateArchiv()
                    } else {
                        ArchivListe(archivierteVorgaenge = uiState.archivierteVorgaenge)
                    }
                }
            }
        }
    }

    uiState.auswahlDialog?.let { dialog ->
        AuswahlDialog(
            dialogState = dialog,
            onWeiterDemontieren = onWeiterDemontieren,
            onMontageStarten = onMontageStarten,
            onVerwerfen = onDialogVerwerfen,
        )
    }

    uiState.loeschenDialog?.let { dialog ->
        LoeschenBestaetigungDialog(
            dialogState = dialog,
            onBestaetigt = onLoeschenBestaetigt,
            onAbgebrochen = onLoeschenAbgebrochen,
        )
    }
}

// ============================================================
// Filter-Chips (Tab-Ersatz)
// ============================================================

@Composable
private fun FilterChipBar(
    selectedTab: Int,
    offeneAnzahl: Int,
    archivAnzahl: Int,
    onTabGewaehlt: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = BoltMindDimensions.spacingM,
                vertical = BoltMindDimensions.spacingS,
            ),
        horizontalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingS),
    ) {
        FilterChipItem(
            label = stringResource(R.string.tab_offen),
            count = offeneAnzahl,
            isSelected = selectedTab == 0,
            onClick = { onTabGewaehlt(0) },
            modifier = Modifier.weight(1f),
        )
        FilterChipItem(
            label = stringResource(R.string.tab_archiv),
            count = archivAnzahl,
            isSelected = selectedTab == 1,
            onClick = { onTabGewaehlt(1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chipAlpha",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) BoltPrimary.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "chipBg",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BoltPrimary else BoltOutline,
        animationSpec = tween(durationMillis = 300),
        label = "chipBorder",
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) BoltPrimary else BoltOnSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "chipText",
    )

    val shape = MaterialTheme.shapes.small

    Surface(
        onClick = onClick,
        shape = shape,
        color = backgroundColor,
        modifier = modifier
            .heightIn(min = BoltMindDimensions.touchTargetMin)
            .then(
                if (isSelected) {
                    Modifier.drawBehind {
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = BoltPrimary.copy(alpha = 0.2f * animatedAlpha)
                                asFrameworkPaint().maskFilter =
                                    BlurMaskFilter(
                                        BoltMindDimensions.glowRadius.toPx(),
                                        BlurMaskFilter.Blur.NORMAL,
                                    )
                            }
                            val outline =
                                shape.createOutline(size, layoutDirection, this@drawBehind)
                            canvas.drawOutline(outline, paint)
                        }
                    }
                } else {
                    Modifier
                },
            )
            .border(BoltMindDimensions.borderThin, borderColor, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = BoltMindDimensions.spacingM,
                    vertical = BoltMindDimensions.spacingS,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(BoltMindDimensions.spacingS))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) {
                                BoltPrimary.copy(alpha = 0.25f)
                            } else {
                                BoltOnSurfaceVariant.copy(alpha = 0.12f)
                            },
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = textColor,
                    )
                }
            }
        }
    }
}

// ============================================================
// FAB mit Gradient + Glow
// ============================================================

@Composable
private fun BoltMindFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        modifier = modifier.size(BoltMindDimensions.fabSize),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_boltmind_logo),
            contentDescription = stringResource(R.string.neuer_vorgang),
            modifier = Modifier.size(BoltMindDimensions.fabSize),
        )
    }
}

// ============================================================
// Vorgangs-Listen
// ============================================================

@Composable
private fun OffeneVorgaengeListe(
    vorgaenge: List<VorgangUiItem>,
    onVorgangGetippt: (Long) -> Unit,
    onLoeschenAngefragt: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(BoltMindDimensions.spacingM),
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
    ) {
        staggeredItems(
            items = vorgaenge,
            key = { it.id },
        ) { _, vorgang ->
            SwipeToDeleteVorgangKarte(
                vorgang = vorgang,
                onClick = { onVorgangGetippt(vorgang.id) },
                onLoeschenAngefragt = { onLoeschenAngefragt(vorgang.id) },
            )
        }
    }
}

@Composable
private fun ArchivListe(
    archivierteVorgaenge: List<ArchivVorgangUiItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(BoltMindDimensions.spacingM),
        verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
    ) {
        staggeredItems(
            items = archivierteVorgaenge,
            key = { it.id },
        ) { _, vorgang ->
            ArchivVorgangKarte(vorgang = vorgang)
        }
    }
}

// ============================================================
// Diagnostic Card — VorgangKarte
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteVorgangKarte(
    vorgang: VorgangUiItem,
    onClick: () -> Unit,
    onLoeschenAngefragt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onLoeschenAngefragt()
                false
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                label = "swipeBackground",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .padding(horizontal = BoltMindDimensions.spacingL),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.loeschen_hintergrund),
                    tint = MaterialTheme.colorScheme.onError,
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier,
    ) {
        VorgangKarte(
            vorgang = vorgang,
            onClick = onClick,
        )
    }
}

@Composable
private fun VorgangKarte(
    vorgang: VorgangUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .ambientGlow()
            .border(
                BoltMindDimensions.borderThin,
                MaterialTheme.colorScheme.outlineVariant,
                shape,
            ),
    ) {
        Column {
            // Hero Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_IMAGE_HEIGHT),
            ) {
                if (sollteFotoAnzeigen(vorgang.fahrzeugFotoPfad)) {
                    AsyncImage(
                        model = File(vorgang.fahrzeugFotoPfad!!),
                        contentDescription = stringResource(R.string.fahrzeugfoto),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(BoltSurfaceVariant, BoltSurface),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera_placeholder),
                            contentDescription = null,
                            tint = BoltOnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(BoltMindDimensions.iconLarge),
                        )
                    }
                }

                // Scrim-Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    BoltSurface.copy(alpha = 0.85f),
                                ),
                            ),
                        ),
                )

                // Schritt-Badge
                SchrittBadge(
                    anzahl = vorgang.anzahlSchritte,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(BoltMindDimensions.spacingS),
                )
            }

            // Info Section
            Column(
                modifier = Modifier.padding(
                    horizontal = BoltMindDimensions.spacingM,
                    vertical = BoltMindDimensions.spacingS,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "#${vorgang.auftragsnummer}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    StatusBadge(typ = StatusBadgeTyp.Offen)
                }
                Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXs))
                Text(
                    text = datumText(vorgang.erstelltAm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ============================================================
// Archiv-Karte (gedämpft)
// ============================================================

@Composable
private fun ArchivVorgangKarte(
    vorgang: ArchivVorgangUiItem,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                BoltMindDimensions.borderThin,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape,
            ),
    ) {
        Column {
            // Hero Image (gedämpft)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HERO_IMAGE_HEIGHT_SMALL),
            ) {
                if (sollteFotoAnzeigen(vorgang.fahrzeugFotoPfad)) {
                    AsyncImage(
                        model = File(vorgang.fahrzeugFotoPfad!!),
                        contentDescription = stringResource(R.string.fahrzeugfoto),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BoltSurfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera_placeholder),
                            contentDescription = null,
                            tint = BoltOnSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(BoltMindDimensions.iconLarge),
                        )
                    }
                }

                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, BoltSurface.copy(alpha = 0.9f)),
                            ),
                        ),
                )

                SchrittBadge(
                    anzahl = vorgang.anzahlSchritte,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(BoltMindDimensions.spacingS),
                )
            }

            // Info Section
            Column(
                modifier = Modifier.padding(
                    horizontal = BoltMindDimensions.spacingM,
                    vertical = BoltMindDimensions.spacingS,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "#${vorgang.auftragsnummer}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusBadge(typ = StatusBadgeTyp.Archiviert)
                }
                Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXs))
                Text(
                    text = stringResource(R.string.gesamtdauer, dauerText(vorgang.gesamtdauer)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.abgeschlossen_am,
                        datumText(vorgang.abschlussDatum),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ============================================================
// Schritt-Badge (Gauge-Style)
// ============================================================

@Composable
private fun SchrittBadge(
    anzahl: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(SCHRITT_BADGE_SIZE)
            .border(
                width = BoltMindDimensions.borderMedium,
                color = BoltPrimary.copy(alpha = 0.7f),
                shape = CircleShape,
            )
            .background(
                color = BoltSurface.copy(alpha = 0.85f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$anzahl",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ============================================================
// Empty-States
// ============================================================

@Composable
private fun EmptyStateOffen(
    onNeuerVorgangGetippt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(BoltMindDimensions.spacingXl),
        ) {
            // Icon mit Glow-Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = BoltPrimary.copy(alpha = 0.15f)
                                asFrameworkPaint().maskFilter =
                                    BlurMaskFilter(24.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                            }
                            canvas.drawCircle(center, 56.dp.toPx(), paint)
                        }
                    },
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(BoltSurfaceContainerHigh)
                        .border(
                            BoltMindDimensions.borderThin,
                            BoltPrimary.copy(alpha = 0.3f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = null,
                        modifier = Modifier.size(BoltMindDimensions.iconLarge),
                        tint = BoltPrimary.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))

            Text(
                text = stringResource(R.string.empty_state_titel),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingS))

            Text(
                text = stringResource(R.string.empty_state_beschreibung),
                style = MaterialTheme.typography.bodyLarge,
                color = BoltOnSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXl))

            BoltMindButton(
                text = stringResource(R.string.empty_state_cta),
                onClick = onNeuerVorgangGetippt,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(BoltMindDimensions.iconInButton),
                    )
                },
                modifier = Modifier.fillMaxWidth(0.7f),
            )
        }
    }
}

@Composable
private fun EmptyStateArchiv(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(BoltMindDimensions.spacingXl),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BoltSurfaceContainerHigh)
                    .border(
                        BoltMindDimensions.borderThin,
                        BoltOnSurfaceVariant.copy(alpha = 0.2f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = BoltOnSurfaceVariant.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))

            Text(
                text = stringResource(R.string.empty_state_archiv_titel),
                style = MaterialTheme.typography.titleLarge,
                color = BoltOnSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(BoltMindDimensions.spacingXs))

            Text(
                text = stringResource(R.string.empty_state_archiv_beschreibung),
                style = MaterialTheme.typography.bodyMedium,
                color = BoltOnSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ============================================================
// Dialoge
// ============================================================

@Composable
private fun AuswahlDialog(
    dialogState: AuswahlDialogState,
    onWeiterDemontieren: () -> Unit,
    onMontageStarten: () -> Unit,
    onVerwerfen: () -> Unit,
) {
    BoltMindDialog(onDismissRequest = onVerwerfen) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FotoPreview(
                fotoPfad = dialogState.fahrzeugFotoPfad,
                contentDescription = stringResource(R.string.fahrzeugfoto),
                groesse = BoltMindDimensions.touchTargetMin,
            )
            Spacer(modifier = Modifier.width(BoltMindDimensions.spacingM))
            Text(
                text = "#${dialogState.auftragsnummer}",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))
        BoltMindButton(
            text = stringResource(R.string.weiter_demontieren),
            onClick = onWeiterDemontieren,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BoltMindDimensions.buttonTall),
        )
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingM))
        BoltMindButton(
            text = stringResource(R.string.montage_starten),
            onClick = onMontageStarten,
            style = BoltMindButtonStyle.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BoltMindDimensions.buttonTall),
        )
    }
}

@Composable
private fun LoeschenBestaetigungDialog(
    dialogState: LoeschenDialogState,
    onBestaetigt: () -> Unit,
    onAbgebrochen: () -> Unit,
) {
    BoltMindDialog(onDismissRequest = onAbgebrochen) {
        Text(
            text = stringResource(R.string.loeschen_bestaetigung, dialogState.auftragsnummer),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(BoltMindDimensions.spacingL))
        Column(
            verticalArrangement = Arrangement.spacedBy(BoltMindDimensions.spacingM),
        ) {
            BoltMindButton(
                text = stringResource(R.string.loeschen),
                onClick = onBestaetigt,
                style = BoltMindButtonStyle.Danger,
                modifier = Modifier.fillMaxWidth(),
            )
            BoltMindButton(
                text = stringResource(R.string.abbrechen),
                onClick = onAbgebrochen,
                style = BoltMindButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ============================================================
// Konstanten
// ============================================================

private val HERO_IMAGE_HEIGHT = 160.dp
private val HERO_IMAGE_HEIGHT_SMALL = 120.dp
private val SCHRITT_BADGE_SIZE = 44.dp

// ============================================================
// Previews
// ============================================================

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun UebersichtScreenMitVorgaengenPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(
                vorgaenge = listOf(
                    VorgangUiItem(
                        id = 1L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0815",
                        anzahlSchritte = 12,
                        erstelltAm = DatumAnzeige.Heute,
                    ),
                    VorgangUiItem(
                        id = 2L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0712",
                        anzahlSchritte = 8,
                        erstelltAm = DatumAnzeige.Gestern,
                    ),
                ),
            ),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun UebersichtScreenLeerPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun UebersichtScreenArchivPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(
                selectedTab = 1,
                archivierteVorgaenge = listOf(
                    ArchivVorgangUiItem(
                        id = 3L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0501",
                        anzahlSchritte = 15,
                        gesamtdauer = DauerAnzeige.StundenMinuten(2, 15),
                        abschlussDatum = DatumAnzeige.Formatiert("01.05.2024"),
                    ),
                    ArchivVorgangUiItem(
                        id = 4L,
                        fahrzeugFotoPfad = null,
                        auftragsnummer = "2024-0320",
                        anzahlSchritte = 7,
                        gesamtdauer = DauerAnzeige.Minuten(45),
                        abschlussDatum = DatumAnzeige.Formatiert("20.03.2024"),
                    ),
                ),
            ),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun UebersichtScreenArchivLeerPreview() {
    BoltMindTheme {
        UebersichtScreen(
            uiState = UebersichtUiState(selectedTab = 1),
            onVorgangGetippt = {},
            onNeuerVorgangGetippt = {},
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onDialogVerwerfen = {},
            onLoeschenAngefragt = {},
            onLoeschenBestaetigt = {},
            onLoeschenAbgebrochen = {},
            onTabGewaehlt = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun AuswahlDialogPreview() {
    BoltMindTheme {
        AuswahlDialog(
            dialogState = AuswahlDialogState(
                vorgangId = 1L,
                auftragsnummer = "2024-0815",
                fahrzeugFotoPfad = null,
            ),
            onWeiterDemontieren = {},
            onMontageStarten = {},
            onVerwerfen = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF060B14)
@Composable
private fun LoeschenBestaetigungDialogPreview() {
    BoltMindTheme {
        LoeschenBestaetigungDialog(
            dialogState = LoeschenDialogState(
                vorgangId = 1L,
                auftragsnummer = "2024-0815",
            ),
            onBestaetigt = {},
            onAbgebrochen = {},
        )
    }
}
