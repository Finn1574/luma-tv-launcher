package org.bolta.lumatv.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.bolta.lumatv.data.AppEntry

/**
 * Fokussierbare Glaskachel im Apple-TV-Stil:
 * - Skalierung + Anhebung bei Fokus
 * - 3D-Neigung (Parallaxe) je nach zuletzt gedrueckter Richtung
 * - wandernder Glanzstreifen ueber dem Glas
 */
@Composable
fun AppTile(
    app: AppEntry,
    isFavorite: Boolean,
    focusRequester: FocusRequester? = null,
    onFocused: (AppEntry, Offset) -> Unit,
    onLaunch: (AppEntry) -> Unit,
    onContext: (AppEntry) -> Unit,
    tileWidth: Int = 196,
    tileHeight: Int = 118
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val scale by animateFloatAsState(if (focused) 1.11f else 1f, spring(0.72f, 320f), label = "scale")
    val lift by animateFloatAsState(if (focused) -10f else 0f, spring(0.75f, 300f), label = "lift")
    val glow by animateFloatAsState(if (focused) 1f else 0f, tween(260), label = "glow")

    var tilt by remember { mutableStateOf(Offset.Zero) }
    val tiltX by animateFloatAsState(tilt.y, spring(0.45f, 240f), label = "tiltX")
    val tiltY by animateFloatAsState(tilt.x, spring(0.45f, 240f), label = "tiltY")

    val sheenAnim = rememberInfiniteTransition(label = "sheen")
    val sheen by sheenAnim.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "sheenv"
    )

    var center by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .padding(horizontal = 9.dp, vertical = 14.dp)
            .size(tileWidth.dp, tileHeight.dp)
            .onGloballyPositioned {
                val p = it.positionInRoot()
                center = Offset(p.x + it.size.width / 2f, p.y + it.size.height / 2f)
            }
            .graphicsLayer {
                scaleX = scale; scaleY = scale
                translationY = lift
                rotationX = tiltX * 7f
                rotationY = tiltY * 9f
                cameraDistance = 14f * density
                shadowElevation = 18f * glow
            }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyDown) {
                    when (ev.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { tilt = Offset(-1f, 0f); false }
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { tilt = Offset(1f, 0f); false }
                        android.view.KeyEvent.KEYCODE_DPAD_UP -> { tilt = Offset(0f, 1f); false }
                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> { tilt = Offset(0f, -1f); false }
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER -> {
                            if (ev.nativeKeyEvent.repeatCount > 0) { onContext(app); true } else false
                        }
                        android.view.KeyEvent.KEYCODE_MENU -> { onContext(app); true }
                        else -> false
                    }
                } else if (ev.type == KeyEventType.KeyUp) {
                    tilt = Offset.Zero
                    when (ev.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER -> {
                            if (ev.nativeKeyEvent.repeatCount == 0) { onLaunch(app); true } else true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable(interactionSource = interaction)
            .liquidGlass(corner = 20.dp, accent = app.accent, glow = glow, sheen = if (focused) sheen else -1f)
    ) {
        LaunchedEffect(focused) { if (focused) onFocused(app, center) }

        if (app.banner != null) {
            Image(
                bitmap = app.banner,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.92f)
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                        )
                    )
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                Text(
                    app.label,
                    color = Luma.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                app.icon?.let {
                    Image(
                        bitmap = it,
                        contentDescription = app.label,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    app.label,
                    color = Luma.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isFavorite) {
            Text(
                "\u2605",
                color = Color(0xFFFFD86B),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(7.dp)
            )
        }
    }
}
