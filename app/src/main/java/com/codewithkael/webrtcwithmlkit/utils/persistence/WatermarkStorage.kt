package com.codewithkael.webrtcwithmlkit.utils.persistence

import android.content.Context
import androidx.core.content.edit
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.WatermarkLocation

object WatermarkStorage {
    private const val PREF = "wm_pref"
    private const val KEY_URI = "wm_uri"
    private const val KEY_LOC = "wm_loc"
    private const val KEY_MARGIN = "wm_margin_dp"
    private const val KEY_SIZE = "wm_size_fraction"

    data class Config(
        val uri: String?,
        val location: WatermarkLocation,
        val marginDp: Float,
        val sizeFraction: Float
    )

    fun load(ctx: Context): Config {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val uri = sp.getString(KEY_URI, null)
        val locStr = sp.getString(KEY_LOC, WatermarkLocation.BOTTOM_LEFT.name)
        val loc =
            runCatching { WatermarkLocation.valueOf(locStr!!) }.getOrDefault(WatermarkLocation.TOP_LEFT)
        val marginDp = sp.getFloat(KEY_MARGIN, 12f)
        val sizeFraction = sp.getFloat(KEY_SIZE, 0.20f)
        return Config(uri, loc, marginDp, sizeFraction)
    }

    fun save(ctx: Context, cfg: Config) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putString(KEY_URI, cfg.uri).putString(KEY_LOC, cfg.location.name)
                .putFloat(KEY_MARGIN, cfg.marginDp).putFloat(KEY_SIZE, cfg.sizeFraction)
        }
    }
}
