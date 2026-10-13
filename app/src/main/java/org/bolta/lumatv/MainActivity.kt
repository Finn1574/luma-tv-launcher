
package org.bolta.lumatv

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bolta.lumatv.data.*
import org.bolta.lumatv.ui.*

class MainActivity : ComponentActivity() {

    private lateinit var prefs: Prefs
    private lateinit var sfx: Sfx

    private val apps = mutableStateOf<List<AppEntry>>(emptyList())
    private val weather = mutableStateOf<WeatherNow?>(null)
    private val lastInput = mutableStateOf(System.currentTimeMillis())
    private val settingsRev = mutableIntStateOf(0)

    private val pkgReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) = reloadApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        sfx = Sfx { prefs.sound }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        registerReceiver(pkgReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })

        reloadApps()
        lifecycleScope.launch { weather.value = Weather.fetch() }

        setContent { LumaRoot() }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(pkgReceiver) }
        sfx.release()
    }

    override fun onResume() {
        super.onResume()
        lastInput.value = System.currentTimeMillis()
        reloadApps()
    }

    /** Home-Taste darf nicht aus dem Launcher heraus navigieren. */
    override fun onBackPressed() { /* bewusst leer: Launcher ist die Wurzel */ }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        lastInput.value = System.currentTimeMillis()
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) sfx.move()
        return super.dispatchKeyEvent(event)
    }

    private fun reloadApps() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) { AppRepo.load(this@MainActivity, prefs) }
            apps.value = list
        }
    }

    private fun launch(app: AppEntry) {
        sfx.select()
        prefs.noteLaunch(app.packageName)
        val i = Intent(Intent.ACTION_MAIN)
            .setComponent(ComponentName(app.packageName, app.activityName))
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        val ok = runCatching { startActivity(i); true }.getOrDefault(false)
        if (!ok) {
            runCatching {
                packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
            }
        }
    }

    private fun system(action: String) {
        val intent = when (action) {
            "settings" -> Intent(Settings.ACTION_SETTINGS)
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "apps" -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
            "amazon" -> packageManager.getLaunchIntentForPackage("com.amazon.tv.launcher")
                ?: Intent(Settings.ACTION_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }
        runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    @Composable
    private fun LumaRoot() {
        var quickOpen by remember { mutableStateOf(false) }
        var contextApp by remember { mutableStateOf<AppEntry?>(null) }
        var spotlight by remember { mutableStateOf<Offset?>(null) }
        var tick by remember { mutableLongStateOf(0L) }
        var idle by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                tick = System.currentTimeMillis()
                idle = prefs.screensaver &&
                    (System.currentTimeMillis() - lastInput.value > IDLE_MS)
                delay(1000)
            }
        }
        // Wetter alle 20 Minuten auffrischen
        LaunchedEffect(Unit) {
            while (true) {
                delay(20 * 60 * 1000L)
                weather.value = Weather.fetch() ?: weather.value
            }
        }

        val rev = settingsRev.intValue

        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AuroraBackground(
                modifier = Modifier.fillMaxSize(),
                spotlight = spotlight,
                particles = prefs.particles && !idle,
                intensity = 1f
            )

            key(rev, apps.value) {
                HomeScreen(
                    apps = apps.value,
                    prefs = prefs,
                    weather = weather.value,
                    clockTick = tick / 30_000,
                    onLaunch = { launch(it) },
                    onContext = { contextApp = it },
                    onFocusMoved = { _, center -> spotlight = center }
                )
            }

            contextApp?.let { app ->
                AppContextMenu(
                    app = app,
                    isFavorite = prefs.favorites().contains(app.packageName),
                    onDismiss = { contextApp = null },
                    onAction = { a ->
                        when (a) {
                            "launch" -> { contextApp = null; launch(app) }
                            "fav" -> { prefs.toggleFavorite(app.packageName); settingsRev.intValue++; contextApp = null }
                            "hide" -> { prefs.toggleHidden(app.packageName); contextApp = null; reloadApps() }
                            "info" -> {
                                contextApp = null
                                runCatching {
                                    startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.parse("package:" + app.packageName))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                            "uninstall" -> {
                                contextApp = null
                                runCatching {
                                    startActivity(
                                        Intent(Intent.ACTION_DELETE)
                                            .setData(Uri.parse("package:" + app.packageName))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            if (quickOpen) {
                QuickPanel(
                    particles = prefs.particles,
                    sound = prefs.sound,
                    screensaver = prefs.screensaver,
                    clock24 = prefs.clock24,
                    onToggle = { k ->
                        when (k) {
                            "particles" -> prefs.particles = !prefs.particles
                            "sound" -> prefs.sound = !prefs.sound
                            "screensaver" -> prefs.screensaver = !prefs.screensaver
                            "clock24" -> prefs.clock24 = !prefs.clock24
                        }
                        settingsRev.intValue++
                    },
                    onSystem = { quickOpen = false; system(it) },
                    onDismiss = { quickOpen = false }
                )
            }

            if (idle) {
                Screensaver(clockTick = tick / 10_000, weather = weather.value, clock24 = prefs.clock24)
            }
        }

        LaunchedEffect(Unit) { menuOpener = { quickOpen = !quickOpen } }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            menuOpener?.invoke()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private var menuOpener: (() -> Unit)? = null

    companion object {
        private const val IDLE_MS = 90_000L
    }
}
