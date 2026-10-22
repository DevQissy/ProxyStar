package com.devqissy.proxystar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.devqissy.proxystar.ui.theme.*



private val ConstellationLine = Color(0xFF7DD3FC)
private val DustColor = Color(0xFFCBD5E1)
private val MilkyWay = Color(0xFFE2E8F0)

private const val FIELD_STARS = 140

private data class StarPoint(
    val x: Float, val y: Float,
    val phase: Float, val speed: Float,
    val size: Float
)

private data class Dust(
    val x: Float, val y: Float,
    val phase: Float, val speed: Float, val size: Float
)

private data class PreparedShape(
    val points: List<StarPoint>,
    val edges: List<Pair<Int, Int>>,
    val aspect: Float
)

@Composable
internal fun SpaceBackdrop(
    modifier: Modifier = Modifier,
    onShapeSelected: (ConstellationShape) -> Unit = {}
) {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsed = (now - start) / 1_000_000_000f
                if (elapsed - t >= 0.125f) t = elapsed
            }
        }
    }

    val shape = remember { ConstellationCatalog.ALL.random() }
    LaunchedEffect(shape) { onShapeSelected(shape) }

    val prepared = remember(shape) {
        val r = Random(shape.nameLatin.hashCode().toLong())

        val rot = (r.nextFloat() - 0.5f) * 1.57f
        val cosR = cos(rot)
        val sinR = sin(rot)

        val cx = shape.points.map { it.first }.average().toFloat()
        val cy = shape.points.map { it.second }.average().toFloat()
        val rotated = shape.points.map { (nx, ny) ->
            val dx = nx - cx
            val dy = ny - cy
            (cx + dx * cosR - dy * sinR) to (cy + dx * sinR + dy * cosR)
        }

        val minX = rotated.minOf { it.first }
        val maxX = rotated.maxOf { it.first }
        val minY = rotated.minOf { it.second }
        val maxY = rotated.maxOf { it.second }
        val bw = (maxX - minX).coerceAtLeast(0.0001f)
        val bh = (maxY - minY).coerceAtLeast(0.0001f)

        PreparedShape(
            points = rotated.map { (nx, ny) ->
                StarPoint(
                    x = (nx - minX) / bw,
                    y = (ny - minY) / bh,
                    phase = r.nextFloat() * 6.28f,
                    speed = 0.12f + r.nextFloat() * 0.18f,
                    size = 0.85f + r.nextFloat() * 0.4f
                )
            },
            edges = shape.edges,
            aspect = bh / bw
        )
    }

    val dust = remember {
        List(FIELD_STARS) { i ->
            val r = Random(i * 991 + 7)
            Dust(
                r.nextFloat(), r.nextFloat(),
                r.nextFloat() * 6.28f,
                0.08f + r.nextFloat() * 0.15f,
                0.7f + r.nextFloat() * 0.9f
            )
        }
    }

    val bandDust = remember {
        List(120) { i ->
            val r = Random(i * 131 + 7)
            Dust(
                r.nextFloat(), (r.nextFloat() + r.nextFloat() - 1f) * 0.5f,
                r.nextFloat() * 6.28f,
                0.08f + r.nextFloat() * 0.15f,
                0.6f + r.nextFloat() * 0.7f
            )
        }
    }

    val brightStars = remember {
        val rCount = Random(20261001L)
        val count = 10 + rCount.nextInt(11)
        val r = Random(20261002L)
        List(count) {
            StarPoint(
                x = 0.06f + r.nextFloat() * 0.88f,
                y = 0.05f + r.nextFloat() * 0.88f,
                phase = r.nextFloat() * 6.28f,
                speed = 0.10f + r.nextFloat() * 0.15f,
                size = 1.1f + r.nextFloat() * 0.5f
            )
        }
    }

    Box(modifier.background(Brush.verticalGradient(listOf(Bg0, Bg1, Bg0)))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val twoPi = (2.0 * Math.PI).toFloat()

            fun nebula(cx: Float, cy: Float, rad: Float, color: Color) =
                drawCircle(
                    Brush.radialGradient(listOf(color, Color.Transparent), Offset(cx, cy), rad),
                    rad, Offset(cx, cy)
                )
            nebula(w * 0.20f, h * 0.24f, w * 0.55f, Color(0x146D28D9))
            nebula(w * 0.82f, h * 0.60f, w * 0.48f, Color(0x100D9488))

            rotate(degrees = -22f, pivot = center) {
                drawRect(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to MilkyWay.copy(alpha = 0.06f),
                        1f to Color.Transparent,
                        startY = h * 0.30f, endY = h * 0.70f
                    ),
                    topLeft = Offset(-w * 0.2f, h * 0.30f),
                    size = Size(w * 1.4f, h * 0.40f)
                )
                bandDust.forEach { d ->
                    val c = Offset(d.x * w * 1.4f - w * 0.2f, h / 2 + d.y * h * 0.30f)
                    val a = 0.04f + 0.08f * (0.5f + 0.5f * sin(t * twoPi * d.speed + d.phase))
                    drawCircle(MilkyWay.copy(alpha = a), 0.9.dp.toPx() * d.size, c)
                }
            }

            dust.forEach { d ->
                val c = Offset(d.x * w, d.y * h)
                val a = 0.04f + 0.10f * (0.5f + 0.5f * sin(t * twoPi * d.speed + d.phase))
                drawCircle(DustColor.copy(alpha = a), 1.1.dp.toPx() * d.size, c)
            }

            brightStars.forEach { s ->
                val c = Offset(s.x * w, s.y * h)
                val tw = 0.74f + 0.26f * sin(t * twoPi * s.speed + s.phase)
                drawGlare(c, s.size * tw)
            }

            val bandTop = h / 3f
            val bandH = h - bandTop

            var targetW = 0.45f * w
            var targetH = targetW * prepared.aspect
            val maxH = bandH * 0.74f
            if (targetH > maxH) {
                targetH = maxH
                targetW = targetH / prepared.aspect
            }
            val offX = (w - targetW) / 2f
            val offY = bandTop + (bandH - targetH) / 2f

            val px = prepared.points.map { p ->
                Offset(offX + p.x * targetW, offY + p.y * targetH)
            }

            if (prepared.edges.isNotEmpty()) {
                val path = Path()
                prepared.edges.forEach { (a, b) ->
                    path.moveTo(px[a].x, px[a].y)
                    path.lineTo(px[b].x, px[b].y)
                }
                drawPath(
                    path,
                    color = ConstellationLine.copy(alpha = 0.30f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 9f), phase = 0f)   // 🔑 ثابت
                    )
                )
            }

            prepared.points.forEachIndexed { i, p ->
                val tw = 0.72f + 0.28f * sin(t * twoPi * p.speed + p.phase)
                drawGlare(px[i], p.size * tw)
            }
        }
    }
}

private fun DrawScope.drawGlare(c: Offset, b: Float) {
    if (b <= 0.05f) return
    val core = 1.4.dp.toPx() * b
    val glowR = 5.dp.toPx() * b
    val spike = 7.5.dp.toPx() * b

    drawCircle(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.55f * b), Color.Transparent),
            c, glowR
        ),
        glowR, c
    )
    drawCircle(Color.White.copy(alpha = 0.95f), core, c)

    fun spikeBrush(from: Offset, to: Offset) = Brush.linearGradient(
        0f to Color.Transparent,
        0.5f to Color.White.copy(alpha = 0.50f * b),
        1f to Color.Transparent,
        start = from, end = to
    )
    drawLine(spikeBrush(Offset(c.x - spike, c.y), Offset(c.x + spike, c.y)),
        Offset(c.x - spike, c.y), Offset(c.x + spike, c.y), 0.7.dp.toPx())
    drawLine(spikeBrush(Offset(c.x, c.y - spike), Offset(c.x, c.y + spike)),
        Offset(c.x, c.y - spike), Offset(c.x, c.y + spike), 0.7.dp.toPx())
}