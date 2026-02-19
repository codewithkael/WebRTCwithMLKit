package com.codewithkael.webrtcwithmlkit.utils.imageProcessor

import android.graphics.Bitmap

class VideoEffectsPipeline() {

    private val textRecognition = TextRecognitionEffect()


    data class Enabled(
        val textRecognition: Boolean,
    )

    suspend fun process(
        input: Bitmap,
        enabled: Enabled,
    ): Bitmap {
        var out = input
        if (enabled.textRecognition) out = textRecognition.apply(out)
        return out
    }

    fun close() {
        textRecognition.close()
    }
}
