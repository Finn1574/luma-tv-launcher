package org.bolta.lumatv.data

import android.media.AudioManager
import android.media.ToneGenerator

/** Dezente Fokus-/Start-Toene, bewusst leise und kurz gehalten. */
class Sfx(private val enabled: () -> Boolean) {
    private val tone by lazy { runCatching { ToneGenerator(AudioManager.STREAM_SYSTEM, 22) }.getOrNull() }

    fun move() { if (enabled()) runCatching { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 18) } }
    fun select() { if (enabled()) runCatching { tone?.startTone(ToneGenerator.TONE_PROP_ACK, 40) } }
    fun release() { runCatching { tone?.release() } }
}
