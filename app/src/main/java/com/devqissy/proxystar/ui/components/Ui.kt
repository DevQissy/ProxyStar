package com.devqissy.proxystar.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devqissy.proxystar.R
import com.devqissy.proxystar.data.Proxy
import com.devqissy.proxystar.ui.theme.Amber
import com.devqissy.proxystar.ui.theme.Green
import com.devqissy.proxystar.ui.theme.Red
import com.devqissy.proxystar.ui.theme.TextDim
import kotlinx.coroutines.delay

internal val CardRadius = 22.dp

internal val RowRadius = 16.dp

internal val GlassCard = Color(0xFF0B0F18).copy(alpha = 0.58f)

internal val Glass = Color.White.copy(alpha = 0.055f)

internal val GlassBorder = Color.White.copy(alpha = 0.10f)

internal val GlassBorderSoft = Color.White.copy(alpha = 0.07f)

internal val LocalPulsePhase = staticCompositionLocalOf<State<Float>> {
    mutableFloatStateOf(1f)
}

@Composable
internal fun AnimationClock(content: @Composable () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulsePhase = pulse.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulsePhase"
    )

    CompositionLocalProvider(
        LocalPulsePhase provides pulsePhase,
        content = content
    )
}


@Composable
internal fun Modifier.bouncyClick(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "press"
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

@Composable
internal fun PulsingDot(color: Color, size: Dp = 8.dp) {
    val phase = LocalPulsePhase.current
    Box(
        Modifier
            .size(size)
            .drawBehind {
                drawCircle(color.copy(alpha = 0.30f + 0.70f * phase.value))
            }
    )
}

@Composable
internal fun SquareAction(
    iconRes: Int,
    desc: String,
    tint: Color,
    onClick: () -> Unit,
    size: Dp = 46.dp,
    iconSize: Dp = 24.dp,
    radius: Dp = 15.dp
) {
    val shape = RoundedCornerShape(radius)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(Glass)
            .border(BorderStroke(1.dp, GlassBorder), shape)
            .bouncyClick(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), desc, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
internal fun BareIconAction(
    iconRes: Int,
    desc: String,
    tint: Color,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        Modifier
            .size(size)
            .bouncyClick(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), desc, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
internal fun DotsLabel(
    text: String,
    color: Color = Color.White,
    style: TextStyle = MaterialTheme.typography.titleSmall
) {
    var dots by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(420)
            dots = if (dots >= 3) 1 else dots + 1
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = style,
            color = color,
            maxLines = 1
        )
        Text(
            ".".repeat(dots),
            style = style,
            color = color,
            maxLines = 1,
            modifier = Modifier.width(18.dp)
        )
    }
}

internal fun tierColor(p: Proxy): Color = when {
    p.alive == null -> TextDim
    p.alive == false -> Red
    (p.latencyMs ?: 9999) < 400 -> Green
    else -> Amber
}

@Composable
internal fun LatencyPill(proxy: Proxy) {
    val tier = tierColor(proxy)
    Surface(shape = CircleShape, color = tier.copy(alpha = 0.14f)) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (proxy.alive == null) PulsingDot(tier, 6.dp)
            Text(
                when {
                    proxy.alive == null -> "…"
                    proxy.alive == false -> stringResource(R.string.dead)
                    else -> "${proxy.latencyMs}ms"
                },
                style = MaterialTheme.typography.labelMedium,
                color = tier
            )
        }
    }
}

@Composable
internal fun SignalBars(proxy: Proxy) {
    val count = when {
        proxy.alive == null -> 1
        proxy.alive == false -> 0
        (proxy.latencyMs ?: 9999) < 400 -> 4
        (proxy.latencyMs ?: 9999) < 700 -> 3
        else -> 2
    }
    val tier = tierColor(proxy)
    val off = Color.White.copy(alpha = 0.10f)

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf(5.dp, 8.dp, 11.dp, 14.dp).forEachIndexed { i, h ->
            Box(
                Modifier
                    .size(width = 3.dp, height = h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < count) tier else off)
            )
        }
    }
}


internal fun sourceLabel(source: String): String {
    val path = source.substringAfter("://").substringBefore("?").trimEnd('/')
    val parts = path.split('/')
    return when {
        source.contains("raw.githubusercontent.com") && parts.size > 2 -> parts[2]
        source.contains("github.io") && parts.size > 1 -> parts[1]
        source.contains("t.me") && parts.size > 1 -> parts.last()
        parts.size > 1 -> parts[1]
        else -> parts.firstOrNull().orEmpty()
    }
}
