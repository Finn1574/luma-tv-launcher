package org.bolta.lumatv.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import java.util.Calendar

/**
 * Lebender Hintergrund: driftende Aurora-Blobs + Sternenstaub + Fokus-Spotlight.
 * Die Grundstimmung folgt der Tageszeit (morgens blauer, abends violetter).
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    spotlight: Offset?,
    particles: Boolean,
    intensity: Float = 1f
) {
    val t = rememberInfiniteTransition(label = "aurora")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(42_000, easing = LinearEasing)),
        label = "phase"
    )
    val breathe by t.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(9_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val dayMix = remember(hour) {
        // 0 = Nacht/violett, 1 = Tag/blau
        when (hour) {
            in 6..10 -> 0.85f
            in 11..16 -> 1f
            in 17..20 -> 0.5f
            else -> 0.15f
        }
    }

    val starSeeds = remember {
        List(70) { i ->
            val r = java.util.Random((i * 9781L) + 17)
            Triple(r.nextFloat(), r.nextFloat(), 0.3f + r.nextFloat() * 0.7f)
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    lerpC(Luma.DeepPurple, Luma.MidnightBlue, dayMix),
                    Luma.Void,
                    lerpC(Luma.Indigo, Luma.MidnightBlue, dayMix * 0.8f)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            size = Size(w, h)
        )

        data class Blob(val c: Color, val ax: Float, val ay: Float, val sp: Float, val rad: Float)
        val blobs = listOf(
            Blob(Luma.Violet, 0.26f, 0.30f, 1.0f, 0.62f),
            Blob(Luma.Blue, 0.74f, 0.26f, -0.75f, 0.55f),
            Blob(Luma.Magenta, 0.52f, 0.78f, 0.55f, 0.50f),
            Blob(Luma.Cyan, 0.14f, 0.82f, -1.25f, 0.38f)
        )
        blobs.forEachIndexed { i, b ->
            val p = phase * b.sp + i * 1.3f
            val cx = w * (b.ax + 0.07f * cos(p))
            val cy = h * (b.ay + 0.06f * sin(p * 1.35f))
            val radius = w * b.rad * breathe * 0.55f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(b.c.copy(alpha = 0.30f * intensity), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        if (particles) {
            starSeeds.forEachIndexed { i, (sx, sy, br) ->
                val drift = (phase * 0.08f + i * 0.21f)
                val x = ((sx + drift * 0.02f) % 1f) * w
                val y = (sy * h) + sin(phase + i.toFloat()) * 6f
                val a = (0.18f + 0.32f * ((sin(phase * 2.2f + i * 0.9f) + 1f) / 2f)) * br
                drawCircle(Color.White.copy(alpha = a * 0.55f), radius = 1.1f + br * 1.6f, center = Offset(x, y))
            }
        }

        spotlight?.let { s ->
            val r = w * 0.42f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                    center = s, radius = r
                ),
                radius = r, center = s
            )
        }

        // Vignette fuer Apple-TV-Tiefe
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.78f
            )
        )
    }
}

internal fun lerpC(a: Color, b: Color, t: Float): Color = Color(
    a.red + (b.red - a.red) * t,
    a.green + (b.green - a.green) * t,
    a.blue + (b.blue - a.blue) * t,
    1f
)
