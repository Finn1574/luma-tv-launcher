package org.bolta.lumatv.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.bolta.lumatv.data.WeatherNow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ruhemodus im Aerial-Stil: langsam driftende Aurora, grosse Uhr,
 * die sanft ueber den Bildschirm wandert (Burn-in-Schutz inklusive).
 */
@Composable
fun Screensaver(clockTick: Long, weather: WeatherNow?, clock24: Boolean) {
    val t = rememberInfiniteTransition(label = "ss")
    val drift by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(70_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    val fade by t.animateFloat(
        0.65f, 1f,
        infiniteRepeatable(tween(6_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fade"
    )

    val now = remember(clockTick) { Date() }
    val timeFmt = remember(clock24) { SimpleDateFormat(if (clock24) "HH:mm" else "h:mm", Locale.GERMANY) }
    val dateFmt = remember { SimpleDateFormat("EEEE, d. MMMM", Locale.GERMANY) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AuroraBackground(
            modifier = Modifier.fillMaxSize(),
            spotlight = Offset(300f + drift * 900f, 260f + drift * 320f),
            particles = true,
            intensity = 1.35f
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationX = (drift - 0.5f) * 260f
                    translationY = (drift - 0.5f) * 120f
                    alpha = fade
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                timeFmt.format(now),
                color = Color.White,
                fontSize = 108.sp,
                fontWeight = FontWeight.Thin
            )
            Text(dateFmt.format(now), color = Luma.TextSecondary, fontSize = 18.sp)
            weather?.let {
                Spacer(Modifier.height(10.dp))
                Text("${it.glyph}  ${it.tempC}\u00b0  ${it.city}", color = Luma.TextSecondary, fontSize = 15.sp)
            }
        }
    }
}
