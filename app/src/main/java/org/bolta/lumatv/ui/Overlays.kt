package org.bolta.lumatv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.bolta.lumatv.data.AppEntry

/** Einzeiliger Glas-Knopf, D-Pad-tauglich. */
@Composable
fun GlassRow(
    label: String,
    hint: String? = null,
    accent: Color = Luma.Violet,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val glow by animateFloatAsState(if (focused) 1f else 0f, tween(200), label = "g")
    val scale by animateFloatAsState(if (focused) 1.035f else 1f, spring(0.8f, 350f), label = "s")

    Box(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyUp &&
                    (ev.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                        ev.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER)
                ) { onClick(); true } else false
            }
            .focusable(interactionSource = interaction)
            .liquidGlass(corner = 15.dp, accent = accent, glow = glow)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Luma.TextPrimary, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            hint?.let { Text(it, color = Luma.TextSecondary, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun Scrim(onDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .onKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyUp &&
                    (ev.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_BACK ||
                        ev.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_MENU)
                ) { onDismiss(); true } else false
            },
        content = content
    )
}

/** Schnellzugriff: Systemwege + Luma-Schalter. */
@Composable
fun QuickPanel(
    particles: Boolean,
    sound: Boolean,
    screensaver: Boolean,
    clock24: Boolean,
    onToggle: (String) -> Unit,
    onSystem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }

    Scrim(onDismiss) {
        GlassPanel(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 40.dp)
                .width(420.dp)
                .fillMaxHeight(0.86f),
            corner = 28.dp,
            accent = Luma.Blue,
            glow = 0.55f,
            strong = true
        ) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                Text("Schnellzugriff", color = Luma.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(14.dp))

                GlassRow("Einstellungen", accent = Luma.Blue, focusRequester = fr) { onSystem("settings") }
                GlassRow("WLAN", accent = Luma.Blue) { onSystem("wifi") }
                GlassRow("Bluetooth", accent = Luma.Blue) { onSystem("bluetooth") }
                GlassRow("Anzeige & T\u00f6ne", accent = Luma.Blue) { onSystem("display") }
                GlassRow("Apps verwalten", accent = Luma.Blue) { onSystem("apps") }
                GlassRow("Amazon-Startseite", accent = Luma.Magenta) { onSystem("amazon") }

                Spacer(Modifier.height(14.dp))
                Text("Luma", color = Luma.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                GlassRow("Sternenstaub", hint = onOff(particles)) { onToggle("particles") }
                GlassRow("Ton-Feedback", hint = onOff(sound)) { onToggle("sound") }
                GlassRow("Bildschirmschoner", hint = onOff(screensaver)) { onToggle("screensaver") }
                GlassRow("24-Stunden-Uhr", hint = onOff(clock24)) { onToggle("clock24") }
            }
        }
    }
}

private fun onOff(b: Boolean) = if (b) "An" else "Aus"

/** Kontextmenue einer App (langer Druck auf OK). */
@Composable
fun AppContextMenu(
    app: AppEntry,
    isFavorite: Boolean,
    onAction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }

    Scrim(onDismiss) {
        GlassPanel(
            modifier = Modifier.align(Alignment.Center).width(460.dp),
            corner = 26.dp,
            accent = app.accent,
            glow = 0.6f,
            strong = true
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(app.label, color = Luma.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Light)
                Text(app.packageName, color = Luma.TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.height(16.dp))
                GlassRow("\u25B6  Starten", accent = app.accent, focusRequester = fr) { onAction("launch") }
                GlassRow(if (isFavorite) "\u2605  Favorit entfernen" else "\u2606  Als Favorit", accent = app.accent) { onAction("fav") }
                GlassRow("\u2298  Aus Luma ausblenden", accent = app.accent) { onAction("hide") }
                GlassRow("\u2139  App-Info", accent = app.accent) { onAction("info") }
                GlassRow("\u2715  Deinstallieren", accent = Luma.Magenta) { onAction("uninstall") }
            }
        }
    }
}
