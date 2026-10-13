package org.bolta.lumatv.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.palette.graphics.Palette
import org.bolta.lumatv.ui.Luma

data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap?,
    val banner: ImageBitmap?,
    val accent: Color,
    val isGame: Boolean
)

object AppRepo {

    fun load(context: Context, prefs: Prefs): List<AppEntry> {
        val pm = context.packageManager
        val found = LinkedHashMap<String, ResolveInfo>()

        fun collect(category: String) {
            val i = Intent(Intent.ACTION_MAIN).addCategory(category)
            pm.queryIntentActivities(i, 0).forEach { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg != context.packageName) found.putIfAbsent(pkg, ri)
            }
        }
        collect(Intent.CATEGORY_LEANBACK_LAUNCHER)
        collect(Intent.CATEGORY_LAUNCHER)

        val hidden = prefs.hidden()
        return found.values.mapNotNull { ri ->
            val ai = ri.activityInfo
            if (hidden.contains(ai.packageName)) return@mapNotNull null
            val label = ri.loadLabel(pm)?.toString() ?: ai.packageName
            val bannerDr: Drawable? = runCatching { ai.loadBanner(pm) }.getOrNull()
                ?: runCatching { pm.getApplicationBanner(ai.packageName) }.getOrNull()
            val iconDr: Drawable? = runCatching { ri.loadIcon(pm) }.getOrNull()
            val iconBmp = iconDr?.toBitmapSafe(128)
            val accent = iconBmp?.let { dominant(it) } ?: Luma.Violet
            AppEntry(
                label = label,
                packageName = ai.packageName,
                activityName = ai.name,
                icon = iconBmp?.asImageBitmap(),
                banner = bannerDr?.toBitmapSafe(320)?.asImageBitmap(),
                accent = accent,
                isGame = isGame(pm, ai.packageName)
            )
        }.sortedBy { it.label.lowercase() }
    }

    private fun isGame(pm: PackageManager, pkg: String): Boolean = runCatching {
        val ai = pm.getApplicationInfo(pkg, 0)
        @Suppress("DEPRECATION")
        ai.category == android.content.pm.ApplicationInfo.CATEGORY_GAME
    }.getOrDefault(false)

    private fun dominant(bmp: Bitmap): Color {
        val p = runCatching { Palette.from(bmp).clearFilters().generate() }.getOrNull()
        val rgb = p?.getVibrantColor(0) ?: 0
        val c = if (rgb != 0) rgb else (p?.getDominantColor(0) ?: 0)
        if (c == 0) return Luma.Violet
        return Luma.harmonize(
            Color(
                red = ((c shr 16) and 0xFF) / 255f,
                green = ((c shr 8) and 0xFF) / 255f,
                blue = (c and 0xFF) / 255f,
                alpha = 1f
            )
        )
    }

    private fun Drawable.toBitmapSafe(max: Int): Bitmap? = runCatching {
        if (this is BitmapDrawable && bitmap != null) {
            val b = bitmap
            if (b.width <= max) b else Bitmap.createScaledBitmap(b, max, max * b.height / b.width, true)
        } else {
            val w = intrinsicWidth.takeIf { it > 0 } ?: max
            val h = intrinsicHeight.takeIf { it > 0 } ?: max
            val scale = minOf(1f, max.toFloat() / w)
            val bw = (w * scale).toInt().coerceAtLeast(1)
            val bh = (h * scale).toInt().coerceAtLeast(1)
            val out = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            setBounds(0, 0, bw, bh)
            draw(canvas)
            out
        }
    }.getOrNull()
}
