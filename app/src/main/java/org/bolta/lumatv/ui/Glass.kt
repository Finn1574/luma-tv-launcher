package org.bolta.lumatv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * "Liquid Glass": halbtransparente Scheibe mit Innenleuchten, Kantenlicht und
 * wanderndem Glanzstreifen. Kein RenderEffect-Blur, damit es auch auf Fire OS 7
 * (API 28) laeuft - der Tiefeneindruck entsteht aus Gradienten statt echtem Blur.
 */
fun Modifier.liquidGlass(
    corner: Dp = 22.dp,
    accent: Color = Luma.Violet,
    glow: Float = 0f,
    sheen: Float = -1f,
    strong: Boolean = false
): Modifier = this
    .clip(RoundedCornerShape(corner))
    .drawBehind {
        val w = size.width
        val h = size.height

        drawRect(
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (strong) 0.20f else 0.13f),
                    Color.White.copy(alpha = 0.05f),
                    accent.copy(alpha = 0.10f + 0.16f * glow)
                ),
                start = Offset(0f, 0f), end = Offset(w, h)
            )
        )
        // Innenleuchten von unten (Apple-TV-Materialtiefe)
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, accent.copy(alpha = 0.10f + 0.22f * glow)),
                startY = h * 0.35f, endY = h
            )
        )
        // Oberes Kantenlicht
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.30f + 0.25f * glow), Color.Transparent),
                startY = 0f, endY = h * 0.14f
            )
        )
        // Wandernder Glanz beim Fokus
        if (sheen >= 0f) {
            val x = -w * 0.6f + sheen * (w * 1.9f)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.26f * glow),
                        Color.Transparent
                    ),
                    start = Offset(x, 0f),
                    end = Offset(x + w * 0.42f, h)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, h)
            )
        }
    }
    .drawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        val stroke = 1.6f + 1.4f * glow
        drawRoundRectBorder(w, h, corner.toPx(), stroke, accent, glow)
    }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRectBorder(
    w: Float, h: Float, r: Float, stroke: Float, accent: Color, glow: Float
) {
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f + 0.35f * glow),
                accent.copy(alpha = 0.35f + 0.45f * glow),
                Color.White.copy(alpha = 0.18f + 0.3f * glow)
            ),
            start = Offset(0f, 0f), end = Offset(w, h)
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
    )
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    accent: Color = Luma.Violet,
    glow: Float = 0f,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.liquidGlass(corner, accent, glow, strong = strong), content = content)
}
