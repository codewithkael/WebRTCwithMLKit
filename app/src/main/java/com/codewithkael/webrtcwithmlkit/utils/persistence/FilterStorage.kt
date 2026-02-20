package com.codewithkael.webrtcwithmlkit.utils.persistence

import android.content.Context
import androidx.core.content.edit

object FilterStorage {
    private const val PREF = "filters_pref"
    private const val KEY_TEXT_RECOGNITION = "flt_text_recognition"
    private const val KEY_WATERMARK = "flt_watermark"
    private const val KEY_FACE = "flt_face"
    private const val KEY_FACE_MESH = "flt_face_mesh"
    private const val KEY_BLUR = "flt_blur"

    data class Config(
        val textRecognition: Boolean,
        val watermark: Boolean,
        val faceDetect: Boolean,
        val faceMesh: Boolean,
        val blurBackground: Boolean,
        )

    fun load(ctx: Context): Config {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return Config(
            textRecognition = sp.getBoolean(KEY_TEXT_RECOGNITION, false),
            watermark = sp.getBoolean(KEY_WATERMARK, false),
            faceDetect = sp.getBoolean(KEY_FACE, false),
            faceMesh = sp.getBoolean(KEY_FACE_MESH, false),
            blurBackground = sp.getBoolean(KEY_BLUR, false),

            )
    }

    fun save(ctx: Context, cfg: Config) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_TEXT_RECOGNITION, cfg.textRecognition)
                .putBoolean(KEY_WATERMARK, cfg.watermark)
                .putBoolean(KEY_FACE, cfg.faceDetect)
                .putBoolean(KEY_FACE_MESH, cfg.faceMesh)
                .putBoolean(KEY_BLUR, cfg.blurBackground)
        }
    }
}
