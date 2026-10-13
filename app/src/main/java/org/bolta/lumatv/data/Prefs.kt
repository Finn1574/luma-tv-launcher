package org.bolta.lumatv.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("luma", Context.MODE_PRIVATE)

    var particles: Boolean
        get() = sp.getBoolean("particles", true)
        set(v) = sp.edit().putBoolean("particles", v).apply()

    var sound: Boolean
        get() = sp.getBoolean("sound", true)
        set(v) = sp.edit().putBoolean("sound", v).apply()

    var screensaver: Boolean
        get() = sp.getBoolean("screensaver", true)
        set(v) = sp.edit().putBoolean("screensaver", v).apply()

    var clock24: Boolean
        get() = sp.getBoolean("clock24", true)
        set(v) = sp.edit().putBoolean("clock24", v).apply()

    var weather: Boolean
        get() = sp.getBoolean("weather", true)
        set(v) = sp.edit().putBoolean("weather", v).apply()

    fun favorites(): Set<String> = sp.getStringSet("favs", emptySet()) ?: emptySet()

    fun toggleFavorite(pkg: String): Boolean {
        val cur = favorites().toMutableSet()
        val added = if (cur.contains(pkg)) { cur.remove(pkg); false } else { cur.add(pkg); true }
        sp.edit().putStringSet("favs", cur).apply()
        return added
    }

    fun hidden(): Set<String> = sp.getStringSet("hidden", emptySet()) ?: emptySet()

    fun toggleHidden(pkg: String) {
        val cur = hidden().toMutableSet()
        if (!cur.add(pkg)) cur.remove(pkg)
        sp.edit().putStringSet("hidden", cur).apply()
    }

    fun noteLaunch(pkg: String) {
        sp.edit()
            .putInt("count_$pkg", sp.getInt("count_$pkg", 0) + 1)
            .putLong("last_$pkg", System.currentTimeMillis())
            .apply()
    }

    fun launchCount(pkg: String) = sp.getInt("count_$pkg", 0)
    fun lastUsed(pkg: String) = sp.getLong("last_$pkg", 0L)
}
