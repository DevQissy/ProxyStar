package com.devqissy.proxystar.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devqissy.proxystar.R
import com.devqissy.proxystar.data.Proxy
import com.devqissy.proxystar.ui.ConstellationShape
import com.devqissy.proxystar.ui.ProxyUiState
import com.devqissy.proxystar.ui.theme.Blue
import com.devqissy.proxystar.ui.theme.Cyan
import com.devqissy.proxystar.ui.theme.Purple
import com.devqissy.proxystar.ui.theme.TextDim
import com.devqissy.proxystar.ui.theme.TextFaint

@Composable
internal fun HeroCard(
    state: ProxyUiState,
    shape: ConstellationShape?,
    haptic: HapticFeedback,
    onSwitchNext: () -> Unit,
    onRetry: () -> Unit,
    onConnect: (Proxy) -> Unit
) {
    val alive = state.proxies.filter { it.alive == true }
    val current = alive.getOrNull(
        state.selectedIndex.coerceIn(0, (alive.size - 1).coerceAtLeast(0))
    )

    val mode = when {
        current != null -> HeroMode.READY
        state.isLoading || state.isChecking -> HeroMode.LOADING
        else -> HeroMode.EMPTY
    }

    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by androidx.compose.animation.core.animateFloatAsState(
        if (shown) 1f else 0f, tween(380), label = "heroAppear"
    )

    Box(Modifier.graphicsLayer { alpha = appear }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CardRadius),
            color = GlassCard,
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Cyan.copy(alpha = 0.50f),
                        Purple.copy(alpha = 0.35f),
                        Cyan.copy(alpha = 0.50f)
                    )
                )
            )
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .height(164.dp)
            ) {
                    Crossfade(targetState = mode, animationSpec = tween(320), label = "hero") { m ->
                        when (m) {
                            HeroMode.READY -> ReadyContent(
                                current = current!!,
                                aliveCount = alive.size,
                                busy = state.isLoading || state.isChecking || state.isRefreshing,
                                haptic = haptic,
                                onSwitchNext = onSwitchNext,
                                onConnect = onConnect
                            )

                        HeroMode.LOADING -> LoadingContent(shape)
                        HeroMode.EMPTY -> EmptyContent(state, onRetry)
                    }
                }
            }
        }
    }
}

private enum class HeroMode { LOADING, READY, EMPTY }


@Composable
private fun ReadyContent(
    current: Proxy,
    aliveCount: Int,
    busy: Boolean,
    haptic: HapticFeedback,
    onSwitchNext: () -> Unit,
    onConnect: (Proxy) -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.ic_chevrons_up),
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.current_pick),
                style = MaterialTheme.typography.labelMedium,
                color = Cyan
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            LatencyRing(current)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "${current.server}:${current.port}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LatencyPill(current)
                    Text(
                        sourceLabel(current.source),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(Cyan, Blue, Purple)))
                    .bouncyClick {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConnect(current)
                    },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = busy, animationSpec = tween(220), label = "cta") { loading ->
                    if (loading) {
                        DotsLabel(stringResource(R.string.hero_loading))
                    } else {
                        Text(
                            stringResource(R.string.connect_telegram),
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Glass,
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                IconButton(
                    onClick = onSwitchNext,
                    enabled = aliveCount > 1,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_next),
                        stringResource(R.string.next_pick),
                        tint = if (aliveCount > 1) Color.White else TextFaint
                    )
                }
            }
        }
    }
}


@Composable
private fun LoadingContent(shape: ConstellationShape?) {
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        if (shape != null) {
            val nameRes = remember(shape.id) {
                context.resources.getIdentifier("name_${shape.id}", "string", context.packageName)
            }
            val blurbRes = remember(shape.id) {
                context.resources.getIdentifier("blurb_${shape.id}", "string", context.packageName)
            }
            val localName = if (nameRes != 0) stringResource(nameRes) else shape.nameLatin
            val showLatin = localName != shape.nameLatin

            Text(
                if (showLatin) "$localName · ${shape.nameLatin}" else localName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.discovery_stars_count, shape.nameLatin, shape.points.size),
                style = MaterialTheme.typography.labelSmall,
                color = TextFaint,
                maxLines = 1
            )

            if (blurbRes != 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(blurbRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }
    }
}


@Composable
private fun EmptyContent(state: ProxyUiState, onRetry: () -> Unit) {
    val unreachable = state.error == "no_sources" || state.error == "offline"

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(
                if (unreachable) R.string.error_connection_title else R.string.no_alive
            ),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            stringResource(
                if (unreachable) R.string.error_no_sources else R.string.empty_discovered
            ),
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
        if (state.diag.isNotBlank()) {
            Text(
                state.diag,
                style = MaterialTheme.typography.labelSmall,
                color = TextFaint
            )
        }

        Spacer(Modifier.weight(1f))

        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Glass)
                .bouncyClick(onRetry),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_refresh),
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.error_retry),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun LatencyRing(proxy: Proxy) {
    val target = if (proxy.alive != true) 0f else {
        val ms = proxy.latencyMs ?: 1000
        val norm = ((Math.log10(ms.toDouble()) - 1.7) /
                (Math.log10(1200.0) - 1.7)).toFloat().coerceIn(0.05f, 1f)
        1f - norm
    }

    val resetKey = "${proxy.id}:${proxy.alive}:${proxy.latencyMs}"
    var sweep by remember(resetKey) { mutableFloatStateOf(0f) }

    LaunchedEffect(resetKey) {
        if (target > 0f) {
            animate(0f, target, animationSpec = tween(700, easing = FastOutSlowInEasing)) { v, _ ->
                sweep = v
            }
        } else {
            sweep = 0f
        }
    }

    val tier = tierColor(proxy)

    Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(Color.White.copy(alpha = 0.08f), -90f, 360f, false, style = stroke)
            if (sweep > 0.01f) {
                drawArc(
                    Brush.sweepGradient(listOf(Cyan, Blue, Purple, Cyan)),
                    -90f, 360f * sweep, false, style = stroke
                )
            }
        }
        Text(
            "${proxy.latencyMs}",
            style = MaterialTheme.typography.labelLarge,
            color = tier
        )
    }
}
