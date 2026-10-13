package org.bolta.lumatv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherNow(val tempC: Int, val code: Int, val city: String) {
    /** WMO-Code -> Symbol, bewusst reduziert gehalten. */
    val glyph: String
        get() = when (code) {
            0 -> "\u2600"
            1, 2 -> "\u26C5"
            3 -> "\u2601"
            in 45..48 -> "\u2591"
            in 51..67 -> "\u2614"
            in 71..77 -> "\u2744"
            in 80..82 -> "\u2614"
            in 95..99 -> "\u26A1"
            else -> "\u2601"
        }
}

object Weather {
    /** Open-Meteo + ipapi: beides ohne API-Key, beides nur lesend. */
    suspend fun fetch(): WeatherNow? = withContext(Dispatchers.IO) {
        runCatching {
            val loc = JSONObject(get("https://ipapi.co/json/"))
            val lat = loc.optDouble("latitude", 52.52)
            val lon = loc.optDouble("longitude", 13.405)
            val city = loc.optString("city", "")
            val w = JSONObject(
                get("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code")
            ).getJSONObject("current")
            WeatherNow(
                tempC = Math.round(w.getDouble("temperature_2m")).toInt(),
                code = w.getInt("weather_code"),
                city = city
            )
        }.getOrNull()
    }

    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 6000
        c.readTimeout = 6000
        c.setRequestProperty("User-Agent", "LumaTV/1.0")
        return c.inputStream.bufferedReader().use { it.readText() }.also { c.disconnect() }
    }
}
