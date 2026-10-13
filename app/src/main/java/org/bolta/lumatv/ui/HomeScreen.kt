package org.bolta.lumatv.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.bolta.lumatv.data.AppEntry
import org.bolta.lumatv.data.Prefs
import org.bolta.lumatv.data.WeatherNow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Row(val title: String, val apps: List<AppEntry>, val big: Boolean = false)

@Composable
fun HomeScreen(
    apps: List<AppEntry>,
    prefs: Prefs,
    weather: WeatherNow?,
    clockTick: Long,
    onLaunch: (AppEntry) -> Unit,
    onContext: (AppEntry) -> Unit,
    onFocusMoved: (AppEntry, Offset) -> Unit
) {
    val favs = remember(apps, clockTick) { prefs.favorites() }
    val rows = remember(apps, favs) { buildRows(apps, favs, prefs) }
    val first = remember { FocusRequester() }

    LaunchedEffect(rows.isNotEmpty()) {
        if (rows.isNotEmpty()) runCatching { first.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 26.dp, bottom = 34.dp)
    ) {
        TopBar(weather = weather, prefs = prefs, clockTick = clockTick)
        Spacer(Modifier.height(10.dp))

        rows.forEachIndexed { rowIndex, row ->
            Text(
                row.title,
                color = Luma.TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 46.dp, top = 10.dp, bottom = 2.dp)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 38.dp)) {
                items(row.apps, key = { it.packageName + row.title }) { app ->
                    AppTile(
                        app = app,
                        isFavorite = favs.contains(app.packageName),
                        focusRequester = if (rowIndex == 0 && app == row.apps.first()) first else null,
                        onFocused = onFocusMoved,
                        onLaunch = onLaunch,
                        onContext = onContext,
                        tileWidth = if (row.big) 236 else 186,
                        tileHeight = if (row.big) 136 else 112
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "\u2630 Men\u00fc-Taste \u00b7 Schnellzugriff      \u25CF Lang dr\u00fccken \u00b7 App-Optionen",
            color = Luma.TextSecondary.copy(alpha = 0.55f),
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 46.dp, top = 6.dp)
        )
    }
}

private fun buildRows(apps: List<AppEntry>, favs: Set<String>, prefs: Prefs): List<Row> {
    if (apps.isEmpty()) return emptyList()
    val out = mutableListOf<Row>()
    val favApps = apps.filter { favs.contains(it.packageName) }
    if (favApps.isNotEmpty()) out += Row("Favoriten", favApps, big = true)

    val recent = apps.filter { prefs.lastUsed(it.packageName) > 0 }
        .sortedByDescending { prefs.lastUsed(it.packageName) }
        .take(10)
    if (recent.isNotEmpty()) out += Row("Zuletzt verwendet", recent)

    val games = apps.filter { it.isGame }
    if (games.isNotEmpty()) out += Row("Spiele", games)

    out += Row("Alle Apps", apps)
    return out
}

@Composable
private fun TopBar(weather: WeatherNow?, prefs: Prefs, clockTick: Long) {
    val now = remember(clockTick) { Date() }
    val timeFmt = remember(prefs.clock24) {
        SimpleDateFormat(if (prefs.clock24) "HH:mm" else "h:mm a", Locale.GERMANY)
    }
    val dateFmt = remember { SimpleDateFormat("EEEE, d. MMMM", Locale.GERMANY) }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0.35f, 0.75f,
        infiniteRepeatable(tween(3400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulsev"
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Luma",
                color = Luma.TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                dateFmt.format(now),
                color = Luma.TextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.weight(1f))

        weather?.let { w ->
            GlassPanel(
                modifier = Modifier.height(44.dp),
                corner = 22.dp,
                accent = Luma.Cyan,
                glow = pulse * 0.4f
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(w.glyph, fontSize = 17.sp, color = Luma.TextPrimary)
                    Spacer(Modifier.width(9.dp))
                    Text("${w.tempC}\u00b0", color = Luma.TextPrimary, fontSize = 16.sp)
                    if (w.city.isNotEmpty()) {
                        Spacer(Modifier.width(7.dp))
                        Text(w.city, color = Luma.TextSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
        }

        GlassPanel(
            modifier = Modifier.height(44.dp),
            corner = 22.dp,
            accent = Luma.Violet,
            glow = pulse
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp).fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    timeFmt.format(now),
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
