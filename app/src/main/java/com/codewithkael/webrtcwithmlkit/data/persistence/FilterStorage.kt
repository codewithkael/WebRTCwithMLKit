package com.codewithkael.webrtcwithmlkit.data.persistence

import android.content.Context
import androidx.core.content.edit

object FilterStorage {
    private const val PREF = "filters_pref"
    private const val KEY_TEXT_RECOGNITION = "flt_text_recognition"
    private const val KEY_WATERMARK = "flt_watermark"
    private const val KEY_FACE = "flt_face"
    private const val KEY_FACE_MESH = "flt_face_mesh"
    private const val KEY_BLUR = "flt_blur"
    private const val KEY_IMAGE_LABELING = "flt_image_labeling"
    private const val KEY_OBJECT_DETECTION = "flt_object_detection"
    private const val KEY_POSE_DETECTION = "flt_pose_detection"
    private const val KEY_BG_REPLACE = "flt_bg_replace"

    data class Config(
        val textRecognition: Boolean,
        val watermark: Boolean,
        val faceDetect: Boolean,
        val faceMesh: Boolean,
        val blurBackground: Boolean,
        val imageLabeling: Boolean,
        val objectDetection: Boolean,
        val poseDetection: Boolean,
        val replaceBackground: Boolean,
        )


    fun load(ctx: Context): Config {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return Config(
            textRecognition = sp.getBoolean(KEY_TEXT_RECOGNITION, false),
            watermark = sp.getBoolean(KEY_WATERMARK, false),
            faceDetect = sp.getBoolean(KEY_FACE, false),
            faceMesh = sp.getBoolean(KEY_FACE_MESH, false),
            blurBackground = sp.getBoolean(KEY_BLUR, false),
            imageLabeling = sp.getBoolean(KEY_IMAGE_LABELING, false),
            objectDetection = sp.getBoolean(KEY_OBJECT_DETECTION, false),
            poseDetection = sp.getBoolean(KEY_POSE_DETECTION, false),
            replaceBackground = sp.getBoolean(KEY_BG_REPLACE, false),
            )
    }

    fun save(ctx: Context, cfg: Config) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_TEXT_RECOGNITION, cfg.textRecognition)
                .putBoolean(KEY_WATERMARK, cfg.watermark)
                .putBoolean(KEY_FACE, cfg.faceDetect)
                .putBoolean(KEY_FACE_MESH, cfg.faceMesh)
                .putBoolean(KEY_BLUR, cfg.blurBackground)
                .putBoolean(KEY_IMAGE_LABELING, cfg.imageLabeling)
                .putBoolean(KEY_OBJECT_DETECTION, cfg.objectDetection)
                .putBoolean(KEY_POSE_DETECTION, cfg.poseDetection)
                .putBoolean(KEY_BG_REPLACE, cfg.replaceBackground)
        }
    }
}
