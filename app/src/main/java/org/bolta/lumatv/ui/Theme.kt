package org.bolta.lumatv.ui

import androidx.compose.ui.graphics.Color

/** Luma-Palette: tiefes Violett -> Indigo -> Elektroblau, Apple-TV-Anmutung. */
object Luma {
    val Void = Color(0xFF07040F)
    val DeepPurple = Color(0xFF1A0B33)
    val Indigo = Color(0xFF221155)
    val MidnightBlue = Color(0xFF0C1340)

    val Violet = Color(0xFF8B5CF6)
    val VioletBright = Color(0xFFA78BFA)
    val Blue = Color(0xFF3B82F6)
    val Cyan = Color(0xFF38BDF8)
    val Magenta = Color(0xFFC084FC)

    val TextPrimary = Color(0xFFF4F1FF)
    val TextSecondary = Color(0xFFB9AEE0)

    val GlassFill = Color(0x1AFFFFFF)
    val GlassFillStrong = Color(0x2EFFFFFF)
    val GlassBorder = Color(0x4DFFFFFF)
    val GlassShadow = Color(0x80000000)

    /** Akzentfarbe einer App ins Luma-Schema ziehen, statt sie roh zu benutzen. */
    fun harmonize(c: Color): Color {
        val target = if (c.blue >= c.red) Blue else Violet
        return Color(
            red = c.red * 0.45f + target.red * 0.55f,
            green = c.green * 0.35f + target.green * 0.65f,
            blue = (c.blue * 0.45f + target.blue * 0.55f).coerceAtLeast(0.55f),
            alpha = 1f
        )
    }
}
