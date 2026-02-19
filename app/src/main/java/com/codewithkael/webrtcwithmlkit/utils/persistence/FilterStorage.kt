package com.codewithkael.webrtcwithmlkit.utils.persistence

import android.content.Context
import androidx.core.content.edit

object FilterStorage {
    private const val PREF = "filters_pref"
    private const val KEY_TEXT_RECOGNITION = "flt_text_recognition"


    data class Config(
        val textRecognition: Boolean
    )

    fun load(ctx: Context): Config {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return Config(
            textRecognition = sp.getBoolean(KEY_TEXT_RECOGNITION, false)
        )
    }

    fun save(ctx: Context, cfg: Config) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_TEXT_RECOGNITION, cfg.textRecognition)
        }
    }
}
