package com.codewithkael.webrtcwithmlkit.data.persistence

import android.content.Context
import androidx.core.content.edit
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.BackgroundScaleMode

object BackgroundStorage {
    private const val PREF = "bg_pref"
    private const val KEY_URI = "bg_uri"
    private const val KEY_SCALE = "bg_scale_mode"

    data class Config(
        val uri: String?,
        val scaleMode: BackgroundScaleMode
    )

    fun load(ctx: Context): Config {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val uri = sp.getString(KEY_URI, null)
        val modeStr = sp.getString(KEY_SCALE, BackgroundScaleMode.CENTER_CROP.name)
        val mode = runCatching { BackgroundScaleMode.valueOf(modeStr!!) }
            .getOrDefault(BackgroundScaleMode.CENTER_CROP)
        return Config(uri = uri, scaleMode = mode)
    }

    fun save(ctx: Context, cfg: Config) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putString(KEY_URI, cfg.uri)
            putString(KEY_SCALE, cfg.scaleMode.name)
        }
    }
}